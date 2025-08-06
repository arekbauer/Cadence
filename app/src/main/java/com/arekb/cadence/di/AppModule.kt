package com.arekb.cadence.di

import com.arekb.cadence.data.remote.api.SpotifyAuthApiService
import com.arekb.cadence.data.repository.AuthRepository
import com.arekb.cadence.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    /**
     * This function binds the AuthRepository interface to its implementation.
     * Hilt now knows that whenever a component needs an AuthRepository,
     * it should provide an instance of AuthRepositoryImpl.
     *
     * @Binds is more efficient than @Provides for this use case.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    // We use a companion object for @Provides functions in an abstract module
    companion object {

        /**
         * This function provides a singleton instance of the SpotifyAuthApiService.
         * Hilt will use this to create the Retrofit service needed by AuthRepositoryImpl.
         */
        @Provides
        @Singleton
        fun provideSpotifyAuthApiService(): SpotifyAuthApiService {
            return Retrofit.Builder()
                // The base URL for the token exchange endpoint
                .baseUrl("https://accounts.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyAuthApiService::class.java)
        }
    }
}