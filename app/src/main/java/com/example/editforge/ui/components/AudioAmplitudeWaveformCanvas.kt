package com.example.editforge.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class WaveformRenderStyle(val label: String) {
    MIRRORED_AMPLITUDE("Mirrored"),
    UPRIGHT_BARS("Upright"),
    FREQUENCY_ENVELOPE("Envelope")
}

@Composable
fun AudioAmplitudeWaveformCanvas(
    peaks: List<Float>,
    totalDurationSeconds: Float,
    audioTitle: String,
    modifier: Modifier = Modifier,
    initialPlayheadProgress: Float = 0.0f,
    onPlayheadSeek: (Float) -> Unit = {},
    externalIsPlaying: Boolean? = null,
    onPlay: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null
) {
    var playheadProgress by remember(audioTitle) { mutableStateOf(initialPlayheadProgress) }
    var internalIsPlaying by remember(audioTitle) { mutableStateOf(false) }
    var hasStarted by remember(audioTitle) { mutableStateOf(false) }
    var renderStyle by remember { mutableStateOf(WaveformRenderStyle.MIRRORED_AMPLITUDE) }
    var isLooping by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    val isPlaying = externalIsPlaying ?: internalIsPlaying

    // Safe tone generator for auditory playback preview feedback
    val toneGen = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Throwable) {
            null
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGen?.release()
            } catch (e: Throwable) { }
        }
    }

    LaunchedEffect(isPlaying, isMuted) {
        if (isPlaying && !isMuted) {
            while (isPlaying) {
                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
                } catch (e: Throwable) { }
                delay(400L)
            }
        }
    }

    // Glow pulse animation for when audio is playing
    val pulseAnim = remember { Animatable(0.4f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            pulseAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(0.4f)
        }
    }

    // Playhead auto-advance timer when playing
    LaunchedEffect(isPlaying, playheadProgress, totalDurationSeconds) {
        if (isPlaying) {
            val stepTimeMs = 40L
            val stepProgress = (stepTimeMs / 1000f) / totalDurationSeconds.coerceAtLeast(1f)
            while (isPlaying) {
                delay(stepTimeMs)
                val next = playheadProgress + stepProgress
                if (next >= 1.0f) {
                    if (isLooping) {
                        playheadProgress = 0.0f
                    } else {
                        playheadProgress = 1.0f
                        internalIsPlaying = false
                    }
                    onPlayheadSeek(playheadProgress)
                } else {
                    playheadProgress = next
                    onPlayheadSeek(next)
                }
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()

    // Derived acoustic metrics from peaks
    val avgRms = remember(peaks) {
        val mean = peaks.map { it * it }.average().toFloat()
        val db = -20f + (mean * 12f)
        String.format("%.1f LUFS", db.coerceIn(-24f, -7f))
    }
    val peakDb = remember(peaks) {
        val maxPeak = peaks.maxOrNull() ?: 0.95f
        val db = -0.1f - ((1f - maxPeak) * 3f)
        String.format("%.1f dBFS", db)
    }

    val playbackStatus = when {
        isPlaying -> "PLAYING"
        hasStarted -> "PAUSED"
        else -> "STOPPED"
    }

    val handlePlay: () -> Unit = {
        if (playheadProgress >= 0.99f) {
            playheadProgress = 0f
            onPlayheadSeek(0f)
        }
        internalIsPlaying = true
        hasStarted = true
        onPlay?.invoke()
    }

    val handlePause: () -> Unit = {
        internalIsPlaying = false
        onPause?.invoke()
    }

    val handleStop: () -> Unit = {
        internalIsPlaying = false
        hasStarted = false
        playheadProgress = 0f
        onPlayheadSeek(0f)
        onStop?.invoke()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCardSurfaceElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("audio_amplitude_waveform_container")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Header Row: Waveform Style Selector & Audio Amplitude Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Waveform Analysis",
                        tint = ForgeElectricAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Amplitude Waveform",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Style switch pills
                    Row(
                        modifier = Modifier
                            .background(StudioCardSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        WaveformRenderStyle.entries.forEach { style ->
                            val selected = renderStyle == style
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (selected) ForgeCrimsonDark else Color.Transparent)
                                    .clickable { renderStyle = style }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = style.label,
                                    fontSize = 9.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) ForgeNeonRed else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Metering Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(StudioCardSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PK: $peakDb",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForgeElectricAmber,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(StudioCardSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RMS: $avgRms",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForgeWaveformViolet,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Interactive Canvas Waveform Area
            val waveformPrimary = StudioTheme.colors.primary
            val waveformSecondary = StudioTheme.colors.secondary

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F111A))
                    .border(1.dp, Color(0xFF1F2436), RoundedCornerShape(10.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(peaks, totalDurationSeconds) {
                            detectTapGestures { offset ->
                                val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                                playheadProgress = ratio
                                onPlayheadSeek(ratio)
                            }
                        }
                        .pointerInput(peaks, totalDurationSeconds) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                                playheadProgress = ratio
                                onPlayheadSeek(ratio)
                            }
                        }
                        .testTag("waveform_canvas_area")
                ) {
                    drawAmplitudeCanvas(
                        peaks = peaks,
                        progress = playheadProgress,
                        totalDuration = totalDurationSeconds,
                        style = renderStyle,
                        pulseFactor = if (isPlaying) pulseAnim.value else 0.5f,
                        textMeasurer = textMeasurer,
                        primaryColor = waveformPrimary,
                        secondaryColor = waveformSecondary
                    )
                }
            }

            // Transport Control & Playhead Timestamp Bar with Play, Pause, Stop
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioCardSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: Playback State Status Badge + Time Counters
                val currentSeconds = (playheadProgress * totalDurationSeconds).toInt()
                val totalSeconds = totalDurationSeconds.toInt()
                val remainingSeconds = (totalSeconds - currentSeconds).coerceAtLeast(0)
                val currentFormatted = String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60)
                val totalFormatted = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
                val remainingFormatted = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                when (playbackStatus) {
                                    "PLAYING" -> ForgeCrimsonDark
                                    "PAUSED" -> Color(0xFF2E2413)
                                    else -> Color(0xFF1B2030)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                when (playbackStatus) {
                                    "PLAYING" -> ForgeNeonRed
                                    "PAUSED" -> ForgeElectricAmber
                                    else -> StudioBorder
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("playback_status_badge")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when (playbackStatus) {
                                        "PLAYING" -> ForgeNeonRed
                                        "PAUSED" -> ForgeElectricAmber
                                        else -> TextTertiary
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PREVIEW: $playbackStatus",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = when (playbackStatus) {
                                "PLAYING" -> ForgeNeonRed
                                "PAUSED" -> ForgeElectricAmber
                                else -> TextSecondary
                            }
                        )
                    }

                    // Time indicators: Elapsed / Total & Remaining
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentFormatted,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForgeElectricAmber,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "/",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = totalFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "(-$remainingFormatted)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Interactive Seek Slider
                Slider(
                    value = playheadProgress,
                    onValueChange = {
                        playheadProgress = it
                        onPlayheadSeek(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .testTag("audio_preview_seek_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = ForgeElectricAmber,
                        activeTrackColor = ForgeNeonRed,
                        inactiveTrackColor = StudioBorder
                    )
                )

                // Primary Control Buttons Row: Rewind, Play, Pause, Stop, Fast Forward, Loop & Mute
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            val newProgress = (playheadProgress - (10f / totalDurationSeconds.coerceAtLeast(1f))).coerceAtLeast(0f)
                            playheadProgress = newProgress
                            onPlayheadSeek(newProgress)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("dashboard_rewind_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10 seconds",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // PLAY BUTTON
                    Button(
                        onClick = handlePlay,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("dashboard_play_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) ForgeNeonRed else ForgeCrimsonDark,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play preview audio",
                            tint = if (isPlaying) TextPrimary else ForgeElectricAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Play",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // PAUSE BUTTON
                    Button(
                        onClick = handlePause,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("dashboard_pause_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (playbackStatus == "PAUSED") Color(0xFF3B2F17) else StudioCardSurfaceElevated,
                            contentColor = if (playbackStatus == "PAUSED") ForgeElectricAmber else TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        border = if (playbackStatus == "PAUSED") BorderStroke(1.dp, ForgeElectricAmber) else BorderStroke(1.dp, StudioBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause preview audio",
                            tint = if (playbackStatus == "PAUSED") ForgeElectricAmber else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pause",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    // STOP BUTTON
                    Button(
                        onClick = handleStop,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("dashboard_stop_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (playbackStatus == "STOPPED") Color(0xFF1B2030) else StudioCardSurfaceElevated,
                            contentColor = if (playbackStatus == "STOPPED") TextSecondary else TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        border = BorderStroke(1.dp, StudioBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop preview audio",
                            tint = if (playbackStatus == "STOPPED") TextTertiary else ForgeNeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stop",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            val newProgress = (playheadProgress + (10f / totalDurationSeconds.coerceAtLeast(1f))).coerceAtMost(1f)
                            playheadProgress = newProgress
                            onPlayheadSeek(newProgress)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("dashboard_forward_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 10 seconds",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Loop Toggle
                    IconButton(
                        onClick = { isLooping = !isLooping },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("dashboard_loop_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop preview",
                            tint = if (isLooping) ForgeElectricAmber else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Mute / Volume Monitor Toggle
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("dashboard_mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute preview audio" else "Mute preview audio",
                            tint = if (isMuted) TextTertiary else ForgeElectricAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawAmplitudeCanvas(
    peaks: List<Float>,
    progress: Float,
    totalDuration: Float,
    style: WaveformRenderStyle,
    pulseFactor: Float,
    textMeasurer: TextMeasurer,
    primaryColor: Color,
    secondaryColor: Color
) {
    val width = size.width
    val height = size.height
    val centerY = height / 2f

    // 1. Draw subtle background horizontal dB reference grid lines
    val gridLines = listOf(0.2f, 0.5f, 0.8f)
    gridLines.forEach { relY ->
        val y = height * relY
        drawLine(
            color = Color(0xFF181C2B),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }

    // 2. Draw vertical time subdivisions (every 30 seconds or 4 subdivisions)
    val subdivisions = 4
    for (i in 1 until subdivisions) {
        val x = (width / subdivisions) * i
        val timeAtMarker = (totalDuration / subdivisions) * i
        val markerLabel = String.format("%d:%02d", (timeAtMarker / 60).toInt(), (timeAtMarker % 60).toInt())

        drawLine(
            color = Color(0xFF1B2030),
            start = Offset(x, 0f),
            end = Offset(x, height - 14f),
            strokeWidth = 1f
        )

        drawText(
            textMeasurer = textMeasurer,
            text = markerLabel,
            topLeft = Offset(x + 4f, height - 14f),
            style = TextStyle(
                color = Color(0xFF64748B),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        )
    }

    // 3. Draw Center Zero-Crossing Baseline
    drawLine(
        color = Color(0xFF23293D),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 1.2f
    )

    // 4. Calculate Bar Geometry
    val effectivePeaks = if (peaks.isEmpty()) List(48) { 0.5f } else peaks
    val barCount = effectivePeaks.size
    val spacingPx = 2.5f
    val barWidth = ((width - ((barCount - 1) * spacingPx)) / barCount).coerceAtLeast(2f)

    // Active cursor X coordinate
    val cursorX = progress * width

    // 5. Draw Amplitude Bars
    effectivePeaks.forEachIndexed { i, peakValue ->
        val x = i * (barWidth + spacingPx)
        val isPlayed = (x + barWidth) <= cursorX
        val clampedPeak = peakValue.coerceIn(0.08f, 0.98f)

        when (style) {
            WaveformRenderStyle.MIRRORED_AMPLITUDE -> {
                // Symmetrical top and bottom amplitude excursions around center line
                val halfBarHeight = (clampedPeak * (centerY * 0.88f)).coerceAtLeast(3f)
                val topY = centerY - halfBarHeight
                val totalBarHeight = halfBarHeight * 2f

                val barBrush = if (isPlayed) {
                    val glowShift = sin((x / width * 3.1415f) + pulseFactor).toFloat() * 0.1f
                    Brush.verticalGradient(
                        colors = listOf(
                            secondaryColor,
                            primaryColor,
                            primaryColor,
                            secondaryColor
                        ),
                        startY = topY,
                        endY = topY + totalBarHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF38405B),
                            Color(0xFF22283A),
                            Color(0xFF22283A),
                            Color(0xFF38405B)
                        ),
                        startY = topY,
                        endY = topY + totalBarHeight
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, totalBarHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            WaveformRenderStyle.UPRIGHT_BARS -> {
                // Ground-up bar chart style from bottom
                val barHeight = (clampedPeak * (height * 0.78f)).coerceAtLeast(4f)
                val topY = height - 16f - barHeight

                val barBrush = if (isPlayed) {
                    Brush.verticalGradient(
                        colors = listOf(secondaryColor, primaryColor),
                        startY = topY,
                        endY = height - 16f
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF434C68), Color(0xFF1E2333)),
                        startY = topY,
                        endY = height - 16f
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            WaveformRenderStyle.FREQUENCY_ENVELOPE -> {
                // Envelope style: dual complementary excursions
                val upperHeight = (clampedPeak * (centerY * 0.90f)).coerceAtLeast(3f)
                val lowerHeight = (clampedPeak * 0.65f * (centerY * 0.85f)).coerceAtLeast(2f)

                val barBrush = if (isPlayed) {
                    Brush.verticalGradient(
                        colors = listOf(secondaryColor, primaryColor),
                        startY = centerY - upperHeight,
                        endY = centerY + lowerHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF4C5677), Color(0xFF1A1F2C)),
                        startY = centerY - upperHeight,
                        endY = centerY + lowerHeight
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(x, centerY - upperHeight),
                    size = Size(barWidth, upperHeight + lowerHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }

    // 6. Draw Glowing Needle Cursor Playhead
    // Background glow shadow
    drawLine(
        color = secondaryColor.copy(alpha = 0.35f * pulseFactor),
        start = Offset(cursorX, 0f),
        end = Offset(cursorX, height),
        strokeWidth = 6.dp.toPx()
    )
    // Sharp needle
    drawLine(
        color = secondaryColor,
        start = Offset(cursorX, 0f),
        end = Offset(cursorX, height),
        strokeWidth = 2.dp.toPx()
    )

    // Playhead top triangle marker
    val markerSize = 6.dp.toPx()
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(cursorX - markerSize, 0f)
        lineTo(cursorX + markerSize, 0f)
        lineTo(cursorX, markerSize * 1.5f)
        close()
    }
    drawPath(path = path, color = secondaryColor)
}
