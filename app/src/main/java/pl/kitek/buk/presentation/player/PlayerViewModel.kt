package pl.kitek.buk.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.common.addTo
import pl.kitek.buk.common.viewModel.BaseViewModel
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.repository.BookRepository
import pl.kitek.buk.data.service.player.PlayerController

class PlayerViewModel(
    private val bookId: String,
    private val bookRepository: BookRepository,
    private val playerController: PlayerController
) : BaseViewModel() {

    private val playerState = MutableLiveData<PlayerState>()
    private val book: MutableLiveData<Book> by lazy {
        MutableLiveData<Book>().also { loadBook() }
    }

    fun getBook(): LiveData<Book> = book
    fun getPlayerState(): LiveData<PlayerState> = playerState

    fun play() {
        playerController.play(bookId)
        playerState.value = PlayerState.Playing
    }

    fun pause() {
        playerController.pause()
        playerState.value = PlayerState.Paused
    }

    fun stop() {
        playerController.stop()
        playerState.value = PlayerState.Stopped
    }

    private fun loadBook() {
        bookRepository.getBook(bookId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ book ->
                this.book.value = book
            }, {
                // TODO
            }).addTo(disposable)
    }

}

