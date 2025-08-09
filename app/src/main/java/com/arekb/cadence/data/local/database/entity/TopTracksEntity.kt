package com.arekb.cadence.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "top_tracks", primaryKeys = ["id", "timeRange"])
data class TopTracksEntity(
    val id: Int = 0,
    val trackName: String,
    val artistNames: String,
    val imageUrl: String?,
    val timeRange: String,
    val rank: Int
)