package pl.kitek.buk.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe
import pl.kitek.buk.data.model.BookProgress

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProgress(model: BookProgress): Completable

    @Query("SELECT * FROM book_progress WHERE bookId = :bookId LIMIT 1")
    fun getProgress(bookId: String): Maybe<BookProgress>

}
