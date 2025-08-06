package com.arekb.cadence.data.repository

import com.spotify.sdk.android.auth.AuthorizationRequest

interface AuthRepository {
    /**
     * Builds the authorization request needed by the Spotify SDK.
     */
    fun getAuthorizationRequest(): AuthorizationRequest

    /**
     * Exchanges the authorization code for an access and refresh token.
     * @param code The authorization code from the Spotify SDK response.
     * @return A Result indicating success or failure.
     */
    suspend fun exchangeCodeForToken(code: String): Result<Unit>

    /**
     * Checks if a user is currently logged in by verifying if an access token exists.
     * This is now a suspend function because it reads from the asynchronous DataStore.
     */
    suspend fun isLoggedIn(): Boolean
}