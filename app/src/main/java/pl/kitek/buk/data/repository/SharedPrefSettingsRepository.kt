package pl.kitek.buk.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import pl.kitek.buk.R
import pl.kitek.buk.common.startWithIfEmpty
import pl.kitek.buk.data.model.ServerSettings

class SharedPrefSettingsRepository(context: Context) : SettingsRepository {

    private val urlKey = context.getString(R.string.pref_server_url)
    private val isAuthEnabledKey = context.getString(R.string.pref_auth_enabled)
    private val usernameKey = context.getString(R.string.pref_auth_username)
    private val passwordKey = context.getString(R.string.pref_auth_password)

    private var subscribersCount = 0
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val serverSettingsPublisher: BehaviorSubject<ServerSettings> = BehaviorSubject.create()
    private val updates = serverSettingsPublisher
        .startWithIfEmpty(serverSettingsPublisher, getServerSettings().toObservable())
        .distinctUntilChanged()
        .doOnSubscribe {
            sharedPreferences.registerOnSharedPreferenceChangeListener(changeListener)
            subscribersCount += 1
        }
        .doOnDispose {
            subscribersCount -= 1
            if (subscribersCount <= 0) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(changeListener)
            }
        }

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        val hasObservers = serverSettingsPublisher.hasObservers()
        val isValidKey = listOf(urlKey, isAuthEnabledKey, usernameKey, passwordKey).contains(key)

        if (hasObservers && isValidKey) serverSettingsPublisher.onNext(readSettings())
    }

    override fun getServerSettings(): Single<ServerSettings> = Single.fromCallable {
        readSettings()
    }

    override fun observeServerSettings(): Observable<ServerSettings> = updates

    private fun readSettings(): ServerSettings {
        val url = sharedPreferences.getString(urlKey, "") ?: ""
        val isAuthEnabled = sharedPreferences.getBoolean(isAuthEnabledKey, false)
        val username = sharedPreferences.getString(usernameKey, "") ?: ""
        val password = sharedPreferences.getString(passwordKey, "") ?: ""

        return ServerSettings(url, isAuthEnabled, username, password)
    }
}
