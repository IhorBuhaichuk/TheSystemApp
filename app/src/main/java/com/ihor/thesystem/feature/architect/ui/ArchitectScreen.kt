package com.ihor.thesystem.feature.architect.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ihor.thesystem.core.ui.components.GlitchText
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectUiState
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectViewModel

@Composable
fun ArchitectScreen(
    viewModel: ArchitectViewModel = hiltViewModel(),
    onAcknowledge: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Базові кольори системи (з існуючої палітри)
    val neonCyan = Color(0xFF00FFFF)
    val neonRed = Color(0xFFFF003C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is ArchitectUiState.Loading -> {
                CircularProgressIndicator(color = neonCyan)
            }
            is ArchitectUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlitchText(
                        text = "ПОМИЛКА СИСТЕМИ",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = neonRed, fontWeight = FontWeight.Bold)
                }
            }
            is ArchitectUiState.Success -> {
                val report = state.data

                Column(modifier = Modifier.fillMaxSize()) {
                    GlitchText(
                        text = "ЗВІТ АРХІТЕКТОРА",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (report.isFallback) {
                        Text(
                            text = "[ УВАГА: ОФЛАЙН РЕЖИМ. ДАНІ З ЛОКАЛЬНОЇ МАТРИЦІ ]",
                            color = neonRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(text = "СТАТУС: ${report.stageStatus}", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "АНАЛІЗ: ${report.feedback}", color = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "ВІДНОВЛЕННЯ ЦНС: ${report.recoveryHours} ГОД", color = neonCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "ДИРЕКТИВИ НА НАСТУПНИЙ ЦИКЛ:", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        items(report.directives) { directive ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sciPanel(borderColor = neonCyan, backgroundColor = Color.Transparent)
                                    .padding(16.dp)
                            ) {
                                Text(text = "ID ВПРАВИ: ${directive.exerciseId}", color = neonCyan, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "ЦІЛЬОВА ВАГА: ${directive.targetWeight} КГ", color = Color.White)
                                Text(text = "СХЕМА (СЕТИ x ПОВТОРИ): ${directive.targetSets} x ${directive.targetReps}", color = Color.LightGray)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.acknowledge()
                            onAcknowledge()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(text = "[ ACKNOWLEDGE ]", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}