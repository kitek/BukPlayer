package pl.kitek.buk.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.common.addTo
import pl.kitek.buk.common.viewModel.BaseViewModel
import pl.kitek.buk.data.model.BookState
import pl.kitek.buk.data.repository.BookRepository
import pl.kitek.buk.data.service.player.PlayerController
import timber.log.Timber

class PlayerViewModel(
    private val bookId: String,
    private val bookRepository: BookRepository,
    private val playerController: PlayerController
) : BaseViewModel() {

    private val bookStateLiveData = MutableLiveData<BookState>()

    fun getBookState(): LiveData<BookState> = bookStateLiveData

    init {
        observeBookState()
    }

    private fun observeBookState() {
        bookRepository.observeBookState(bookId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bookState ->
                bookStateLiveData.value = bookState
            }, {
                Timber.tag("kitek").d("onError: $it")
            }).addTo(disposable)
    }

    fun play() {
        playerController.play(bookId)
    }

    fun pause() {
        playerController.pause()
    }

}

