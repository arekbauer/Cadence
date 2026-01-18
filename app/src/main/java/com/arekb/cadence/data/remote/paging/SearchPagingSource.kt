package com.arekb.cadence.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arekb.cadence.core.model.SearchResult
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.mappers.asDomainModel

class SearchPagingSource(
    private val spotifyApiService: SpotifyApiService,
    private val query: String,
    private val type: String
): PagingSource<Int, SearchResult>() {

    /**
     * Loads the next page of data from the Spotify API.
     * @param params The parameters for the load operation.
     * @return A [LoadResult] containing the loaded data and metadata.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        // The current page is the key, or 0 if it's the first load
        val offset = params.key ?: 0

        return try {
            val response = spotifyApiService.searchForItem(
                query = query,
                type = type,
                limit = params.loadSize,
                offset = offset
            )

            // Map the different DTO types to the single SearchResult domain model
            val results = mutableListOf<SearchResult>()
            response.artists?.items?.map { dto ->
                results.add(SearchResult.ArtistItem(dto.asDomainModel()))
            }
            response.albums?.items?.map { dto ->
                results.add(SearchResult.AlbumItem(dto.asDomainModel()))
            }

            val nextKey = if (response.artists?.next != null || response.albums?.next != null) {
                offset + params.loadSize
            } else {
                null
            }

            LoadResult.Page(
                data = results,
                prevKey = if (offset == 0) null else offset - params.loadSize,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * Gets the key for the next or previous page of data.
     * @param state The current paging state.
     * @return The key for the next or previous page, or null if there is no key.
     */
    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}