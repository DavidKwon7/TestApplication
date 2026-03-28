package com.handylab.testapplication.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

/**
 * [KmaRepository] 단위 테스트.
 *
 * 네트워크 없이 실행 가능한 CSV 파싱 로직과 정적 데이터를 검증합니다.
 * [KmaRepository.getBlossomData]의 실제 API 호출은 통합 테스트에서 다룹니다.
 */
class KmaRepositoryTest {

    private lateinit var repository: KmaRepository

    @Before
    fun setUp() {
        repository = KmaRepository()
    }

    // ── formatDate ────────────────────────────────────────────────────────────

    @Test
    fun `formatDate - YYYYMMDD 8자리를 MM-DD로 변환한다`() {
        assertEquals("04-03", repository.formatDate("20240403"))
        assertEquals("03-25", repository.formatDate("20240325"))
        assertEquals("12-31", repository.formatDate("20241231"))
    }

    @Test
    fun `formatDate - YYYY-MM-DD 10자리도 MM-DD로 변환한다`() {
        assertEquals("04-03", repository.formatDate("2024-04-03"))
    }

    @Test
    fun `formatDate - 예상치 못한 형식은 원본을 반환한다`() {
        assertEquals("unknown", repository.formatDate("unknown"))
    }

    // ── parseKmaData ──────────────────────────────────────────────────────────

    @Test
    fun `parseKmaData - YYYYMMDD 형식 날짜를 올바르게 파싱한다`() {
        val csv = """
            #comment
            1,108,20240403,205,202
            1,108,20240410,205,203
        """.trimIndent()

        val result = repository.parseKmaData(csv)
        val seoul = result.find { it.cityName == "서울" }

        assertEquals("04-03", seoul?.bloomDate)
        assertEquals("04-10", seoul?.fullBloomDate)
    }

    @Test
    fun `parseKmaData - 낙화(204) 이벤트가 있으면 상태가 짐이다`() {
        val csv = """
            1,184,20240325,205,202
            1,184,20240401,205,203
            1,184,20240410,205,204
        """.trimIndent()

        val result = repository.parseKmaData(csv)
        val jeju = result.find { it.cityName == "제주" }

        assertEquals("짐", jeju?.status)
    }

    @Test
    fun `parseKmaData - 만발(203)만 있고 낙화가 없으면 상태가 만발이다`() {
        val csv = """
            1,184,20240325,205,202
            1,184,20240401,205,203
        """.trimIndent()

        val result = repository.parseKmaData(csv)
        val jeju = result.find { it.cityName == "제주" }

        assertEquals("만발", jeju?.status)
    }

    @Test
    fun `parseKmaData - 개화(202)만 있으면 상태가 개화이다`() {
        val csv = "1,108,20240403,205,202"

        val result = repository.parseKmaData(csv)
        val seoul = result.find { it.cityName == "서울" }

        assertEquals("개화", seoul?.status)
    }

    @Test
    fun `parseKmaData - 이벤트가 없는 도시는 상태가 개화 전이다`() {
        val result = repository.parseKmaData("") // 빈 응답
        val seoul = result.find { it.cityName == "서울" }

        assertEquals("개화 전", seoul?.status)
        assertEquals("미정", seoul?.bloomDate)
        assertEquals("미정", seoul?.fullBloomDate)
    }

    @Test
    fun `parseKmaData - 주석 라인(#)은 무시한다`() {
        val csv = """
            # This is a comment
            #another comment
            1,108,20240403,205,202
        """.trimIndent()

        val result = repository.parseKmaData(csv)
        val seoul = result.find { it.cityName == "서울" }

        assertNotEquals("미정", seoul?.bloomDate)
    }

    @Test
    fun `parseKmaData - 결과는 항상 13개 도시를 포함한다`() {
        val result = repository.parseKmaData("")
        assertEquals(13, result.size)
    }

    @Test
    fun `parseKmaData - 일부 도시만 있으면 나머지는 미정이다`() {
        // 서울(108)만 있는 경우 — 제주(184)는 미정이어야 함
        val csv = "1,108,20240403,205,202"

        val result = repository.parseKmaData(csv)
        val jeju = result.find { it.cityName == "제주" }

        assertEquals("미정", jeju?.bloomDate)
        assertEquals("개화 전", jeju?.status)
    }

    // ── getStaticBlossomData ──────────────────────────────────────────────────

    @Test
    fun `getStaticBlossomData - 13개 도시 데이터를 반환한다`() {
        val result = repository.getStaticBlossomData()
        assertEquals(13, result.size)
    }

    @Test
    fun `getStaticBlossomData - 모든 도시의 bloomDate는 미정이 아니다`() {
        val result = repository.getStaticBlossomData()
        result.forEach { info ->
            assertNotEquals("미정을 반환하면 안됨: ${info.cityName}", "미정", info.bloomDate)
        }
    }

    @Test
    fun `getStaticBlossomData - 제주가 포함된다`() {
        val result = repository.getStaticBlossomData()
        val jeju = result.find { it.cityName == "제주" }

        assertNotEquals(null, jeju)
        assertNotEquals("미정", jeju?.bloomDate)
    }

    @Test
    fun `getStaticBlossomData - 제주가 서울보다 먼저 개화한다`() {
        val result = repository.getStaticBlossomData()
        val jeju = result.find { it.cityName == "제주" }
        val seoul = result.find { it.cityName == "서울" }

        // "MM-DD" 문자열 사전순 비교 (같은 연도이므로 유효)
        assert(jeju!!.bloomDate < seoul!!.bloomDate) {
            "제주(${jeju.bloomDate})가 서울(${seoul.bloomDate})보다 먼저 개화해야 합니다"
        }
    }
}
