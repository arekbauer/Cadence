package com.arekb.cadence.core.network.mappers

import com.arekb.cadence.core.model.Album
import com.arekb.cadence.core.model.Artist
import com.arekb.cadence.core.model.Track
import com.arekb.cadence.core.model.User
import com.arekb.cadence.core.network.dto.AlbumSearchDto
import com.arekb.cadence.core.network.dto.ArtistObject
import com.arekb.cadence.core.network.dto.ImageObject
import com.arekb.cadence.core.network.dto.SimpleAlbumObject
import com.arekb.cadence.core.network.dto.TopArtistObject
import com.arekb.cadence.core.network.dto.TrackObject
import com.arekb.cadence.core.network.dto.UserProfile

// Helper to get image URL
fun List<ImageObject>?.asUrl(): String? {
    return this?.firstOrNull()?.url
}

fun TrackObject.asDomainModel(): Track {
    return Track(
        id = this.id,
        name = this.name,
        artists = this.artists.map { it.asDomainModel() },
        albumImageUrl = this.album.images.asUrl(),
        durationMs = 0L
    )
}

fun ArtistObject.asDomainModel(): Artist {
    return Artist(
        id = "",
        name = this.name,
        imageUrl = null,
        genres = emptyList(),
        popularity = null
    )
}

fun UserProfile.asDomainModel(): User {
    return User(
        id = this.id,
        displayName = this.displayName,
        email = this.email,
        imageUrl = this.images.asUrl()
    )
}

fun AlbumSearchDto.asDomainModel(): Album {
    return Album(
        id = this.id,
        name = this.name,
        imageUrl = this.images.asUrl(),
        albumType = "album",
        totalTracks = 0,
        releaseDate = "",
        artists = emptyList()
    )
}


fun SimpleAlbumObject.asDomainModel(): Album {
    return Album(
        id = this.id,
        name = this.name,
        imageUrl = this.images.asUrl(),
        albumType = this.albumType,
        totalTracks = this.totalTracks,
        releaseDate = this.releaseDate,
        artists = this.artists.map { it.asDomainModel() }
    )
}

fun TopArtistObject.asDomainModel(): Artist {
    return Artist(
        id = this.id,
        name = this.name,
        imageUrl = this.images.asUrl(),
        genres = this.genres,
        popularity = this.popularity
    )
}