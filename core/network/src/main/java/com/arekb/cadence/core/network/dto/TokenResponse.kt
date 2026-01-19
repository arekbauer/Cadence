package com.arekb.cadence.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents the successful JSON response from Spotify's token endpoint
 */
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String?, // Refresh token is not always returned on refresh calls

    @SerializedName("expires_in")
    val expiresIn: Int, // The lifetime of the access token in seconds

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("scope")
    val scope: String
)