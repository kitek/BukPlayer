package pl.kitek.buk.data.model

data class Page<T>(
    val metadata: Metadata,
    val items: List<T>
)
