package pl.kitek.buk.data.model


data class BookEntity(
    val id: String,
    val title: String,
    val author: String,
    val path: String,
    val hash: String,
    val description: String,
    val coverPath: String,
    val filesCount: Int,
    val durationInSeconds: Long
)
