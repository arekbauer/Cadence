package com.arekb.cadence.data.repository

import com.arekb.cadence.data.local.database.dao.TopTracksDao
import com.arekb.cadence.data.local.database.dao.UserProfileDao
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService,
    private val dao: UserProfileDao,
    private val tracksDao: TopTracksDao
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
                            TopTracksEntity(
                                id = track.id,
                                trackName = track.name,
                                artistNames = track.artists.firstOrNull()?.name ?: "Unknown Artist",
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
}

private val CACHE_EXPIRATION_MS = TimeUnit.HOURS.toMillis(1)