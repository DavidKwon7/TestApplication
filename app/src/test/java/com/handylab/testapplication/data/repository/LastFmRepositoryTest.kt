package com.handylab.testapplication.data.repository

import com.handylab.testapplication.data.model.LastFmArtist
import com.handylab.testapplication.data.model.LastFmImage
import com.handylab.testapplication.data.model.LastFmTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [LastFmRepository] 단위 테스트.
 *
 * 네트워크 없이 실행 가능한 트랙 변환 로직([LastFmRepository.mapToChartItem])을 검증합니다.
 * 실제 API 호출([LastFmRepository.getTopTracks])은 통합 테스트에서 다룹니다.
 */
class LastFmRepositoryTest {

    private lateinit var repository: LastFmRepository

    @Before
    fun setUp() {
        repository = LastFmRepository()
    }

    // ── 앨범 커버 URL 처리 ─────────────────────────────────────────────────────

    @Test
    fun `mapToChartItem - 유효한 이미지 URL이 있으면 마지막 비어있지 않은 URL을 사용한다`() {
        val track = makeTrack(
            images = listOf(
                LastFmImage("https://example.com/small.jpg", "small"),
                LastFmImage("https://example.com/large.jpg", "large")
            )
        )

        val result = repository.mapToChartItem(track, 1)

        assertEquals("https://example.com/large.jpg", result.coverUrl)
    }

    @Test
    fun `mapToChartItem - placeholder 해시가 포함된 URL은 빈 문자열로 처리한다`() {
        val placeholderUrl = "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png"
        val track = makeTrack(images = listOf(LastFmImage(placeholderUrl, "extralarge")))

        val result = repository.mapToChartItem(track, 1)

        assertEquals("", result.coverUrl)
    }

    @Test
    fun `mapToChartItem - 이미지 목록이 비어있으면 coverUrl은 빈 문자열이다`() {
        val track = makeTrack(images = emptyList())

        val result = repository.mapToChartItem(track, 1)

        assertEquals("", result.coverUrl)
    }

    @Test
    fun `mapToChartItem - 빈 URL 이미지는 건너뛰고 유효한 URL을 사용한다`() {
        val track = makeTrack(
            images = listOf(
                LastFmImage("", "small"),
                LastFmImage("https://example.com/large.jpg", "large")
            )
        )

        val result = repository.mapToChartItem(track, 1)

        assertEquals("https://example.com/large.jpg", result.coverUrl)
    }

    // ── 봄 노래 감지 ──────────────────────────────────────────────────────────

    @Test
    fun `mapToChartItem - 봄 키워드가 포함된 곡은 isSpringSong이 true이다`() {
        val springTitles = listOf("봄봄봄", "나만, 봄", "봄 사랑 벚꽃 말고", "봄이 좋냐??", "입춘")

        springTitles.forEach { title ->
            val track = makeTrack(name = title)
            val result = repository.mapToChartItem(track, 1)
            assertTrue("'$title'은 봄 노래로 감지되어야 합니다", result.isSpringSong)
        }
    }

    @Test
    fun `mapToChartItem - 봄 키워드가 없는 곡은 isSpringSong이 false이다`() {
        val track = makeTrack(name = "Dynamite")

        val result = repository.mapToChartItem(track, 1)

        assertFalse(result.isSpringSong)
    }

    // ── 순위 및 기본 필드 ──────────────────────────────────────────────────────

    @Test
    fun `mapToChartItem - rank가 올바르게 설정된다`() {
        val track = makeTrack()

        val result = repository.mapToChartItem(track, 5)

        assertEquals(5, result.rank)
    }

    @Test
    fun `mapToChartItem - 트랙 이름과 아티스트 이름이 올바르게 매핑된다`() {
        val track = makeTrack(name = "봄봄봄", artistName = "로이킴")

        val result = repository.mapToChartItem(track, 1)

        assertEquals("봄봄봄", result.title)
        assertEquals("로이킴", result.artist)
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private fun makeTrack(
        name: String = "Test Song",
        artistName: String = "Test Artist",
        images: List<LastFmImage> = emptyList()
    ) = LastFmTrack(
        name = name,
        artist = LastFmArtist(name = artistName),
        image = images
    )
}
