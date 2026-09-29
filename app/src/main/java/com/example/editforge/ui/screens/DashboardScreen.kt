package com.example.editforge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.Project
import com.example.editforge.data.model.ProjectStatus
import com.example.editforge.ui.components.AudioAmplitudeWaveformCanvas
import com.example.editforge.ui.components.AudioUploadMasteringCard
import com.example.editforge.ui.components.AuthHeaderBadge
import com.example.editforge.ui.components.AuthProfileDialog
import com.example.editforge.ui.components.MetricCard
import com.example.editforge.ui.components.StatusBadge
import com.example.editforge.ui.components.formatDuration
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ActiveProjectFilter(val label: String) {
    ALL("All Tracks"),
    ACTIVE("Active Editing"),
    ANALYZING("Ingest / DSP"),
    GENERATING("Stems / Processing"),
    READY("Ready for Cuts"),
    COMPLETE("Mastered")
}

@Composable
fun DashboardScreen(
    viewModel: EditForgeViewModel,
    onNavigateUpload: () -> Unit,
    onNavigateProject: (String) -> Unit,
    onNavigateDemos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val allExports by viewModel.allExports.collectAsState()
    val creditBalance by viewModel.creditBalance.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisProgress by viewModel.analysisProgress.collectAsState()
    val analysisStageText by viewModel.analysisStageText.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val isFetchingFirestore by viewModel.isFetchingFirestore.collectAsState()
    val firestoreLastSyncTime by viewModel.firestoreLastSyncTime.collectAsState()
    val cloudSyncStateText by viewModel.cloudSyncStateText.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val playingId by viewModel.playingId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    val studioColors = StudioTheme.colors

    var showAuthDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ActiveProjectFilter.ALL) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var showQuickUploader by remember { mutableStateOf(false) }
    var showNewCloudProjectDialog by remember { mutableStateOf(false) }

    // Spinning animation for refresh button
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

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
            onSyncNow = { viewModel.fetchActiveProjectsFromFirestore(force = true) },
            onClearError = { viewModel.clearAuthError() }
        )
    }

    // Delete Confirmation Dialog
    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = {
                Text(
                    text = "Delete Audio Project?",
                    fontWeight = FontWeight.Bold,
                    color = studioColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${projectToDelete?.projectName}\" from your studio and Firestore cloud storage?",
                    color = studioColors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDelete?.let { viewModel.deleteProject(it.id) }
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = studioColors.primary,
                        contentColor = studioColors.onPrimary
                    )
                ) {
                    Text("Delete Project")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = studioColors.textSecondary)
                }
            },
            containerColor = studioColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Quick New Cloud Audio Project Dialog
    if (showNewCloudProjectDialog) {
        var trackName by remember { mutableStateOf("Cybernetic Groove - Studio Master") }
        var audioFileName by remember { mutableStateOf("Cybernetic Groove.wav") }
        var selectedStatus by remember { mutableStateOf(ProjectStatus.ANALYZING) }

        AlertDialog(
            onDismissRequest = { showNewCloudProjectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = studioColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Cloud Audio Project",
                        fontWeight = FontWeight.Bold,
                        color = studioColors.textPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Creates and registers an active audio session directly in Firestore for real-time cut-down forging.",
                        fontSize = 12.sp,
                        color = studioColors.textSecondary
                    )
                    OutlinedTextField(
                        value = trackName,
                        onValueChange = { trackName = it },
                        label = { Text("Track Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = studioColors.primary,
                            unfocusedBorderColor = studioColors.border,
                            focusedTextColor = studioColors.textPrimary,
                            unfocusedTextColor = studioColors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = audioFileName,
                        onValueChange = { audioFileName = it },
                        label = { Text("Master Audio File") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = studioColors.primary,
                            unfocusedBorderColor = studioColors.border,
                            focusedTextColor = studioColors.textPrimary,
                            unfocusedTextColor = studioColors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (trackName.isNotBlank()) {
                            viewModel.createCloudAudioProject(
                                name = trackName.trim(),
                                fileName = audioFileName.trim(),
                                duration = 195f,
                                status = selectedStatus
                            )
                        }
                        showNewCloudProjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = studioColors.primary,
                        contentColor = studioColors.onPrimary
                    )
                ) {
                    Text("Ingest to Firestore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewCloudProjectDialog = false }) {
                    Text("Cancel", color = studioColors.textSecondary)
                }
            },
            containerColor = studioColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Filter projects based on search query and active status
    val filteredProjects = remember(projects, searchQuery, selectedFilter) {
        projects.filter { project ->
            val matchesSearch = searchQuery.isBlank() ||
                    project.projectName.contains(searchQuery, ignoreCase = true) ||
                    project.fileName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                ActiveProjectFilter.ALL -> true
                ActiveProjectFilter.ACTIVE -> project.status == ProjectStatus.ANALYZING ||
                        project.status == ProjectStatus.GENERATING ||
                        project.status == ProjectStatus.ANALYZED
                ActiveProjectFilter.ANALYZING -> project.status == ProjectStatus.ANALYZING || project.status == ProjectStatus.UPLOADED
                ActiveProjectFilter.GENERATING -> project.status == ProjectStatus.GENERATING
                ActiveProjectFilter.READY -> project.status == ProjectStatus.ANALYZED
                ActiveProjectFilter.COMPLETE -> project.status == ProjectStatus.COMPLETE
            }

            matchesSearch && matchesFilter
        }
    }

    val activeEditingCount = projects.count {
        it.status == ProjectStatus.ANALYZING ||
                it.status == ProjectStatus.GENERATING ||
                it.status == ProjectStatus.ANALYZED
    }
    val readyCount = projects.count { it.status == ProjectStatus.COMPLETE }
    val isAnySyncing = isFetchingFirestore || isCloudSyncing

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(studioColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Studio Dashboard Hero Header & Cloud Connection Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                studioColors.primaryContainer.copy(alpha = 0.45f),
                                studioColors.surface,
                                studioColors.background
                            )
                        )
                    )
                    .border(1.dp, studioColors.border, RoundedCornerShape(16.dp))
                    .padding(18.dp)
                    .testTag("dashboard_hero_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Top Row: Title + Firestore Live Status & Auth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Live pulsing dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isAnySyncing) studioColors.secondary else studioColors.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAnySyncing) "FETCHING FIRESTORE..." else "FIRESTORE CLOUD ACTIVE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isAnySyncing) studioColors.secondary else studioColors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Audio Editing Projects",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = studioColors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Firestore Re-fetch button with spinning feedback
                            IconButton(
                                onClick = { viewModel.fetchActiveProjectsFromFirestore(force = true) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(studioColors.surfaceElevated)
                                    .border(1.dp, studioColors.border, CircleShape)
                                    .testTag("firestore_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Fetch latest from Firestore",
                                    tint = if (isAnySyncing) studioColors.primary else studioColors.textPrimary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(if (isAnySyncing) spinAngle else 0f)
                                )
                            }

                            AuthHeaderBadge(
                                userProfile = currentUser,
                                isSyncing = isAnySyncing,
                                onClick = { showAuthDialog = true }
                            )
                        }
                    }

                    // Firestore Status Strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(studioColors.surfaceElevated.copy(alpha = 0.7f))
                            .border(1.dp, studioColors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Cloud Source",
                                    tint = studioColors.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAnySyncing) "Syncing with Google Firestore..." else "${projects.size} active tracks synced with Firestore",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studioColors.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val lastSyncStr = remember(firestoreLastSyncTime) {
                                firestoreLastSyncTime?.let {
                                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it))
                                } ?: "Live"
                            }
                            Text(
                                text = "Updated $lastSyncStr",
                                fontSize = 10.sp,
                                color = studioColors.textTertiary
                            )
                        }
                    }

                    // Action CTAs: Ingest Master Track & Quick Ingest to Cloud
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateUpload,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = studioColors.primary,
                                contentColor = studioColors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("dashboard_upload_master_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Upload Master Track",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { showNewCloudProjectDialog = true },
                            border = BorderStroke(1.dp, studioColors.border),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = studioColors.surface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_quick_add_cloud_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = studioColors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Track",
                                color = studioColors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "Active Projects",
                    value = "${projects.size}",
                    icon = Icons.Default.GraphicEq,
                    modifier = Modifier.weight(1f),
                    subValue = "$activeEditingCount in editing"
                )
                MetricCard(
                    label = "Rendered Cuts",
                    value = "${allExports.size}",
                    icon = Icons.Default.ContentCut,
                    modifier = Modifier.weight(1f),
                    subValue = "WAV / MP3"
                )
                MetricCard(
                    label = "Studio Credits",
                    value = "$creditBalance",
                    icon = Icons.Default.Token,
                    modifier = Modifier.weight(1f),
                    subValue = "Monthly Pool"
                )
            }
        }

        // 3. Quick Audio Upload / Mastering Workbench Toggle
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(studioColors.surface)
                    .border(1.dp, studioColors.border, RoundedCornerShape(12.dp))
                    .clickable { showQuickUploader = !showQuickUploader }
                    .padding(14.dp)
                    .testTag("dashboard_quick_upload_toggle")
            ) {
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
                                .background(studioColors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (showQuickUploader) Icons.Default.ExpandLess else Icons.Default.Bolt,
                                contentDescription = "Quick Audio Ingest",
                                tint = studioColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Quick Master & Cut-Down Workbench",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = studioColors.textPrimary
                            )
                            Text(
                                text = if (showQuickUploader) "Tap to collapse quick workbench" else "Ingest master audio directly & sync to Firestore",
                                style = MaterialTheme.typography.bodySmall,
                                color = studioColors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showQuickUploader) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = studioColors.textSecondary
                    )
                }
            }
        }

        // Embedded Quick Mastering Tool (when expanded)
        if (showQuickUploader) {
            item {
                AudioUploadMasteringCard(
                    creditBalance = creditBalance,
                    isAnalyzing = isAnalyzing,
                    analysisProgress = analysisProgress,
                    analysisStageText = analysisStageText,
                    onStartMastering = { name, fileName, duration, autoBundle, _ ->
                        viewModel.uploadAndAnalyze(name, fileName, duration, autoBundle) { newId ->
                            viewModel.selectProject(newId)
                            onNavigateProject(newId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. Showcase Demos Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(studioColors.surfaceElevated)
                    .border(1.dp, studioColors.border, RoundedCornerShape(12.dp))
                    .clickable { onNavigateDemos() }
                    .padding(14.dp)
                    .testTag("dashboard_demos_promo_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(studioColors.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Audio Demos",
                            tint = studioColors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audition Broadcast Cut-Down Demos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = studioColors.textPrimary
                        )
                        Text(
                            text = "A/B compare 60s radio cuts, 30s hooks, 15s reels, and Demucs stems.",
                            style = MaterialTheme.typography.bodySmall,
                            color = studioColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Demos",
                        tint = studioColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 5. Search Bar & Status Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search active projects or audio files...",
                            color = studioColors.textTertiary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = studioColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = studioColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = studioColors.primary,
                        unfocusedBorderColor = studioColors.border,
                        focusedContainerColor = studioColors.surface,
                        unfocusedContainerColor = studioColors.surface,
                        focusedTextColor = studioColors.textPrimary,
                        unfocusedTextColor = studioColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input")
                )

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ActiveProjectFilter.entries) { filter ->
                        val isSelected = selectedFilter == filter
                        val count = when (filter) {
                            ActiveProjectFilter.ALL -> projects.size
                            ActiveProjectFilter.ACTIVE -> activeEditingCount
                            ActiveProjectFilter.ANALYZING -> projects.count { it.status == ProjectStatus.ANALYZING || it.status == ProjectStatus.UPLOADED }
                            ActiveProjectFilter.GENERATING -> projects.count { it.status == ProjectStatus.GENERATING }
                            ActiveProjectFilter.READY -> projects.count { it.status == ProjectStatus.ANALYZED }
                            ActiveProjectFilter.COMPLETE -> readyCount
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = studioColors.primaryContainer,
                                selectedLabelColor = studioColors.onPrimaryContainer,
                                containerColor = studioColors.surface,
                                labelColor = studioColors.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) studioColors.primary else studioColors.border
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 6. Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Active Firestore Projects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = studioColors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(studioColors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${filteredProjects.size} of ${projects.size}",
                            color = studioColors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (filteredProjects.isNotEmpty()) {
                    Text(
                        text = "Tap to open studio",
                        style = MaterialTheme.typography.bodySmall,
                        color = studioColors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 7. Active Audio Editing Project Cards
        if (filteredProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(studioColors.surface)
                        .border(1.dp, studioColors.border, RoundedCornerShape(14.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.CloudQueue,
                            contentDescription = "No projects",
                            tint = studioColors.textTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching audio tracks" else "No active projects in Firestore",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = studioColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Try changing your search terms or filter selection."
                            else
                                "Fetch your audio tracks from Firestore cloud storage or ingest a master WAV/MP3 track to begin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = studioColors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.fetchActiveProjectsFromFirestore(force = true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = studioColors.secondary,
                                    contentColor = studioColors.onSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync from Firestore")
                            }

                            Button(
                                onClick = onNavigateUpload,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = studioColors.primary,
                                    contentColor = studioColors.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Master Track")
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredProjects, key = { it.id }) { project ->
                val projectExports = allExports.filter { it.projectId == project.id }
                val isCurrentPlaying = playingId == project.id && isPlaying

                ActiveAudioProjectCard(
                    project = project,
                    exportsCount = projectExports.size,
                    isPlaying = isCurrentPlaying,
                    progress = if (playingId == project.id) playbackProgress else 0f,
                    onPlayToggle = { viewModel.togglePlay(project.id, project.duration) },
                    onOpen = { onNavigateProject(project.id) },
                    onDelete = { projectToDelete = project }
                )
            }
        }
    }
}

/**
 * Rich, responsive card representing an active audio editing project fetched from Firestore:
 * - Title & original filename
 * - Active editing pipeline status (Ingesting, Analyzing, Demucs GPU Stem Separation, Deliverables Ready)
 * - Firestore Cloud Sync pill badge
 * - Musical specs (Duration, File Size, Retention countdown, Cuts count)
 * - Real-time playback waveform preview
 * - Quick Studio CTA and Delete action
 */
@Composable
fun ActiveAudioProjectCard(
    project: Project,
    exportsCount: Int,
    isPlaying: Boolean,
    progress: Float,
    onPlayToggle: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studioColors = StudioTheme.colors
    val dateStr = remember(project.createdAt) {
        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(project.createdAt))
    }

    val isActiveEditing = project.status == ProjectStatus.ANALYZING ||
            project.status == ProjectStatus.GENERATING ||
            project.status == ProjectStatus.UPLOADED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .testTag("audio_project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = studioColors.surface),
        border = BorderStroke(
            width = if (isPlaying) 1.5.dp else if (isActiveEditing) 1.2.dp else 1.dp,
            color = if (isPlaying) studioColors.primary else if (isActiveEditing) studioColors.secondary.copy(alpha = 0.8f) else studioColors.border
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header: Icon, Title, Filename & Firestore Cloud Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPlaying) studioColors.primaryContainer else studioColors.surfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isPlaying) studioColors.primary else studioColors.border,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (project.status) {
                                ProjectStatus.ANALYZING -> Icons.Default.HourglassTop
                                ProjectStatus.GENERATING -> Icons.Default.Autorenew
                                else -> if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Audiotrack
                            },
                            contentDescription = "Track Icon",
                            tint = if (isPlaying) studioColors.primary else if (isActiveEditing) studioColors.secondary else studioColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = project.projectName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = studioColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = project.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = studioColors.textTertiary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Firestore Cloud Badge
                    Box(
                        modifier = Modifier
                            .background(studioColors.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .border(1.dp, studioColors.primary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Firestore Cloud Synced",
                                tint = studioColors.primary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Firestore",
                                color = studioColors.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Project Status Badge
                    StatusBadge(status = project.status)
                }
            }

            // 2. Active Audio Editing Pipeline Status Indicator (when processing/analyzing)
            if (isActiveEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(studioColors.surfaceElevated)
                        .border(1.dp, studioColors.secondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = studioColors.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (project.status) {
                                    ProjectStatus.ANALYZING -> "Audio DSP: Transient & Key Profile Analysis"
                                    ProjectStatus.GENERATING -> "Active Editing: GPU Stem Extraction & Cut-Downs"
                                    else -> "Ingesting Master Track..."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = studioColors.secondary
                            )
                        }

                        Text(
                            text = "In Progress",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = studioColors.secondary
                        )
                    }
                }
            }

            // 3. Audio Metadata Chips: Duration, File Size, Created Date, Deliverable Cuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration Tag
                Box(
                    modifier = Modifier
                        .background(studioColors.surfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, studioColors.border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Duration",
                            tint = studioColors.secondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDuration(project.duration),
                            color = studioColors.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // File Size Tag
                Box(
                    modifier = Modifier
                        .background(studioColors.surfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, studioColors.border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = formatFileSize(project.fileSize),
                        color = studioColors.textSecondary,
                        fontSize = 10.sp
                    )
                }

                // Exports Count Tag
                Box(
                    modifier = Modifier
                        .background(studioColors.surfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, studioColors.border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Cuts",
                            tint = studioColors.primary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$exportsCount cuts ready",
                            color = studioColors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                if (project.coreBundleUnlocked) {
                    Box(
                        modifier = Modifier
                            .background(studioColors.secondary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "BUNDLE",
                            color = studioColors.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Audio Playback Progress Bar (Visible when playing)
            AnimatedVisibility(visible = isPlaying) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = studioColors.primary,
                        trackColor = studioColors.border,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auditioning studio master preview",
                            fontSize = 10.sp,
                            color = studioColors.primary
                        )
                        Text(
                            text = formatDuration(project.duration * progress),
                            fontSize = 10.sp,
                            color = studioColors.textSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = studioColors.border, thickness = 0.8.dp)

            // 5. Card Bottom Actions: Timestamp, Preview Player Button, Open Studio & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = studioColors.textTertiary,
                    fontSize = 10.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Preview playback button
                    IconButton(
                        onClick = onPlayToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) studioColors.primaryContainer else studioColors.surfaceElevated)
                            .testTag("play_preview_button_${project.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                            tint = if (isPlaying) studioColors.primary else studioColors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete project button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(studioColors.surfaceElevated)
                            .testTag("delete_project_button_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Project",
                            tint = studioColors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Open Project Studio CTA Button
                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = studioColors.primary,
                            contentColor = studioColors.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_project_button_${project.id}")
                    ) {
                        Text(
                            text = "Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Utility helper to format file size in MB
 */
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "15.0 MB"
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}
