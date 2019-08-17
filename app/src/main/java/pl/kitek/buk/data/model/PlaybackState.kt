package pl.kitek.buk.data.model

sealed class PlaybackState {

    object Playing : PlaybackState() {
        override fun toString(): String = "Playing"
    }

    object Paused : PlaybackState() {
        override fun toString(): String = "Paused"
    }

    object Stopped : PlaybackState() {
        override fun toString(): String = "Stopped"
    }
}
