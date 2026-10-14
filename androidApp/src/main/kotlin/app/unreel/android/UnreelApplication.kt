package app.unreel.android

import android.app.Application
import app.unreel.android.di.androidAppModule
import app.unreel.data.di.dataModule
import app.unreel.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Base Application class initializing Koin dependency injection.
 */
class UnreelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@UnreelApplication)
            modules(
                dataModule,
                presentationModule,
                androidAppModule,
            )
        }
    }
}
