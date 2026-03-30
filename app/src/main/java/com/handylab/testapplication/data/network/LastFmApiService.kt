package com.handylab.testapplication.data.network

import com.handylab.testapplication.data.model.LastFmTopTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApiService {

    @GET("2.0/")
    suspend fun getTopTracksByTag(
        @Query("method") method: String,
        @Query("tag") tag: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String,
        @Query("limit") limit: Int
    ): LastFmTopTracksResponse
}
