package com.arekb.cadence.core.model

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val durationMs: Long,
)