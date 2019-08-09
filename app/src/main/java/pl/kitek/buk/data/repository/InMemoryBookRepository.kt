package pl.kitek.buk.data.repository

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.Page
import pl.kitek.buk.data.service.BookRestServiceFactory

class InMemoryBookRepository(
    private val bookServiceFactory: BookRestServiceFactory,
    private val settingsRepository: SettingsRepository
) : BookRepository {

    private val bookFactory = BookFactory(settingsRepository)

    override fun observeBooks(): Observable<Page<Book>> {
        return settingsRepository.observeServerSettings()
            .flatMap { settings ->
                bookServiceFactory.create(settings)
                    .toObservable()
                    .onErrorResumeNext { _: Throwable -> Observable.empty() }
            }
            .flatMap { service ->
                service.getBooks()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .onErrorResumeNext { _: Throwable -> Observable.empty() }
            }
            .flatMap { entities ->
                bookFactory.mapToBooks(entities).toObservable()
                    .onErrorResumeNext { _: Throwable -> Observable.empty() }
            }
    }

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
