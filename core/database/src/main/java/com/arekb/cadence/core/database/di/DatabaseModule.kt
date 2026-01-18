package com.arekb.cadence.core.database.di

import android.content.Context
import androidx.room.Room
import com.arekb.cadence.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "cadence-db"
            ).fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideUserProfileDao(database: AppDatabase) = database.userProfileDao()

    @Provides
    fun provideTopTracksDao(database: AppDatabase) = database.topTracksDao()

    @Provides
    fun provideTopArtistsDao(database: AppDatabase) = database.topArtistsDao()

    @Provides
    fun provideNewReleasesDao(database: AppDatabase) = database.newReleasesDao()

}