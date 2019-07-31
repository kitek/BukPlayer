package pl.kitek.buk.data.repository

import io.reactivex.Single
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.model.BookEntity
import pl.kitek.buk.data.model.Page
import pl.kitek.buk.data.service.BookRestServiceFactory

class InMemoryBookRepository(
    private val bookServiceFactory: BookRestServiceFactory,
    private val settingsRepository: SettingsRepository
) : BookRepository {

    override fun getBooks(): Single<Page<Book>> {
        val bookFactory = BookFactory(settingsRepository)

        return bookServiceFactory.create()
            .flatMap { service -> service.getBooks() }
            .flatMap { entities -> bookFactory.create(entities) }
    }

    private class BookFactory(private val settingsRepository: SettingsRepository) {

        fun create(entities: Page<BookEntity>): Single<Page<Book>> {
            return settingsRepository.getServerUrl().map { url -> mapToBook(entities, url) }
        }

        private fun mapToBook(entities: Page<BookEntity>, url: String): Page<Book> {
            val books = entities.items.map { entity ->

                val path = if (entity.path.startsWith("http")) entity.path else "$url${entity.path}"
                val coverPath = if (entity.coverPath.startsWith("http")) entity.coverPath else "$url${entity.coverPath}"

                Book(entity.id, entity.title, entity.author, path, entity.description, coverPath)
            }

            return Page(entities.metadata, books)
        }
    }

}
