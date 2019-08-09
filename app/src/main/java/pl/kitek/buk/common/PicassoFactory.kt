package pl.kitek.buk.common

import android.annotation.SuppressLint
import android.content.Context
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient

object PicassoFactory {

    @SuppressLint("StaticFieldLeak")
    lateinit var instance: Picasso
        private set

    fun setup(httpClient: OkHttpClient, context: Context) {
        instance = Picasso.Builder(context)
            .downloader(OkHttp3Downloader(httpClient))
            .build()
    }
}
