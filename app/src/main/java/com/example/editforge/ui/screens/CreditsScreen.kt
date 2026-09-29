package com.example.editforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editforge.data.model.CreditTransaction
import com.example.editforge.data.model.SubscriptionTier
import com.example.editforge.ui.theme.*
import com.example.editforge.ui.viewmodel.EditForgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreditsScreen(
    viewModel: EditForgeViewModel,
    modifier: Modifier = Modifier
) {
    val creditBalance by viewModel.creditBalance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Balance Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spendable Credits",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .background(ForgeElectricAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Pro Studio Plan",
                                color = ForgeElectricAmber,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$creditBalance",
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 36.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "credits available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Credits renew monthly. Failed renders are automatically refunded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Deliverables Rate Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Studio Deliverables Rate Card",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RateRow(item = "Full Mix Master", cost = "1 credit")
                        RateRow(item = "60s / 30s / 15s Cut-Downs", cost = "2 credits each")
                        RateRow(item = "Alternative Variation Cut", cost = "1 credit")
                        RateRow(item = "Sting (3-6s Bumper)", cost = "1 credit")
                        RateRow(item = "AI Stem Separation (Vocals, Drums, Bass)", cost = "3 credits each")
                        HorizontalDivider(color = StudioBorder)
                        RateRow(item = "Core Bundle (60s + 30s + 15s + Sting)", cost = "5 credits (Save 2cr)", highlight = true)
                    }
                }
            }
        }

        // Subscription Plans
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Subscription Tiers",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                SubscriptionTier.entries.forEach { tier ->
                    val isCurrent = tier == SubscriptionTier.PRO
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) StudioCardSurfaceElevated else StudioCardSurface)
                            .border(
                                1.dp,
                                if (isCurrent) ForgeElectricAmber else StudioBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
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
                                        color = TextPrimary
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(ForgeElectricAmber, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = StudioDarkBackground,
                                                fontSize = 9.sp,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${tier.monthlyCredits} credits / month",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tier.price,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ForgeElectricAmber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.purchaseCredits(tier.monthlyCredits, tier.label)
                                    },
                                    enabled = !isCurrent,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForgeNeonRed,
                                        disabledContainerColor = StudioCardSurfaceElevated,
                                        contentColor = TextPrimary,
                                        disabledContentColor = TextSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = if (isCurrent) "Current" else "Switch", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transaction History Ledger Header
        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No credit activity recorded yet.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            items(transactions) { tx ->
                TransactionRow(tx = tx)
            }
        }
    }
}

@Composable
fun RateRow(item: String, cost: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item,
            style = MaterialTheme.typography.bodySmall,
            color = if (highlight) ForgeElectricAmber else TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = cost,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) ForgeNeonRed else TextPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TransactionRow(tx: CreditTransaction, modifier: Modifier = Modifier) {
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
    val isPositive = tx.amount > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioCardSurface, RoundedCornerShape(10.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.reason,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                if (tx.description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isPositive) "+${tx.amount} cr" else "${tx.amount} cr",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPositive) StatusSuccess else ForgeNeonRed,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bal: ${tx.balanceAfter} cr",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
