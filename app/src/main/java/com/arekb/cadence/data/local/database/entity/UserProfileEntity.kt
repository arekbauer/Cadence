package com.arekb.cadence.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String?,
    val imageUrl: String?
)