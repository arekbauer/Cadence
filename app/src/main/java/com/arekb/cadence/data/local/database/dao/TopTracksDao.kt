package com.arekb.cadence.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopTracksDao {
    /**
     * Inserts a list of top tracks into the database.
     * If a track with the same primary key (id + time_range) already exists,
     * it will be replaced.
     * @param tracks The list of TopTrackEntity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopTracks(tracks: List<TopTracksEntity>)

    /**
     * Retrieves a list of top tracks for a specific time range, ordered by their rank.
     * This function returns a Flow, so the UI will automatically update whenever
     * the data in this table changes.
     * @param timeRange The time range to query for (e.g., "short_term").
     * @return A Flow emitting the list of top tracks.
     */
    @Query("SELECT * FROM top_tracks WHERE timeRange = :timeRange ORDER BY `rank` ASC")
    fun getTopTracks(timeRange: String): Flow<List<TopTracksEntity>>

    /**
     * Deletes all tracks from the database that match a specific time range.
     * This is used to clear out old data before inserting a fresh list from the API.
     * @param timeRange The time range to clear (e.g., "short_term").
     */
    @Query("DELETE FROM top_tracks WHERE timeRange = :timeRange")
    suspend fun clearTopTracks(timeRange: String)
}