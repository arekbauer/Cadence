package com.arekb.cadence.data.repository

import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
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
     * @return A [Flow] of [Result] containing the user's top tracks.
     */
    fun getTopTracks(timeRange: String, limit: Int): Flow<Result<List<TopTracksEntity>?>>

    /**
     * Retrieves the user's top artists - either fetches or retrieves from cache.
     * @param timeRange The time range to fetch tracks for.
     * @param limit The maximum number of tracks to retrieve.
     * @return A [Flow] of [Result] containing the user's top artists.
     */
    fun getTopArtists(timeRange: String, limit: Int): Flow<Result<List<TopArtistsEntity>?>>

    /**
     * Forces a refresh a network call of the user's top tracks.
     * @param timeRange The time range to fetch tracks for.
     */
    suspend fun forceRefreshTopTracks(timeRange: String): Result<Unit>

    /**
     * Forces a refresh a network call of the user's top artists.
     * @param timeRange The time range to fetch tracks for.
     */
    suspend fun forceRefreshTopArtists(timeRange: String): Result<Unit>

}
