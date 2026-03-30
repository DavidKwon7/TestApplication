package com.handylab.testapplication.data.repository

import com.handylab.testapplication.data.model.MusicChartItem

interface LastFmRepository {
    suspend fun getTopTracks(): List<MusicChartItem>
}
