package com.handylab.testapplication.data.repository

import com.handylab.testapplication.BuildConfig
import com.handylab.testapplication.data.model.CherryBlossomInfo
import com.handylab.testapplication.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 기상청(KMA) 벚꽃 개화 데이터를 제공하는 Repository 구현체.
 *
 * 기상청 계절관측 API(`sfc_ssn.php`)를 통해 주요 13개 도시의 벚꽃 개화 정보를 조회합니다.
 * API 호출 실패 시 [getStaticBlossomData]의 정적 데이터를 반환합니다.
 */
class KmaRepositoryImpl : KmaRepository {

    private val authKey = BuildConfig.KMA_AUTH_KEY

    /**
     * 관측소 번호(stn)와 도시 정보를 묶는 내부 모델.
     *
     * @property stn      기상청 관측소 번호
     * @property cityName 화면에 표시할 도시 이름
     * @property lat      위도 (Google Maps 마커용)
     * @property lng      경도 (Google Maps 마커용)
     */
    data class CityStation(
        val stn: String,
        val cityName: String,
        val lat: Double,
        val lng: Double
    )

    /** 조회 대상 13개 도시 관측소 목록. */
    internal val stations = listOf(
        CityStation("108", "서울", 37.5665, 126.9780),
        CityStation("112", "인천", 37.4563, 126.7052),
        CityStation("119", "수원", 37.2636, 127.0286),
        CityStation("133", "대전", 36.3504, 127.3845),
        CityStation("143", "대구", 35.8714, 128.6014),
        CityStation("159", "부산", 35.1796, 129.0756),
        CityStation("152", "울산", 35.5384, 129.3114),
        CityStation("283", "경주", 35.8562, 129.2247),
        CityStation("155", "진해", 35.1493, 128.6605),
        CityStation("156", "광주", 35.1595, 126.8526),
        CityStation("146", "전주", 35.8242, 127.1480),
        CityStation("105", "강릉", 37.7519, 128.8760),
        CityStation("184", "제주", 33.4996, 126.5312)
    )

