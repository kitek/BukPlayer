package pl.kitek.buk.data.service.player

import android.content.Context

class BukPlayerController(
    private val context: Context
) : PlayerController {

    override fun play(bookId: String) {
        PlayerService.play(bookId, context)
    }

    override fun pause() {
        PlayerService.pause(context)
    }

    override fun stop() {
        PlayerService.stop(context)
    }
}
