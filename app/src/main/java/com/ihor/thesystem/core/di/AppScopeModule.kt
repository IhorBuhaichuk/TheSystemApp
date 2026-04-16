package com.ihor.thesystem.core.di

import android.app.Application
import com.ihor.thesystem.TheSystemApp
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.AppLogger
import com.ihor.thesystem.core.util.RealClock
import com.ihor.thesystem.core.util.TimberLoggerImpl
import dagger.Binds
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
abstract class AppScopeModule {

    @Binds
    @Singleton
    abstract fun bindAppLogger(logger: TimberLoggerImpl): AppLogger

    companion object {
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
}
