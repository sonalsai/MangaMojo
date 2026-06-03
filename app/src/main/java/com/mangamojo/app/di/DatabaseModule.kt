package com.mangamojo.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mangamojo.app.data.local.MangaMojoDatabase
import com.mangamojo.app.data.local.dao.BookmarkDao
import com.mangamojo.app.data.local.dao.FavoriteDao
import com.mangamojo.app.data.local.dao.HistoryDao
import com.mangamojo.app.data.local.dao.MangaCacheDao
import com.mangamojo.app.data.local.dao.ReadingProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mangamojo_settings",
)

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MangaMojoDatabase =
        Room.databaseBuilder(context, MangaMojoDatabase::class.java, MangaMojoDatabase.NAME)
            // Phase 1 has no migrations yet; recreate on schema change.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideMangaCacheDao(db: MangaMojoDatabase): MangaCacheDao = db.mangaCacheDao()
    @Provides fun provideFavoriteDao(db: MangaMojoDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideHistoryDao(db: MangaMojoDatabase): HistoryDao = db.historyDao()
    @Provides fun provideReadingProgressDao(db: MangaMojoDatabase): ReadingProgressDao =
        db.readingProgressDao()
    @Provides fun provideBookmarkDao(db: MangaMojoDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore
}
