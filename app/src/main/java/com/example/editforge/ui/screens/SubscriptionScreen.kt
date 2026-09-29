package com.example.editforge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.editforge.data.model.SubscriptionTier
import com.example.editforge.data.paddle.BillingCycle
import com.example.editforge.data.paddle.PaddleInvoice
import com.example.editforge.data.paddle.SubscriptionStatus
import com.example.editforge.ui.components.PaddleCheckoutDialog
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel

@Composable
fun SubscriptionScreen(
    viewModel: EditForgeViewModel,
    modifier: Modifier = Modifier
) {
    val creditBalance by viewModel.creditBalance.collectAsState()
    val paddleInfo by viewModel.paddleSubscription.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    val studioColors = StudioTheme.colors

    var selectedCycle by remember { mutableStateOf(paddleInfo.cycle) }
    var checkoutTargetTier by remember { mutableStateOf<SubscriptionTier?>(null) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    // Paddle Checkout Modal Dialog
    if (checkoutTargetTier != null) {
        PaddleCheckoutDialog(
            targetTier = checkoutTargetTier!!,
            billingCycle = selectedCycle,
            onDismiss = { checkoutTargetTier = null },
            onConfirmUpgrade = { cardLast4 ->
                viewModel.upgradeWithPaddle(checkoutTargetTier!!, selectedCycle, cardLast4)
                checkoutTargetTier = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(studioColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {

        // 1. Header with Paddle MoR Verification Badge
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Membership & Billing",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = studioColors.textPrimary
                        )
                        Text(
                            text = "Professional Audio Processing & Render Credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = studioColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Paddle Verified Merchant Badge
                    Box(
                        modifier = Modifier
                            .background(studioColors.surfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, studioColors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Paddle Verified",
                                tint = studioColors.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paddle MoR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = studioColors.secondary
                            )
                        }
                    }
                }
            }
        }

        // 2. Current User Membership Status Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(studioColors.primaryContainer.copy(alpha = 0.5f), studioColors.surface, studioColors.surfaceElevated)
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(studioColors.primary.copy(alpha = 0.6f), studioColors.secondary.copy(alpha = 0.4f))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp)
                    .testTag("current_membership_card")
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
                                    .background(ForgeCrimsonDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Membership",
                                    tint = ForgeElectricAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${paddleInfo.tier.label} Plan",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (paddleInfo.status == SubscriptionStatus.ACTIVE) Color(0xFF1B3D2B) else Color(0xFF3B2024),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = paddleInfo.status.label.uppercase(),
                                            color = if (paddleInfo.status == SubscriptionStatus.ACTIVE) Color(0xFF4ADE80) else ForgeNeonRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Customer: ${currentUser?.email ?: paddleInfo.customerEmail}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Next cycle amount
                        Text(
                            text = paddleInfo.lastChargeAmount,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ForgeElectricAmber
                        )
                    }

                    HorizontalDivider(color = StudioBorder, thickness = 1.dp)

                    // Available Credits Metric & Renewal Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Credits",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$creditBalance",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "/ ${paddleInfo.tier.monthlyCredits} quota",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Next Renewal Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = paddleInfo.nextBillingDate,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Payment method summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = paddleInfo.paymentMethodSummary,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (paddleInfo.status == SubscriptionStatus.ACTIVE) {
                            Text(
                                text = "Cancel Subscription",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { showCancelConfirm = true }
                                    .testTag("subscription_cancel_button")
                            )
                        } else {
                            Text(
                                text = "Resume Subscription",
                                color = Color(0xFF4ADE80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.resumePaddleSubscription() }
                                    .testTag("subscription_resume_button")
                            )
                        }
                    }
                }
            }
        }

        // Cancel Confirmation Dialog if clicked
        if (showCancelConfirm) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF33161C), RoundedCornerShape(12.dp))
                        .border(1.dp, ForgeNeonRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Cancel Your Subscription?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForgeNeonRed
                        )
                        Text(
                            text = "You will retain access to your remaining $creditBalance credits until ${paddleInfo.nextBillingDate}. You can reactivate anytime.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.cancelPaddleSubscription()
                                    showCancelConfirm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeCrimsonDark, contentColor = ForgeNeonRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Confirm Cancellation", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { showCancelConfirm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Keep Plan", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Billing Interval Selector (Monthly vs Annual - 20% discount)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Upgrade Audio Processing Tiers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioCardSurfaceElevated)
                        .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BillingCycle.entries.forEach { cycle ->
                        val isSelected = selectedCycle == cycle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForgeCrimsonDark else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) ForgeNeonRed else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCycle = cycle }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cycle.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Upgrade Options Cards (All 4 Tiers)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SubscriptionTier.entries.forEach { tier ->
                    val isCurrent = tier == paddleInfo.tier
                    val displayPrice = if (selectedCycle == BillingCycle.ANNUAL) {
                        when (tier) {
                            SubscriptionTier.FREE -> "$0/mo"
                            SubscriptionTier.PLUS -> "$9.59/mo"
                            SubscriptionTier.PRO -> "$17.59/mo"
                            SubscriptionTier.PREMIER -> "$63.99/mo"
                        }
                    } else {
                        tier.price
                    }

                    val features = when (tier) {
                        SubscriptionTier.FREE -> listOf(
                            "5 spendable credits per month",
                            "Basic 128 kbps MP3 export",
                            "7-day master audio retention"
                        )
                        SubscriptionTier.PLUS -> listOf(
                            "50 spendable credits per month",
                            "60s, 30s & 15s musical cut-downs",
                            "24-bit uncompressed WAV masters",
                            "30-day master audio retention"
                        )
                        SubscriptionTier.PRO -> listOf(
                            "150 spendable credits per month",
                            "Demucs 4-Stem GPU audio separation",
                            "Core Deliverable Bundle auto-generation",
                            "AES -14.0 LUFS target loudness matching",
                            "Unlimited alternative cut variations"
                        )
                        SubscriptionTier.PREMIER -> listOf(
                            "500 spendable credits per month",
                            "Dedicated priority GPU render queue",
                            "Multi-track batch mastering",
                            "Custom loudness & boundary cue points",
                            "Direct studio engineer API access"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) StudioCardSurfaceElevated else StudioCardSurface)
                            .border(
                                1.dp,
                                if (isCurrent) ForgeElectricAmber else StudioBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp)
                            .testTag("tier_card_${tier.name.lowercase()}")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tier.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 16.sp
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(ForgeElectricAmber, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "CURRENT PLAN",
                                                    color = StudioDarkBackground,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${tier.monthlyCredits} credits / month",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ForgeElectricAmber,
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = displayPrice,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (selectedCycle == BillingCycle.ANNUAL && tier != SubscriptionTier.FREE) {
                                        Text(
                                            text = "billed annually",
                                            fontSize = 9.sp,
                                            color = TextTertiary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = StudioBorder, thickness = 1.dp)

                            // Features List
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                features.forEach { feat ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isCurrent) ForgeElectricAmber else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = feat,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isCurrent) TextPrimary else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Upgrade button
                            if (isCurrent) {
                                OutlinedButton(
                                    onClick = { },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, StudioBorder)
                                ) {
                                    Text(text = "Active Plan", color = TextTertiary, fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { checkoutTargetTier = tier },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("upgrade_button_${tier.name.lowercase()}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForgeNeonRed,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Upgrade with Paddle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Payment Gateway & Web App Sync Architecture
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(studioColors.surface)
                    .border(1.dp, studioColors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("payment_gateway_sync_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    imageVector = Icons.Default.SyncAlt,
                                    contentDescription = "Sync Gateway",
                                    tint = studioColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Web & Mobile Payment Gateway",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = studioColors.textPrimary
                                )
                                Text(
                                    text = "Paddle Merchant of Record (MoR) Integration",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studioColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(studioColors.secondary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "CLOUD SYNC",
                                color = studioColors.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Can the webapp payment gateway (Paddle) be used on Android? Yes! Paddle provides secure hosted checkout links (Cards, PayPal, Apple Pay). Purchases on web or Android immediately sync your credits and tier in real-time through your Supabase account. (For Play Store releases, digital items consumed inside the app also support Google Play Billing).",
                        style = MaterialTheme.typography.bodySmall,
                        color = studioColors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.syncAllToCloudNow() },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, studioColors.border)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = studioColors.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sync with Webapp",
                                color = studioColors.textPrimary,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { checkoutTargetTier = SubscriptionTier.PLUS },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = studioColors.primary,
                                contentColor = studioColors.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Paddle Checkout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. On-Demand Credit Booster Pack (Matches Webapp: 100 credits for $9.99, paying members only)
        item {
            val isPayingMember = paddleInfo.tier != SubscriptionTier.FREE
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "On-Demand Credit Booster Pack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = studioColors.textPrimary
                        )
                        Text(
                            text = "Matches web app: 100 credits for $9.99 (Paying members only)",
                            style = MaterialTheme.typography.bodySmall,
                            color = studioColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (!isPayingMember) {
                        Box(
                            modifier = Modifier
                                .background(studioColors.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MEMBERS ONLY",
                                color = studioColors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!isPayingMember) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(studioColors.surface)
                            .border(1.dp, studioColors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = studioColors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Credit Top-Ups Reserved for Paying Members",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = studioColors.textPrimary
                                )
                                Text(
                                    text = "To maintain GPU render capacity, credit packs (100 credits / $9.99) are exclusive to Plus, Pro, and Premier members. Upgrade above to unlock top-ups.",
                                    fontSize = 11.sp,
                                    color = studioColors.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(studioColors.surface)
                            .border(1.dp, studioColors.secondary, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                            .testTag("credit_pack_100_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+100 Studio Credits",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = studioColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(studioColors.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "POPULAR",
                                            color = studioColors.secondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Instant allocation · Render up to 50 additional cutdowns",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studioColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.purchaseCreditsWithPaddle(100, "100 Credits Pack", "$9.99")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = studioColors.secondary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("buy_pack_100")
                            ) {
                                Text(
                                    text = "$9.99",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Paddle Billing Invoices & Receipts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Paddle Billing History & Receipts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioCardSurface)
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        paddleInfo.invoices.forEach { invoice ->
                            InvoiceRow(invoice = invoice)
                            if (invoice != paddleInfo.invoices.last()) {
                                HorizontalDivider(color = StudioBorder)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreditPackCard(
    amount: Int,
    price: String,
    label: String,
    isPopular: Boolean = false,
    modifier: Modifier = Modifier,
    onBuy: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPopular) StudioCardSurfaceElevated else StudioCardSurface)
            .border(
                1.dp,
                if (isPopular) ForgeNeonRed else StudioBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (isPopular) ForgeNeonRed.copy(alpha = 0.2f) else StudioCardSurface,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    color = if (isPopular) ForgeNeonRed else TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "+$amount",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 22.sp
            )
            Text(
                text = "credits",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Button(
                onClick = onBuy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("buy_pack_${amount}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) ForgeNeonRed else Color(0xFF1E3A5F),
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = price, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InvoiceRow(invoice: PaddleInvoice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = invoice.planName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = "${invoice.date} · ${invoice.invoiceId}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = invoice.amountFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF143020), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = invoice.status.uppercase(),
                    color = Color(0xFF4ADE80),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
