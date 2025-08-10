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

    fun getTopTracks(timeRange: String, limit: Int): Flow<Result<List<TopTracksEntity>?>>
}
