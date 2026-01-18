package com.arekb.cadence.mappers

// 1. New Domain Models

// 2. Existing DTOs (Network)

// 3. Existing Entities (Database)
import com.arekb.cadence.core.model.Album
import com.arekb.cadence.core.model.Artist
import com.arekb.cadence.core.model.Track
import com.arekb.cadence.core.model.User
import com.arekb.cadence.data.local.database.entity.NewReleasesEntity
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.dto.ArtistObject
import com.arekb.cadence.data.remote.dto.ImageObject
import com.arekb.cadence.data.remote.dto.TrackObject
import com.arekb.cadence.data.remote.dto.UserProfile

// --- SHARED HELPER ---
fun List<ImageObject>?.asUrl(): String? {
    return this?.firstOrNull()?.url
}

// ==========================================
// NETWORK MAPPERS (DTO -> Domain)
// ==========================================

fun TrackObject.asDomainModel(): Track {
    return Track(
        id = this.id,
        name = this.name,
        // Map list of ArtistObjects to Domain Artists
        artists = this.artists.map { it.asDomainModel() },
        // Flatten the nested AlbumObject to just the URL
        albumImageUrl = this.album.images.asUrl(),
        // DTO missing duration? Default to 0 until you update DTO
        durationMs = 0L
    )
}

fun ArtistObject.asDomainModel(): Artist {
    return Artist(
        // DTO missing ID? Use empty string so app doesn't crash
        id = "",
        name = this.name,
        // ArtistObject in TrackObject usually has no images
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

// ==========================================
// DATABASE MAPPERS (Entity -> Domain)
// ==========================================

fun TopTracksEntity.asDomainModel(): Track {
    // Your Entity stores artists as a single string "Artist A, Artist B".
    // We must split it to match the List<Artist> requirement.
    val stubArtists = this.artistNames.split(", ").map { name ->
        Artist(id = "", name = name, imageUrl = null)
    }

    return Track(
        id = this.id,
        name = this.trackName,
        artists = stubArtists,
        albumImageUrl = this.imageUrl,
        durationMs = 0L // Entity doesn't store duration yet
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
        // Entity only has "artistName" string. Wrap it in a list.
        artists = listOf(Artist(id = "", name = this.artistName, imageUrl = null))
    )
}