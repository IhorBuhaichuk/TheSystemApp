package com.ihor.thesystem

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.ihor.thesystem.core.worker.DailyResetWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TheSystemApp : Application(), ImageLoaderFactory, Configuration.Provider {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun newImageLoader(): ImageLoader = imageLoader

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        try {
            DailyResetWorker.scheduleIfNotRunning(this)
        } catch (e: Exception) {
            Timber.e(e, "Unable to schedule daily reset worker during app startup")
        }
    }
}
