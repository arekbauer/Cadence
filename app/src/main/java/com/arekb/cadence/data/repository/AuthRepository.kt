package com.arekb.cadence.data.repository

import com.spotify.sdk.android.auth.AuthorizationRequest

interface AuthRepository {
    fun getAuthorizationRequest(): AuthorizationRequest
    suspend fun exchangeCodeForToken(code: String): Result<Unit>
}