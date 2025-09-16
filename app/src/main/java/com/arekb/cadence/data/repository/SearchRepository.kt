package com.arekb.cadence.data.repository

import androidx.paging.PagingData
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

    suspend fun findArtistById(artistId: String): Result<SearchResult?>
}