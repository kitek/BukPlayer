package pl.kitek.buk.presentation.shelf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.common.addTo
import pl.kitek.buk.common.viewModel.BaseViewModel
import pl.kitek.buk.data.model.Book
import pl.kitek.buk.data.repository.BookRepository

class ShelfViewModel(
    private val bookRepository: BookRepository
) : BaseViewModel() {

    private val books: MutableLiveData<List<Book>> by lazy {
        MutableLiveData<List<Book>>().also { loadBooks() }
    }

    fun getBooks(): LiveData<List<Book>> = books

    private fun loadBooks() {
        bookRepository.getBooks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ books ->
                this.books.value = books.items
            }, {

            }).addTo(disposable)
    }

}
