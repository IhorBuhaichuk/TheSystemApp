package com.ihor.thesystem

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.ihor.thesystem.core.worker.DailyResetWorker
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TheSystemApp : Application(), ImageLoaderFactory, Configuration.Provider {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject
    lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        applicationScope.launch {
            delay(NON_CRITICAL_STARTUP_DELAY_MILLIS)
            try {
                DailyResetWorker.scheduleIfNotRunning(this@TheSystemApp)
            } catch (e: Exception) {
                Timber.e(e, "Unable to schedule daily reset worker after startup")
            }
        }
    }

    private companion object {
        const val NON_CRITICAL_STARTUP_DELAY_MILLIS = 10_000L
    }
}
