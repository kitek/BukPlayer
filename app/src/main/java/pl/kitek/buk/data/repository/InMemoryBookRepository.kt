package pl.kitek.buk.data.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.data.db.BookDao
import pl.kitek.buk.data.model.*
import pl.kitek.buk.data.service.BookRestServiceFactory

class InMemoryBookRepository(
    private val bookServiceFactory: BookRestServiceFactory,
    private val settingsRepository: SettingsRepository,
    private val bookDao: BookDao
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

    override fun getBookDetails(id: String): Single<BookDetails> {
        return Single.zip(
            getBook(id).toSingle().flatMap { book ->
                getBookFiles(book.path).flatMap { files -> Single.just(Pair(book, files)) }
            },
            getProgress(id).toSingle(BookProgress(id, 0L, 0)),
            BiFunction { (book, files): Pair<Book, Page<BookFile>>, progress: BookProgress ->
                BookDetails(
                    book.id,
                    book.title,
                    book.author,
                    book.path,
                    book.description,
                    book.coverPath,
                    files,
                    progress
                )
            }
        )
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

    override fun getProgress(bookId: String): Maybe<BookProgress> {
        return bookDao.getProgress(bookId)
    }

    override fun setProgress(bookId: String, playbackPosition: Long, currentWindowIndex: Int): Completable {
        return bookDao.saveProgress(BookProgress(bookId, playbackPosition, currentWindowIndex))
    }
}
