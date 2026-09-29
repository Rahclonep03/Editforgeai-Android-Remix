package com.example.editforge.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.supabase.LOVABLE_MCP_TOOLS
import com.example.editforge.ui.components.AuthProfileDialog
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

@Composable
fun SettingsScreen(
    viewModel: EditForgeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val studioColors = StudioTheme.colors

    val currentUser by viewModel?.currentUserState?.collectAsState() ?: remember { mutableStateOf(null) }
    val isSyncing by viewModel?.isCloudSyncing?.collectAsState() ?: remember { mutableStateOf(false) }
    val cloudSyncStateText by viewModel?.cloudSyncStateText?.collectAsState() ?: remember { mutableStateOf("Firestore Connected") }
    val projects by viewModel?.projects?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val authError by viewModel?.authError?.collectAsState() ?: remember { mutableStateOf(null) }
    val isAuthLoading by viewModel?.isAuthLoading?.collectAsState() ?: remember { mutableStateOf(false) }

    val currentPalette by viewModel?.currentThemePalette?.collectAsState() ?: remember { mutableStateOf(StudioThemePalette.CYBER_FORGE) }
    val supabaseConfig by viewModel?.supabaseConfig?.collectAsState() ?: remember { mutableStateOf(null) }
    val supabaseLogs by viewModel?.supabaseLogs?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    var showAuthDialog by remember { mutableStateOf(false) }

    // Supabase inputs state
    var supabaseUrlInput by remember(supabaseConfig) { mutableStateOf(supabaseConfig?.projectUrl ?: "https://editforge-studio.supabase.co") }
    var supabaseKeyInput by remember(supabaseConfig) { mutableStateOf(supabaseConfig?.anonKey ?: "sb-anon-key-placeholder-editforge") }
    var supabaseTokenInput by remember(supabaseConfig) { mutableStateOf(supabaseConfig?.bearerToken ?: "") }
    var connectionTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    if (showAuthDialog && viewModel != null) {
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

    val isLoggedIn = currentUser != null && !currentUser!!.isAnonymous
    val isGoogle = currentUser?.provider == "google.com" || currentUser?.provider == "google"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(studioColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Studio Settings & Branding",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = studioColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customize theme branding palettes, configure Lovable & Supabase cloud sync, and manage studio authentication.",
                style = MaterialTheme.typography.bodyMedium,
                color = studioColors.textSecondary,
                fontSize = 13.sp
            )
        }

        // =================================================================
        // 1. BRANDING COLOR PALETTES (Representing the 3 Logos)
        // =================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(studioColors.surface)
                .border(1.dp, studioColors.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("theme_palette_section")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Palette Icon",
                                tint = studioColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Branding Colour Palette",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = studioColors.textPrimary
                            )
                            Text(
                                text = "Select from official EditForge branding aesthetics",
                                style = MaterialTheme.typography.bodySmall,
                                color = studioColors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(studioColors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentPalette.badgeLabel,
                            color = studioColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Selectable Palette Cards (all StudioThemePalette entries)
                StudioThemePalette.entries.forEach { palette ->
                    val isSelected = currentPalette == palette
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) studioColors.surfaceElevated else studioColors.background
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) palette.primaryColor else studioColors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel?.selectThemePalette(palette)
                            }
                            .padding(14.dp)
                            .testTag("palette_option_${palette.id}")
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
                                // 3 Color Swatch Dots previewing the logo colors
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(palette.primaryColor)
                                            .border(1.5.dp, Color.Black, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(palette.secondaryColor)
                                            .border(1.5.dp, Color.Black, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(palette.backgroundColor)
                                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = palette.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = studioColors.textPrimary
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(palette.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = palette.primaryColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = palette.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = studioColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel?.selectThemePalette(palette) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = palette.primaryColor,
                                    unselectedColor = studioColors.textTertiary
                                )
                            )
                        }
                    }
                }
            }
        }

        // =================================================================
        // 2. LOVABLE & SUPABASE CLOUD INTEGRATION (Option A)
        // =================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(studioColors.surface)
                .border(1.dp, studioColors.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("supabase_integration_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                .background(Color(0xFF3ECF8E).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Supabase Logo",
                                tint = Color(0xFF3ECF8E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lovable & Supabase Cloud",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = studioColors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF143026), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MCP SYNC",
                                        color = Color(0xFF3ECF8E),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Direct connection to your Lovable projects & audio storage bucket",
                                style = MaterialTheme.typography.bodySmall,
                                color = studioColors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Syncs with the 5 MCP tools on route /mcp: list_projects, get_project, list_exports, get_export_download_url (600s signed link), and rename_project.",
                    style = MaterialTheme.typography.bodySmall,
                    color = studioColors.textSecondary,
                    fontSize = 11.sp
                )

                // Supabase Project URL Input
                OutlinedTextField(
                    value = supabaseUrlInput,
                    onValueChange = { supabaseUrlInput = it },
                    label = { Text("Supabase Project URL", fontSize = 11.sp) },
                    placeholder = { Text("https://<project-ref>.supabase.co", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3ECF8E),
                        unfocusedBorderColor = studioColors.border,
                        focusedLabelColor = Color(0xFF3ECF8E),
                        cursorColor = Color(0xFF3ECF8E)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Supabase Anon API Key Input
                OutlinedTextField(
                    value = supabaseKeyInput,
                    onValueChange = { supabaseKeyInput = it },
                    label = { Text("Supabase Anon Public API Key", fontSize = 11.sp) },
                    placeholder = { Text("eyJhbGciOi...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3ECF8E),
                        unfocusedBorderColor = studioColors.border,
                        focusedLabelColor = Color(0xFF3ECF8E),
                        cursorColor = Color(0xFF3ECF8E)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Optional User Bearer Token
                OutlinedTextField(
                    value = supabaseTokenInput,
                    onValueChange = { supabaseTokenInput = it },
                    label = { Text("User Auth Token (Bearer / Optional)", fontSize = 11.sp) },
                    placeholder = { Text("Paste Supabase Auth session token for RLS", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_token_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3ECF8E),
                        unfocusedBorderColor = studioColors.border,
                        focusedLabelColor = Color(0xFF3ECF8E),
                        cursorColor = Color(0xFF3ECF8E)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Connection status message
                AnimatedVisibility(visible = connectionTestStatus != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF14241B), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF3ECF8E).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = connectionTestStatus ?: "",
                            color = Color(0xFF4ADE80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel?.updateSupabaseConfig(supabaseUrlInput, supabaseKeyInput, supabaseTokenInput)
                            isTestingConnection = true
                            viewModel?.testSupabaseConnection { success, message ->
                                isTestingConnection = false
                                connectionTestStatus = message
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3ECF8E), contentColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("save_and_test_supabase_button")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF0F172A), strokeWidth = 2.dp)
                        } else {
                            Text(text = "Connect & Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel?.loadSupabaseDemoPreset()
                            supabaseUrlInput = "https://editforge-studio.supabase.co"
                            supabaseKeyInput = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.editforge-public-anon"
                            supabaseTokenInput = "lovable-oauth-token-rahclonep"
                            connectionTestStatus = "Loaded Lovable Studio preset credentials"
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, studioColors.border),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = "Load Studio Preset", fontSize = 11.sp, color = studioColors.textSecondary)
                    }
                }

                // MCP Tools Table Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(studioColors.background, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Active MCP Tool Handlers",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = studioColors.textPrimary
                    )
                    LOVABLE_MCP_TOOLS.forEach { tool ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "• ${tool.name}", color = studioColors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = tool.actionType, color = studioColors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Live Sync Logs preview
                if (supabaseLogs.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0C12), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = "Lovable Sync Log", color = Color(0xFF3ECF8E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        supabaseLogs.take(4).forEach { logLine ->
                            Text(text = logLine, color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // =================================================================
        // 3. FIREBASE AUTH & USER ACCOUNT
        // =================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(studioColors.surface)
                .border(1.dp, if (isLoggedIn) Color(0xFF2B4C7E) else studioColors.border, RoundedCornerShape(14.dp))
                .padding(16.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isGoogle) Color(0xFF4285F4)
                                    else if (isLoggedIn) studioColors.primary
                                    else Color(0xFF283044)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGoogle) {
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isLoggedIn) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                    contentDescription = "User Avatar",
                                    tint = studioColors.textPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isLoggedIn) {
                                    currentUser?.displayName ?: currentUser?.email ?: "Studio Producer"
                                } else {
                                    "Guest Session (Cloud Sync Enabled)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = studioColors.textPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isLoggedIn) {
                                    currentUser?.email ?: "Google Account Linked"
                                } else {
                                    "Anonymous UID: ${currentUser?.uid?.take(12) ?: "initializing"}..."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = studioColors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showAuthDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoggedIn) studioColors.surfaceElevated else studioColors.primary,
                            contentColor = studioColors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = if (isLoggedIn) BorderStroke(1.dp, studioColors.border) else null,
                        modifier = Modifier.testTag("settings_auth_button")
                    ) {
                        Text(
                            text = if (isLoggedIn) "Account" else "Sign In",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = studioColors.border, thickness = 1.dp)

                SettingItem(
                    label = "Auth Provider",
                    value = if (isGoogle) "Google Sign-In (Firebase Auth)" else if (isLoggedIn) "Email & Password" else "Anonymous Guest",
                    textColor = studioColors.textPrimary
                )
                SettingItem(
                    label = "Studio UID",
                    value = currentUser?.uid?.take(16)?.plus("...") ?: "Not signed in",
                    textColor = studioColors.textPrimary
                )
                SettingItem(
                    label = "Cloud Sync Status",
                    value = cloudSyncStateText,
                    textColor = studioColors.textPrimary
                )

                // Quick Action Sync Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel?.syncAllToCloudNow() },
                        enabled = !isSyncing,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("settings_sync_now_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, studioColors.secondary.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = studioColors.secondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isSyncing) "Syncing..." else "Sync Cloud Now", fontSize = 11.sp)
                    }

                    if (!isLoggedIn) {
                        Button(
                            onClick = { viewModel?.signInWithGoogle(email = "Rahclonep@gmail.com") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("settings_google_signin_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F))
                        ) {
                            Text(text = "Sign in with Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =================================================================
        // 4. RETENTION POLICY & SPECIFICATIONS
        // =================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(studioColors.surface, RoundedCornerShape(12.dp))
                .border(1.dp, studioColors.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Retention Policy",
                        tint = studioColors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Master Audio Retention Policy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = studioColors.textPrimary
                    )
                }
                Text(
                    text = "To maintain cloud storage efficiency while protecting client assets, uncompressed master audio files are stored for 30 days after upload. Musical analysis metadata, structural cue points, and generated deliverables remain permanently available in your studio library.",
                    style = MaterialTheme.typography.bodySmall,
                    color = studioColors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(studioColors.surface, RoundedCornerShape(12.dp))
                .border(1.dp, studioColors.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Audio Specs",
                        tint = studioColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audio Processing Engine Specs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = studioColors.textPrimary
                    )
                }
                SettingItem("Sample Rate Processing", "48.0 kHz / 96.0 kHz native", textColor = studioColors.textPrimary)
                SettingItem("Bit Depth Resolution", "24-bit PCM floating-point precision", textColor = studioColors.textPrimary)
                SettingItem("Target Loudness", "-14.0 LUFS integrated (Streaming Standard)", textColor = studioColors.textPrimary)
                SettingItem("True Peak Limiter", "-1.0 dBFS ceiling threshold", textColor = studioColors.textPrimary)
                SettingItem("Format Compatibility", "WAV, AIFF, FLAC, MP3 (up to 320 kbps)", textColor = studioColors.textPrimary)
            }
        }

        // Ownership guarantee
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(studioColors.surface, RoundedCornerShape(12.dp))
                .border(1.dp, studioColors.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Ownership",
                        tint = StatusSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Artist Ownership Guarantee",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = studioColors.textPrimary
                    )
                }
                Text(
                    text = "You retain 100% ownership, copyright, and publishing rights to all audio masters uploaded and all edits or stem deliverables generated through EditForge AI. EditForge claims zero royalties or publisher shares.",
                    style = MaterialTheme.typography.bodySmall,
                    color = studioColors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SettingItem(label: String, value: String, textColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
