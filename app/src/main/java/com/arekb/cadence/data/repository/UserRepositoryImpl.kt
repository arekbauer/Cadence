package com.arekb.cadence.data.repository

import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.dto.UserProfile
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService
) : UserRepository {

    /**
     * Retrieves the user's profile information.
     *
     * @return a [Result] object containing the [UserProfile] on success, or an exception on failure.
     */
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
}
