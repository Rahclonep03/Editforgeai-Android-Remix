package com.example.editforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.firebase.UserProfileData
import com.example.editforge.ui.theme.*

@Composable
fun AuthHeaderBadge(
    userProfile: UserProfileData?,
    isSyncing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoggedIn = userProfile != null && !userProfile.isAnonymous
    val isGoogle = userProfile?.provider == "google.com" || userProfile?.provider == "google"
    val displayText = when {
        isLoggedIn -> userProfile?.displayName ?: userProfile?.email?.substringBefore("@") ?: "Producer"
        else -> "Guest · Cloud"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isLoggedIn) Color(0xFF162030) else StudioCardSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isLoggedIn) Color(0xFF388BFD).copy(alpha = 0.5f) else StudioBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("auth_header_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avatar icon
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isGoogle) Color(0xFF4285F4)
                    else if (isLoggedIn) ForgeNeonRed
                    else Color(0xFF3B435B)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isGoogle) {
                Text(
                    text = "G",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            } else {
                Icon(
                    imageVector = if (isLoggedIn) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                    contentDescription = "User Avatar",
                    tint = TextPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (isLoggedIn) TextPrimary else TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )

        // Cloud indicator dot / icon
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isSyncing) ForgeElectricAmber else Color(0xFF22C55E))
        )
    }
}
