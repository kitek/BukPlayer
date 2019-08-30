package pl.kitek.buk.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.player_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pl.kitek.buk.R
import pl.kitek.buk.presentation.player.observer.PlayerObserver

class PlayerFragment : Fragment(), View.OnClickListener {

    private val args: PlayerFragmentArgs by navArgs()
    private val viewModel: PlayerViewModel by viewModel { parametersOf(args.bookId) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.title = args.bookTitle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.player_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playBtn.setOnClickListener(this)
        pauseBtn.setOnClickListener(this)
        replay15Btn.setOnClickListener(this)
        forward15Btn.setOnClickListener(this)

        viewModel.getBookState().observe(this, PlayerObserver(view))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.playBtn -> viewModel.play()
            R.id.pauseBtn -> viewModel.pause()
        }
    }
}
