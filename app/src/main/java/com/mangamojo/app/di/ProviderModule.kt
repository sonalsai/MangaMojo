package com.mangamojo.app.di

import com.mangamojo.app.domain.provider.MangaProvider
import com.mangamojo.app.providers.mangadex.MangaDexProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the active manga source. Phase 1 wires the single
 * [MangaDexProvider]. To add providers in Phase 2, switch this to a
 * multibinding (`@Binds @IntoSet`) and have the repository iterate the set to
 * merge / fall back across sources.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @Singleton
    abstract fun bindMangaProvider(impl: MangaDexProvider): MangaProvider
}
