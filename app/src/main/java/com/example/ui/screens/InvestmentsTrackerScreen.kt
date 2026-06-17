package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserInvestment
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.InvestmentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvestmentsTrackerScreen(
    viewModel: InvestmentViewModel
) {
    val investments by viewModel.userInvestments.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(
            text = "Your Investment Holdings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Track active lockers, compounding growth, and lock durations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (investments.isEmpty()) {
            Box(
                modifier = Modifier.weight(1.0f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPasteOff,
                        contentDescription = "Empty portfolio",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Investment Holdings Found",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Funds deposited? Visit the 'Offers' tab to subscribe.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // High-Level Dashboard Calculations for Active Lockups
            val activeInvestments = remember(investments) { investments.filter { it.status == "ACTIVE" } }
            val totalActiveAmount = remember(activeInvestments) { activeInvestments.sumOf { it.amount } }
            val totalActiveEarnings = remember(activeInvestments) { activeInvestments.sumOf { it.projectedEarnings } }
            val averageProgress = remember(activeInvestments) {
                if (activeInvestments.isEmpty()) 0f
                else {
                    val now = System.currentTimeMillis()
                    val totalFrac = activeInvestments.map { holding ->
                        val totalSpan = (holding.lockUntilDate - holding.startDate).toDouble()
                        val elapsedSpan = (now - holding.startDate).toDouble()
                        if (totalSpan <= 0) 1f else (elapsedSpan / totalSpan).coerceIn(0.0..1.0).toFloat()
                    }.sum()
                    totalFrac / activeInvestments.size
                }
            }

            // Interactive/Visual Summary Dashboard Pane
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("lockup_dashboard_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Dashboard Graph icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ACTIVE LOCKUP DASHBOARD",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${activeInvestments.size} ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Key Statistics Grid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Total Locked Capital
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Locked Capital",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%,.0f", totalActiveAmount)} FCFA",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Stat 2: Total Future Harvest/Yields to claim
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Maturity Harvest",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "+${String.format(Locale.getDefault(), "%,.0f", totalActiveEarnings)} FCFA",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PositiveGreen
                                )
                            }
                        }
                    }

                    // Aggregate Progress representation
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aggregate Lockup Progress",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(averageProgress * 100).toInt()}% Completed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Shared visual timeline progress bar
                        LinearProgressIndicator(
                            progress = { averageProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            color = if (averageProgress >= 1.0f) PositiveGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "Based on aggregate duration elapsed across all active lockup profiles.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1.0f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(investments) { holding ->
                    HoldingCardItem(
                        holding = holding,
                        onClaimClick = { viewModel.claimEarnings(holding.id) },
                        onSimulateShift = { viewModel.simulateUnlock(holding.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HoldingCardItem(
    holding: UserInvestment,
    onClaimClick: () -> Unit,
    onSimulateShift: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isLocked = now < holding.lockUntilDate && holding.status == "ACTIVE"
    val isClaimed = holding.status == "CLAIMED"

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val remainingDays = if (holding.status == "CLAIMED") 0 
    else {
        val diffMs = holding.lockUntilDate - now
        if (diffMs <= 0) 0 else (diffMs / (24 * 60 * 60 * 1000) + 1).toInt()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holding_item_${holding.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = holding.productTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Maturity rate +${holding.yieldPercent}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Render Badge
                val badgeColor = when {
                    isClaimed -> MaterialTheme.colorScheme.surfaceVariant
                    isLocked -> Color(0xFFF59E0B)  // Warning Orange
                    else -> PositiveGreen
                }
                val badgeText = when {
                    isClaimed -> "REDEEMED"
                    isLocked -> "LOCKED"
                    else -> "UNLOCKED"
                }

                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invested Principal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${String.format(Locale.US, "%,.0f", holding.amount)} FCFA", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Accrued Yield", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        text = "+${String.format(Locale.US, "%,.0f", holding.projectedEarnings)} FCFA",
                        fontWeight = FontWeight.Bold,
                        color = PositiveGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Maturity Lock", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        text = if (isClaimed) "Redeemed" else "$remainingDays days left",
                        fontWeight = FontWeight.Bold,
                        color = if (remainingDays == 0 && !isClaimed) PositiveGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar mapping time elapsed
            val totalSpan = (holding.lockUntilDate - holding.startDate).toDouble()
            val elapsedSpan = (now - holding.startDate).toDouble()
            val completionFraction = if (isClaimed) 1f 
            else if (totalSpan <= 0) 1f 
            else (elapsedSpan / totalSpan).coerceIn(0.0..1.0).toFloat()

            LinearProgressIndicator(
                progress = { completionFraction },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                color = if (completionFraction >= 1f) PositiveGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Initiated: ${sdf.format(Date(holding.startDate))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "Matures: ${sdf.format(Date(holding.lockUntilDate))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom CTA row (Time Machine simulation + Claim Earnings triggers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLocked) {
                    // Simulation Accelerator CTA
                    OutlinedButton(
                        onClick = onSimulateShift,
                        modifier = Modifier
                            .weight(1.0f)
                            .height(40.dp)
                            .testTag("simulate_leap_button_${holding.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FastForward, contentDescription = "Simulate", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fast Forward 30D", fontSize = 12.sp)
                        }
                    }
                }

                if (holding.status == "ACTIVE" && !isLocked) {
                    Button(
                        onClick = onClaimClick,
                        modifier = Modifier
                            .weight(1.0f)
                            .height(40.dp)
                            .testTag("claim_earnings_button_${holding.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Claim", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claim Principal + Yield", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
