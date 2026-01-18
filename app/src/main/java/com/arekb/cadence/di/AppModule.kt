package com.arekb.cadence.di

import com.arekb.cadence.data.repository.AuthRepository
import com.arekb.cadence.data.repository.AuthRepositoryImpl
import com.arekb.cadence.data.repository.SearchRepository
import com.arekb.cadence.data.repository.SearchRepositoryImpl
import com.arekb.cadence.data.repository.UserRepository
import com.arekb.cadence.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository
}