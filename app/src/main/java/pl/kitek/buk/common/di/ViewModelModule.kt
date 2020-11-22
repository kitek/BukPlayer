package pl.kitek.buk.common.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.kitek.buk.presentation.player.PlayerViewModel
import pl.kitek.buk.presentation.settings.SettingsViewModel
import pl.kitek.buk.presentation.shelf.ShelfViewModel

val viewModelModule = module {

    viewModel { ShelfViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { (bookId: String) -> PlayerViewModel(bookId, get(), get()) }

}
