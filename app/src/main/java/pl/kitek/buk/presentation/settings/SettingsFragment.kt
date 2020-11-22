package pl.kitek.buk.presentation.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.kitek.buk.R

class SettingsFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupUrlValidation()
    }

    //region Validation

    private fun setupUrlValidation() {

        val key = getString(R.string.pref_server_url)

        findPreference<EditTextPreference>(key)?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        val isUrlCorrect = viewModel.validateUrl(newValue.toString())

        if (!isUrlCorrect) showUrlErrorSnackbar()

        return isUrlCorrect
    }

    private fun showUrlErrorSnackbar() {
        val view = view ?: return

        Snackbar
            .make(view, R.string.invalid_url_error, Snackbar.LENGTH_LONG)
            .show()
    }

    //endregion
}
