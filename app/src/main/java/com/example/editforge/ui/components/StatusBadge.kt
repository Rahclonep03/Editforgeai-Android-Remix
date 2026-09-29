package com.example.editforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.JobStatus
import com.example.editforge.data.model.ProjectStatus
import com.example.editforge.ui.theme.*

@Composable
fun StatusBadge(
    status: ProjectStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        ProjectStatus.COMPLETE -> Triple(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            StatusSuccess.copy(alpha = 0.4f)
        )
        ProjectStatus.ANALYZING, ProjectStatus.GENERATING -> Triple(
            StatusProcessing.copy(alpha = 0.15f),
            StatusProcessing,
            StatusProcessing.copy(alpha = 0.4f)
        )
        ProjectStatus.ANALYZED -> Triple(
            ForgeElectricAmber.copy(alpha = 0.15f),
            ForgeElectricAmber,
            ForgeElectricAmber.copy(alpha = 0.4f)
        )
        ProjectStatus.UPLOADED -> Triple(
            TextSecondary.copy(alpha = 0.15f),
            TextSecondary,
            TextSecondary.copy(alpha = 0.4f)
        )
        ProjectStatus.FAILED -> Triple(
            StatusFailed.copy(alpha = 0.15f),
            StatusFailed,
            StatusFailed.copy(alpha = 0.4f)
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun JobStatusBadge(
    status: JobStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        JobStatus.COMPLETE -> Triple(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            StatusSuccess.copy(alpha = 0.4f)
        )
        JobStatus.PROCESSING -> Triple(
            StatusProcessing.copy(alpha = 0.15f),
            StatusProcessing,
            StatusProcessing.copy(alpha = 0.4f)
        )
        JobStatus.QUEUED -> Triple(
            ForgeElectricAmber.copy(alpha = 0.15f),
            ForgeElectricAmber,
            ForgeElectricAmber.copy(alpha = 0.4f)
        )
        JobStatus.FAILED -> Triple(
            StatusFailed.copy(alpha = 0.15f),
            StatusFailed,
            StatusFailed.copy(alpha = 0.4f)
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
