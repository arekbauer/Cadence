package com.arekb.cadence.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arekb.cadence.core.database.dao.NewReleasesDao
import com.arekb.cadence.core.database.dao.TopArtistsDao
import com.arekb.cadence.core.database.dao.TopTracksDao
import com.arekb.cadence.core.database.dao.UserProfileDao
import com.arekb.cadence.core.database.entity.NewReleasesEntity
import com.arekb.cadence.core.database.entity.TopArtistsEntity
import com.arekb.cadence.core.database.entity.TopTracksEntity
import com.arekb.cadence.core.database.entity.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class, TopTracksEntity::class,
        TopArtistsEntity::class, NewReleasesEntity::class],
    version = 6,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun topTracksDao(): TopTracksDao
    abstract fun topArtistsDao(): TopArtistsDao
    abstract fun newReleasesDao(): NewReleasesDao
}