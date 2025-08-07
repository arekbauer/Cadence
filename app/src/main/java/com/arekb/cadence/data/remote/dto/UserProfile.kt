package com.arekb.cadence.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents an image object from the Spotify API.
 */
data class ImageObject(
    @SerializedName("url")
    val url: String,

    @SerializedName("height")
    val height: Int?,

    @SerializedName("width")
    val width: Int?
)

/**
 * Represents the user profile object from the Spotify API.
 */
data class UserProfile(
    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("id")
    val id: String,

    @SerializedName("images")
    val images: List<ImageObject>
)
