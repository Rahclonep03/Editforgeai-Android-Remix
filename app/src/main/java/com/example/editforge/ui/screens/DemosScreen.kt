package com.example.editforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.AudioDemo
import com.example.editforge.data.model.AudioDemosData
import com.example.editforge.ui.components.WaveformView
import com.example.editforge.ui.components.formatDuration
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

@Composable
fun DemosScreen(
    viewModel: EditForgeViewModel,
    modifier: Modifier = Modifier
) {
    val playingId by viewModel.playingId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Audio Cut-Down Demos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Listen to real transformation examples generated from the same master track. Each cut preserves natural downbeats and resolves at musical boundaries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Demo Cards
        items(AudioDemosData.DEMOS) { demo ->
            val isThisPlaying = playingId == demo.id && isPlaying
            val progress = if (playingId == demo.id) playbackProgress else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = demo.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Duration: ${demo.durationLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeElectricAmber,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.togglePlay(demo.id, demo.duration)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isThisPlaying) ForgeCrimsonDark else StudioCardSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isThisPlaying) "Pause demo" else "Play demo",
                                tint = if (isThisPlaying) TextPrimary else ForgeElectricAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    WaveformView(
                        peaks = demo.peaks,
                        progress = progress,
                        onSeek = { ratio ->
                            if (playingId != demo.id) {
                                viewModel.togglePlay(demo.id, demo.duration)
                            }
                            viewModel.seekTo(ratio)
                        },
                        totalDuration = demo.duration
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = demo.copy,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 17.sp,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
