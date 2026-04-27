package com.ihor.thesystem.presentation.common.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

private val hologramMatrix = ColorMatrix(
    floatArrayOf(
        0f,     0f,     0f,     0f,   0f,    // R
        -0.3f,  -0.59f, -0.11f, 0f,   255f,  // G
        -0.3f,  -0.59f, -0.11f, 0f,   255f,  // B
        0f,     0f,     0f,     0.85f, 0f    // A
    )
)

@Composable
fun ExerciseAnimationPlayer(
    exerciseExternalId: String?,
    modifier: Modifier = Modifier,
    animationDelay: Long = 1000L
) {
    if (exerciseExternalId == null) return

    var currentFrame by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(exerciseExternalId) {
        while (true) {
            delay(animationDelay)
            currentFrame = if (currentFrame == 0) 1 else 0
        }
    }

    val imageUrl = remember(exerciseExternalId, currentFrame) {
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/$exerciseExternalId/$currentFrame.jpg"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = imageUrl,
            animationSpec = tween(durationMillis = 500),
            label = "ExerciseFrameCrossfade"
        ) { targetUrl ->
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(targetUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Exercise Animation Frame",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.Cyan,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.Cyan.copy(alpha = 0.4f)
                        )
                    }
                }
            )
        }
    }
}
