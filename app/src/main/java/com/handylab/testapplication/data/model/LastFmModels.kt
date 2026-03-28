package com.handylab.testapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastFmTopTracksResponse(
    val tracks: LastFmTracks
)

@Serializable
data class LastFmTracks(
    val track: List<LastFmTrack>
)

@Serializable
data class LastFmTrack(
    val name: String,
    val artist: LastFmArtist,
    val image: List<LastFmImage> = emptyList(),
    @SerialName("@attr") val attr: LastFmTrackAttr? = null
)

@Serializable
data class LastFmArtist(
    val name: String
)

@Serializable
data class LastFmImage(
    @SerialName("#text") val url: String,
    val size: String
)

@Serializable
data class LastFmTrackAttr(
    val rank: String
)
