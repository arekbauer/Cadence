package com.arekb.cadence.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents the top-level response from the /top/tracks endpoints.
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
 * Represents the top-level response from the /top/artists endpoints.
 */
data class TopArtistResponse(
    @SerializedName("items")
    val items: List<TopArtistObject>
)

/**
 * Represents a top artist object.
 */
data class TopArtistObject(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("images")
    val images: List<ImageObject>,
    @SerializedName("popularity")
    val popularity: Int,
    @SerializedName("genres")
    val genres: List<String>
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
