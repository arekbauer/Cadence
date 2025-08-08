package com.arekb.cadence.data.repository

import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.dto.TopItemsResponse
import com.arekb.cadence.data.remote.dto.UserProfile
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService
) : UserRepository {

    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val response = api.getCurrentUserProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user's profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopTracks(timeRange: String, limit: Int): Result<TopItemsResponse> {
        return try {
            val response = api.getTopTracks(timeRange, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch top tracks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
