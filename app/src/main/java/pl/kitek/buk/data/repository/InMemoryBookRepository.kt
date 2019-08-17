package pl.kitek.buk.data.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import pl.kitek.buk.data.db.BookDao
import pl.kitek.buk.data.model.*
import pl.kitek.buk.data.service.BookRestServiceFactory

class InMemoryBookRepository(
    private val bookServiceFactory: BookRestServiceFactory,
    private val settingsRepository: SettingsRepository,
    private val bookDao: BookDao
) : BookRepository {

    private val bookFactory = BookFactory(settingsRepository)
    private val defaultPlaybackState = Pair("", PlaybackState.Stopped)
    private val defaultProgress = Pair("", CurrentProgress())

    private val playbackSubject =
        BehaviorSubject.createDefault<Pair<String, PlaybackState>>(defaultPlaybackState)
    private val progressSubject =
        BehaviorSubject.createDefault<Pair<String, CurrentProgress>>(defaultProgress)

    override fun observeBookState(id: String): Observable<BookState> {
        return Observable.combineLatest(
            getBook(id).toObservable(),
            observeBookPlaybackState(id),
            observeBookProgress(id),

            Function3 { book: Book, playbackState: PlaybackState, progress: CurrentProgress ->
                BookState(
                    book.id,
                    book.title,
                    book.coverPath,
                    playbackState,
                    progress
                )
            }
        )
    }

    private fun observeBookPlaybackState(id: String): Observable<PlaybackState> {
        return playbackSubject.map { (bookId, playbackState) ->
            if (id == bookId) playbackState else PlaybackState.Stopped
        }
    }

    private fun observeBookProgress(id: String): Observable<CurrentProgress> {
        return progressSubject.flatMap { (bookId, progress) ->
            if (id == bookId) Observable.just(progress)
            else getBookCurrentProgress(id)
        }
    }

    override fun updateBookProgress(
        id: String,
        playbackPosition: Long,
        fileIndex: Int,
        files: List<BookFile>,
        totalDurationInSeconds: Long
    ) {
        if (files.isEmpty()) return
        if (totalDurationInSeconds <= 0L) return

        val progress = CurrentProgress.of(
            playbackPosition, fileIndex, files, totalDurationInSeconds
        )
        progressSubject.onNext(Pair(id, progress))
    }

    private fun getBookCurrentProgress(id: String): Observable<CurrentProgress> {
        return getBookDetails(id).toObservable().map { bookDetails ->
            val playbackPosition = bookDetails.progress.playbackPosition
            val fileIndex = bookDetails.progress.currentWindowIndex
            val files = bookDetails.files.items
            val totalDurationInSeconds = bookDetails.durationInSeconds

            CurrentProgress.of(playbackPosition, fileIndex, files, totalDurationInSeconds)
        }
    }

    override fun updateBookState(id: String, playbackState: PlaybackState) {
        playbackSubject.onNext(Pair(id, playbackState))
    }

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
                bookFactory.updateBookPaths(entities).toObservable()
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
                    book.durationInSeconds,
                    progress
                )
            }
        )
    }

    override fun getBooks(): Single<Page<Book>> {
        return bookServiceFactory.create()
            .flatMap { service -> service.getBooks() }
            .flatMap { entities -> bookFactory.updateBookPaths(entities) }
    }

    override fun getBookFiles(path: String): Single<Page<BookFile>> {
        return bookServiceFactory.create()
            .flatMap { service -> service.getBookFiles(path) }
            .flatMap { entities -> bookFactory.updateBookFilePaths(entities) }
    }

    override fun getProgress(bookId: String): Maybe<BookProgress> {
        return bookDao.getProgress(bookId)
    }

    override fun setProgress(
        bookId: String,
        playbackPosition: Long,
        currentWindowIndex: Int
    ): Completable {
        return bookDao.saveProgress(BookProgress(bookId, playbackPosition, currentWindowIndex))
    }
}
