package com.arekb.cadence.data.local.database.entity

import androidx.room.Entity

@Entity(tableName = "top_artists", primaryKeys = ["id", "timeRange"])
data class TopArtistsEntity(
    val id: String,
    val artistName: String,
    val imageUrl: String?,
    val timeRange: String,
    val popularity: Int,
    val rank: Int,
    val lastFetched: Long
)