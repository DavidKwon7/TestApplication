package com.handylab.testapplication.data.model

data class CherryBlossomInfo(
    val cityName: String,
    val lat: Double,
    val lng: Double,
    val bloomDate: String,
    val fullBloomDate: String,
    val status: String // "개화 전", "개화", "만발", "짐" 등
)
