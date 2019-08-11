package pl.kitek.buk.data.service.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import org.koin.android.ext.android.inject

class PlayerService : Service() {

    private val bukPlayer: BukPlayer by inject()

    override fun onCreate() {
        super.onCreate()
        bukPlayer.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: "") {
            ACTION_PLAY -> {
                val bookId = intent?.getStringExtra(BOOK_ID) ?: ""
                bukPlayer.startPlaying(bookId, this)
            }
            ACTION_PAUSE -> bukPlayer.pausePlaying()
            ACTION_STOP -> bukPlayer.stopPlaying(this)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        bukPlayer.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"

        private const val BOOK_ID = "bookId"

        fun createPlayIntent(bookId: String, context: Context): Intent {
            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_PLAY
            intent.putExtra(BOOK_ID, bookId)

            return intent
        }

        fun play(bookId: String, context: Context) {
            val intent = createPlayIntent(bookId, context)

            startService(context, intent)
        }

        fun pause(context: Context) {
            if (!BukPlayer.isRunning) return

            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_PAUSE
            startService(context, intent)
        }

        fun stop(context: Context) {
            if (!BukPlayer.isRunning) return

            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_STOP
            startService(context, intent)
        }

        private fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

    }
}
