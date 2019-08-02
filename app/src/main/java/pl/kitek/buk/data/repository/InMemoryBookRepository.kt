package pl.kitek.buk.data.repository

import io.reactivex.Maybe
import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.Page
import pl.kitek.buk.data.service.BookRestServiceFactory

class InMemoryBookRepository(
    private val bookServiceFactory: BookRestServiceFactory,
    settingsRepository: SettingsRepository
) : BookRepository {

    private val bookFactory = BookFactory(settingsRepository)

    override fun getBook(id: String): Maybe<Book> {
        return getBooks().flatMapMaybe { page ->
            val book = page.items.firstOrNull { item -> item.id == id }

            Maybe.just(book)
        }
    }

    override fun getBooks(): Single<Page<Book>> {
        return bookServiceFactory.create()
            .flatMap { service -> service.getBooks() }
            .flatMap { entities -> bookFactory.mapToBooks(entities) }
    }

    override fun getBookFiles(path: String): Single<Page<BookFile>> {
        return bookServiceFactory.create()
            .flatMap { service -> service.getBookFiles(path) }
            .flatMap { entities -> bookFactory.mapToBookFiles(entities) }
    }
}
