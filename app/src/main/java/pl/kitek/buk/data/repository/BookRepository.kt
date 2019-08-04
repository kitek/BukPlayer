package pl.kitek.buk.data.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.BookProgress
import pl.kitek.buk.data.model.Page

interface BookRepository {

    fun getBook(id: String): Maybe<Book>
    fun getBooks(): Single<Page<Book>>
    fun getBookFiles(path: String): Single<Page<BookFile>>
    fun getProgress(bookId: String): Maybe<BookProgress>
    fun setProgress(bookId: String, playbackPosition: Long, currentWindowIndex: Int): Completable

}
