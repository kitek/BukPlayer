package pl.kitek.buk

import android.app.Application
import android.os.StrictMode
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pl.kitek.buk.common.di.networkModule
import pl.kitek.buk.common.di.repositoryModule
import pl.kitek.buk.common.di.viewModelModule
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        enableDebug()
        startKoin {
            androidContext(this@App)
            modules(listOf(networkModule, repositoryModule, viewModelModule))
        }
    }

    private fun enableDebug() {
        if (!BuildConfig.DEBUG) return

        Timber.plant(Timber.DebugTree())

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }
}
