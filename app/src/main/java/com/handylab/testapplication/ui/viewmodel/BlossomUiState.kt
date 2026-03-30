package com.handylab.testapplication.ui.viewmodel

import com.handylab.testapplication.data.model.CherryBlossomInfo

sealed class BlossomUiState {
    data object Loading : BlossomUiState()
    data class Success(val list: List<CherryBlossomInfo>) : BlossomUiState()
    data class Error(val message: String) : BlossomUiState()
}
