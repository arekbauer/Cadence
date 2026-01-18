package com.arekb.cadence.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arekb.cadence.core.model.ArtistDetails
import com.arekb.cadence.core.model.SearchResult
import com.arekb.cadence.core.network.api.SpotifyApiService
import com.arekb.cadence.core.network.mappers.asDomainModel
import com.arekb.cadence.core.network.paging.SearchPagingSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService
) : SearchRepository {

    override fun getSearchResultsStream(query: String, type: String): Flow<PagingData<SearchResult>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { SearchPagingSource(api, query, type) }
        ).flow
    }

    override suspend fun getArtistPageData(artistId: String): Result<ArtistDetails> {
        return try {
            coroutineScope {
                val detailsDeferred = async { api.getArtist(artistId) }
                val topTracksDeferred = async { api.getArtistTopTracks(artistId) }
                val albumsDeferred = async { api.getArtistAlbums(artistId) }

                val detailsDto = detailsDeferred.await()
                val topTracksDto = topTracksDeferred.await()
                val albumsDto = albumsDeferred.await()

                // MAP DTOs -> DOMAIN
                val pageData = ArtistDetails(
                    artist = detailsDto.asDomainModel(),
                    topTracks = topTracksDto.items.map { it.asDomainModel() },
                    albums = albumsDto.items.map { it.asDomainModel() },
                    popularity = detailsDto.popularity
                )

                Result.success(pageData)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}