package com.arekb.cadence.core.data.repository

import com.arekb.cadence.core.data.BuildConfig
import com.arekb.cadence.core.network.api.SpotifyAuthApiService
import com.arekb.cadence.core.network.auth.TokenManager
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

// Redirect URI
private const val REDIRECT_URI = "cadence-app://callback"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: SpotifyAuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override fun getAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            AuthorizationResponse.Type.CODE,
            REDIRECT_URI
        )
            .setScopes(arrayOf("user-read-private", "user-top-read",
                "playlist-modify-public", "user-follow-read",
                "user-read-recently-played"))
            .build()
    }

    override suspend fun exchangeCodeForToken(code: String): Result<Unit> {
        return try {
            // Get credentials from BuildConfig
            val credentials = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())

            // Expect a TokenResponse object
            val response = api.exchangeCodeForToken(
                authorization = basicAuth,
                code = code,
                redirectUri = REDIRECT_URI
            )

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenManager.saveAccessToken(tokenResponse.accessToken)
                tokenResponse.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to exchange code for token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    override suspend fun refreshAccessToken(): Result<Unit> {
        val refreshToken = tokenManager.getRefreshToken() ?: return Result.failure(Exception("No refresh token available"))
        return try {
            val credentials = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())

            val response = api.refreshAccessToken(
                authorization = basicAuth,
                refreshToken = refreshToken
            )

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenManager.saveAccessToken(tokenResponse.accessToken)
                // Spotify sometimes returns a new refresh token, so save it if it exists
                tokenResponse.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                Result.success(Unit)
            } else {
                // If refresh fails, the token is invalid. Clear them.
                tokenManager.clearTokens()
                Result.failure(Exception("Refresh token failed"))
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.failure(e)
        }
    }
}