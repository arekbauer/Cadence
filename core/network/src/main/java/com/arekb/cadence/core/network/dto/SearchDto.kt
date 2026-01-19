package com.arekb.cadence.core.network.dto

/**
 * The top-level response from the /search endpoint.
 * It contains paging objects for each type of result.
 */
data class SearchResponseDto(
    val artists: PagingDto<TopArtistObject>?,
    val albums: PagingDto<AlbumSearchDto>?
)

/**
 * A generic wrapper for paginated content from the Spotify API.
 */
data class PagingDto<T>(
    val items: List<T>,
    val next: String?,
    val total: Int
)

data class AlbumSearchDto(val id: String, val name: String, val images: List<ImageObject>?)