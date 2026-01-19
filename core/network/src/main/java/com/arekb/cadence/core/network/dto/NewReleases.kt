package com.arekb.cadence.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents a new releases response from the Spotify API.
 */
data class NewReleasesResponse(
    @SerializedName("albums")
    val albums: AlbumList
)

/**
 * Represents the object that contains the list of albums, plus pagination metadata.
 * This corresponds to the content *inside* the "albums" object in the JSON.
 *
 * Keeping pagination fields as they are part of this container object and might be useful.
 */
data class AlbumList(
    @SerializedName("href")
    val href: String,
    @SerializedName("items")
    val items: List<Album>,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("total")
    val total: Int
)

/**
 * Represents an album item from the Spotify API,
 * simplified to only include the requested relevant fields.
 */
data class Album(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("total_tracks")
    val totalTracks: Int,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("images")
    val images: List<ImageObject>,
    @SerializedName("artists")
    val artists: List<ArtistObject>
)