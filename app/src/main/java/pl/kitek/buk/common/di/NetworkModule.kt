package pl.kitek.buk.common.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import org.koin.dsl.module
import pl.kitek.buk.data.service.BookRestServiceFactory
import java.util.*

val networkModule = module {

    single<Moshi> {
        Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
    }

    single { BookRestServiceFactory(get(), get(), get()) }

}
