package com.arekb.cadence.mappers

import com.arekb.cadence.core.model.Album
import com.arekb.cadence.core.model.Artist
import com.arekb.cadence.core.model.Track
import com.arekb.cadence.core.model.User
import com.arekb.cadence.data.local.database.entity.NewReleasesEntity
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity

fun TopTracksEntity.asDomainModel(): Track {
    val stubArtists = this.artistNames.split(", ").map { name ->
        Artist(id = "", name = name, imageUrl = null)
    }

    return Track(
        id = this.id,
        name = this.trackName,
        artists = stubArtists,
        albumImageUrl = this.imageUrl,
        durationMs = 0L
    )
}

fun TopArtistsEntity.asDomainModel(): Artist {
    return Artist(
        id = this.id,
        name = this.artistName,
        imageUrl = this.imageUrl,
        genres = this.genres.split(", ").filter { it.isNotBlank() && it != "Unknown" },
        popularity = this.popularity
    )
}

fun UserProfileEntity.asDomainModel(): User {
    return User(
        id = this.id,
        displayName = this.displayName,
        email = this.email,
        imageUrl = this.imageUrl
    )
}

fun NewReleasesEntity.asDomainModel(): Album {
    return Album(
        id = this.id,
        name = this.name,
        albumType = "album",
        totalTracks = this.totalTracks,
        imageUrl = this.imageUrl,
        releaseDate = this.releaseDate,
        artists = listOf(Artist(id = "", name = this.artistName, imageUrl = null))
    )
}