package com.arekb.cadence.core.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val genres: List<String> = emptyList(),
    val popularity: Int? = null
)