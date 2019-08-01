package pl.kitek.buk.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pl.kitek.buk.R

class PlayerFragment : Fragment() {

    private val args: PlayerFragmentArgs by navArgs()
    private val viewModel: PlayerViewModel by viewModel { parametersOf(args.bookId) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.player_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getBook().observe(this, Observer { book ->
            activity?.title = book.title
        })
    }

}
