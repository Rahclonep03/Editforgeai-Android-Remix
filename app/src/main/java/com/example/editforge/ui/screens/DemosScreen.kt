package com.example.editforge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier,
    onNavigateProject: ((String) -> Unit)? = null
) {
    val playingId by viewModel.playingId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val studioColors = StudioTheme.colors

    val masterTrack = AudioDemosData.MASTER_TRACK
    val isMasterPlaying = playingId == masterTrack.id && isPlaying
    val masterProgress = if (playingId == masterTrack.id) playbackProgress else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(studioColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Header with Cybernetic Groove Demo Banner
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(studioColors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "OFFICIAL DEMO TRACK",
                            color = studioColors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "122 BPM · E Minor",
                        color = studioColors.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cybernetic Groove.wav",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = studioColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Experience real transformation examples forged from the original studio master track. Tap play to listen with authentic audio and live synchronized waveform progression.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = studioColors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. Featured Hero Card: Original Master Track (Cybernetic Groove.wav)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("demo_master_hero_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = studioColors.surface),
                border = BorderStroke(
                    width = if (isMasterPlaying) 1.8.dp else 1.dp,
                    color = if (isMasterPlaying) studioColors.primary else studioColors.primary.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    studioColors.primaryContainer.copy(alpha = 0.35f),
                                    studioColors.surface
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title, Badge & Play/Pause
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isMasterPlaying) studioColors.primary else studioColors.primary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMasterPlaying) Icons.Default.GraphicEq else Icons.Default.Audiotrack,
                                    contentDescription = "Master Track Icon",
                                    tint = if (isMasterPlaying) studioColors.onPrimary else studioColors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Cybernetic Groove.wav",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = studioColors.textPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Original Studio Master · Duration 2:30",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studioColors.secondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.togglePlay(masterTrack.id, masterTrack.duration)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isMasterPlaying) studioColors.primary else studioColors.primaryContainer)
                                .border(1.dp, studioColors.primary, CircleShape)
                                .testTag("play_master_demo_button")
                        ) {
                            Icon(
                                imageVector = if (isMasterPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isMasterPlaying) "Pause Master" else "Play Master",
                                tint = if (isMasterPlaying) studioColors.onPrimary else studioColors.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Audio Specs Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DemoSpecPill(text = "24-bit / 48kHz WAV")
                        DemoSpecPill(text = "122 BPM")
                        DemoSpecPill(text = "E Minor")
                        DemoSpecPill(text = "-13.5 LUFS")
                    }

                    // Interactive Master Waveform with Scrubber
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WaveformView(
                            peaks = masterTrack.peaks,
                            progress = masterProgress,
                            onSeek = { ratio ->
                                if (playingId != masterTrack.id) {
                                    viewModel.togglePlay(masterTrack.id, masterTrack.duration)
                                }
                                viewModel.seekTo(ratio)
                            },
                            totalDuration = masterTrack.duration,
                            activeColor = studioColors.primary,
                            inactiveColor = studioColors.surfaceElevated
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val currentSec = (masterProgress * masterTrack.duration)
                            Text(
                                text = formatDuration(currentSec),
                                fontSize = 11.sp,
                                color = if (isMasterPlaying) studioColors.primary else studioColors.textTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = masterTrack.durationLabel,
                                fontSize = 11.sp,
                                color = studioColors.textTertiary
                            )
                        }
                    }

                    Text(
                        text = masterTrack.copy,
                        style = MaterialTheme.typography.bodySmall,
                        color = studioColors.textSecondary,
                        lineHeight = 16.sp,
                        fontSize = 12.sp
                    )

                    // Quick CTA: Open in Studio Forge
                    if (onNavigateProject != null) {
                        Button(
                            onClick = { onNavigateProject(masterTrack.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = studioColors.primary,
                                contentColor = studioColors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_cybernetic_in_studio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Cybernetic Groove in Studio Forge",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Section Title: Cut-Down Deliverables
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Forged Deliverables & Cuts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = studioColors.textPrimary
                )
                Box(
                    modifier = Modifier
                        .background(studioColors.surfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, studioColors.border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${AudioDemosData.DEMOS.size} Audition Cuts",
                        color = studioColors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 4. Demo Deliverable Cards
        items(AudioDemosData.DEMOS) { demo ->
            val isThisPlaying = playingId == demo.id && isPlaying
            val progress = if (playingId == demo.id) playbackProgress else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("demo_card_${demo.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = studioColors.surface),
                border = BorderStroke(
                    width = if (isThisPlaying) 1.4.dp else 1.dp,
                    color = if (isThisPlaying) studioColors.secondary else studioColors.border
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(studioColors.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = demo.badge,
                                        color = studioColors.secondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = demo.durationLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studioColors.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = demo.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = studioColors.textPrimary,
                                fontSize = 14.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.togglePlay(demo.id, demo.duration)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isThisPlaying) studioColors.secondary else studioColors.surfaceElevated)
                                .border(1.dp, if (isThisPlaying) studioColors.secondary else studioColors.border, RoundedCornerShape(8.dp))
                                .testTag("play_demo_${demo.id}")
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isThisPlaying) "Pause demo" else "Play demo",
                                tint = if (isThisPlaying) studioColors.onSecondary else studioColors.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Waveform View
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        WaveformView(
                            peaks = demo.peaks,
                            progress = progress,
                            onSeek = { ratio ->
                                if (playingId != demo.id) {
                                    viewModel.togglePlay(demo.id, demo.duration)
                                }
                                viewModel.seekTo(ratio)
                            },
                            totalDuration = demo.duration,
                            activeColor = studioColors.secondary,
                            inactiveColor = studioColors.surfaceElevated
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val elapsed = progress * demo.duration
                            Text(
                                text = formatDuration(elapsed),
                                fontSize = 10.sp,
                                color = if (isThisPlaying) studioColors.secondary else studioColors.textTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = demo.durationLabel,
                                fontSize = 10.sp,
                                color = studioColors.textTertiary
                            )
                        }
                    }

                    Text(
                        text = demo.copy,
                        style = MaterialTheme.typography.bodySmall,
                        color = studioColors.textSecondary,
                        lineHeight = 16.sp,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 5. Attribution & Engineering Note
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(studioColors.surfaceElevated.copy(alpha = 0.6f))
                    .border(1.dp, studioColors.border, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = studioColors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo edits created with EDITFORGE from an original master (Cybernetic Groove.wav), used with permission. Full 30-day cloud retention and unlimited revisions available with any Studio Pro pass.",
                        style = MaterialTheme.typography.bodySmall,
                        color = studioColors.textTertiary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoSpecPill(text: String) {
    val studioColors = StudioTheme.colors
    Box(
        modifier = Modifier
            .background(studioColors.surfaceElevated, RoundedCornerShape(6.dp))
            .border(1.dp, studioColors.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = studioColors.textSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
