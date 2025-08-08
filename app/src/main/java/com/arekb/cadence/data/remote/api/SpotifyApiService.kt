package com.arekb.cadence.data.remote.api

import com.arekb.cadence.data.remote.dto.UserProfile
import retrofit2.Response
import retrofit2.http.GET

/**
 * Interface for the main Spotify Web API (api.spotify.com)
 */
interface SpotifyApiService {

    @GET("v1/me")
    suspend fun getCurrentUserProfile(): Response<UserProfile>
}