package com.example.editforge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.editforge.data.firebase.UserProfileData
import com.example.editforge.ui.theme.*

@Composable
fun AuthProfileDialog(
    userProfile: UserProfileData?,
    isLoading: Boolean,
    errorMessage: String?,
    projectsCount: Int,
    cloudSyncState: String,
    onDismiss: () -> Unit,
    onSignInWithGoogle: (email: String) -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onCreateAccountWithEmail: (email: String, pass: String) -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    onClearError: () -> Unit
) {
    var emailInput by remember { mutableStateOf(userProfile?.email ?: "Rahclonep@gmail.com") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }

    val isGoogleUser = userProfile?.provider == "google.com" || userProfile?.provider == "google"
    val isLoggedIn = userProfile != null && !userProfile.isAnonymous

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("auth_profile_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(ForgeNeonRed.copy(alpha = 0.5f), StudioBorder)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
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
                                .background(ForgeCrimsonDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLoggedIn) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                contentDescription = "Auth Status",
                                tint = ForgeNeonRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isLoggedIn) "Account & Cloud Sync" else "Sign In to EditForge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Firebase Auth & Cloud Firestore Persistence",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_auth_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Error Message Banner if any
                AnimatedVisibility(visible = errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF33161C), RoundedCornerShape(8.dp))
                            .border(1.dp, ForgeNeonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = ForgeNeonRed,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onClearError,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear error",
                                    tint = ForgeNeonRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                if (isLoggedIn) {
                    // Profile Overview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioCardSurfaceElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isGoogleUser) Color(0xFF1A385C) else ForgeCrimsonDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (userProfile?.displayName?.take(1) ?: userProfile?.email?.take(1) ?: "U").uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userProfile?.displayName ?: "Studio Producer",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Provider badge
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isGoogleUser) Color(0xFF1E3A5F) else StudioCardSurface,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(1.dp, if (isGoogleUser) Color(0xFF4285F4) else StudioBorder, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isGoogleUser) "Google" else "Email",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isGoogleUser) Color(0xFF8AB4F8) else TextSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = userProfile?.email ?: "No email associated",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            HorizontalDivider(color = StudioBorder, thickness = 1.dp)

                            // Cloud Sync Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Cloud Persistence",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF22C55E))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cloudSyncState,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4ADE80),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Synced Projects",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "$projectsCount master tracks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ForgeElectricAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Cloud Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSyncNow,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("sync_cloud_now_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeCrimsonDark, contentColor = ForgeNeonRed),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ForgeNeonRed.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sync",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSignOut,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("auth_sign_out_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCardSurfaceElevated, contentColor = TextSecondary),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, StudioBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sign Out", fontSize = 12.sp)
                        }
                    }
                } else {
                    // Sign In / Sign Up Form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Direct 1-Tap Google Sign-In Card for Identified User
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF132238))
                                .border(1.dp, Color(0xFF28548A), RoundedCornerShape(12.dp))
                                .clickable { onSignInWithGoogle("Rahclonep@gmail.com") }
                                .padding(12.dp)
                                .testTag("one_tap_google_sign_in_rahclonep")
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
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4285F4)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Sign in as Rahclonep",
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF1E3A5F), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "1-TAP",
                                                    color = Color(0xFF8AB4F8),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Rahclonep@gmail.com · Google Account",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "1-Tap sign in",
                                    tint = Color(0xFF8AB4F8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Primary Google Sign In Button
                        Button(
                            onClick = {
                                val email = if (emailInput.isNotBlank() && emailInput.contains("@")) emailInput.trim() else "Rahclonep@gmail.com"
                                onSignInWithGoogle(email)
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("google_sign_in_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF1F1F1F),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Icon",
                                    tint = Color(0xFF4285F4),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (emailInput.isNotBlank() && emailInput.contains("@")) "Continue as ${emailInput.trim()} (Google)" else "Continue with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Divider OR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = StudioBorder)
                            Text(
                                text = "  OR WITH EMAIL  ",
                                fontSize = 10.sp,
                                color = TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = StudioBorder)
                        }

                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForgeNeonRed,
                                unfocusedBorderColor = StudioBorder,
                                focusedLabelColor = ForgeNeonRed,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password", fontSize = 12.sp) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForgeNeonRed,
                                unfocusedBorderColor = StudioBorder,
                                focusedLabelColor = ForgeNeonRed,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Submit Button
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                    if (isSignUpMode) {
                                        onCreateAccountWithEmail(emailInput.trim(), passwordInput.trim())
                                    } else {
                                        onSignInWithEmail(emailInput.trim(), passwordInput.trim())
                                    }
                                }
                            },
                            enabled = !isLoading && emailInput.isNotBlank() && passwordInput.length >= 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("auth_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgeNeonRed,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Sign In with Email",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Toggle Mode Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSignUpMode) "Already have an account?" else "Don't have an account?",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSignUpMode) "Sign In" else "Create One",
                                color = ForgeElectricAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { isSignUpMode = !isSignUpMode }
                                    .testTag("auth_mode_toggle")
                            )
                        }
                    }
                }

                // Cloud Backend Information Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Firebase Project",
                                tint = ForgeElectricAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Architecture Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ForgeElectricAmber,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Connected to Firebase project: editforge-studio-auth\nReal-time Cloud Firestore synchronization with offline Room cache.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
