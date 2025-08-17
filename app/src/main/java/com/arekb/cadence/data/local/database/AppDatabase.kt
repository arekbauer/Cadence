package com.arekb.cadence.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arekb.cadence.data.local.database.dao.TopArtistsDao
import com.arekb.cadence.data.local.database.dao.TopTracksDao
import com.arekb.cadence.data.local.database.dao.UserProfileDao
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class, TopTracksEntity::class, TopArtistsEntity::class],
    version = 4,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun topTracksDao(): TopTracksDao
    abstract fun topArtistsDao(): TopArtistsDao
}