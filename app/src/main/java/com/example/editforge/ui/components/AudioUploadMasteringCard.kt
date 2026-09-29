package com.example.editforge.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.AudioDemosData
import com.example.editforge.ui.theme.*

enum class MasteringProfile(val label: String, val lufs: String, val description: String) {
    STREAMING("Streaming Target", "-14 LUFS", "Optimal integrated loudness for Spotify, Apple Music & YouTube"),
    CLUB("Club Impact", "-9 LUFS", "Maximum punch, heavy bass saturation & loud transients for DJ sets"),
    BROADCAST("Broadcast Dynamic", "-16 LUFS", "High headroom & transparent dynamic range for film & broadcast")
}

data class QuickDemoAudio(
    val title: String,
    val fileName: String,
    val durationSeconds: Float,
    val sampleRate: String,
    val bitDepth: String
)

val DASHBOARD_AUDIO_PRESETS = listOf(
    QuickDemoAudio(
        title = "Neon Horizons - Electronic Master",
        fileName = "neon_horizons_24b_48k.wav",
        durationSeconds = 214.5f,
        sampleRate = "48 kHz",
        bitDepth = "24-bit PCM"
    ),
    QuickDemoAudio(
        title = "Midnight Drive - Synthwave Hook",
        fileName = "midnight_drive_radio_96k.wav",
        durationSeconds = 188.0f,
        sampleRate = "96 kHz",
        bitDepth = "24-bit PCM"
    ),
    QuickDemoAudio(
        title = "Apex Frequency - Commercial Pop",
        fileName = "apex_frequency_master.wav",
        durationSeconds = 175.0f,
        sampleRate = "44.1 kHz",
        bitDepth = "16-bit Master"
    )
)

