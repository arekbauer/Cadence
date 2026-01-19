package com.arekb.cadence.core.data.repository

import com.arekb.cadence.core.data.util.networkBoundResource
import com.arekb.cadence.core.database.dao.NewReleasesDao
import com.arekb.cadence.core.database.dao.TopArtistsDao
import com.arekb.cadence.core.database.dao.TopTracksDao
import com.arekb.cadence.core.database.dao.UserProfileDao
import com.arekb.cadence.core.database.entity.NewReleasesEntity
import com.arekb.cadence.core.database.entity.TopArtistsEntity
import com.arekb.cadence.core.database.entity.TopTracksEntity
import com.arekb.cadence.core.database.entity.UserProfileEntity
import com.arekb.cadence.core.database.mappers.asDomainModel
import com.arekb.cadence.core.model.Album
import com.arekb.cadence.core.model.Artist
import com.arekb.cadence.core.model.Genre
import com.arekb.cadence.core.model.Track
import com.arekb.cadence.core.model.User
import com.arekb.cadence.core.network.api.SpotifyApiService
import com.arekb.cadence.core.network.mappers.asDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService,
    private val dao: UserProfileDao,
    private val tracksDao: TopTracksDao,
    private val artistsDao: TopArtistsDao,
    private val newReleasesDao: NewReleasesDao
) : UserRepository {

    override fun getProfile(): Flow<Result<User?>> = networkBoundResource(
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
        shouldFetch = { true }
    ).map { result ->
        // MAPPER: Result<Entity?> -> Result<User?>
        result.map { entity ->
            entity?.asDomainModel()
        }
    }

    override fun getTopTracks(timeRange: String, limit: Int): Flow<Result<List<Track>?>> {
        val resourceFlow = networkBoundResource(
            query = {
                tracksDao.getTopTracks(timeRange)
            },
            fetch = {
                api.getTopTracks(timeRange, limit)
            },
            saveFetchResult = { response ->
                // LOGIC: Map DTO -> Entity and Save
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    val entities = dto.items.map { track ->
                        TopTracksEntity(
                            id = track.id,
                            trackName = track.name,
                            artistNames = track.artists.joinToString(", ") { it.name },
                            imageUrl = track.album.images.firstOrNull()?.url ?: "",
                            timeRange = timeRange,
                            rank = dto.items.indexOf(track) + 1,
                            lastFetched = System.currentTimeMillis()
                        )
                    }
                    tracksDao.clearTopTracks(timeRange)
                    tracksDao.insertTopTracks(entities)
                }
            },
            shouldFetch = { cached ->
                cached.isNullOrEmpty() || (System.currentTimeMillis() - cached.first().lastFetched > CACHE_EXPIRATION_MS)
            }
        )

        // Map the Result<Entity> to Result<Domain>
        return resourceFlow.map { result ->
            // "result" is of type Result<List<TopTracksEntity>?>
            result.map { entities ->
                // "entities" is List<TopTracksEntity>?
                entities?.map { entity ->
                    // Call the mapper we wrote in DomainMappers.kt
                    entity.asDomainModel()
                }
            }
        }
    }

    override fun getTopArtists(timeRange: String, limit: Int): Flow<Result<List<Artist>?>> {
        val resourceFlow = networkBoundResource(
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
                    TopArtistsEntity(
                        id = artist.id,
                        artistName = artist.name,
                        imageUrl = artist.images.firstOrNull()?.url,
                        timeRange = timeRange,
                        rank = topArtistsDto.items.indexOf(artist) + 1,
                        lastFetched = System.currentTimeMillis(),
                        popularity = artist.popularity,
                        genres = artist.genres.joinToString(", ").ifEmpty { "Unknown" }
                    )
                }
                artistsDao.clearTopArtists(timeRange)
                artistsDao.insertTopArtists(topArtistsEntities)
            }
        },
        shouldFetch = { cachedArtists ->
            if (cachedArtists.isNullOrEmpty()) { true }
            else (System.currentTimeMillis() - cachedArtists.first().lastFetched > CACHE_EXPIRATION_MS)
        }
        )
        return resourceFlow.map { result ->
            result.map { list ->
                list?.map { it.asDomainModel() }
            }
        }
    }

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
                    TopArtistsEntity(
                        id = artist.id,
                        artistName = artist.name,
                        imageUrl = artist.images.firstOrNull()?.url,
                        timeRange = timeRange,
                        rank = topArtistsDto.items.indexOf(artist) + 1,
                        lastFetched = System.currentTimeMillis(),
                        popularity = artist.popularity,
                        genres = artist.genres.joinToString(", ").ifEmpty { "Unknown" }
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

    override suspend fun getRecentlyPlayed(): Result<List<Track>> {
        return try {
            val response = api.getRecentlyPlayed(limit = 1)
            if (response.isSuccessful && response.body() != null) {

                val domainTracks = response.body()!!.items.map { historyItem ->
                    // historyItem.track is a TrackObject, so we use our mapper
                    historyItem.track.asDomainModel()
                }
                Result.success(domainTracks)
            } else {
                Result.failure(Exception("Failed to fetch recently played tracks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNewReleases(limit: Int): Flow<Result<List<Album>?>> {
        val resourceFlow = networkBoundResource(
            query = {
                newReleasesDao.getNewReleases()
            },
            fetch = {
                api.getNewReleases(limit)
            },
            saveFetchResult = { response ->
                if (response.isSuccessful && response.body() != null) {
                    val newReleasesResponse = response.body()!!
                    // Access the list of items via the 'albums' property
                    val albumItems = newReleasesResponse.albums.items

                    // Map the DTOs from the API to your database entities
                    val newReleaseEntities = albumItems.map { album ->
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
                if (cachedReleases.isNullOrEmpty()) true
                else (System.currentTimeMillis() - cachedReleases.first().lastFetched > CACHE_EXPIRATION_MS_NEW_RELEASES)
            }
        )
        return resourceFlow.map { result ->
            result.map { list ->
                list?.map { it.asDomainModel() }
            }
        }
    }

    override fun getTopGenresStream(): Flow<Result<List<Genre>>> {
        // Reuse the existing single source of truth
        return getTopArtists(timeRange = "long_term", limit = 50).map { result ->
            result.map { artists ->
                // This is the logic we moved from the ViewModel
                val genreMap = mutableMapOf<String, MutableList<Artist>>()

                artists?.forEach { artist ->
                    artist.genres.forEach { genreName ->
                        genreMap.getOrPut(genreName) { mutableListOf() }.add(artist)
                    }
                }

                genreMap.map { (name, artistList) ->
                    Genre(name = name, artists = artistList)
                }.sortedByDescending { it.artistCount }
            }
        }
    }

    override fun getUserPopularityScore(timeRange: String): Flow<Result<Int>> {
        return getTopArtists(timeRange, 50).map { result ->
            result.map { artists ->
                if (artists.isNullOrEmpty()) return@map 0

                val maxWeight = artists.size
                val totalWeight = (maxWeight * (maxWeight + 1)) / 2.0

                if (totalWeight == 0.0) return@map 0

                val weightedSum = artists.withIndex().sumOf { (index, artist) ->
                    val weight = maxWeight - index
                    ((artist.popularity ?: 0) * weight).toDouble()
                }

                (weightedSum / totalWeight).toInt()
            }
        }
    }
}

private val CACHE_EXPIRATION_MS = TimeUnit.HOURS.toMillis(1)
private val CACHE_EXPIRATION_MS_NEW_RELEASES = TimeUnit.HOURS.toMillis(24)