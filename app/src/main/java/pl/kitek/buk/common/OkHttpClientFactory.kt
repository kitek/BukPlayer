package pl.kitek.buk.common

import android.content.Context
import io.reactivex.Single
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import pl.kitek.buk.data.model.ServerSettings
import timber.log.Timber

class OkHttpClientFactory(
    private val applicationContext: Context
) {

    var client: OkHttpClient? = null
    private var serverSettings: ServerSettings? = null

    fun create(serverSettings: ServerSettings): Single<OkHttpClient> {
        return Single.fromCallable { createClient(serverSettings) }
    }

    private fun createClient(settings: ServerSettings): OkHttpClient {
        Timber.tag("kitek").d("createClient ")

        return if (settings == serverSettings && null != client) {
            PicassoFactory.setup(client!!, applicationContext)
            client!!
        } else {

            val builder = OkHttpClient.Builder()
            if (settings.isBasicAuthEnabled) builder.addInterceptor(BasicAuthInterceptor(settings))
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })

            val client = builder.build()
            PicassoFactory.setup(client, applicationContext)

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
}
