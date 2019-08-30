package pl.kitek.buk.data.model

data class BookDetails(
    val id: String,
    val title: String,
    val author: String,
    val path: String,
    val description: String,
    val coverPath: String,
    val files: Page<BookFile>,
    val durationInSeconds: Long,
    val progress: BookProgress
)
