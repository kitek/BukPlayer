package pl.kitek.buk.presentation.player

sealed class PlayerState {

    object Playing : PlayerState()
    object Paused : PlayerState()
    object Stopped : PlayerState()

}
