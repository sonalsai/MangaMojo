package com.mangamojo.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Registers the app's recurring background jobs. Called once on app startup;
 * [ExistingPeriodicWorkPolicy.KEEP] makes repeated calls idempotent.
 */
object SyncScheduler {

    private const val CACHE_CLEANUP_WORK = "mangamojo_cache_cleanup"
    private const val LIBRARY_REFRESH_WORK = "mangamojo_library_refresh"

    fun schedulePeriodicWork(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val cacheCleanup = PeriodicWorkRequestBuilder<CacheCleanupWorker>(1, TimeUnit.DAYS).build()
        workManager.enqueueUniquePeriodicWork(
            CACHE_CLEANUP_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            cacheCleanup,
        )

        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val libraryRefresh = PeriodicWorkRequestBuilder<LibraryRefreshWorker>(12, TimeUnit.HOURS)
            .setConstraints(networkConstraint)
            .build()
        workManager.enqueueUniquePeriodicWork(
            LIBRARY_REFRESH_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            libraryRefresh,
        )
    }
}
