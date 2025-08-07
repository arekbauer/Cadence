package com.arekb.cadence.di

import com.arekb.cadence.data.remote.api.AuthInterceptor
import com.arekb.cadence.data.remote.api.SpotifyApiService
import com.arekb.cadence.data.remote.api.SpotifyAuthApiService
import com.arekb.cadence.data.repository.AuthRepository
import com.arekb.cadence.data.repository.AuthRepositoryImpl
import com.arekb.cadence.data.repository.UserRepository
import com.arekb.cadence.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
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

    companion object {

        // Auth API
        @Provides
        @Singleton
        @Named("AuthRetrofit")
        fun provideAuthRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://accounts.spotify.com/")
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
        fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()
        }

        @Provides
        @Singleton
        @Named("ApiRetrofit")
        fun provideApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideSpotifyApiService(@Named("ApiRetrofit") retrofit: Retrofit): SpotifyApiService {
            return retrofit.create(SpotifyApiService::class.java)
        }
    }
}