package com.ihor.thesystem.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.domain.model.AppStartDestination
import com.ihor.thesystem.domain.usecase.ObserveAppStartDestinationUseCase
import com.ihor.thesystem.domain.usecase.RecordBetaAppOpenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    observeAppStartDestination: ObserveAppStartDestinationUseCase,
    private val recordBetaAppOpen: RecordBetaAppOpenUseCase
) : ViewModel() {

    val startDestination: StateFlow<AppStartDestination?> =
        observeAppStartDestination()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = null
            )

    init {
        viewModelScope.launch {
            try {
                recordBetaAppOpen()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.w(e, "Failed to record beta app entry metric")
            }
        }
    }
}
