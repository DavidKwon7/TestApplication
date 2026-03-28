package com.handylab.testapplication.ui.viewmodel

import com.handylab.testapplication.data.model.CherryBlossomInfo
import com.handylab.testapplication.data.model.MusicChartItem
import com.handylab.testapplication.data.repository.KmaRepository
import com.handylab.testapplication.data.repository.LastFmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [SpringAppViewModel] 단위 테스트.
 *
 * 실제 네트워크 대신 Fake Repository를 주입하여 ViewModel의 상태 전환 로직을 검증합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpringAppViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 초기 상태 ──────────────────────────────────────────────────────────────

    @Test
    fun `초기 musicUiState는 Loading이다`() = runTest {
        val viewModel = SpringAppViewModel(
            kmaRepository = FakeKmaRepository(),
            lastFmRepository = FakeLastFmRepository(shouldFail = true)
        )

        assertEquals(MusicUiState.Loading, viewModel.musicUiState.value)
    }

    @Test
    fun `초기 isLoading은 false이다`() {
        val viewModel = SpringAppViewModel(
            kmaRepository = FakeKmaRepository(),
            lastFmRepository = FakeLastFmRepository()
        )

        assertFalse(viewModel.isLoading.value)
    }

    // ── 벚꽃 데이터 로딩 ──────────────────────────────────────────────────────

    @Test
    fun `벚꽃 데이터 로드 성공 시 blossomList가 채워진다`() = runTest {
        val fakeData = listOf(
            CherryBlossomInfo("제주", 33.4996, 126.5312, "03-25", "04-01", "개화 전")
        )
        val viewModel = SpringAppViewModel(
            kmaRepository = FakeKmaRepository(data = fakeData),
            lastFmRepository = FakeLastFmRepository()
        )

        advanceUntilIdle()

        assertEquals(fakeData, viewModel.blossomList.value)
    }

    @Test
    fun `벚꽃 데이터 로드 완료 후 isLoading은 false가 된다`() = runTest {
        val viewModel = SpringAppViewModel(
            kmaRepository = FakeKmaRepository(),
            lastFmRepository = FakeLastFmRepository()
        )

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    // ── 음악 차트 로딩 ────────────────────────────────────────────────────────

    @Test
    fun `음악 차트 로드 성공 시 musicUiState가 Success가 된다`() = runTest {
        val fakeChart = listOf(
            MusicChartItem(rank = 1, title = "봄봄봄", artist = "로이킴", coverUrl = "")
        )
        val viewModel = SpringAppViewModel(
            kmaRepository = FakeKmaRepository(),
            lastFmRepository = FakeLastFmRepository(data = fakeChart)
        )

        advanceUntilIdle()

        val state = viewModel.musicUiState.value
        assertTrue(state is MusicUiState.Success)
        assertEquals(fakeChart, (state as MusicUiState.Success).list)
    }

    @Test
    fun `음악 차트 로드 실패 시 musicUiState가 Error가 된다`() = runTest {
        val viewModel = SpringAppViewModel(
            kmaRepository = FakeKmaRepository(),
            lastFmRepository = FakeLastFmRepository(shouldFail = true)
        )

        advanceUntilIdle()

        assertTrue(viewModel.musicUiState.value is MusicUiState.Error)
    }

    // ── Fake 구현체 ───────────────────────────────────────────────────────────

    private class FakeKmaRepository(
        private val data: List<CherryBlossomInfo> = emptyList()
    ) : KmaRepository() {
        override suspend fun getBlossomData(): List<CherryBlossomInfo> = data
    }

    private class FakeLastFmRepository(
        private val data: List<MusicChartItem> = emptyList(),
        private val shouldFail: Boolean = false
    ) : LastFmRepository() {
        override suspend fun getTopTracks(): List<MusicChartItem> {
            if (shouldFail) throw RuntimeException("네트워크 오류")
            return data
        }
    }
}
