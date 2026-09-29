package com.example.editforge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.editforge.data.model.StructureSection
import com.example.editforge.ui.theme.*

@Composable
fun WaveformView(
    peaks: List<Float>,
    progress: Float, // 0.0f .. 1.0f
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sections: List<StructureSection> = emptyList(),
    totalDuration: Float = 60f,
    activeColor: Color = ForgeNeonRed,
    inactiveColor: Color = ForgeWaveformDim.copy(alpha = 0.6f),
    cursorColor: Color = Color.Unspecified
) {
    val effectiveCursorColor = if (cursorColor != Color.Unspecified) cursorColor else ForgeElectricAmber
    val amberHighlight = ForgeElectricAmber

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(100.dp)) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(ratio)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek(ratio)
                    }
                }
        ) {
            val totalBars = peaks.size.coerceAtLeast(32)
            val barSpacing = 3.dp.toPx()
            val totalBarWidth = (size.width - ((totalBars - 1) * barSpacing)) / totalBars
            val barWidth = totalBarWidth.coerceAtLeast(2.dp.toPx())

            // Draw background sections tint if provided
            if (sections.isNotEmpty() && totalDuration > 0) {
                sections.forEach { section ->
                    val startX = (section.start / totalDuration) * size.width
                    val endX = (section.end / totalDuration) * size.width
                    val sectionColor = when (section.label.lowercase()) {
                        "intro" -> Color(0xFFFBBF24).copy(alpha = 0.12f)
                        "verse" -> Color(0xFF38BDF8).copy(alpha = 0.12f)
                        "chorus" -> Color(0xFFFF3B47).copy(alpha = 0.18f)
                        "bridge" -> Color(0xFF818CF8).copy(alpha = 0.12f)
                        "outro" -> Color(0xFF10B981).copy(alpha = 0.12f)
                        else -> Color.White.copy(alpha = 0.08f)
                    }
                    drawRect(
                        color = sectionColor,
                        topLeft = Offset(startX, 0f),
                        size = Size((endX - startX).coerceAtLeast(4f), size.height)
                    )
                }
            }

            // Draw center baseline
            val centerY = size.height / 2f

            peaks.forEachIndexed { index, peakValue ->
                val x = index * (barWidth + barSpacing)
                val barProgress = x / size.width
                val isPlayed = barProgress <= progress

                val clampedPeak = peakValue.coerceIn(0.12f, 0.98f)
                val barHeight = clampedPeak * (size.height * 0.85f)
                val topY = centerY - (barHeight / 2f)

                val color = if (isPlayed) {
                    Brush.verticalGradient(
                        colors = listOf(amberHighlight, activeColor),
                        startY = topY,
                        endY = topY + barHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(inactiveColor, inactiveColor.copy(alpha = 0.3f)),
                        startY = topY,
                        endY = topY + barHeight
                    )
                }

                drawRoundRect(
                    brush = color,
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // Draw active cursor playhead
            val cursorX = progress * size.width
            drawLine(
                color = effectiveCursorColor,
                start = Offset(cursorX, 0f),
                end = Offset(cursorX, size.height),
                strokeWidth = 2.5.dp.toPx()
            )

            // Draw playhead head circle
            drawCircle(
                color = effectiveCursorColor,
                radius = 5.dp.toPx(),
                center = Offset(cursorX, 6.dp.toPx())
            )
        }
    }
}
