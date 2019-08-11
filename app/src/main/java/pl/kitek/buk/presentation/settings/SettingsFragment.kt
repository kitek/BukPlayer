package pl.kitek.buk.presentation.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.kitek.buk.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}
