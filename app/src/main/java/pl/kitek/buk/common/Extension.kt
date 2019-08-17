package pl.kitek.buk.common

import android.graphics.Bitmap
import android.widget.ImageView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

fun Disposable.addTo(disposable: CompositeDisposable) {
    disposable.add(this)
}

fun ImageView.loadImage(url: String) {
    if (url.isEmpty()) return

    PicassoFactory.instance.load(url).config(Bitmap.Config.RGB_565).into(this)
}

fun <T> Observable<T>.startWithIfEmpty(subject: BehaviorSubject<T>, default: Observable<T>): Observable<T> {
    return this.startWith(Observable.defer {
        if (subject.hasValue()) Observable.empty<T>() else default
    })
}

