package com.mangamojo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mangamojo.app.data.local.entity.CachedChapterEntity
import com.mangamojo.app.data.local.entity.CachedMangaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaCacheDao {

    /* ----- Manga metadata ----- */
    @Upsert
    suspend fun upsertManga(manga: CachedMangaEntity)

    @Query("SELECT * FROM cached_manga WHERE mangaId = :mangaId")
    suspend fun getManga(mangaId: String): CachedMangaEntity?

    @Query("SELECT COUNT(*) FROM cached_manga")
    fun observeMangaCount(): Flow<Int>

    /* ----- Chapter feed ----- */
    @Upsert
    suspend fun upsertChapters(chapters: List<CachedChapterEntity>)

    @Query("SELECT * FROM cached_chapters WHERE mangaId = :mangaId ORDER BY orderIndex ASC")
    suspend fun getChapters(mangaId: String): List<CachedChapterEntity>

    @Query("DELETE FROM cached_chapters WHERE mangaId = :mangaId")
    suspend fun deleteChaptersForManga(mangaId: String)

    /* ----- Maintenance ----- */
    @Query("DELETE FROM cached_manga")
    suspend fun clearManga()

    @Query("DELETE FROM cached_chapters")
    suspend fun clearChapters()

    @Query("DELETE FROM cached_manga WHERE cachedAt < :cutoff AND mangaId NOT IN (:protectedIds)")
    suspend fun evictStaleManga(cutoff: Long, protectedIds: List<String>)

    @Query("DELETE FROM cached_chapters WHERE cachedAt < :cutoff AND mangaId NOT IN (:protectedIds)")
    suspend fun evictStaleChapters(cutoff: Long, protectedIds: List<String>)
}
