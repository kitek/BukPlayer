package pl.kitek.buk.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.common.addTo
import pl.kitek.buk.common.viewModel.BaseViewModel
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.repository.BookRepository

class PlayerViewModel(
    private val bookId: String,
    private val bookRepository: BookRepository
) : BaseViewModel() {

    private val book: MutableLiveData<Book> by lazy {
        MutableLiveData<Book>().also { loadBook() }
    }

    fun getBook(): LiveData<Book> = book

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

