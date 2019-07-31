package pl.kitek.buk.common

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(disposable: CompositeDisposable) {
    disposable.add(this)
}

fun ImageView.loadImage(url: String) {
    if (url.isEmpty()) return

    val req = Picasso.with(context).load(url).config(Bitmap.Config.RGB_565)
    req.into(this)
}
