package com.ihor.thesystem

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class TheSystemApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
