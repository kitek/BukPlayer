package pl.kitek.buk.data.service.player

import android.content.Context

class BukPlayerController(
    private val applicationContext: Context
) : PlayerController {

    override fun play(bookId: String) {
        PlayerService.play(bookId, applicationContext)
    }

    override fun pause() {
        PlayerService.pause(applicationContext)
    }

    override fun stop() {
        PlayerService.stop(applicationContext)
    }
}
