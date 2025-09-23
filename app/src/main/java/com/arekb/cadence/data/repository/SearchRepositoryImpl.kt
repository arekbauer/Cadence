package com.arekb.cadence.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.paging.SearchPagingSource
import com.arekb.cadence.data.remote.paging.SearchResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    override suspend fun getArtistPageData(artistId: String): Result<ArtistPageData> {
        return try {
            // Ff any of the parallel calls fail, the others are automatically cancelled.
            coroutineScope {
                val detailsDeferred = async { api.getArtist(artistId) }
                val topTracksDeferred = async { api.getArtistTopTracks(artistId) }
                val albumsDeferred = async { api.getArtistAlbums(artistId) }

                // Wait for all three requests to complete
                val detailsResponse = detailsDeferred.await()
                val topTracksResponse = topTracksDeferred.await()
                val albumsResponse = albumsDeferred.await()

                // Combine the results into the single ArtistPageData object.
                val pageData = ArtistPageData(
                    details = detailsResponse,
                    topTracks = topTracksResponse,
                    albums = albumsResponse
                )

                Result.success(pageData)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}