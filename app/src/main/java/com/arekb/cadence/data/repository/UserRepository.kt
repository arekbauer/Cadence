package com.arekb.cadence.data.repository

import com.arekb.cadence.data.remote.dto.UserProfile

interface UserRepository {
    suspend fun getProfile(): Result<UserProfile>
}