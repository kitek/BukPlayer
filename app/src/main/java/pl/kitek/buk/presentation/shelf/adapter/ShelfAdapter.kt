package pl.kitek.buk.presentation.shelf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.shelf_item.view.*
import pl.kitek.buk.R
import pl.kitek.buk.common.loadImage
import pl.kitek.buk.data.model.Book

class ShelfAdapter : RecyclerView.Adapter<ShelfAdapter.ShelfViewHolder>() {

    var items: List<Book> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.shelf_item, parent, false)

        return ShelfViewHolder(
            itemView.bookItemImage,
            itemView.bookItemTitle,
            itemView.bookItemAuthor,
            itemView
        )
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        val book = items[position]
        holder.bind(book)
    }

    override fun getItemCount(): Int = items.size

    class ShelfViewHolder(
        private val bookItemImage: ImageView,
        private val bookItemTitle: TextView,
        private val bookItemAuthor: TextView,
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(book: Book) {
            bookItemImage.loadImage(book.coverPath)
            bookItemTitle.text = book.title
            bookItemAuthor.text = book.author
        }
    }
}
