package pl.kitek.buk.data.service.player

interface PlayerController {

    fun play(bookId: String)
    fun pause()
    fun stop()

}
