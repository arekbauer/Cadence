package com.arekb.cadence.data.repository

import com.arekb.cadence.data.remote.dto.UserProfile

/**
 * Repository responsible for managing user data.
 */
interface UserRepository {
    /**
     * Fetches the user's Spotify profile.
     *
     * This function is responsible for retrieving the profile information
     * of the currently authenticated user from the backend.
     *
     * @return A [Result] object which encapsulates either a [UserProfile] on success
     * or an exception on failure.
     */
    suspend fun getProfile(): Result<UserProfile>
}
