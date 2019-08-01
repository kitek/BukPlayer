package pl.kitek.buk.data.repository

import io.reactivex.Single
import pl.kitek.buk.data.model.*

class BookFactory(private val settingsRepository: SettingsRepository) {

    fun mapToBooks(entities: Page<BookEntity>): Single<Page<Book>> {
        return settingsRepository.getServerUrl().map { baseUrl ->
            val books = entities.items.map { entity ->
                val path = createAbsoluteUrl(entity.path, baseUrl)
                val coverPath = createAbsoluteUrl(entity.coverPath, baseUrl)

                Book(entity.id, entity.title, entity.author, path, entity.description, coverPath)
            }

            Page(entities.metadata, books)
        }
    }

    fun mapToBookFiles(entities: Page<BookFileEntity>): Single<Page<BookFile>> {
        return settingsRepository.getServerUrl().map { baseUrl ->
            val files = entities.items.map { entity ->
                BookFile(createAbsoluteUrl(entity.path, baseUrl))
            }

            Page(entities.metadata, files)
        }
    }

    private fun createAbsoluteUrl(path: String, baseUrl: String): String {
        return if (path.startsWith("http")) path else "$baseUrl$path"
    }
}
