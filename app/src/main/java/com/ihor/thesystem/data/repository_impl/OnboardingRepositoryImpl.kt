package com.ihor.thesystem.data.repository_impl

import android.content.Context
import com.ihor.thesystem.domain.repository.OnboardingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : OnboardingRepository {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val completed = MutableStateFlow(preferences.getBoolean(KEY_COMPLETED, false))

    override fun isOnboardingCompleted(): Flow<Boolean> =
        completed.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferences.edit().putBoolean(KEY_COMPLETED, completed).apply()
        this.completed.value = completed
    }

    private companion object {
        const val PREFERENCES_NAME = "the_system_onboarding"
        const val KEY_COMPLETED = "onboarding_completed"
    }
}
