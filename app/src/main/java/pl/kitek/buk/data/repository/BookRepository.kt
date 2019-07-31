package pl.kitek.buk.data.repository

import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.Page

interface BookRepository {

    fun getBooks(): Single<Page<Book>>

}
