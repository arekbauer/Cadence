package com.arekb.cadence.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents a new releases response from the Spotify API.
 */
data class NewReleasesResponse(
    @SerializedName("items")
    val albums: AlbumList
)

/**
 * Represents the object that contains the list of albums.
 */
data class AlbumList(
    @SerializedName("items")
    val items: List<Album>
)

/**
 * Represents an album item from the Spotify API.
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