package com.handylab.testapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.handylab.testapplication.data.model.CherryBlossomInfo
import com.handylab.testapplication.data.repository.KmaRepository
import com.handylab.testapplication.data.repository.LastFmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 앱의 메인 ViewModel.
 *
 * 벚꽃 개화 정보(KMA)와 음악 차트(Last.fm) 두 가지 데이터 스트림을 관리합니다.
 * UI는 각 [StateFlow]를 구독하여 상태 변화에 반응합니다.
 *
 * @param kmaRepository    벚꽃 개화 데이터 소스
 * @param lastFmRepository 음악 차트 데이터 소스
 */
class SpringAppViewModel(
    private val kmaRepository: KmaRepository = KmaRepository(),
    private val lastFmRepository: LastFmRepository = LastFmRepository()
) : ViewModel() {

    /** 벚꽃 개화 정보 목록. 지도 화면과 홈 화면에서 사용됩니다. */
    private val _blossomList = MutableStateFlow<List<CherryBlossomInfo>>(emptyList())
    val blossomList: StateFlow<List<CherryBlossomInfo>> = _blossomList.asStateFlow()

    /** 음악 차트 UI 상태. Loading → Success 또는 Error로 전환됩니다. */
    private val _musicUiState = MutableStateFlow<MusicUiState>(MusicUiState.Loading)
    val musicUiState: StateFlow<MusicUiState> = _musicUiState.asStateFlow()

    /** 벚꽃 데이터 로딩 중 여부. 홈 화면 로딩 인디케이터에 사용됩니다. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchBlossomData()
        fetchMusicData()
    }

    /** 기상청 API에서 벚꽃 개화 데이터를 가져옵니다. API 실패 시 정적 데이터를 사용합니다. */
    private fun fetchBlossomData() {
        viewModelScope.launch {
            _isLoading.value = true
            _blossomList.value = kmaRepository.getBlossomData()
            _isLoading.value = false
        }
    }

    /**
     * Last.fm에서 K-Pop 인기 트랙 목록을 가져옵니다.
     *
     * 에러 화면의 '다시 시도' 버튼에서도 호출됩니다.
     */
    fun fetchMusicData() {
        viewModelScope.launch {
            _musicUiState.value = MusicUiState.Loading
            try {
                val chart = lastFmRepository.getTopTracks()
                _musicUiState.value = MusicUiState.Success(chart)
            } catch (e: Exception) {
                e.printStackTrace()
                _musicUiState.value = MusicUiState.Error("음악 차트를 불러오지 못했습니다.\n(에러: ${e.localizedMessage})")
            }
        }
    }

    companion object {
        /**
         * [ViewModelProvider]에서 사용할 기본 팩토리.
         *
         * Activity에서 `ViewModelProvider(this, SpringAppViewModel.Factory)`로 사용합니다.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SpringAppViewModel() }
        }
    }
}
