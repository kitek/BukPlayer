package pl.kitek.buk.common.di

import org.koin.dsl.module
import pl.kitek.buk.common.OkHttpClientFactory
import pl.kitek.buk.data.service.player.BukPlayerController
import pl.kitek.buk.data.service.player.PlayerController

val androidModule = module {

    single<PlayerController> { BukPlayerController(get()) }
    single { OkHttpClientFactory(get()) }

}
