package com.arekb.cadence.data.remote.api

import com.arekb.cadence.data.remote.dto.NewReleasesResponse
import com.arekb.cadence.data.remote.dto.RecentlyPlayedResponse
import com.arekb.cadence.data.remote.dto.SearchResponseDto
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

    /**
     * Fetches the user's profile.
     */
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

    /**
     * Fetches the user's recently played tracks.
     * @param limit The number of items to return. Default: 20. Max: 50.
     */
    @GET("v1/me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Query("limit") limit: Int
    ): Response<RecentlyPlayedResponse>

    /**
     * Fetches the user's new releases.
     * @param limit The number of items to return. Default: 20. Max: 50.
     */
    @GET("v1/browse/new-releases")
    suspend fun getNewReleases(
        @Query("limit") limit: Int
    ): Response<NewReleasesResponse>

    /**
     * Searches for an item.
     * @param query The search query.
     * @param type The type of item to search for. Valid values: track, album, artist.
     * @param limit The number of items to return. Default: 20. Max: 50.
     */
    @GET("v1/search")
    suspend fun searchForItem(
        @Query("q") query: String,
        @Query("type") type: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): SearchResponseDto
}