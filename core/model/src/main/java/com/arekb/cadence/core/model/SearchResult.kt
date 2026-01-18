package com.arekb.cadence.core.model

sealed interface SearchResult {
    data class TrackItem(val track: Track) : SearchResult
    data class AlbumItem(val album: Album) : SearchResult
    data class ArtistItem(val artist: Artist) : SearchResult
}