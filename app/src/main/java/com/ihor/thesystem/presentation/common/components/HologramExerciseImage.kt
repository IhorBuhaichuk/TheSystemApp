package com.ihor.thesystem.presentation.common.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.ihor.thesystem.core.theme.SystemTheme
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

// Винесено за межі функції для уникнення реалокації пам'яті при кожній рекомпозиції
private val hologramMatrix = ColorMatrix(
    floatArrayOf(
        0f,     0f,     0f,     0f,   0f,    // R
        -0.3f,  -0.59f, -0.11f, 0f,   255f,  // G
        -0.3f,  -0.59f, -0.11f, 0f,   255f,  // B
        0f,     0f,     0f,     0.85f, 0f    // A
    )
)

@Composable
fun HologramExerciseImage(
    gifUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = SystemTheme.colors
    
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val imageRequest = remember(gifUrl, context) {
        ImageRequest.Builder(context)
            .data(gifUrl)
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = imageRequest,
        imageLoader = imageLoader,
        contentDescription = "Exercise Animation",
        modifier = modifier,
        contentScale = ContentScale.Fit,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accentPrimary)
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
                    tint = colors.accentPrimary.copy(alpha = 0.4f)
                )
            }
        }
    )
}
