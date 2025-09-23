package com.arekb.cadence.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents an image object.
 */
data class ArtistAlbumsResponse(
    @SerializedName("total")
    val total: Int,
    @SerializedName("items")
    val items: List<SimpleAlbumObject>
)

/**
 * Represents a simplified album object within the items list.
 */
data class SimpleAlbumObject(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("album_type")
    val albumType: String,
    @SerializedName("total_tracks")
    val totalTracks: Int,
    @SerializedName("images")
    val images: List<ImageObject>,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("release_date_precision")
    val releaseDatePrecision: String,
    @SerializedName("artists")
    val artists: List<ArtistObject>,
)

/**
 * Represents the top-level response from the /top/tracks endpoints.
 */
data class ArtistTopTracksResponse(
    @SerializedName("tracks")
    val items: List<TrackObject>
)