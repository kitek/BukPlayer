package pl.kitek.buk.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_progress")
data class BookProgress(
    @PrimaryKey @ColumnInfo(name = "bookId") val bookId: String,
    val playbackPosition: Long,
    val currentWindowIndex: Int
)
