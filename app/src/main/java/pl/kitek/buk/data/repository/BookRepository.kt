package pl.kitek.buk.data.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import pl.kitek.buk.data.model.*

interface BookRepository {

    fun observeBookState(id: String): Observable<BookState>
    fun updateBookState(id: String, playbackState: PlaybackState)
    fun updateBookProgress(
        id: String,
        playbackPosition: Long,
        fileIndex: Int,
        files: List<BookFile>,
        totalDurationInSeconds: Long
    )

    fun observeBooks(): Observable<Page<Book>>
    fun getBookDetails(id: String): Single<BookDetails>
    fun getBook(id: String): Maybe<Book>

    fun getBooks(): Single<Page<Book>>
    fun getBookFiles(path: String): Single<Page<BookFile>>

    fun getProgress(bookId: String): Maybe<BookProgress>
    fun setProgress(bookId: String, playbackPosition: Long, currentWindowIndex: Int): Completable

}
