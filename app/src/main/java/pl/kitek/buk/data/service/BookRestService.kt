package pl.kitek.buk.data.service

import io.reactivex.Single
import pl.kitek.buk.data.model.BookEntity
import pl.kitek.buk.data.model.Page
import retrofit2.http.GET

interface BookRestService {

    @GET("index.json")
    fun getBooks(): Single<Page<BookEntity>>


}
