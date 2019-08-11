package pl.kitek.buk.common

import android.annotation.SuppressLint
import android.content.Context
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import pl.kitek.buk.data.model.ServerSettings

object PicassoFactory {

    @SuppressLint("StaticFieldLeak")
    lateinit var instance: Picasso
        private set

    private var settings: ServerSettings? = null

    fun setup(
        httpClient: OkHttpClient,
        context: Context,
        settings: ServerSettings
    ) {
        if (settings == this.settings) return

        instance = Picasso.Builder(context)
            .downloader(OkHttp3Downloader(httpClient))
            .build()

        this.settings = settings
    }
}
