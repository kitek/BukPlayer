package pl.kitek.buk.data.repository

import io.reactivex.Single
import pl.kitek.buk.data.model.*

class BookFactory(private val settingsRepository: SettingsRepository) {

    fun mapToBooks(entities: Page<BookEntity>): Single<Page<Book>> {
        return settingsRepository.getServerSettings().map { settings ->
            val books = entities.items.map { entity ->
                val path = createAbsoluteUrl(entity.path, settings.url)
                val coverPath = createAbsoluteUrl(entity.coverPath, settings.url)

                Book(entity.id, entity.title, entity.author, path, entity.description, coverPath)
            }

            Page(entities.metadata, books)
        }
    }

    fun mapToBookFiles(entities: Page<BookFileEntity>): Single<Page<BookFile>> {
        return settingsRepository.getServerSettings().map { settings ->
            val files = entities.items.map { entity ->
                BookFile(createAbsoluteUrl(entity.path, settings.url))
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
