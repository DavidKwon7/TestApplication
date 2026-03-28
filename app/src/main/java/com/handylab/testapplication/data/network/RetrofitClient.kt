package com.handylab.testapplication.data.network

import com.handylab.testapplication.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 앱 전역에서 사용하는 Retrofit 클라이언트 싱글턴.
 *
 * - [kmaApiService]: 기상청(KMA) API 서비스 인스턴스
 * - [lastFmApiService]: Last.fm API 서비스 인스턴스
 *
 * 두 서비스는 공통 [OkHttpClient]를 공유하며, 디버그 빌드에서만 HTTP 로그가 출력됩니다.
 */
object RetrofitClient {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 기상청 API 서비스.
     * 응답이 CSV 텍스트이므로 [ScalarsConverterFactory]를 사용합니다.
     */
    val kmaApiService: KmaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apihub.kma.go.kr/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(KmaApiService::class.java)
    }

    /**
     * Last.fm API 서비스.
     * 응답이 JSON이므로 [kotlinx.serialization] 기반 컨버터를 사용합니다.
     */
    val lastFmApiService: LastFmApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://ws.audioscrobbler.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(LastFmApiService::class.java)
    }
}