@Composable
fun AudioUploadMasteringCard(
    creditBalance: Int,
    isAnalyzing: Boolean,
    analysisProgress: Float,
    analysisStageText: String,
    onStartMastering: (title: String, fileName: String, duration: Float, autoForge: Boolean, profile: MasteringProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedPresetIndex by remember { mutableStateOf(0) }
    var customAudioName by remember { mutableStateOf<String?>(null) }
    var customFileName by remember { mutableStateOf<String?>(null) }
    var customFileSize by remember { mutableStateOf<String?>(null) }
    var customDurationSeconds by remember { mutableStateOf(195.0f) }

    var selectedProfile by remember { mutableStateOf(MasteringProfile.STREAMING) }
    var autoForgeBundle by remember { mutableStateOf(true) }
    var detectStructure by remember { mutableStateOf(true) }

    // Android System File Picker for Audio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "Selected_Audio.wav"
            var fileSize = "Unknown size"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                        if (sizeIndex != -1) {
                            val bytes = cursor.getLong(sizeIndex)
                            fileSize = String.format("%.1f MB", bytes / (1024f * 1024f))
                        }
                    }
                }
            } catch (e: Exception) {
                // fallback
            }

            val cleanTitle = fileName.substringBeforeLast(".")
            customAudioName = cleanTitle.replace("_", " ").replace("-", " ")
            customFileName = fileName
            customFileSize = fileSize
            customDurationSeconds = 205.0f
        }
    }

    val activeTitle = customAudioName ?: DASHBOARD_AUDIO_PRESETS[selectedPresetIndex].title
    val activeFileName = customFileName ?: DASHBOARD_AUDIO_PRESETS[selectedPresetIndex].fileName
    val activeDuration = if (customAudioName != null) customDurationSeconds else DASHBOARD_AUDIO_PRESETS[selectedPresetIndex].durationSeconds

    val currentPeaks = remember(selectedPresetIndex, customAudioName) {
        if (customAudioName != null) {
            val hash = (customAudioName ?: "audio").hashCode()
            List(64) { i ->
                val phase = (i.toFloat() / 64f) * 3.1415f * 4f
                val base = (kotlin.math.sin(phase.toDouble()).toFloat() * 0.35f + 0.55f)
                val variation = ((i * 7 + kotlin.math.abs(hash)) % 13) * 0.03f
                (base + variation).coerceIn(0.18f, 0.96f)
            }
        } else {
            when (selectedPresetIndex) {
                0 -> AudioDemosData.DEMOS[0].peaks
                1 -> AudioDemosData.DEMOS[1].peaks
                else -> AudioDemosData.DEMOS[2].peaks
            }
        }
    }

    val requiredCredits = if (autoForgeBundle) 5 else 0
    val canProceed = creditBalance >= requiredCredits && !isAnalyzing
    val studioColors = StudioTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        studioColors.primaryContainer.copy(alpha = 0.5f),
                        studioColors.surface,
                        studioColors.surface
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(studioColors.primary.copy(alpha = 0.6f), studioColors.border, studioColors.secondary.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForgeCrimsonDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Ingestion Icon",
                            tint = ForgeNeonRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Audio Ingestion & Mastering",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select master audio for structure mapping & cut-downs",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(ForgeElectricAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$creditBalance cr",
                        color = ForgeElectricAmber,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }

            // In-Card Live Analysis Mode
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, ForgeNeonRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analyzing & Mastering Master Track...",
                                style = MaterialTheme.typography.titleMedium,
                                color = ForgeElectricAmber,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${(analysisProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = ForgeNeonRed,
                                fontSize = 13.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { analysisProgress },
                            color = ForgeNeonRed,
                            trackColor = StudioBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Text(
                            text = analysisStageText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            } else {
                // Interactive File Picker Box (Tap to pick audio file from device storage)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioCardSurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (customAudioName != null) ForgeNeonRed else StudioBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            audioPickerLauncher.launch("audio/*")
                        }
                        .testTag("select_audio_file_button")
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (customAudioName != null) ForgeNeonRed.copy(alpha = 0.2f)
                                    else ForgeWaveformViolet.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (customAudioName != null) Icons.Default.AudioFile else Icons.Default.CloudUpload,
                                contentDescription = "Pick audio file",
                                tint = if (customAudioName != null) ForgeNeonRed else ForgeWaveformViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (customAudioName != null) activeTitle else "Tap to Select Audio from Device",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (customAudioName != null) "$activeFileName · $customFileSize · WAV/MP3" else "Supports uncompressed WAV, AIFF, FLAC, and high-bitrate MP3",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customAudioName != null) StudioCardSurface else ForgeNeonRed,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (customAudioName != null) "Change" else "Browse",
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Studio Presets Quick Tabs
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Or Choose Studio Demo Track",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        if (customAudioName != null) {
                            Text(
                                text = "Clear custom file",
                                color = ForgeElectricAmber,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable {
                                    customAudioName = null
                                    customFileName = null
                                    customFileSize = null
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DASHBOARD_AUDIO_PRESETS.forEachIndexed { idx, preset ->
                            val isChosen = customAudioName == null && selectedPresetIndex == idx
                            val shortLabel = when (idx) {
                                0 -> "Synthwave 48k"
                                1 -> "Radio Pop 96k"
                                else -> "Funk Master"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) ForgeCrimsonDark else StudioCardSurfaceElevated)
                                    .border(
                                        width = 1.dp,
                                        color = if (isChosen) ForgeNeonRed else StudioBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        customAudioName = null
                                        customFileName = null
                                        selectedPresetIndex = idx
                                    }
                                    .testTag("quick_preset_button_$idx")
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortLabel,
                                    fontSize = 11.sp,
                                    color = if (isChosen) TextPrimary else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Interactive Canvas Waveform Visualization for Selected Audio File
                AudioAmplitudeWaveformCanvas(
                    peaks = currentPeaks,
                    totalDurationSeconds = activeDuration,
                    audioTitle = activeTitle,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mastering Loudness Profile Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mastering Profile & Target Loudness",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MasteringProfile.entries.forEach { profile ->
                            val isSelected = selectedProfile == profile
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) studioColors.primaryContainer.copy(alpha = 0.5f) else StudioCardSurfaceElevated)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) studioColors.secondary else StudioBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedProfile = profile }
                                    .testTag("mastering_preset_${profile.name.lowercase()}")
                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = profile.lufs,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) ForgeElectricAmber else TextPrimary
                                    )
                                    Text(
                                        text = profile.label,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Options Row: Auto-Forge Bundle & Musical Boundary Detection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Forge Core Bundle (60s, 30s, 15s + Sting)",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Batch generates standard cut-downs (5 credits)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = autoForgeBundle,
                        onCheckedChange = { autoForgeBundle = it },
                        modifier = Modifier.testTag("auto_forge_toggle"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = ForgeNeonRed
                        )
                    )
                }

                // Primary Start Analysis & Mastering Button
                Button(
                    onClick = {
                        onStartMastering(
                            activeTitle,
                            activeFileName,
                            activeDuration,
                            autoForgeBundle,
                            selectedProfile
                        )
                    },
                    enabled = canProceed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_mastering_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForgeNeonRed,
                        disabledContainerColor = StudioCardSurfaceElevated,
                        contentColor = TextPrimary,
                        disabledContentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Start Mastering",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (creditBalance < requiredCredits) "Insufficient Credits ($requiredCredits cr needed)" else "Analyze & Master Master Track",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
