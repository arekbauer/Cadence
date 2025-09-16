package com.arekb.cadence.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.paging.SearchPagingSource
import com.arekb.cadence.data.remote.paging.SearchResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService
) : SearchRepository {

    override fun getSearchResultsStream(query: String, type: String): Flow<PagingData<SearchResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10, // How many items to load per page
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchPagingSource(api, query, type) }
        ).flow
    }

    override suspend fun findArtistById(artistId: String): Result<SearchResult?> {
        return try {
            val response = api.searchForItem(
                query = artistId,
                type = "artist",
                limit = 1, // We only need the top result
                offset = 0
            )

            // Find the first artist in the response items
            val artistDto = response.artists?.items?.firstOrNull()

            if (artistDto != null) {
                // Map the DTO to your SearchResult domain model
                val searchResult = SearchResult(
                    id = artistDto.id,
                    imageUrl = artistDto.images.firstOrNull()?.url,
                    title = artistDto.name,
                    subtitle = "Artist"
                )
                Result.success(searchResult)
            } else {
                // Return success with a null value if no artist was found
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}