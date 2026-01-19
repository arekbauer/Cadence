package com.arekb.cadence.core.model

data class Album(
    val id: String,
    val name: String,
    val albumType: String,
    val totalTracks: Int,
    val imageUrl: String?,
    val releaseDate: String,
    val artists: List<Artist>
)