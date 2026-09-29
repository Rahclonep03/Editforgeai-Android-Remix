package com.example.editforge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.Project
import com.example.editforge.ui.components.AuthHeaderBadge
import com.example.editforge.ui.components.AuthProfileDialog
import com.example.editforge.ui.components.formatDuration
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

@Composable
fun HomeScreen(
    viewModel: EditForgeViewModel,
    onNavigateUpload: () -> Unit,
    onNavigateStudio: () -> Unit,
    onNavigateDemos: () -> Unit,
    onNavigateCredits: () -> Unit,
    onNavigateProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val allExports by viewModel.allExports.collectAsState()
    val creditBalance by viewModel.creditBalance.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val cloudSyncStateText by viewModel.cloudSyncStateText.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val playingId by viewModel.playingId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val studioColors = StudioTheme.colors

    var showAuthDialog by remember { mutableStateOf(false) }

    if (showAuthDialog) {
        AuthProfileDialog(
            userProfile = currentUser,
            isLoading = isAuthLoading,
            errorMessage = authError,
            projectsCount = projects.size,
            cloudSyncState = cloudSyncStateText,
            onDismiss = { showAuthDialog = false },
            onSignInWithGoogle = { email -> viewModel.signInWithGoogle(email = email) },
            onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onCreateAccountWithEmail = { email, pass -> viewModel.createAccountWithEmail(email, pass) },
            onSignOut = { viewModel.signOut() },
            onSyncNow = { viewModel.syncAllToCloudNow() },
            onClearError = { viewModel.clearAuthError() }
        )
    }

    val greetingName = when {
        currentUser != null && !currentUser!!.isAnonymous ->
            currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Producer"
        else -> "Producer"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {

        // 1. Welcome Header with Brand & Auth Status Badge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForgeCrimsonDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "EditForge AI Logo",
                            tint = ForgeNeonRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EDITFORGE AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(ForgeNeonRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "STUDIO",
                                    color = ForgeNeonRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Welcome back, $greetingName",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                AuthHeaderBadge(
                    userProfile = currentUser,
                    isSyncing = isCloudSyncing,
                    onClick = { showAuthDialog = true }
                )
            }
        }

        // 2. Main Hero Welcome Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                studioColors.primaryContainer.copy(alpha = 0.5f),
                                studioColors.surfaceElevated,
                                studioColors.surface
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(studioColors.primary.copy(alpha = 0.6f), studioColors.border, studioColors.secondary.copy(alpha = 0.4f))
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(20.dp)
                    .testTag("home_hero_banner")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(ForgeElectricAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AI MASTERING & STRUCTURE CUTS",
                                color = ForgeElectricAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Engine Online",
                                fontSize = 11.sp,
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "From Full Master to Multi-Format Cuts in Seconds",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            lineHeight = 26.sp
                        )
                        Text(
                            text = "Automatically forge 60s radio edits, 30s hook-first cuts, 15s vertical clips, and branded stings that resolve at natural musical phrases without fade-outs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateUpload,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_start_project_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgeNeonRed,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "New Master", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateStudio,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_open_studio_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ForgeElectricAmber.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgeElectricAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Studio",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Open Studio", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Live Balance & Billing Quick Status Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
                    .clickable { onNavigateCredits() }
                    .testTag("home_billing_quick_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ForgeElectricAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Credits",
                                tint = ForgeElectricAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$creditBalance Credits Available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF282218), RoundedCornerShape(4.dp))
                                        .border(1.dp, ForgeElectricAmber.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Pro Studio Plan",
                                        fontSize = 10.sp,
                                        color = ForgeElectricAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Monthly allocation active · Tap to manage billing & packages",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open billing",
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Quick Action Tiles Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Studio Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Master Audio",
                        subtitle = "Ingest WAV & AIFF",
                        icon = Icons.Default.GraphicEq,
                        accentColor = ForgeNeonRed,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateUpload
                    )
                    QuickActionTile(
                        title = "Cut-Down Demos",
                        subtitle = "Cybernetic Groove · 2:30",
                        icon = Icons.Default.MusicNote,
                        accentColor = ForgeWaveformViolet,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateDemos
                    )
                    QuickActionTile(
                        title = "Studio Hub",
                        subtitle = "${projects.size} Master Tracks",
                        icon = Icons.Default.Folder,
                        accentColor = ForgeElectricAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateStudio
                    )
                }
            }
        }

        // 5. Recent Master Projects Showcase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Master Tracks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "View All (${projects.size})",
                        color = ForgeElectricAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateStudio() }
                    )
                }

                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCardSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No projects yet. Ingest your first master track above!",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    projects.take(3).forEach { project ->
                        HomeProjectItemCard(
                            project = project,
                            isPlaying = isPlaying && playingId == project.id,
                            onPlayToggle = {
                                viewModel.togglePlay(project.id, project.duration)
                            },
                            onSelect = { onNavigateProject(project.id) }
                        )
                    }
                }
            }
        }

        // 6. "How Billing Works" Comprehensive Explainer Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(studioColors.surface)
                    .border(1.dp, studioColors.border, RoundedCornerShape(16.dp))
                    .padding(18.dp)
                    .testTag("home_billing_explainer_section")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(studioColors.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Billing Info",
                                    tint = studioColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "How Billing & Credits Work",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = studioColors.textPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Synced across Web & Mobile via Paddle & Supabase",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studioColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        TextButton(onClick = onNavigateCredits) {
                            Text(
                                text = "Rates",
                                color = studioColors.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = studioColors.border)

                    // 4 Key Billing Points matching Webapp
                    BillingRuleRow(
                        icon = Icons.Default.Layers,
                        title = "Pay Per Rendered Master",
                        description = "1 cr for Full Mix, 2 cr for 60s/30s/15s edits, 3 cr for 4-stems, or 5 cr for complete Core Bundle."
                    )
                    BillingRuleRow(
                        icon = Icons.Default.Autorenew,
                        title = "Monthly Membership Allocation",
                        description = "Plus (50 cr / $11.99), Pro (150 cr / $21.99), or Premier (500 cr / $79.99). Renews each cycle with web sync."
                    )
                    BillingRuleRow(
                        icon = Icons.Default.AddShoppingCart,
                        title = "Member-Only Top-Up Pack",
                        description = "100 extra credits for $9.99 exclusively for active paying members with instant Paddle checkout."
                    )
                    BillingRuleRow(
                        icon = Icons.Default.Shield,
                        title = "100% Failure Protection",
                        description = "If an analysis or DSP render fails, credits are automatically refunded immediately."
                    )

                    Button(
                        onClick = onNavigateCredits,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = studioColors.primary,
                            contentColor = studioColors.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "View Plans & Top-Up Credit Balance",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 7. Core DSP Capabilities Tour Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "EditForge Neural Audio Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )

                    CapabilityRow("Musical Boundary Detection", "Finds natural downbeats and bars so short cuts end musically.")
                    CapabilityRow("LUFS Target Matching", "Integrated -14.0 LUFS (AES TD1004) for streaming & broadcast.")
                    CapabilityRow("Demucs 4-Stem GPU Split", "Extracts clean vocals, drums, bass, and musical accompaniment.")
                    CapabilityRow("Cloud Firestore Sync", "Projects backed up securely to Firebase with offline cache.")
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StudioCardSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HomeProjectItemCard(
    project: Project,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioCardSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onPlayToggle,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPlaying) ForgeNeonRed else ForgeCrimsonDark)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause preview",
                        tint = if (isPlaying) TextPrimary else ForgeElectricAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = project.projectName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatDuration(project.duration)} · ${project.fileName} · ${project.status.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open project",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun BillingRuleRow(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B2838)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF8AB4F8),
                modifier = Modifier.size(14.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun CapabilityRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = ForgeElectricAmber,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
