package pl.kitek.buk.common.viewModel

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {

    protected val subscription = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        subscription.clear()
    }
}
