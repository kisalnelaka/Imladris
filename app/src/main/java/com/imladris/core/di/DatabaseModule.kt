package com.imladris.core.di

import android.content.Context
import androidx.room.Room
import com.imladris.core.data.local.ImladrisDatabase
import com.imladris.core.data.local.LibraryDao
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
    fun provideDatabase(@ApplicationContext context: Context): ImladrisDatabase {
        return Room.databaseBuilder(
            context,
            ImladrisDatabase::class.java,
            "imladris_db"
        ).build()
    }

    @Provides
    fun provideLibraryDao(database: ImladrisDatabase): LibraryDao {
        return database.libraryDao()
    }
}
