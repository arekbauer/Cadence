package com.arekb.cadence.core.network.di

import com.arekb.cadence.core.network.api.SpotifyApiService
import com.arekb.cadence.core.network.api.SpotifyAuthApiService
import com.arekb.cadence.core.network.api.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Auth API
    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/") // Use BuildConfig.SPOTIFY_AUTH_URL if you set it up
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSpotifyAuthApiService(@Named("AuthRetrofit") retrofit: Retrofit): SpotifyAuthApiService {
        return retrofit.create(SpotifyAuthApiService::class.java)
    }

    // Main API
    @Provides
    @Singleton
    fun provideOkHttpClient(authenticator: TokenRefreshAuthenticator): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    @Named("ApiRetrofit")
    fun provideApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.spotify.com/") // Use BuildConfig.SPOTIFY_BASE_URL if set
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSpotifyApiService(@Named("ApiRetrofit") retrofit: Retrofit): SpotifyApiService {
        return retrofit.create(SpotifyApiService::class.java)
    }
}