package pl.kitek.buk.data.service

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Single
import okhttp3.OkHttpClient
import pl.kitek.buk.common.OkHttpClientFactory
import pl.kitek.buk.data.model.ServerSettings
import pl.kitek.buk.data.repository.SettingsRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class BookRestServiceFactory(
    private val settingsRepository: SettingsRepository,
    private val httpClientFactory: OkHttpClientFactory,
    private val parser: Moshi
) {

    private var service: BookRestService? = null
    private var serverSettings: ServerSettings? = null

    fun create(): Single<BookRestService> {
        return settingsRepository.getServerSettings().flatMap { serverSettings ->
            if (serverSettings.url.isEmpty()) {
                Single.error<BookRestService>(InvalidServerUrlException())
            } else {
                httpClientFactory.create(serverSettings).flatMap { httpClient ->
                    Single.just(createService(serverSettings, httpClient))
                }
            }
        }
    }

    fun create(settings: ServerSettings): Single<BookRestService> {
        return if (settings.url.isEmpty()) {
            Single.error(InvalidServerUrlException())
        } else {
            httpClientFactory.create(settings).flatMap { httpClient ->
                Single.just(createService(settings, httpClient))
            }
        }
    }

    private fun createService(settings: ServerSettings, httpClient: OkHttpClient): BookRestService {
        if (this.serverSettings == settings && null != service) return service as BookRestService

        val retrofit = Retrofit.Builder()
            .baseUrl(settings.url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(parser))
            .client(httpClient)
            .build()

        val service = retrofit.create(BookRestService::class.java)

        this.service = service
        this.serverSettings = settings

        return service
    }

    class InvalidServerUrlException : Throwable("Invalid Server URL")

}