    /**
     * 벚꽃 개화 데이터를 반환합니다.
     *
     * 현재 연도부터 시작해 전년도까지 순서대로 API를 시도합니다.
     * **13개 도시 전체**의 개화일이 확인된 경우에만 유효한 데이터로 판단합니다.
     * 하나라도 "미정"이 있으면 해당 연도의 데이터가 불완전한 것으로 보고 다음 연도를 시도합니다.
     * 모두 실패하면 [getStaticBlossomData]를 반환합니다.
     *
     * ### `any` 대신 `all`을 사용하는 이유
     * `any` (하나라도 개화일이 있으면 반환)를 쓰면, 제주·부산 등 일부 도시만 개화한
     * 시즌 초반에 나머지 도시가 모두 "미정"인 불완전한 결과가 그대로 반환됩니다.
     * `all`을 쓰면 13개 도시가 모두 실제 관측 완료된 연도(보통 직전 연도)의
     * 데이터를 보여주므로 지도에 모든 마커 정보가 정상 표시됩니다.
     */
    override suspend fun getBlossomData(): List<CherryBlossomInfo> = withContext(Dispatchers.IO) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        for (year in currentYear downTo (currentYear - 1)) {
            try {
                val responseText = RetrofitClient.kmaApiService.getSeasonalData(
                    stn = "0",
                    ssn = "205",
                    tm1 = "${year}0101",
                    tm2 = "${year}1231",
                    authKey = authKey
                )
                val result = parseKmaData(responseText)
                // 13개 도시 전체 개화일이 확인된 경우에만 유효한 데이터로 판단
                if (result.all { it.bloomDate != "미정" }) {
                    return@withContext result
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        getStaticBlossomData()
    }

    /**
     * 기상청 API의 CSV 응답을 파싱하여 [CherryBlossomInfo] 목록으로 변환합니다.
     *
     * CSV 형식: `#`으로 시작하는 주석 라인 제외, 각 라인은 쉼표로 구분된 필드로 구성됩니다.
     * - `parts[1]`: 관측소 번호(stn)
     * - `parts[2]`: 관측 날짜 (YYYYMMDD 형식, 예: "20240403")
     * - `parts[4]`: 계절 이벤트 코드 (202=개화, 203=만발, 204=낙화)
     *
     * @param text 기상청 API의 원시 CSV 응답 문자열
     * @return 파싱된 [CherryBlossomInfo] 목록
     */
    internal fun parseKmaData(text: String): List<CherryBlossomInfo> {
        // stn → (eventCode → date) 형태로 이벤트 맵 구성
        val eventMap = mutableMapOf<String, MutableMap<String, String>>()

        for (line in text.lines()) {
            if (line.isBlank() || line.startsWith("#")) continue
            val parts = line.split(",").map { it.trim() }
            if (parts.size >= 5) {
                val stn = parts[1]
                val tm = parts[2]      // YYYYMMDD (예: "20240403")
                val ssnMd = parts[4]   // 이벤트 코드
                eventMap.getOrPut(stn) { mutableMapOf() }[ssnMd] = tm
            }
        }

        return stations.map { station ->
            val events = eventMap[station.stn] ?: emptyMap()
            val bloomDate = events["202"]?.let { formatDate(it) } ?: "미정"
            val fullBloomDate = events["203"]?.let { formatDate(it) } ?: "미정"
            val status = when {
                events.containsKey("204") -> "짐"
                events.containsKey("203") -> "만발"
                events.containsKey("202") -> "개화"
                else -> "개화 전"
            }
            CherryBlossomInfo(
                cityName = station.cityName,
                lat = station.lat,
                lng = station.lng,
                bloomDate = bloomDate,
                fullBloomDate = fullBloomDate,
                status = status
            )
        }
    }

    /**
     * 기상청 날짜 문자열을 화면 표시용 `MM-DD` 형식으로 변환합니다.
     *
     * 기상청 API는 `YYYYMMDD` 형식(8자리)으로 날짜를 반환합니다.
     * 예: `"20240403"` → `"04-03"`
     *
     * @param raw API에서 받은 날짜 원시 문자열
     * @return `MM-DD` 형식 문자열, 형식이 맞지 않으면 원본 반환
     */
    internal fun formatDate(raw: String): String = when (raw.length) {
        8  -> "${raw.substring(4, 6)}-${raw.substring(6, 8)}"  // YYYYMMDD → MM-DD
        10 -> raw.substring(5)                                   // YYYY-MM-DD → MM-DD (하이픈 포함 형식 대응)
        else -> raw
    }

    /**
     * API 실패 시 사용하는 정적 벚꽃 개화 데이터.
     *
     * 평년 기준 주요 도시의 예상 개화/만발일입니다.
     * 제주에서 시작해 북쪽으로 이동하는 개화 패턴을 반영합니다.
     */
    internal fun getStaticBlossomData(): List<CherryBlossomInfo> = listOf(
        CherryBlossomInfo("제주", 33.4996, 126.5312, "03-25", "04-01", "개화 전"),
        CherryBlossomInfo("부산", 35.1796, 129.0756, "03-28", "04-03", "개화 전"),
        CherryBlossomInfo("진해", 35.1493, 128.6605, "03-28", "04-03", "개화 전"),
        CherryBlossomInfo("울산", 35.5384, 129.3114, "03-29", "04-04", "개화 전"),
        CherryBlossomInfo("광주", 35.1595, 126.8526, "03-30", "04-05", "개화 전"),
        CherryBlossomInfo("대구", 35.8714, 128.6014, "04-01", "04-07", "개화 전"),
        CherryBlossomInfo("경주", 35.8562, 129.2247, "04-01", "04-07", "개화 전"),
        CherryBlossomInfo("전주", 35.8242, 127.1480, "04-02", "04-08", "개화 전"),
        CherryBlossomInfo("대전", 36.3504, 127.3845, "04-03", "04-09", "개화 전"),
        CherryBlossomInfo("서울", 37.5665, 126.9780, "04-03", "04-10", "개화 전"),
        CherryBlossomInfo("수원", 37.2636, 127.0286, "04-04", "04-10", "개화 전"),
        CherryBlossomInfo("인천", 37.4563, 126.7052, "04-05", "04-11", "개화 전"),
        CherryBlossomInfo("강릉", 37.7519, 128.8760, "04-05", "04-12", "개화 전")
    )
}
