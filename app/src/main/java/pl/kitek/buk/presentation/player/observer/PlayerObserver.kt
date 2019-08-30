package pl.kitek.buk.presentation.player.observer

import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.player_fragment.view.*
import pl.kitek.buk.R
import pl.kitek.buk.common.loadImage
import pl.kitek.buk.data.model.BookState
import pl.kitek.buk.data.model.PlaybackState

class PlayerObserver(private val view: View) : Observer<BookState> {

    override fun onChanged(bookState: BookState) {
        updatePlaybackState(bookState.currentPlaybackState)
        updateCover(bookState.coverPath)
        updateProgress(
            bookState.currentProgress.completedPercent,
            bookState.currentProgress.timeLeftFormatted
        )
    }

    private fun updatePlaybackState(currentPlaybackState: PlaybackState) {
        when (currentPlaybackState) {
            is PlaybackState.Playing -> {
                view.playBtn.visibility = View.GONE
                view.pauseBtn.visibility = View.VISIBLE
            }
            is PlaybackState.Paused, PlaybackState.Stopped -> {
                view.playBtn.visibility = View.VISIBLE
                view.pauseBtn.visibility = View.GONE
            }
        }
    }

    private fun updateCover(coverPath: String) {
        view.bookCoverImageView.loadImage(coverPath)
    }

    private fun updateProgress(completedPercent: Int, timeLeft: String) {
        val progress = view.resources.getString(
            R.string.player_progress, completedPercent, timeLeft
        )
        view.bookProgressTextView.text = progress
    }

}
