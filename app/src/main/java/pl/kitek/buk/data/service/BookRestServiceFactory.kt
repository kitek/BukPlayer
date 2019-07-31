package pl.kitek.buk.data.service

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Single
import okhttp3.OkHttpClient
import pl.kitek.buk.data.repository.SettingsRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BookRestServiceFactory(
    private val settingsRepository: SettingsRepository,
    private val httpClient: OkHttpClient,
    private val parser: Moshi
) {

    private var service: BookRestService? = null
    private var serverUrl: String = ""

    fun create(): Single<BookRestService> {
        return settingsRepository.getServerUrl()
            .flatMap { url ->
                if (url.isEmpty()) {
                    Single.error<BookRestService>(InvalidServerUrlException())
                } else {
                    Single.just(createService(url))
                }
            }
    }

    private fun createService(serverUrl: String): BookRestService {
        if (this.serverUrl == serverUrl && null != service) return service as BookRestService

        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(parser))
            .client(httpClient)
            .build()

        val service = retrofit.create(BookRestService::class.java)

        this.service = service
        this.serverUrl = serverUrl

        return service
    }

    class InvalidServerUrlException : Throwable("Invalid Server URL")

}
