package com.ihor.thesystem.core.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/** Visible feedback for the active screen, including dialogs owned by that screen. */
@Composable
fun UiErrorEffect(events: Flow<UiEvent>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(events, lifecycleOwner, context) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { event ->
                if (event is UiEvent.ShowError) {
                    Toast.makeText(context, event.uiText.asString(context), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
