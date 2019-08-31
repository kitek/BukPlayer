package pl.kitek.buk.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.R
import pl.kitek.buk.data.model.BookDetails
import pl.kitek.buk.data.model.PlaybackState
import pl.kitek.buk.data.service.player.PlayerService
import pl.kitek.buk.presentation.MainActivity
import timber.log.Timber

object NotificationHelper {

    const val NOTIFICATION_ID = 1

    private var lastNotificationBookId = ""
    private var downloadCoverDisposable: Disposable? = null
    private const val CHANNEL_ID = "player-notification-channel"

    fun setupChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun updatePlayerNotification(config: NotificationConfig) {
        if (lastNotificationBookId != config.bookId) return

        val notification = createPlayerNotification(config)
        val manager = config.context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun createPlayerNotification(config: NotificationConfig): Notification {
        val context = config.context
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val largeIconBitmap = config.coverBitmap ?: BitmapFactory.decodeResource(
            context.resources, R.drawable.ic_stat_audiobook
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(config.title)
        bigTextStyle.bigText(config.author)

        builder.setStyle(bigTextStyle)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_stat_audiobook)
        builder.setLargeIcon(largeIconBitmap)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setFullScreenIntent(pendingIntent, true)

        addActions(config.bookId, context, config.playbackState, builder)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0))

        lastNotificationBookId = config.bookId
        showBookCover(config)

        return builder.build()
    }

    private fun addActions(
        bookId: String,
        context: Context,
        playbackState: PlaybackState,
        builder: NotificationCompat.Builder
    ) {
        when (playbackState) {
            is PlaybackState.Playing -> {
                builder.addAction(createPauseAction(context))
                builder.addAction(createStopAction(context))
            }
            is PlaybackState.Paused -> {
                builder.addAction(createPlayAction(bookId, context))
                builder.addAction(createStopAction(context))
            }
        }
    }

    private fun createPlayAction(bookId: String, context: Context): NotificationCompat.Action {
        val playIntent = PlayerService.createPlayIntent(bookId, context)
        val pendingPlayIntent = PendingIntent.getService(
            context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Action(
            android.R.drawable.ic_media_play,
            context.getString(R.string.play),
            pendingPlayIntent
        )
    }

    private fun createPauseAction(context: Context): NotificationCompat.Action {
        val pauseIntent = Intent(context, PlayerService::class.java)
        pauseIntent.action = PlayerService.ACTION_PAUSE
        val pendingPauseIntent = PendingIntent.getService(context, 0, pauseIntent, 0)

        return NotificationCompat.Action(
            android.R.drawable.ic_media_pause,
            context.getString(R.string.pause),
            pendingPauseIntent
        )
    }

    private fun createStopAction(context: Context): NotificationCompat.Action {
        val stopIntent = Intent(context, PlayerService::class.java)
        stopIntent.action = PlayerService.ACTION_STOP
        val pendingStopIntent = PendingIntent.getService(context, 0, stopIntent, 0)

        return NotificationCompat.Action(
            android.R.drawable.ic_media_ff,
            context.getString(R.string.stop),
            pendingStopIntent
        )
    }

    private fun showBookCover(config: NotificationConfig) {
        downloadCoverDisposable?.dispose()

        if (config.coverBitmap != null) return
        if (config.coverPath.isEmpty()) return

        downloadCoverDisposable = Single.fromCallable {
            PicassoFactory.instance.load(config.coverPath).get()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bitmap ->
                updatePlayerNotification(config.copy(coverBitmap = bitmap))
            }, { e ->
                Timber.tag("BukPlayer").d(e)
            })
    }

    data class NotificationConfig(
        val context: Context,
        val bookId: String,
        val title: String,
        val author: String,
        val coverPath: String,
        val playbackState: PlaybackState,
        val coverBitmap: Bitmap? = null
    ) {

        companion object {

            fun of(
                context: Context,
                bookDetails: BookDetails,
                playbackState: PlaybackState
            ) = NotificationConfig(
                context,
                bookDetails.id,
                bookDetails.title,
                bookDetails.author,
                bookDetails.coverPath,
                playbackState
            )
        }

    }
}
