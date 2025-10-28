package com.arekb.cadence.data.repository

import androidx.paging.PagingData
import com.arekb.cadence.data.remote.dto.ArtistAlbumsResponse
import com.arekb.cadence.data.remote.dto.ArtistTopTracksResponse
import com.arekb.cadence.data.remote.dto.TopArtistObject
import com.arekb.cadence.data.remote.paging.SearchResult
import kotlinx.coroutines.flow.Flow

data class ArtistPageData(
    val details: TopArtistObject?,
    val topTracks: ArtistTopTracksResponse?,
    val albums: ArtistAlbumsResponse?
)

interface SearchRepository {

    /**
     * Search for items using the Spotify search for items endpoint.
     * @param query The search query.
     * @param type The type of items to search for.
     * @return A [Flow] of [PagingData] of [SearchResult] objects.
     */
    fun getSearchResultsStream(query: String, type: String): Flow<PagingData<SearchResult>>


    /**
     * Get the details, top tracks, and albums of an artist.
     * @param artistId The Spotify ID of the artist.
     * @return A [Result] containing the [ArtistPageData] on success, or an [Exception] on failure.
     */
    suspend fun getArtistPageData(artistId: String): Result<ArtistPageData>
}