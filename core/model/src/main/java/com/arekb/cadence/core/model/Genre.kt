package com.arekb.cadence.core.model

data class Genre(
    val name: String,
    val artists: List<Artist>
) {
    val artistCount: Int
        get() = artists.size
}