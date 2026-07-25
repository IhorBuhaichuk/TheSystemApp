package com.ihor.thesystem.feature.status.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.SystemCard
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData

@Composable
fun ProfileHeaderCard(
    data: StatusUiData,
    onNameTap: () -> Unit,
    onAvatarSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onAvatarSelected(it) } }
    )

    SystemCard(
        modifier = modifier.fillMaxWidth(),
        active = true,
        contentPadding = SystemScreenPadding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Гравець",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = data.playerName.toSystemSentenceCase(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = colors.textPrimary
                    ),
                    modifier = Modifier.systemClickable(onClick = onNameTap),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Rank ${data.globalRank.name.toSystemSentenceCase()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(SystemCardPadding))

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(colors.backgroundElevated)
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    colors.accentPrimary,
                                    colors.accentAi.copy(alpha = 0.20f),
                                    colors.accentPrimary.copy(alpha = 0.52f),
                                    Color.Transparent,
                                    colors.accentPrimary
                                )
                            )
                        ),
                        CircleShape
                    )
                    .systemClickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (data.avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Аватар",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(140.dp),
                        tint = colors.accentPrimary.copy(alpha = 0.18f)
                    )
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(130.dp),
                        tint = colors.accentPrimary.copy(alpha = 0.38f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SystemScreenPadding))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "XP progress",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${data.xpTotal % 1000}/1000",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            SystemProgressBar(
                progress = (data.xpTotal % 1000) / 1000f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                accent = colors.accentPrimary
            )
        }
    }
}
