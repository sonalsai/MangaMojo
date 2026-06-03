package com.mangamojo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mangamojo.app.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mangaId = :mangaId)")
    fun observeIsFavorite(mangaId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mangaId = :mangaId)")
    suspend fun isFavorite(mangaId: String): Boolean

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mangaId = :mangaId")
    suspend fun delete(mangaId: String)

    @Query("DELETE FROM favorites")
    suspend fun clear()

    @Query("SELECT mangaId FROM favorites")
    suspend fun getFavoriteIds(): List<String>
}
