package com.arekb.cadence.data.repository

import com.arekb.cadence.data.remote.api.SpotifyAuthApiService
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.arekb.cadence.BuildConfig
import com.arekb.cadence.data.local.TokenManager
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
                "playlist-modify-public", "user-follow-read"))
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
}