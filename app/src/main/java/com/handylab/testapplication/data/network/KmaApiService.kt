package com.handylab.testapplication.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface KmaApiService {
    @GET("api/typ01/url/sfc_ssn.php")
    suspend fun getSeasonalData(
        @Query("stn") stn: String,
        @Query("ssn") ssn: String,
        @Query("tm1") tm1: String,
        @Query("tm2") tm2: String,
        @Query("authKey") authKey: String
    ): String
}
