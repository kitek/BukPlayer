package pl.kitek.buk.data.model

data class BookState(
    val id: String,
    val title: String,
    val coverPath: String,
    val currentPlaybackState: PlaybackState,
    val currentProgress: CurrentProgress
)
