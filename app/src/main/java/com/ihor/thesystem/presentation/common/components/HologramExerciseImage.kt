package com.ihor.thesystem.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Composable компонент для відображення вправ з ефектом "голограми".
 * Застосовує інверсію, ціанове тонування та напівпрозорість.
 */
@Composable
fun HologramExerciseImage(
    gifUrl: String?,
    modifier: Modifier = Modifier
) {
    // Колірна матриця для ефекту цифрової проекції:
    // 1. Коефіцієнти -0.3f, -0.59f, -0.11f виконують інверсію яскравості (Luma).
    // 2. Червоний канал (перший рядок) повністю занулений (0f), щоб пригнітити червоний колір.
    // 3. Зелений та Блакитний канали отримують інвертовані дані, створюючи ціановий відтінок.
    // 4. Останній рядок встановлює alpha на рівні 0.85f.
    val hologramMatrix = ColorMatrix(
        floatArrayOf(
            0f,     0f,     0f,     0f,   0f,    // R: Повне пригнічення червоного
            -0.3f,  -0.59f, -0.11f, 0f,   255f,  // G: Інверсія + Зелений канал
            -0.3f,  -0.59f, -0.11f, 0f,   255f,  // B: Інверсія + Блакитний канал
            0f,     0f,     0f,     0.85f, 0f    // A: Прозорість 85%
        )
    )

    AsyncImage(
        model = gifUrl,
        contentDescription = "Exercise Hologram",
        modifier = modifier
            .background(Color.Black)
            .fillMaxSize(),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.colorMatrix(hologramMatrix)
    )
}
