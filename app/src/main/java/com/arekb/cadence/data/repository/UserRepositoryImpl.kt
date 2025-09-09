package com.arekb.cadence.data.repository

import com.arekb.cadence.data.local.database.dao.NewReleasesDao
import com.arekb.cadence.data.local.database.dao.TopArtistsDao
import com.arekb.cadence.data.local.database.dao.TopTracksDao
import com.arekb.cadence.data.local.database.dao.UserProfileDao
import com.arekb.cadence.data.local.database.entity.NewReleasesEntity
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.dto.PlayHistoryObject
import com.arekb.cadence.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService,
    private val dao: UserProfileDao,
    private val tracksDao: TopTracksDao,
    private val artistsDao: TopArtistsDao,
    private val newReleasesDao: NewReleasesDao
) : UserRepository {

    override fun getProfile(): Flow<Result<UserProfileEntity?>> = networkBoundResource(
        query = {
            dao.getUserProfile()
        },
        fetch = {
            api.getCurrentUserProfile()
        },
        saveFetchResult = { response ->
            if (response.isSuccessful && response.body() != null) {
                val userProfileDto = response.body()!!
                val userEntity = UserProfileEntity(
                    id = userProfileDto.id,
                    displayName = userProfileDto.displayName,
                    email = userProfileDto.email,
                    imageUrl = userProfileDto.images.firstOrNull()?.url
                )
                dao.insertUser(userEntity)
            }
        },
        shouldFetch = { true } // Currently always fetching profile information LIVE
    )

    override fun getTopTracks(timeRange: String, limit: Int):
            Flow<Result<List<TopTracksEntity>?>> = networkBoundResource(
                query = {
                    tracksDao.getTopTracks(timeRange)
                },
                fetch = {
                    api.getTopTracks(timeRange, limit)
                },
                saveFetchResult = { response ->
                    if (response.isSuccessful && response.body() != null) {
                        val topTracksDto = response.body()!!
                        val topTracksEntities = topTracksDto.items.map { track ->
                        val allArtistNames = track.artists.joinToString(", ") { it.name }
                            TopTracksEntity(
                                id = track.id,
                                trackName = track.name,
                                artistNames = allArtistNames.ifEmpty { "Unknown" },
                                imageUrl = track.album.images.firstOrNull()?.url ?: "",
                                timeRange = timeRange,
                                rank = topTracksDto.items.indexOf(track) + 1,
                                lastFetched = System.currentTimeMillis()
                            )
                        }
                        tracksDao.clearTopTracks(timeRange)
                        tracksDao.insertTopTracks(topTracksEntities)
                    }
                },
        shouldFetch = { cachedTracks ->
            if (cachedTracks.isNullOrEmpty()) {
                true
            } else {
                // Fetch if the cached data is older than 1 hour.
                val firstTrack = cachedTracks.first()
                val isStale = System.currentTimeMillis() - firstTrack.lastFetched > CACHE_EXPIRATION_MS
                isStale
            }
        }
    )

    override fun getTopArtists(timeRange: String, limit: Int):
            Flow<Result<List<TopArtistsEntity>?>> = networkBoundResource(
        query = {
            artistsDao.getTopArtists(timeRange)
        },
        fetch = {
            api.getTopArtists(timeRange, limit)
        },
        saveFetchResult = { response ->
            if (response.isSuccessful && response.body() != null) {
                val topArtistsDto = response.body()!!
                val topArtistsEntities = topArtistsDto.items.map { artist ->
                    val allGenres = artist.genres.joinToString(", ")
                    TopArtistsEntity(
                        id = artist.id,
                        artistName = artist.name,
                        imageUrl = artist.images.firstOrNull()?.url,
                        timeRange = timeRange,
                        rank = topArtistsDto.items.indexOf(artist) + 1,
                        lastFetched = System.currentTimeMillis(),
                        popularity = artist.popularity,
                        genres = allGenres.ifEmpty { "Unknown" }
                    )
                }
                artistsDao.clearTopArtists(timeRange)
                artistsDao.insertTopArtists(topArtistsEntities)
            }
        },
        shouldFetch = { cachedArtists ->
            if (cachedArtists.isNullOrEmpty()) {
                true
            } else {
                // Fetch if the cached data is older than 1 hour.
                val firstArtist = cachedArtists.first()
                val isStale = System.currentTimeMillis() - firstArtist.lastFetched > CACHE_EXPIRATION_MS
                isStale
            }
        }
    )

    override suspend fun forceRefreshTopTracks(timeRange: String): Result<Unit> {
        return try {
            // 1. Directly call the API
            val response = api.getTopTracks(timeRange, 20)

            if (response.isSuccessful && response.body() != null) {
                // 2. Map the network response to database entities
                val topTracksDto = response.body()!!
                val topTracksEntities = topTracksDto.items.map { track ->
                    val allArtistNames = track.artists.joinToString(", ") { it.name }
                    TopTracksEntity(
                        id = track.id,
                        trackName = track.name,
                        artistNames = allArtistNames.ifEmpty { "Unknown" },
                        imageUrl = track.album.images.firstOrNull()?.url ?: "",
                        timeRange = timeRange,
                        rank = topTracksDto.items.indexOf(track) + 1,
                        lastFetched = System.currentTimeMillis()
                    )
                }
                // 3. Save the new data to the database
                tracksDao.clearTopTracks(timeRange)
                tracksDao.insertTopTracks(topTracksEntities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to refresh top tracks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forceRefreshTopArtists(timeRange: String): Result<Unit> {
        return try {
            // 1. Directly call the API
            val response = api.getTopArtists(timeRange, 20)

            if (response.isSuccessful && response.body() != null) {
                // 2. Map the network response to database entities
                val topArtistsDto = response.body()!!
                val topArtistsEntities = topArtistsDto.items.map { artist ->
                    val allGenres = artist.genres.joinToString(", ")
                    TopArtistsEntity(
                        id = artist.id,
                        artistName = artist.name,
                        imageUrl = artist.images.firstOrNull()?.url,
                        timeRange = timeRange,
                        rank = topArtistsDto.items.indexOf(artist) + 1,
                        lastFetched = System.currentTimeMillis(),
                        popularity = artist.popularity,
                        genres = allGenres.ifEmpty { "Unknown" }
                    )
                }
                // 3. Save the new data to the database
                artistsDao.clearTopArtists(timeRange)
                artistsDao.insertTopArtists(topArtistsEntities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to refresh top artists"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentlyPlayed(): Result<List<PlayHistoryObject>> {
        return try {
            val response = api.getRecentlyPlayed(limit = 1)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.items)
            } else {
                Result.failure(Exception("Failed to fetch recently played tracks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNewReleases(limit: Int):
            Flow<Result<List<NewReleasesEntity>?>> = networkBoundResource(
        query = {
            newReleasesDao.getNewReleases()
        },
        fetch = {
            api.getNewReleases(limit)
        },
        saveFetchResult = { response ->
            if (response.isSuccessful && response.body() != null) {
                val newReleasesDto = response.body()!!

                // Map the DTOs from the API to your database entities
                val newReleaseEntities = newReleasesDto.albums.items.map { album ->
                    NewReleasesEntity(
                        id = album.id,
                        name = album.name,
                        artistName = album.artists.firstOrNull()?.name ?: "Unknown",
                        imageUrl = album.images.firstOrNull()?.url ?: "",
                        releaseDate = album.releaseDate,
                        totalTracks = album.totalTracks,
                        lastFetched = System.currentTimeMillis()
                    )
                }

                // Save the fresh data to the database in a transaction
                newReleasesDao.clearNewReleases()
                newReleasesDao.insertNewReleases(newReleaseEntities)
            }
        },
        shouldFetch = { cachedReleases ->
            if (cachedReleases.isNullOrEmpty()) {
                true // Always fetch if the cache is empty.
            } else {
                val firstRelease = cachedReleases.first()
                val isStale = System.currentTimeMillis() - firstRelease.lastFetched > CACHE_EXPIRATION_MS_NEW_RELEASES
                isStale
            }
        }
    )
}

private val CACHE_EXPIRATION_MS = TimeUnit.HOURS.toMillis(1)
private val CACHE_EXPIRATION_MS_NEW_RELEASES = TimeUnit.HOURS.toMillis(24)