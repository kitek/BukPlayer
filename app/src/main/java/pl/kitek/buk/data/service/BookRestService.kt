package pl.kitek.buk.data.service

import io.reactivex.Single
import pl.kitek.buk.data.model.BookEntity
import pl.kitek.buk.data.model.BookFileEntity
import pl.kitek.buk.data.model.Page
import retrofit2.http.GET
import retrofit2.http.Path

interface BookRestService {

    @GET("index.json")
    fun getBooks(): Single<Page<BookEntity>>

    @GET("{bookPath}/book.json")
    fun getBookFiles(@Path(value = "bookPath", encoded = true) bookPath: String): Single<Page<BookFileEntity>>

}
