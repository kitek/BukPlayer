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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.R
import pl.kitek.buk.common.NotificationHelper
import pl.kitek.buk.common.OkHttpClientFactory
import pl.kitek.buk.common.addTo
import pl.kitek.buk.data.model.BookDetails
import pl.kitek.buk.data.model.PlaybackState
import pl.kitek.buk.data.repository.BookRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BukPlayer(
    private val context: Context,
    private val bookRepository: BookRepository,
    private val httpClientFactory: OkHttpClientFactory
) {

    private var player: SimpleExoPlayer? = null
    private var dataSourceFactory: DefaultDataSourceFactory? = null
    private val extractorsFactory =
        DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)

    private var playbackProgress = Observable.interval(1L, TimeUnit.SECONDS)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap {
            val currentPosition = player?.currentPosition ?: 0L
            val currentWindow = player?.currentWindowIndex ?: 0

            Observable.just(Pair(currentPosition, currentWindow))
        }

    private var currentBookId: String = ""
    private var playbackDisposable = CompositeDisposable()
    private var bookDetails: BookDetails? = null
    private lateinit var disposable: CompositeDisposable

    fun onCreate() {
        isRunning = true
        disposable = CompositeDisposable()
        setupPlayer()
    }

    fun startPlaying(bookId: String, service: Service) {
        if (bookId.isEmpty()) return

        if (currentBookId.isNotEmpty() && currentBookId != bookId) {
            stopPlayback(currentBookId)
        }
        if (currentBookId == bookId) {
            resumePlayback(bookId)
        } else {
            startPlayback(bookId, service)
        }
        currentBookId = bookId
    }

    private fun resumePlayback(bookId: String) {
        bookRepository.updateBookState(bookId, PlaybackState.Playing)
        player?.playWhenReady = true
        startObserveProgress()

        bookDetails?.let { details ->
            val config = NotificationHelper.NotificationConfig.of(
                context, details, PlaybackState.Playing
            )
            NotificationHelper.updatePlayerNotification(config)
        }
    }

    private fun startPlayback(bookId: String, service: Service) {
        bookRepository.getBookDetails(bookId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ details: BookDetails ->
                bookDetails = details

                startForegroundService(
                    bookId,
                    details.author,
                    details.title,
                    details.coverPath,
                    service
                )

                bookRepository.updateBookState(bookId, PlaybackState.Playing)

                val sources: Array<MediaSource> = details.files.items.map { file ->
                    Uri.parse(file.path)
                }.map { uri ->
                    ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                        .createMediaSource(uri)
                }.toTypedArray()

                val mediaSource = ConcatenatingMediaSource(*sources)
                player?.prepare(mediaSource)
                player?.playWhenReady = true
                player?.seekTo(
                    details.progress.currentWindowIndex,
                    details.progress.playbackPosition
                )
                startObserveProgress()

            }, { e: Throwable ->
                Timber.tag("BukPlayer").e(e)
            }).addTo(disposable)
    }

    private fun stopPlayback(bookId: String) {
        if (bookId.isEmpty()) return

        player?.playWhenReady = false
        bookRepository.updateBookState(bookId, PlaybackState.Stopped)
        saveProgress()
        stopObserveProgress()
    }

    private fun pausePlayback(bookId: String) {
        if (bookId.isEmpty()) return

        player?.playWhenReady = false
        bookRepository.updateBookState(currentBookId, PlaybackState.Paused)
        saveProgress()
        stopObserveProgress()

        bookDetails?.let { details ->
            val config = NotificationHelper.NotificationConfig.of(
                context, details, PlaybackState.Paused
            )
            NotificationHelper.updatePlayerNotification(config)
        }
    }

    fun pausePlaying() {
        pausePlayback(currentBookId)
    }

    fun stopPlaying(service: Service) {
        stopPlayback(currentBookId)

        service.stopForeground(true)
        service.stopSelf()
    }

    fun onDestroy() {
        isRunning = false
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
        bookDetails = null

        currentBookId = ""
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

    private fun startForegroundService(
        bookId: String, author: String, title: String, coverPath: String, service: Service
    ) {
        val config = NotificationHelper.NotificationConfig(
            context,
            bookId,
            title,
            author,
            coverPath,
            PlaybackState.Playing
        )
        val notification = NotificationHelper.createPlayerNotification(config)
        service.startForeground(NotificationHelper.NOTIFICATION_ID, notification)
    }

    private fun startObserveProgress() {
        playbackProgress.subscribe({ (currentPosition, currentWindow) ->
            val files = bookDetails?.files?.items ?: emptyList()
            val totalDuration = bookDetails?.durationInSeconds ?: 0L

            bookRepository.updateBookProgress(
                currentBookId,
                currentPosition,
                currentWindow,
                files,
                totalDuration
            )
        }, { e ->
            Timber.tag("BukPlayer").d(e)
        }).addTo(playbackDisposable)
    }

    private fun stopObserveProgress() {
        playbackDisposable.clear()
        playbackDisposable = CompositeDisposable()
    }

    companion object {
        var isRunning: Boolean = false
            private set
    }

}
