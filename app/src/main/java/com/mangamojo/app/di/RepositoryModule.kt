package com.mangamojo.app.di

import com.mangamojo.app.data.repository.LibraryRepositoryImpl
import com.mangamojo.app.data.repository.MangaRepositoryImpl
import com.mangamojo.app.data.repository.SettingsRepositoryImpl
import com.mangamojo.app.domain.repository.LibraryRepository
import com.mangamojo.app.domain.repository.MangaRepository
import com.mangamojo.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMangaRepository(impl: MangaRepositoryImpl): MangaRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
