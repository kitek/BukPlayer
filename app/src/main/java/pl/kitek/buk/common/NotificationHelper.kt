package pl.kitek.buk.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import pl.kitek.buk.R
import pl.kitek.buk.data.service.player.PlayerService
import pl.kitek.buk.presentation.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "player-notification-channel"

    fun setupChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createPlayerNotification(
        context: Context,
        bookId: String,
        title: String,
        author: String
    ): Notification {
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_stat_audiobook)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(title)
        bigTextStyle.bigText(author)

        builder.setStyle(bigTextStyle)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_stat_audiobook)
        builder.setLargeIcon(largeIconBitmap)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setFullScreenIntent(pendingIntent, true)

        // Add Play button intent in notification.
        val playIntent = PlayerService.createPlayIntent(bookId, context)
        val pendingPlayIntent = PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playAction = NotificationCompat.Action(
            android.R.drawable.ic_media_play,
            context.getString(R.string.play),
            pendingPlayIntent
        )
        builder.addAction(playAction)

        // Add Pause button intent in notification.
        val pauseIntent = Intent(context, PlayerService::class.java)
        pauseIntent.action = PlayerService.ACTION_PAUSE
        val pendingPauseIntent = PendingIntent.getService(context, 0, pauseIntent, 0)
        val pauseAction = NotificationCompat.Action(
            android.R.drawable.ic_media_pause,
            context.getString(R.string.pause),
            pendingPauseIntent
        )
        builder.addAction(pauseAction)

        // Add Pause button intent in notification.
        val stopIntent = Intent(context, PlayerService::class.java)
        stopIntent.action = PlayerService.ACTION_STOP
        val pendingStopIntent = PendingIntent.getService(context, 0, stopIntent, 0)
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_media_ff,
            context.getString(R.string.stop),
            pendingStopIntent
        )
        builder.addAction(stopAction)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0))

        return builder.build()
    }

}
