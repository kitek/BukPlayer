package pl.kitek.buk.data.repository

import io.reactivex.Observable
import io.reactivex.Single
import pl.kitek.buk.data.model.ServerSettings

interface SettingsRepository {

    fun getServerSettings(): Single<ServerSettings>
    fun observeServerSettings(): Observable<ServerSettings>

}
