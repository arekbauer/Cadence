package com.arekb.cadence.core.model

data class User(
    val id: String,
    val displayName: String,
    val email: String?,
    val imageUrl: String?
)
