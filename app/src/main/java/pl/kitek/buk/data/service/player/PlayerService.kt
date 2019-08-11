package pl.kitek.buk.data.service.player

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import pl.kitek.buk.R
import pl.kitek.buk.common.NotificationHelper
import pl.kitek.buk.common.OkHttpClientFactory
import pl.kitek.buk.common.addTo
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.BookProgress
import pl.kitek.buk.data.model.Page
import pl.kitek.buk.data.repository.BookRepository
import pl.kitek.buk.data.repository.SettingsRepository
import timber.log.Timber

class PlayerService : Service() {

    private val bookRepository: BookRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val httpClientFactory: OkHttpClientFactory by inject()

    private val disposable = CompositeDisposable()

    private var player: SimpleExoPlayer? = null
    private var dataSourceFactory: DefaultDataSourceFactory? = null
    private var bookFiles: Page<BookFile>? = null
    private var currentBookId: String = ""
    private var playbackPosition: Long = 0
    private var windowIndex: Int = 0

    override fun onCreate() {
        super.onCreate()
        setupPlayer()
    }

    private fun setupPlayer() {
        val userAgent = Util.getUserAgent(this, resources.getString(R.string.app_name))
        player = ExoPlayerFactory.newSimpleInstance(this, AudioOnlyRenderersFactory(this), DefaultTrackSelector())

        Timber.tag("kitek").d("httpClientFactory: ${httpClientFactory.client} ")


        dataSourceFactory = DefaultDataSourceFactory(
            this,
            null,
            OkHttpDataSourceFactory(httpClientFactory.client, userAgent)
        )
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

        Single.zip(
            getProgress(bookId), getBookFiles(bookId),
            zipFunc()
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (progress, bookFiles) ->
                this.bookFiles = bookFiles
                this.currentBookId = bookId
                this.playbackPosition = progress.playbackPosition
                this.windowIndex = progress.currentWindowIndex
                startPlaying(bookFiles)
            }, { e: Throwable ->
                Timber.tag(TAG).e("PlayerService error: $e ")
            }).addTo(disposable)
    }

    private fun zipFunc(): BiFunction<BookProgress, Page<BookFile>, Pair<BookProgress, Page<BookFile>>> {
        return BiFunction { progress: BookProgress, book ->
            Pair(
                progress,
                book
            )
        }
    }

    private fun getBookFiles(bookId: String): Single<Page<BookFile>> {
        return bookRepository.getBook(bookId)
            .toSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { book ->
                bookRepository.getBookFiles(book.path)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
    }

    private fun getProgress(bookId: String): Single<BookProgress> {
        return bookRepository.getProgress(bookId)
            .toSingle(BookProgress(bookId, 0L, 0))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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

        player?.seekTo(windowIndex, playbackPosition)
    }

    private fun pausePlaying() {
        player?.playWhenReady = false

        this.playbackPosition = player?.currentPosition ?: 0L
        this.windowIndex = player?.currentWindowIndex ?: 0

        saveProgress()
    }

    private fun stopPlaying() {
        try {
            stopForeground(true)
        } catch (e: Exception) {
            Timber.tag("kitek").e("stopPlaying: $e ")
        }
        stopSelf()
    }

    override fun onDestroy() {
        saveProgress()

        player?.stop()
        player?.release()
        player = null

        disposable.dispose()
    }

    @SuppressLint("CheckResult")
    private fun saveProgress() {
        val playbackPosition = player?.currentPosition ?: 0L
        val currentWindow = player?.currentWindowIndex ?: 0

        bookRepository.setProgress(currentBookId, playbackPosition, currentWindow)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(TAG).d("Progress saved at $playbackPosition/$currentWindow")
            }, {
                Timber.tag(TAG).e("Progress saving error: $it")
            })
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"

        private const val BOOK_ID = "bookId"
        private const val TAG = "PlayerService"

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
            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_PAUSE

            startService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PlayerService::class.java)
            intent.action = ACTION_STOP

            startService(context, intent)
//            context.startService(intent)
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
