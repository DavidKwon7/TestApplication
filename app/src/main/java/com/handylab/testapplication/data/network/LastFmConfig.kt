package com.handylab.testapplication.data.network

import com.handylab.testapplication.BuildConfig

/**
 * Last.fm API 설정.
 *
 * API_KEY는 local.properties의 `LASTFM_API_KEY` 값에서 자동으로 주입됩니다.
 * 키 값을 코드에 직접 작성하지 말고 local.properties에서만 관리하세요.
 */
internal object LastFmConfig {
    val API_KEY: String get() = BuildConfig.LASTFM_API_KEY
}
