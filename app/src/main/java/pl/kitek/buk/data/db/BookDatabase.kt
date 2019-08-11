package pl.kitek.buk.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.kitek.buk.data.model.BookProgress

@Database(entities = [BookProgress::class], version = 1)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {

        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "book_db"
                ).build()

                INSTANCE = db

                return db
            }
        }
    }
}
