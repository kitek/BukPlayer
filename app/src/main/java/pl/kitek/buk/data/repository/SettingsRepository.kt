package pl.kitek.buk.data.repository

import io.reactivex.Single

interface SettingsRepository {

    fun getServerUrl(): Single<String>

}
