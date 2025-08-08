package com.arekb.cadence.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents the top-level response from the /top/tracks and /top/artists endpoints.
 */
data class TopItemsResponse(
    @SerializedName("items")
    val items: List<TrackObject>
)

/**
 * Represents a full track object from the Spotify API.
 * We only include the fields we care about for now.
 */
data class TrackObject(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("artists")
    val artists: List<ArtistObject>,

    @SerializedName("album")
    val album: AlbumObject
)

/**
 * Represents an artist object.
 */
data class ArtistObject(
    @SerializedName("name")
    val name: String
)

/**
 * Represents an album object, which contains the images.
 */
data class AlbumObject(
    @SerializedName("images")
    val images: List<ImageObject>
)