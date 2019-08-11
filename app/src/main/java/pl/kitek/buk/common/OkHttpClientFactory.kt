package pl.kitek.buk.common

import android.content.Context
import android.os.StatFs
import io.reactivex.Single
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import pl.kitek.buk.data.model.ServerSettings
import java.io.File
import kotlin.math.max
import kotlin.math.min

class OkHttpClientFactory(
    private val applicationContext: Context
) {

    var client: OkHttpClient? = null
    private var serverSettings: ServerSettings? = null

    fun create(serverSettings: ServerSettings): Single<OkHttpClient> {
        return Single.fromCallable { createClient(serverSettings) }
    }

    private fun createClient(settings: ServerSettings): OkHttpClient {
        return if (settings == serverSettings && null != client) {
            PicassoFactory.setup(client!!, applicationContext, settings)

            client!!
        } else {

            val cacheDir = defaultCacheDir(applicationContext)
            val cacheSize = calculateDiskCacheSize(cacheDir)
            val builder = OkHttpClient.Builder().cache(Cache(cacheDir, cacheSize))

            if (settings.isBasicAuthEnabled) builder.addInterceptor(BasicAuthInterceptor(settings))
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })

            val client = builder.build()
            PicassoFactory.setup(client, applicationContext, settings)

            this.client = client
            client
        }
    }

    private class BasicAuthInterceptor(
        private val settings: ServerSettings
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val builder = originalRequest.newBuilder()
                .header(
                    "Authorization",
                    Credentials.basic(settings.basicAuthUsername, settings.basicAuthPassword)
                )

            val newRequest = builder.build()
            return chain.proceed(newRequest)
        }

    }

    private fun defaultCacheDir(context: Context): File {
        val cache = File(context.applicationContext.cacheDir, "buk-cache")
        if (!cache.exists()) {
            cache.mkdirs()
        }
        return cache
    }

    private fun calculateDiskCacheSize(dir: File): Long {
        var size = MIN_DISK_CACHE_SIZE.toLong()

        try {
            val statFs = StatFs(dir.absolutePath)
            val available = statFs.blockCount.toLong() * statFs.blockSize
            // Target 2% of the total space.
            size = available / 50
        } catch (ignored: IllegalArgumentException) {
        }

        // Bound inside min/max size for disk cache.
        return max(min(size, MAX_DISK_CACHE_SIZE.toLong()), MIN_DISK_CACHE_SIZE.toLong())
    }

    companion object {
        private const val MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024 // 5MB
        private const val MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    }
}
