package pl.kitek.buk.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.reactivex.Single

class SharedPrefSettingsRepository(context: Context) : SettingsRepository {

    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun getServerUrl(): Single<String> = Single.fromCallable {
        sharedPreferences.getString("serverURL", "")
    }

}
