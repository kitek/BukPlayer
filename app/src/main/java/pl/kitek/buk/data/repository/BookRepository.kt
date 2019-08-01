package pl.kitek.buk.data.repository

import io.reactivex.Maybe
import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.Page

interface BookRepository {

    fun getBook(id: String): Maybe<Book>
    fun getBooks(): Single<Page<Book>>

}
