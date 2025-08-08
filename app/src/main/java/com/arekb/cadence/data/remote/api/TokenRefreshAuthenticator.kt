package com.arekb.cadence.data.remote.api

import com.arekb.cadence.data.local.TokenManager
import com.arekb.cadence.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: dagger.Lazy<AuthRepository>
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

            // Attempt to refresh the token.
            val result = authRepository.get().refreshAccessToken()

            if (result.isSuccess) {
                val newToken = tokenManager.getAccessToken()
                if (newToken != null) {
                    // Success! Retry the original request with the new token.
                    return@runBlocking newRequestWithToken(response.request, newToken)
                }
            }

            // If refresh fails (e.g., refresh token is invalid), give up.
            // This will cause the original request to fail with a 401.
            null
        }
    }

    private fun newRequestWithToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}