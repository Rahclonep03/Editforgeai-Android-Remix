package com.example.editforge.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

data class StudioTrackPreset(
    val title: String,
    val fileName: String,
    val durationSeconds: Float,
    val sampleRate: String,
    val bitDepth: String,
    val genreHint: String
)

val PRESET_MASTERS = listOf(
    StudioTrackPreset(
        title = "Midnight Pulse - Synthwave Master",
        fileName = "midnight_pulse_master_48k_24b.wav",
        durationSeconds = 214f,
        sampleRate = "48.0 kHz",
        bitDepth = "24-bit PCM",
        genreHint = "Synthwave / Cinematic Electronic"
    ),
    StudioTrackPreset(
        title = "Apex Horizon - Modern Pop Master",
        fileName = "apex_horizon_radio_master_96k.wav",
        durationSeconds = 178f,
        sampleRate = "96.0 kHz",
        bitDepth = "24-bit PCM",
        genreHint = "Pop / Commercial Dance"
    ),
    StudioTrackPreset(
        title = "Cybernetic Groove - Bass & Funk",
        fileName = "cybernetic_groove_uncompressed.wav",
        durationSeconds = 192f,
        sampleRate = "44.1 kHz",
        bitDepth = "16-bit Master",
        genreHint = "Electro Funk / Breakbeat"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: EditForgeViewModel,
    onAnalysisFinished: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisProgress by viewModel.analysisProgress.collectAsState()
    val analysisStageText by viewModel.analysisStageText.collectAsState()
    val creditBalance by viewModel.creditBalance.collectAsState()
    val studioColors = StudioTheme.colors

    var pickedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var pickedFileSize by remember { mutableStateOf<Long?>(null) }

    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }
    var customTrackName by remember { mutableStateOf("") }
    var autoForgeBundle by remember { mutableStateOf(true) }
    var normalizeLufs by remember { mutableStateOf(true) }
    var boundaryDetection by remember { mutableStateOf(true) }

    // Native Android Audio File Picker Launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            pickedAudioUri = selectedUri
            selectedPresetIndex = null
            var name = "device_master.wav"
            var size = 18_400_000L
            try {
                val cursor = context.contentResolver.query(selectedUri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) name = it.getString(nameIndex) ?: name
                        if (sizeIndex != -1) size = it.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                name = selectedUri.lastPathSegment ?: "master_track.wav"
            }
            pickedFileName = name
            pickedFileSize = size
            if (customTrackName.isBlank()) {
                customTrackName = name.substringBeforeLast(".")
            }
        }
    }

    // Default to preset 0 if nothing selected
    val activePreset = if (selectedPresetIndex != null) {
        PRESET_MASTERS[selectedPresetIndex!!]
    } else {
        null
    }

    // 1. ANALYSIS OVERLAY (Active when analyzing)
    if (isAnalyzing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioDarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StudioCardSurface)
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(ForgeNeonRed, ForgeElectricAmber.copy(alpha = 0.5f))),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(24.dp)
                    .testTag("analysis_in_progress_card"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ForgeCrimsonDark),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { analysisProgress },
                        color = ForgeNeonRed,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(62.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Analyzing DSP",
                        tint = ForgeElectricAmber,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Analyzing Master Audio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(analysisProgress * 100).toInt()}% · Musical Phrase & Transient Mapping",
                        style = MaterialTheme.typography.labelMedium,
                        color = ForgeElectricAmber,
                        fontSize = 13.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { analysisProgress },
                    color = ForgeNeonRed,
                    trackColor = StudioCardSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioDarkBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = analysisStageText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Cancel Button so the user is never stuck
                OutlinedButton(
                    onClick = { viewModel.cancelAnalysis() },
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("cancel_analysis_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, StudioBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cancel Analysis", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
        return
    }

    // 2. MAIN UPLOAD & INGESTION FORM
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column {
            Text(
                text = "Ingest Master Track",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Upload high-resolution master audio for automatic musical boundary detection, downbeat alignment, and cut-down generation.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Real File Upload Dropzone Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (pickedFileName != null) Color(0xFF14241B) else StudioCardSurfaceElevated
                )
                .border(
                    1.dp,
                    if (pickedFileName != null) Color(0xFF4ADE80) else ForgeNeonRed.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .clickable { audioPickerLauncher.launch("audio/*") }
                .padding(20.dp)
                .testTag("upload_audio_file_picker_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (pickedFileName != null) Color(0xFF1E3A2B) else ForgeCrimsonDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (pickedFileName != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                        contentDescription = "Upload Audio",
                        tint = if (pickedFileName != null) Color(0xFF4ADE80) else ForgeNeonRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (pickedFileName != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = pickedFileName!!,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.1f", (pickedFileSize ?: 15_000_000L) / (1024f * 1024f))} MB · Audio File Loaded",
                            color = Color(0xFF4ADE80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = studioColors.primaryContainer,
                                contentColor = studioColors.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Change Audio File", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                pickedAudioUri = null
                                pickedFileName = null
                                pickedFileSize = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Clear", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Select Audio Track from Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Supports 24-bit / 16-bit WAV, AIFF, FLAC, and high-bitrate MP3",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgeNeonRed,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp).testTag("browse_device_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Browse Device Audio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Demo Studio Presets Section (Alternative if no file on device)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Or Choose Studio Demo Master",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (pickedFileName != null && selectedPresetIndex == null) {
                    Text(
                        text = "Using Device Audio",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            PRESET_MASTERS.forEachIndexed { index, preset ->
                val isSelected = selectedPresetIndex == index && pickedFileName == null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) StudioCardSurfaceElevated else StudioCardSurface)
                        .border(
                            1.dp,
                            if (isSelected) ForgeNeonRed else StudioBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedPresetIndex = index
                            pickedFileName = null
                            pickedAudioUri = null
                            customTrackName = preset.title
                        }
                        .padding(12.dp)
                        .testTag("preset_track_$index")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedPresetIndex = index
                                pickedFileName = null
                                pickedAudioUri = null
                                customTrackName = preset.title
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = ForgeNeonRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${preset.fileName} · ${preset.sampleRate} · ${preset.bitDepth}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = preset.genreHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeElectricAmber,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Custom Track Name Override
        OutlinedTextField(
            value = customTrackName,
            onValueChange = { customTrackName = it },
            label = { Text("Project Title", fontSize = 12.sp) },
            placeholder = { Text(pickedFileName ?: activePreset?.title ?: "My Master Track") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("track_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForgeNeonRed,
                unfocusedBorderColor = StudioBorder,
                focusedLabelColor = ForgeNeonRed,
                cursorColor = ForgeNeonRed
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // AI Engine Configuration
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Audio Processing Configuration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioCardSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Auto-Forge Core Bundle Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Forge Core Bundle",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Automatically renders 60s radio, 30s hook, 15s clip & sting (5 credits).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoForgeBundle,
                            onCheckedChange = { autoForgeBundle = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = ForgeNeonRed
                            )
                        )
                    }

                    HorizontalDivider(color = StudioBorder)

                    // Target Loudness
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Loudness Normalization",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Aligns integrated loudness to -14.0 LUFS broadcast target.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = normalizeLufs,
                            onCheckedChange = { normalizeLufs = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = ForgeNeonRed
                            )
                        )
                    }

                    HorizontalDivider(color = StudioBorder)

                    // Boundary Detection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Musical Boundary Resolution",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Aligns cuts to natural musical bars without crude fade-outs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = boundaryDetection,
                            onCheckedChange = { boundaryDetection = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = ForgeNeonRed
                            )
                        )
                    }
                }
            }
        }

        // Credit notice & Action Button
        val requiredCredits = if (autoForgeBundle) 5 else 0
        val effectiveTitle = customTrackName.ifBlank {
            pickedFileName?.substringBeforeLast(".") ?: activePreset?.title ?: PRESET_MASTERS[0].title
        }
        val effectiveFileName = pickedFileName ?: activePreset?.fileName ?: PRESET_MASTERS[0].fileName
        val effectiveDuration = if (pickedFileName != null) 195f else (activePreset?.durationSeconds ?: 214f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available: $creditBalance credits",
                style = MaterialTheme.typography.bodyMedium,
                color = if (creditBalance >= requiredCredits) ForgeElectricAmber else StatusFailed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (autoForgeBundle) "Forge cost: 5 credits" else "Analysis: Free",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = {
                viewModel.uploadAndAnalyze(
                    name = effectiveTitle,
                    fileName = effectiveFileName,
                    duration = effectiveDuration,
                    autoForgeBundle = autoForgeBundle
                ) { newProjectId ->
                    onAnalysisFinished(newProjectId)
                }
            },
            enabled = creditBalance >= requiredCredits,
            colors = ButtonDefaults.buttonColors(
                containerColor = ForgeNeonRed,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("begin_analysis_button")
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Start Analysis",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Begin Master Audio Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
