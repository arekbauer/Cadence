package com.arekb.cadence.data.repository

import androidx.paging.PagingData
import com.arekb.cadence.data.remote.dto.TopArtistObject
import com.arekb.cadence.data.remote.paging.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    /**
     * Search for items using the Spotify search for items endpoint.
     * @param query The search query.
     * @param type The type of items to search for.
     * @return A [Flow] of [PagingData] of [SearchResult] objects.
     */
    fun getSearchResultsStream(query: String, type: String): Flow<PagingData<SearchResult>>

    /**
     * Get details of an artist using the Spotify artist endpoint.
     * @param artistId The ID of the artist.
     * @return A [Result] containing the [TopArtistObject] on success or an error message on failure.
     */
    suspend fun getArtistById(artistId: String): Result<TopArtistObject>
}