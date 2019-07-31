package pl.kitek.buk.presentation.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import pl.kitek.buk.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}
