package com.handylab.testapplication.data.model

data class MusicChartItem(
    val rank: Int,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val isSpringSong: Boolean = false
)
