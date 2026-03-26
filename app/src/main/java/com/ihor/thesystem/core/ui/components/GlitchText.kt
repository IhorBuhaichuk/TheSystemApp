package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import com.ihor.thesystem.core.theme.GlitchCyan
import com.ihor.thesystem.core.theme.GlitchPink
import com.ihor.thesystem.core.theme.NeonCyan
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.layout.offset

@Composable
fun GlitchText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    primaryColor: Color = NeonCyan
) {
    var offsetX    by remember { mutableStateOf(0) }
    var showGlitch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2000L, 5000L))
            repeat(Random.nextInt(3, 7)) {
                offsetX    = Random.nextInt(-8, 9)
                showGlitch = true
                delay(55L)
            }
            offsetX    = 0
            showGlitch = false
        }
    }

    Box(modifier = modifier) {
        if (showGlitch) {
            Text(
                text     = text,
                style    = style.copy(color = GlitchPink),
                modifier = Modifier
                    .offset { IntOffset(offsetX * 3, 2) }
                    .alpha(0.7f)
            )
            Text(
                text     = text,
                style    = style.copy(color = GlitchCyan),
                modifier = Modifier
                    .offset { IntOffset(-offsetX * 2, -2) }
                    .alpha(0.7f)
            )
        }
        Text(text = text, style = style.copy(color = primaryColor))
    }
}