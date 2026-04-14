package com.imladris.core.di

import com.imladris.core.data.repository.LibraryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun libraryRepository(): LibraryRepository
}
