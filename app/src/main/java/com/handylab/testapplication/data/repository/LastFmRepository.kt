package com.handylab.testapplication.data.repository

import com.handylab.testapplication.data.model.LastFmTrack
import com.handylab.testapplication.data.model.MusicChartItem
import com.handylab.testapplication.data.network.RetrofitClient

/**
 * Last.fm 음악 차트 데이터를 제공하는 Repository.
 *
 * Last.fm `tag.getTopTracks` API를 통해 K-Pop 인기 트랙을 조회하고,
 * UI에서 사용할 [MusicChartItem] 리스트로 변환합니다.
 */
open class LastFmRepository {

    /**
     * Last.fm에서 K-Pop 인기 트랙 목록을 가져옵니다.
     *
     * @return 순위 1위부터 정렬된 [MusicChartItem] 리스트
     * @throws Exception 네트워크 오류 또는 파싱 실패 시
     */
    open suspend fun getTopTracks(): List<MusicChartItem> {
        val response = RetrofitClient.lastFmApiService.getTopTracksByTag()
        return response.tracks.track.mapIndexed { index, track ->
            mapToChartItem(track, index + 1)
        }
    }

    /**
     * Last.fm API 응답의 단일 트랙을 UI 모델([MusicChartItem])로 변환합니다.
     *
     * - Last.fm placeholder 이미지([PLACEHOLDER_HASH])가 포함된 URL은
     *   실제 앨범 아트가 없음을 의미하므로 빈 문자열로 처리합니다.
     * - [springKeywords] 중 하나라도 트랙 제목에 포함되면 [MusicChartItem.isSpringSong]을 true로 설정합니다.
     *
     * @param track API 응답의 원시 트랙 객체
     * @param rank  화면에 표시할 순위 (1부터 시작)
     */
    internal fun mapToChartItem(track: LastFmTrack, rank: Int): MusicChartItem {
        val coverUrl = track.image
            .lastOrNull { it.url.isNotEmpty() && !it.url.contains(PLACEHOLDER_HASH) }
            ?.url ?: ""

        val isSpringSong = springKeywords.any { track.name.contains(it, ignoreCase = true) }

        return MusicChartItem(
            rank = rank,
            title = track.name,
            artist = track.artist.name,
            coverUrl = coverUrl,
            isSpringSong = isSpringSong
        )
    }

    companion object {
        /** Last.fm에서 앨범 아트가 없을 때 반환하는 기본 이미지 해시. */
        internal const val PLACEHOLDER_HASH = "2a96cbd8b46e442fc41c2b86b821562f"

        /** 봄 테마 트랙으로 강조 표시하기 위한 키워드 목록. */
        internal val springKeywords = listOf("봄 사랑 벚꽃 말고", "나만, 봄", "봄봄봄", "봄이 좋냐??", "입춘")
    }
}
