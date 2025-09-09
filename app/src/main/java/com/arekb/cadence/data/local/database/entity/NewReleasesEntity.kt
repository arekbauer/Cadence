package com.arekb.cadence.data.local.database.entity

import androidx.room.Entity

@Entity(tableName = "new_releases", primaryKeys = ["id"])
data class NewReleasesEntity(
    val id: String,
    val name: String,
    val artistName: String,
    val imageUrl: String?,
    val releaseDate: String,
    val totalTracks: Int,
    val lastFetched: Long
)