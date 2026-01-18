package com.arekb.cadence.core.network.api

import com.arekb.cadence.core.network.BuildConfig
import com.arekb.cadence.core.network.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.Base64
import javax.inject.Inject

class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val api: SpotifyAuthApiService
): Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // We need to run this synchronously on OkHttp's background thread.
        return runBlocking {
            val oldToken = tokenManager.getAccessToken()
            val currentTokenInRequest = response.request.header("Authorization")?.substringAfter("Bearer ")

            // If the token has already been refreshed by another call, just use the new one.
            if (oldToken != null && oldToken != currentTokenInRequest) {
                return@runBlocking newRequestWithToken(response.request, oldToken)
            }

            val refreshToken = tokenManager.getRefreshToken() ?: return@runBlocking null
            val credentials = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())

            // Attempt to refresh the token.
            val refreshResponse = try {
                api.refreshAccessToken(
                    authorization = basicAuth,
                    refreshToken = refreshToken
                )
            } catch (e: Exception) {
                null
            }

            if (refreshResponse != null && refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val tokenData = refreshResponse.body()!!

                // Save new tokens
                tokenManager.saveAccessToken(tokenData.accessToken)
                tokenData.refreshToken?.let { tokenManager.saveRefreshToken(it) }

                // Retry original request
                newRequestWithToken(response.request, tokenData.accessToken)
            } else {
                // Refresh failed (session expired) -> Clear tokens so user is logged out
                tokenManager.clearTokens()
                null
            }
        }
    }

    private fun newRequestWithToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}