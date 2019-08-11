package pl.kitek.buk.data.service.player

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.net.Uri
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.R
import pl.kitek.buk.common.NotificationHelper
import pl.kitek.buk.common.OkHttpClientFactory
import pl.kitek.buk.common.addTo
import pl.kitek.buk.data.model.BookDetails
import pl.kitek.buk.data.model.BookFile
import pl.kitek.buk.data.model.Page
import pl.kitek.buk.data.repository.BookRepository
import timber.log.Timber

class BukPlayer(
    private val context: Context,
    private val bookRepository: BookRepository,
    private val httpClientFactory: OkHttpClientFactory
) {

    private var player: SimpleExoPlayer? = null
    private var dataSourceFactory: DefaultDataSourceFactory? = null
    private var bookFiles: Page<BookFile>? = null
    private var currentBookId: String = ""
    private var playbackPosition: Long = 0
    private var windowIndex: Int = 0
    private val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
    private lateinit var disposable: CompositeDisposable

    fun onCreate() {
        isRunning = true
        disposable = CompositeDisposable()
        setupPlayer()
    }

    fun startPlaying(bookId: String, service: Service) {
        if (bookId.isEmpty()) return

        if (bookId == currentBookId) {
            player?.playWhenReady = true
            return
        }

        loadBook(bookId, service)
    }

    private fun loadBook(bookId: String, service: Service) {
        bookRepository.getBookDetails(bookId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bookDetails: BookDetails ->
                bookFiles = bookDetails.files
                currentBookId = bookDetails.id
                playbackPosition = bookDetails.progress.playbackPosition
                windowIndex = bookDetails.progress.currentWindowIndex

                showNotification(bookId, bookDetails.author, bookDetails.title, service)
                startPlayback(bookDetails.files, windowIndex, playbackPosition)

            }, { e: Throwable ->
                Timber.tag("BukPlayer").e("PlayerService error: $e ")
            }).addTo(disposable)
    }

    private fun startPlayback(
        files: Page<BookFile>,
        windowIndex: Int,
        playbackPosition: Long
    ) {
        val sources: Array<MediaSource> = files.items.map { file ->
            Uri.parse(file.path)
        }.map { uri ->
            ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(uri)
        }.toTypedArray()

        val mediaSource = ConcatenatingMediaSource(*sources)
        player?.prepare(mediaSource)
        player?.playWhenReady = true
        player?.seekTo(windowIndex, playbackPosition)
    }

    private fun showNotification(bookId: String, author: String, title: String, service: Service) {
        val notification = NotificationHelper.createPlayerNotification(context, bookId, title, author)
        service.startForeground(1, notification)
    }

    fun pausePlaying() {
        player?.playWhenReady = false

        playbackPosition = player?.currentPosition ?: 0L
        windowIndex = player?.currentWindowIndex ?: 0
        saveProgress()
    }

    fun stopPlaying(service: Service) {
        service.stopForeground(true)
        service.stopSelf()
    }

    fun onDestroy() {
        isRunning = false
        saveProgress()
        releasePlayer()
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
                Timber.tag("BukPlayer").d("Progress saved at $playbackPosition/$currentWindow")
            }, {
                Timber.tag("BukPlayer").e("Progress saving error: $it")
            })
    }

    private fun releasePlayer() {
        player?.stop()
        player?.release()
        player = null
        dataSourceFactory = null
        currentBookId = ""
        bookFiles = null
    }

    private fun setupPlayer() {
        val userAgent = Util.getUserAgent(context, context.resources.getString(R.string.app_name))
        player = ExoPlayerFactory.newSimpleInstance(
            context, AudioOnlyRenderersFactory(context), DefaultTrackSelector()
        )
        dataSourceFactory = DefaultDataSourceFactory(
            context, null, OkHttpDataSourceFactory(httpClientFactory.client, userAgent)
        )
    }

    companion object {
        var isRunning: Boolean = false
            private set
    }

}
