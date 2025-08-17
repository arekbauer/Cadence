package com.arekb.cadence.data.remote.api

import com.arekb.cadence.data.remote.dto.TopArtistResponse
import com.arekb.cadence.data.remote.dto.TopItemsResponse
import com.arekb.cadence.data.remote.dto.UserProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface for the main Spotify Web API (api.spotify.com)
 */
interface SpotifyApiService {

    @GET("v1/me")
    suspend fun getCurrentUserProfile(): Response<UserProfile>

    /**
     * Fetches the user's top tracks.
     * @param timeRange Over what time frame the data is calculated. Valid values: long_term, medium_term, short_term.
     * @param limit The number of items to return. Default: 20. Max: 50.
     */
    @GET("v1/me/top/tracks")
    suspend fun getTopTracks(
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int
    ): Response<TopItemsResponse>

    /**
     * Fetches the user's top artists.
     * @param timeRange Over what time frame the data is calculated. Valid values: long_term, medium_term, short_term.
     * @param limit The number of items to return. Default: 20. Max: 50.
     */
    @GET("v1/me/top/artists")
    suspend fun getTopArtists(
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int
    ): Response<TopArtistResponse>
}