package com.arekb.cadence.data.repository

import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing user data.
 */
interface UserRepository {

    /**
     * Retrieves the user profile from the data source.
     * @return A [Flow] of [Result] containing the user profile.
     */
    fun getProfile(): Flow<Result<UserProfileEntity?>>

    /**
     * Retrieves the user's top tracks - either fetches or retrieves from cache.
     * @param timeRange The time range to fetch tracks for.
     * @param limit The maximum number of tracks to retrieve.
     */
    fun getTopTracks(timeRange: String, limit: Int): Flow<Result<List<TopTracksEntity>?>>

    /**
     * Forces a refresh a network call of the user's top tracks.
     * @param timeRange The time range to fetch tracks for.
     */
    suspend fun forceRefreshTopTracks(timeRange: String): Result<Unit>
}
