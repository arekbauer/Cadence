package com.arekb.cadence.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arekb.cadence.data.local.database.entity.NewReleasesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewReleasesDao {
    /**
     * Retrieves all cached new releases.
     */
    @Query("SELECT * FROM new_releases ORDER BY lastFetched DESC")
    fun getNewReleases(): Flow<List<NewReleasesEntity>>

    /**
     * Inserts a list of new releases, replacing any existing ones.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewReleases(releases: List<NewReleasesEntity>)

    /**
     * Deletes all cached new releases.
     */
    @Query("DELETE FROM new_releases")
    suspend fun clearNewReleases()
}