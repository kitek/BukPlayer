package pl.kitek.buk.data.repository

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.Page

interface BookRepository {

    fun observeBooks(): Observable<Page<Book>>
    fun getBook(id: String): Maybe<Book>
    fun getBooks(): Single<Page<Book>>
    fun getBookFiles(path: String): Single<Page<BookFile>>

}
