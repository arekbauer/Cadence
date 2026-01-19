package com.arekb.cadence.core.data.repository

import com.spotify.sdk.android.auth.AuthorizationRequest

/**
 * Manages authentication with the Spotify API.
 * This interface defines the contract for handling user authorization,
 * exchanging authorization codes for access tokens, and managing the user's login state.
 */
interface AuthRepository {
    /**
     * Builds the authorization request needed by the Spotify SDK.
     * This request contains the necessary parameters for Spotify to identify the application
     * and request the correct permissions from the user.
     *
     * @return An [com.spotify.sdk.android.auth.AuthorizationRequest] object to be used with the Spotify SDK.
     */
    fun getAuthorizationRequest(): AuthorizationRequest

    /**
     * Exchanges the authorization code for an access and refresh token.
     * After the user authorizes the application, Spotify returns a temporary code.
     * This function sends that code to the backend to exchange it for a long-lived
     * access token and a refresh token.
     *
     * @param code The authorization code from the Spotify SDK response.
     * @return A [Result] indicating success or failure of the token exchange.
     */
    suspend fun exchangeCodeForToken(code: String): Result<Unit>

    /**
     * Checks if a user is currently logged in.
     * This is determined by verifying if a valid, unexpired access token is stored locally.
     * This is a suspend function because it may involve asynchronous I/O operations,
     * such as reading from DataStore.
     *
     * @return `true` if the user is considered logged in, `false` otherwise.
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Attempts to refresh the access token using the stored refresh token.
     * This is used when the current access token expires to obtain a new one without
     * requiring the user to log in again.
     *
     * @return A [Result] indicating the success or failure of the token refresh process.
     */
    suspend fun refreshAccessToken(): Result<Unit>
}
