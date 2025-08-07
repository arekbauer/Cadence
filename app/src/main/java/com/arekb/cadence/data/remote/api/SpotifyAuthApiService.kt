package com.arekb.cadence.data.remote.api

import com.arekb.cadence.data.remote.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface SpotifyAuthApiService {

    @FormUrlEncoded
    @POST("api/token")
    suspend fun exchangeCodeForToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<TokenResponse>

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<TokenResponse>

}