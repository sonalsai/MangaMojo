package com.mangamojo.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Retries transient failures (HTTP 429 / 5xx) a small number of times with a
 * fixed backoff. Runs on OkHttp's background dispatcher, so the blocking sleep
 * is safe. Mirrors the lightweight retry policy used by the reference POC.
 */
class RetryInterceptor(
    private val maxRetries: Int = 1,
    private val backoffMs: Long = 800,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response = chain.proceed(chain.request())
        while ((response.code == 429 || response.code in 500..599) && attempt < maxRetries) {
            response.close()
            runCatching { Thread.sleep(backoffMs) }
            attempt++
            response = chain.proceed(chain.request())
        }
        return response
    }
}
