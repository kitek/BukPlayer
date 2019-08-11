package pl.kitek.buk.common.di

import org.koin.dsl.module
import pl.kitek.buk.data.db.BookDatabase
import pl.kitek.buk.data.repository.BookRepository
import pl.kitek.buk.data.repository.InMemoryBookRepository
import pl.kitek.buk.data.repository.SettingsRepository
import pl.kitek.buk.data.repository.SharedPrefSettingsRepository

val repositoryModule = module {

    single<SettingsRepository> { SharedPrefSettingsRepository(get()) }
    single<BookRepository> { InMemoryBookRepository(get(), get(), get()) }
    single { BookDatabase.getDatabase(get()).bookDao() }

}
