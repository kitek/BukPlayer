package pl.kitek.buk.data.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val path: String,
    val description: String,
    val coverPath: String,
    val durationInSeconds: Long
)
