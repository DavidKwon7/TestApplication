package com.handylab.testapplication.data.network

import com.handylab.testapplication.data.model.LastFmTopTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApiService {

    @GET("2.0/")
    suspend fun getTopTracksByTag(
        @Query("method") method: String = "tag.getTopTracks",
        @Query("tag") tag: String = "k-pop",
        @Query("api_key") apiKey: String = LastFmConfig.API_KEY,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 50
    ): LastFmTopTracksResponse
}
