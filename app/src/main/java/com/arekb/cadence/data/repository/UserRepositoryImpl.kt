package com.arekb.cadence.data.repository

import android.util.Log
import com.arekb.cadence.data.local.database.dao.UserProfileDao
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.dto.TopItemsResponse
import com.arekb.cadence.data.remote.dto.UserProfile
import com.arekb.cadence.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SpotifyApiService,
    private val dao: UserProfileDao
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

    override suspend fun getTopTracks(timeRange: String, limit: Int): Result<TopItemsResponse> {
        return try {
            val response = api.getTopTracks(timeRange, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch top tracks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
