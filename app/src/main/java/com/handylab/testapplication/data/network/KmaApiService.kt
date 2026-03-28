package com.handylab.testapplication.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface KmaApiService {
    @GET("api/typ01/url/sfc_ssn.php")
    suspend fun getSeasonalData(
        @Query("stn") stn: String = "0", // 0 for all stations
        @Query("ssn") ssn: String = "205", // 205 for cherry blossom
        @Query("tm1") tm1: String, // start timestamp
        @Query("tm2") tm2: String, // end timestamp
        @Query("authKey") authKey: String
    ): String
}
