package com.ihor.thesystem.core.di

import android.app.Application
import com.ihor.thesystem.TheSystemApp
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.RealClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(app: Application): CoroutineScope {
        return (app as TheSystemApp).applicationScope
    }

    @Provides
    @Singleton
    fun provideAppClock(): AppClock = RealClock()
}
