package com.example.editforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.editforge.ui.screens.*
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    STUDIO("Studio", Icons.Default.GraphicEq),
    DEMOS("Demos", Icons.Default.MusicNote),
    BILLING("Subscription", Icons.Default.WorkspacePremium),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: EditForgeViewModel = viewModel()
            val currentPalette by viewModel.currentThemePalette.collectAsState()
            EditForgeTheme(palette = currentPalette) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: EditForgeViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var isUploadingDirectly by remember { mutableStateOf(false) }
    var viewingProjectId by remember { mutableStateOf<String?>(null) }

    val activeProjectId by viewModel.activeProjectId.collectAsState()
    val playingId by viewModel.playingId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    val studioColors = StudioTheme.colors

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = studioColors.background,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            Column {
                // Mini Player Bar if something is playing
                if (playingId != null) {
                    MiniPlayerBar(
                        playingId = playingId!!,
                        isPlaying = isPlaying,
                        progress = playbackProgress,
                        onTogglePlay = {
                            viewModel.togglePlay(playingId!!, 60f)
                        },
                        onSeek = { ratio ->
                            viewModel.seekTo(ratio)
                        }
                    )
                }

                NavigationBar(
                    containerColor = studioColors.surface,
                    contentColor = studioColors.textPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab && !isUploadingDirectly && viewingProjectId == null
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                isUploadingDirectly = false
                                viewingProjectId = null
                                currentTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = studioColors.primary,
                                selectedTextColor = studioColors.primary,
                                unselectedIconColor = studioColors.textSecondary,
                                unselectedTextColor = studioColors.textSecondary,
                                indicatorColor = studioColors.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(studioColors.background)
                .padding(innerPadding)
        ) {
            if (viewingProjectId != null) {
                ProjectDetailScreen(
                    viewModel = viewModel,
                    onBack = { viewingProjectId = null }
                )
            } else if (isUploadingDirectly) {
                UploadScreen(
                    viewModel = viewModel,
                    onAnalysisFinished = { newId ->
                        viewModel.selectProject(newId)
                        isUploadingDirectly = false
                        viewingProjectId = newId
                    }
                )
            } else {
                when (currentTab) {
                    NavigationTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateUpload = { isUploadingDirectly = true },
                        onNavigateStudio = { currentTab = NavigationTab.STUDIO },
                        onNavigateDemos = { currentTab = NavigationTab.DEMOS },
                        onNavigateCredits = { currentTab = NavigationTab.BILLING },
                        onNavigateProject = { id ->
                            viewModel.selectProject(id)
                            viewingProjectId = id
                        }
                    )
                    NavigationTab.STUDIO -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateUpload = { isUploadingDirectly = true },
                        onNavigateProject = { id ->
                            viewModel.selectProject(id)
                            viewingProjectId = id
                        },
                        onNavigateDemos = { currentTab = NavigationTab.DEMOS }
                    )
                    NavigationTab.DEMOS -> DemosScreen(viewModel = viewModel)
                    NavigationTab.BILLING -> SubscriptionScreen(viewModel = viewModel)
                    NavigationTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    playingId: String,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val studioColors = StudioTheme.colors
    val displayTitle = when {
        playingId.contains("neon") -> "Neon Horizons - Studio Master"
        playingId == "edit-60" -> "Demo: 60s Radio Cut"
        playingId == "edit-30" -> "Demo: 30s Hook Cut"
        playingId == "edit-15" -> "Demo: 15s Social Cut"
        else -> "Master Track Playback"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(studioColors.surfaceElevated)
            .border(1.dp, studioColors.border)
            .clickable { onTogglePlay() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Playing",
                        tint = studioColors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 12.sp,
                        color = studioColors.textPrimary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = studioColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = studioColors.primary,
                trackColor = studioColors.border,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
            )
        }
    }
}
