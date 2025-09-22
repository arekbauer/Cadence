package com.arekb.cadence.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.dto.TopArtistObject
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

    override suspend fun getArtistById(artistId: String): Result<TopArtistObject> {
        return try {
            val response = api.getArtist(artistId)

            val artistDetails = TopArtistObject(
                id = response.id,
                name = response.name,
                images = response.images,
                genres = response.genres,
                popularity = response.popularity,
                uri = response.uri,
            )
            Result.success(artistDetails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}