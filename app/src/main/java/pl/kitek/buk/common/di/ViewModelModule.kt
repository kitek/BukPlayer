package pl.kitek.buk.common.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.kitek.buk.presentation.shelf.ShelfViewModel

val viewModelModule = module {

    viewModel { ShelfViewModel(get()) }
}
