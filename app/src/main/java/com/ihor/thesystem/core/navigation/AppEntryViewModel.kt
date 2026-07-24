package com.ihor.thesystem.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.domain.model.AppStartDestination
import com.ihor.thesystem.domain.usecase.ObserveAppStartDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    observeAppStartDestination: ObserveAppStartDestinationUseCase
) : ViewModel() {

    val startDestination: StateFlow<AppStartDestination?> =
        observeAppStartDestination()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = null
            )
}
