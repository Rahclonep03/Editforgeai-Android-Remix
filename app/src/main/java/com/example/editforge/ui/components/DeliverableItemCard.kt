package com.example.editforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.ExportItem
import com.example.editforge.data.model.JobStatus
import com.example.editforge.ui.theme.*

@Composable
fun DeliverableItemCard(
    export: ExportItem,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onForgeAlt: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioCardSurface, RoundedCornerShape(12.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause Icon Button
            IconButton(
                onClick = onTogglePlay,
                enabled = export.status == JobStatus.COMPLETE,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPlaying) ForgeNeonRed else StudioCardSurfaceElevated)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause ${export.label}" else "Play ${export.label}",
                    tint = if (isPlaying) TextPrimary else ForgeElectricAmber,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = export.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (export.variation > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(ForgeElectricAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Var #${export.variation}",
                                color = ForgeElectricAmber,
                                fontSize = 10.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDuration(export.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    JobStatusBadge(status = export.status)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Alternative Forge Action button
            if (onForgeAlt != null && export.status == JobStatus.COMPLETE) {
                FilledTonalButton(
                    onClick = onForgeAlt,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = StudioCardSurfaceElevated,
                        contentColor = ForgeElectricAmber
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Forge Alternative Variation",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Alt Cut", fontSize = 11.sp)
                }
            }
        }
    }
}

fun formatDuration(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%d:%02d", minutes, secs)
}
