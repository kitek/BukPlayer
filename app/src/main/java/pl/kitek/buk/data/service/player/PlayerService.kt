package pl.kitek.buk.data.service.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import pl.kitek.buk.R
import pl.kitek.buk.common.NotificationHelper
import pl.kitek.buk.common.addTo
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.Page
import pl.kitek.buk.data.repository.BookRepository
import timber.log.Timber

class PlayerService : Service() {

    private val bookRepository: BookRepository by inject()
    private val disposable = CompositeDisposable()

    private var player: SimpleExoPlayer? = null
    private var dataSourceFactory: DefaultDataSourceFactory? = null
    private var bookFiles: Page<BookFile>? = null
    private var currentBookId: String = ""

    override fun onCreate() {
        super.onCreate()
        setupPlayer()
    }

    private fun setupPlayer() {
        val userAgent = Util.getUserAgent(this, resources.getString(R.string.app_name))
        player = ExoPlayerFactory.newSimpleInstance(this, AudioOnlyRenderersFactory(this), DefaultTrackSelector())
        dataSourceFactory = DefaultDataSourceFactory(this, userAgent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: "") {
            ACTION_PLAY -> {
                val bookId = intent?.getStringExtra(BOOK_ID) ?: ""
                startPlaying(bookId)
            }
            ACTION_PAUSE -> pausePlaying()
            ACTION_STOP -> stopPlaying()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startPlaying(bookId: String) {
        if (bookId.isEmpty()) return

        val notification =
            NotificationHelper.createPlayerNotification(this, bookId, "Lorem ipsum", "John Doe") // TODO Set real data
        startForeground(1, notification)

        if (bookId == currentBookId) {
            player?.playWhenReady = true
            return
        }

        bookRepository.getBook(bookId)
            .flatMap { book -> bookRepository.getBookFiles(book.path).toMaybe() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bookFiles ->
                this.bookFiles = bookFiles
                this.currentBookId = bookId
                startPlaying(bookFiles)
            }, {
                Timber.tag("PlayerService").e("error: $it ")
            }).addTo(disposable)
    }

    private fun startPlaying(bookFiles: Page<BookFile>) {
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        val sources: Array<MediaSource> = bookFiles.items.map { file ->
            Uri.parse(file.path)
        }.map { uri ->
            ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(uri)
        }.toTypedArray()

        val mediaSource = ConcatenatingMediaSource(*sources)
        player?.prepare(mediaSource)
        player?.playWhenReady = true
    }

    private fun pausePlaying() {
        player?.playWhenReady = false
    }

    private fun stopPlaying() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        player?.stop()
        player?.release()
        player = null
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
            context.startService(intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_PAUSE

            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_STOP

            context.startService(intent)
        }
    }
}
