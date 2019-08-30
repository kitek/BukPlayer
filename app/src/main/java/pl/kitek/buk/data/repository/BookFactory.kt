package pl.kitek.buk.data.repository

import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.Page

class BookFactory(private val settingsRepository: SettingsRepository) {

    fun updateBookPaths(entities: Page<Book>): Single<Page<Book>> {
        return settingsRepository.getServerSettings().map { settings ->
            val books = entities.items.map { book ->
                book.copy(
                    path = createAbsoluteUrl(book.path, settings.url),
                    coverPath = createAbsoluteUrl(book.coverPath, settings.url)
                )
            }

            Page(entities.metadata, books)
        }
    }

    fun updateBookFilePaths(entities: Page<BookFile>): Single<Page<BookFile>> {
        return settingsRepository.getServerSettings().map { settings ->
            val files = entities.items.map { bookFile ->
                bookFile.copy(
                    path = createAbsoluteUrl(bookFile.path, settings.url)
                )
            }

            Page(entities.metadata, files)
        }
    }

    private fun createAbsoluteUrl(path: String, baseUrl: String): String {
        return when {
            path.isEmpty() -> ""
            path.startsWith("http") -> path
            else -> "$baseUrl$path"
        }
    }
}
