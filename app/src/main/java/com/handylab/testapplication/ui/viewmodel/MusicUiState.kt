package com.handylab.testapplication.ui.viewmodel

import com.handylab.testapplication.data.model.MusicChartItem

sealed class MusicUiState {
    object Loading : MusicUiState()
    data class Success(val list: List<MusicChartItem>) : MusicUiState()
    data class Error(val message: String) : MusicUiState()
}
