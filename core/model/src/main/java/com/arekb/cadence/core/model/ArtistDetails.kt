package com.arekb.cadence.core.model

/**
 * A composite model that holds all the data needed for the Artist Details screen.
 */
data class ArtistDetails(
    val artist: Artist,
    val topTracks: List<Track>,
    val albums: List<Album>,
    val popularity: Int
)