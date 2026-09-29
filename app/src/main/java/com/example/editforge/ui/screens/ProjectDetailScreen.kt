package com.example.editforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.DeliverableType
import com.example.editforge.data.model.ExportItem
import com.example.editforge.data.model.Project
import com.example.editforge.ui.components.DeliverableItemCard
import com.example.editforge.ui.components.StatusBadge
import com.example.editforge.ui.components.WaveformView
import com.example.editforge.ui.components.formatDuration
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

@Composable
fun ProjectDetailScreen(
    viewModel: EditForgeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val activeAnalysis by viewModel.activeAnalysis.collectAsState()
    val activeExports by viewModel.activeExports.collectAsState()
    val creditBalance by viewModel.creditBalance.collectAsState()

    val playingId by viewModel.playingId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (activeProject == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(StudioDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No project selected.", color = TextSecondary)
        }
        return
    }

    val project = activeProject!!
    val analysis = activeAnalysis

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Studio Project?") },
            text = { Text("This will permanently remove ${project.projectName} and all rendered deliverables.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProject(project.id)
                        onBack()
                    }
                ) {
                    Text("Delete", color = ForgeNeonRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = StudioCardSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Top Action bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = project.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Project",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Project Title & File metadata
        item {
            Column {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${project.fileName} · ${formatDuration(project.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ForgeElectricAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "28d retention left",
                            color = ForgeElectricAmber,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Audio DSP Analysis Summary Grid
        if (analysis != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Audio DSP & Structure Profile",
                            style = MaterialTheme.typography.labelLarge,
                            color = ForgeElectricAmber,
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnalysisChip(label = "Tempo", value = "${analysis.bpm} BPM")
                            AnalysisChip(label = "Key", value = analysis.musicKey)
                            AnalysisChip(label = "Integrated", value = "${analysis.lufs} LUFS")
                            AnalysisChip(label = "Energy", value = "${(analysis.energy * 100).toInt()}%")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Genre: ${analysis.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Mood: ${analysis.mood}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeWaveformViolet,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Interactive Master Waveform View
        item {
            val peaks = analysis?.waveform ?: List(48) { 0.5f }
            val sections = analysis?.structure ?: emptyList()
            val isCurrentMasterPlaying = playingId == project.id && isPlaying

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurfaceElevated)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Master Waveform & Downbeats",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        val elapsedSeconds = (playbackProgress * project.duration).toInt()
                        Text(
                            text = "${formatDuration(elapsedSeconds.toFloat())} / ${formatDuration(project.duration)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForgeElectricAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    WaveformView(
                        peaks = peaks,
                        progress = if (playingId == project.id) playbackProgress else 0f,
                        onSeek = { ratio ->
                            viewModel.seekTo(ratio)
                        },
                        sections = sections,
                        totalDuration = project.duration
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Structure section badges
                    if (sections.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sections.forEach { section ->
                                val badgeColor = when (section.label.lowercase()) {
                                    "intro" -> ForgeElectricAmber
                                    "verse" -> ForgeWaveformCyan
                                    "chorus" -> ForgeNeonRed
                                    "bridge" -> ForgeWaveformViolet
                                    else -> StatusSuccess
                                }
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${section.label} (${formatDuration(section.start)}-${formatDuration(section.end)})",
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Player Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.togglePlay(project.id, project.duration)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentMasterPlaying) ForgeCrimsonDark else ForgeNeonRed,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrentMasterPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isCurrentMasterPlaying) "Pause Master" else "Play Master",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCurrentMasterPlaying) "Pause Master Track" else "Preview Master Track",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Deliverables Forge Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deliverables Forge",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Balance: $creditBalance cr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeElectricAmber,
                    fontSize = 12.sp
                )
            }
        }

        // Core Bundle Card / Unlock action
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (project.coreBundleUnlocked) Color(0xFF1E261A) else StudioCardSurface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (project.coreBundleUnlocked) StatusSuccess.copy(alpha = 0.5f) else ForgeNeonRed.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (project.coreBundleUnlocked) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                        contentDescription = "Core Bundle",
                        tint = if (project.coreBundleUnlocked) StatusSuccess else ForgeNeonRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (project.coreBundleUnlocked) "Core Bundle Unlocked" else "Core Bundle (60s, 30s, 15s + Sting)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (project.coreBundleUnlocked) "All standard broadcast & social cut-downs forged" else "Batch generate every standard format with 1 click (5 credits)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (!project.coreBundleUnlocked) {
                        Button(
                            onClick = { viewModel.forgeCoreBundle(project.id) },
                            enabled = creditBalance >= 5,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgeNeonRed,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Forge (5 cr)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Additional Deliverable Forge Options
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Individual & Stem Renders",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SingleForgeButton(
                        label = "60s Cut (2cr)",
                        enabled = creditBalance >= 2,
                        onClick = { viewModel.forgeSingleDeliverable(project.id, DeliverableType.EDIT_60) },
                        modifier = Modifier.weight(1f)
                    )
                    SingleForgeButton(
                        label = "30s Hook (2cr)",
                        enabled = creditBalance >= 2,
                        onClick = { viewModel.forgeSingleDeliverable(project.id, DeliverableType.EDIT_30) },
                        modifier = Modifier.weight(1f)
                    )
                    SingleForgeButton(
                        label = "Sting (1cr)",
                        enabled = creditBalance >= 1,
                        onClick = { viewModel.forgeSingleDeliverable(project.id, DeliverableType.STING) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SingleForgeButton(
                        label = "Vocals Stem (3cr)",
                        enabled = creditBalance >= 3,
                        onClick = { viewModel.forgeSingleDeliverable(project.id, DeliverableType.STEM_VOCALS) },
                        modifier = Modifier.weight(1f)
                    )
                    SingleForgeButton(
                        label = "Drums Stem (3cr)",
                        enabled = creditBalance >= 3,
                        onClick = { viewModel.forgeSingleDeliverable(project.id, DeliverableType.STEM_DRUMS) },
                        modifier = Modifier.weight(1f)
                    )
                    SingleForgeButton(
                        label = "Bass Stem (3cr)",
                        enabled = creditBalance >= 3,
                        onClick = { viewModel.forgeSingleDeliverable(project.id, DeliverableType.STEM_BASS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Rendered Deliverables List
        item {
            Text(
                text = "Rendered Deliverables (${activeExports.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        if (activeExports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardSurface, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No deliverables forged yet. Click 'Unlock Core Bundle' above to generate cut-downs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(activeExports) { export ->
                val isThisExportPlaying = playingId == export.id && isPlaying
                DeliverableItemCard(
                    export = export,
                    isPlaying = isThisExportPlaying,
                    onTogglePlay = {
                        viewModel.togglePlay(export.id, export.duration)
                    },
                    onForgeAlt = {
                        viewModel.forgeAlternative(export)
                    }
                )
            }
        }
    }
}

@Composable
fun AnalysisChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextSecondary, fontSize = 10.sp, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextPrimary, fontSize = 13.sp, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SingleForgeButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary,
            containerColor = StudioCardSurface
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Text(text = label, fontSize = 11.sp, maxLines = 1)
    }
}
