package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.domain.model.PlayerRank

@Composable
fun LevelUpDialog(
    newClass: PlayerRank,
    newMonth: Int,
    onDismiss: () -> Unit
) {
    val scale by rememberInfiniteTransition(label = "pulse")
        .animateFloat(
            initialValue  = 0.95f,
            targetValue   = 1.05f,
            animationSpec = infiniteRepeatable(
                tween(800, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "pulse"
        )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF020408).copy(alpha = 0.95f))
                .border(2.dp, Brush.linearGradient(listOf(NeonGold, Color.Transparent)), RoundedCornerShape(32.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .blur(60.dp)
                    .background(NeonGold.copy(alpha = 0.15f), CircleShape)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Animated hex badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = buildHexagonPath(size, 0f)
                        drawPath(
                            path,
                            Brush.radialGradient(listOf(NeonGold.copy(alpha = 0.3f), Color.Transparent))
                        )
                        drawPath(path, NeonGold, style = Stroke(4.dp.toPx()))
                    }
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.text_level_up),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.text_system_updated),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = newClass.asUiText().asString(LocalContext.current).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.text_month_n, newMonth),
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonGold, fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.text_accept_power),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    )
                }
            }
        }
    }
}
