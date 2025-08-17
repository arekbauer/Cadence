package com.arekb.cadence.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopArtistsDao {
    /**
     * Inserts a list of top tracks into the database.
     * If a track with the same primary key (id + time_range) already exists,
     * it will be replaced.
     * @param artists The list of TopArtistsEntity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopArtists(artists: List<TopArtistsEntity>)

    /**
     * Retrieves a list of top artists for a specific time range, ordered by their rank.
     * This function returns a Flow, so the UI will automatically update whenever
     * the data in this table changes.
     * @param timeRange The time range to query for (e.g., "short_term").
     * @return A Flow emitting the list of top artists.
     */
    @Query("SELECT * FROM top_artists WHERE timeRange = :timeRange ORDER BY `rank` ASC")
    fun getTopArtists(timeRange: String): Flow<List<TopArtistsEntity>>

    /**
     * Deletes all artists from the database that match a specific time range.
     * This is used to clear out old data before inserting a fresh list from the API.
     * @param timeRange The time range to clear (e.g., "short_term").
     */
    @Query("DELETE FROM top_artists WHERE timeRange = :timeRange")
    suspend fun clearTopArtists(timeRange: String)
}