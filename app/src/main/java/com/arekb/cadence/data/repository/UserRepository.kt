package com.arekb.cadence.data.repository

import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.dto.TopItemsResponse
import com.arekb.cadence.data.remote.dto.UserProfile
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

    suspend fun getTopTracks(timeRange: String, limit: Int): Result<TopItemsResponse>
}
