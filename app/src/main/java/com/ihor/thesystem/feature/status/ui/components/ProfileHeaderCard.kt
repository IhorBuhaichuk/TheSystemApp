package com.ihor.thesystem.feature.status.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData

@Composable
fun ProfileHeaderCard(
    data: StatusUiData,
    onNameTap: () -> Unit,
    onAvatarSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onAvatarSelected(it) } }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor = PanelBorder.copy(alpha = 0.5f),
                backgroundColor = PanelSurface.copy(alpha = 0.8f),
                cornerCut = 16.dp
            )
            .padding(20.dp)
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
                    text = "ГРАВЕЦЬ",
                    style = TheSystemTypography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = data.playerName.uppercase(),
                    style = TheSystemTypography.displayLarge.copy(
                        fontSize = 36.sp,
                        lineHeight = 40.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.clickable { onNameTap() }
                )
                Text(
                    text = "RANK ${data.globalRank.name}",
                    style = TheSystemTypography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(
                        BorderStroke(
                            3.dp,
                            Brush.sweepGradient(
                                colors = listOf(
                                    NeonCyan, 
                                    Color.Transparent, 
                                    NeonCyan.copy(alpha = 0.5f), 
                                    Color.Transparent, 
                                    NeonCyan
                                )
                            )
                        ),
                        CircleShape
                    )
                    .clickable {
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
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(140.dp),
                        tint = NeonCyan.copy(alpha = 0.2f)
                    )
                    // Додатковий шар для стилізації "чоловічка"
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(130.dp),
                        tint = NeonCyan.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "XP PROGRESS",
                    style = TheSystemTypography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "${data.xpTotal % 1000}/1000",
                    style = TheSystemTypography.labelSmall,
                    color = NeonCyan
                )
            }
            
            LinearProgressIndicator(
                progress = { (data.xpTotal % 1000) / 1000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonCyan,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}
