package com.arekb.cadence.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents the top-level response from the /me/player/recently-played endpoint.
 */
data class RecentlyPlayedResponse(
    @SerializedName("items")
    val items: List<PlayHistoryObject>
)

/**
 * Represents a single item in the play history.
 * It contains the track that was played and when it was played.
 */
data class PlayHistoryObject(
    @SerializedName("track")
    val track: TrackObject,

    @SerializedName("played_at")
    val playedAt: String
)