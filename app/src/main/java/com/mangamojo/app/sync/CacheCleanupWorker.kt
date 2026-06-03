package com.mangamojo.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mangamojo.app.domain.repository.MangaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodically evicts stale cached metadata (older than the cache policy),
 * preserving anything that is favorited or in recent history.
 */
@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val mangaRepository: MangaRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        mangaRepository.evictStaleCache()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
