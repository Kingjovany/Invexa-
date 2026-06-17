package com.example.ui.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.data.model.User
import com.example.data.model.UserInvestment
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.InvestmentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: InvestmentViewModel,
    user: User,
    onNavigateToKYC: () -> Unit
) {
    val investments by viewModel.userInvestments.collectAsState()
    val announcements by viewModel.allAnnouncements.collectAsState()

    val listState = rememberLazyListState()
    val parallaxOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset * 0.45f
            } else {
                300f
            }
        }
    }

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // Dynamic computations
    val activeInvestmentsTotal = investments.filter { it.status == "ACTIVE" }.sumOf { it.amount }
    val lockedEarningsTotal = investments.filter { it.status == "ACTIVE" }.sumOf { it.projectedEarnings }
    val claimedEarningsTotal = investments.filter { it.status == "CLAIMED" }.sumOf { it.projectedEarnings }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val width = maxWidth
        val isTablet = width > 600.dp

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KYC Alert
            if (user.verificationStatus == "UNVERIFIED" || user.verificationStatus == "REJECTED") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToKYC() }
                            .testTag("kyc_alert_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = "Alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = "Account Verification Required",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Please submit profile ID verification to browse and invest in active products.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = "KYC Nav",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // High Contrast Welcome Header with Invexa Parallax branding
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Subtle elegant scroll parallax movement
                            translationY = -parallaxOffset * 0.3f
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.0f)
                        ) {
                            // Official Invexa App Icon displaying securely after authentication
                            Card(
                                modifier = Modifier
                                    .size(68.dp)
                                    .graphicsLayer {
                                        // Dynamic interactive float/bounce alignment
                                        translationY = (parallaxOffset * 0.15f)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_icon_invexa_1781579473931),
                                    contentDescription = "Invexa App Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Hello, ${user.fullName}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // User Verification Status Indicator badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (user.verificationStatus == "VERIFIED") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = user.verificationStatus,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.verificationStatus == "VERIFIED") MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Invexa Hub • Live Portfolio Workspace",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Announcement Banner Carousels
            if (announcements.isNotEmpty()) {
                item {
                    AnnouncementBanner(announcement = announcements.first())
                }
            }

            // Wallet Balances & Actions layout (Responsive Grid Class)
            item {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.0f)) {
                            WalletBalanceCard(
                                balance = user.walletBalance,
                                refEarnings = user.referralEarnings,
                                onDepositClick = { showDepositDialog = true },
                                onWithdrawClick = { showWithdrawDialog = true }
                            )
                        }
                        Box(modifier = Modifier.weight(1.0f)) {
                            FintechAnalyticsCard(
                                activeInvested = activeInvestmentsTotal,
                                lockedEarnings = lockedEarningsTotal,
                                totalClaimed = claimedEarningsTotal,
                                activeCount = investments.count { it.status == "ACTIVE" }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WalletBalanceCard(
                            balance = user.walletBalance,
                            refEarnings = user.referralEarnings,
                            onDepositClick = { showDepositDialog = true },
                            onWithdrawClick = { showWithdrawDialog = true }
                        )
                        FintechAnalyticsCard(
                            activeInvested = activeInvestmentsTotal,
                            lockedEarnings = lockedEarningsTotal,
                            totalClaimed = claimedEarningsTotal,
                            activeCount = investments.count { it.status == "ACTIVE" }
                        )
                    }
                }
            }

            // Quick Info & Disclosures
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Policy",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LOCKED EARNINGS POLICY (30 Days)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To preserve platform capital liquidity and ensure absolute compliance, all investment yields remain locked inside secure vaults for exactly thirty (30) days. Earnings unlock instantly on completion of the lockup term, becoming immediately claimable back to your primary mobile wallet or PayPal logs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Interactive Dynamic Yield Forecast chart
            item {
                InteractiveAnalyticsChart(activeInvestmentsTotal = activeInvestmentsTotal)
            }
        }
    }

    // MTN / Orange Money / PayPal deposit modal dialog
    if (showDepositDialog) {
        DepositWalletDialog(
            viewModel = viewModel,
            onDismiss = { showDepositDialog = false }
        )
    }

    // Withdrawal modal dialog
    if (showWithdrawDialog) {
        WithdrawWalletDialog(
            viewModel = viewModel,
            walletBalance = user.walletBalance,
            onDismiss = { showWithdrawDialog = false }
        )
    }
}

@Composable
fun AnnouncementBanner(announcement: Announcement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("announcement_banner"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Campaign,
                        contentDescription = "Speaker",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMMUNITY BULLETIN",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(announcement.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WalletBalanceCard(
    balance: Double,
    refEarnings: Double,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WALLET ACCOUNT",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Icon(
                    imageVector = Icons.Filled.Security,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    contentDescription = "Secure"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${String.format(Locale.US, "%,.0f", balance)} FCFA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.testTag("wallet_amount_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = "Gift",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Referral commissions awarded: ${String.format(Locale.US, "%,.0f", refEarnings)} FCFA",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Deposit Button
                Button(
                    onClick = onDepositClick,
                    modifier = Modifier
                        .weight(1.0f)
                        .height(48.dp)
                        .testTag("deposit_trigger_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AddCard, contentDescription = "Deposit")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Funds", fontWeight = FontWeight.Bold)
                    }
                }

                // Withdraw Button
                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier
                        .weight(1.0f)
                        .height(48.dp)
                        .testTag("withdraw_trigger_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = Stroke(width = 1.dp.value).let { ButtonDefaults.outlinedButtonBorder },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Outbox, contentDescription = "Withdraw")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Withdraw", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FintechAnalyticsCard(
    activeInvested: Double,
    lockedEarnings: Double,
    totalClaimed: Double,
    activeCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_overview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "PORTFOLIO SUMMARY",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Invested Cap & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Capital",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", activeInvested)} FCFA",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Badge(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "$activeCount active packages",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Dynamic Row for locked & claimed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFE59866), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Locked Yield",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", lockedEarnings)} FCFA",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PositiveGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Claimed Yield",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", totalClaimed)} FCFA",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DepositWalletDialog(
    viewModel: InvestmentViewModel,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var sendingAccount by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf("MTN Mobile Money") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Secure Wallet",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Secure Deposit Modal", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Fund your Invexa account securely. Select a provider, enter the amount in FCFA, fill in your sending details, and provide the receipt transition ID.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Gateway Choice
                Column {
                    Text("Choose Payment Provider:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("MTN Mobile Money", "Orange Money", "PayPal").forEach { gateway ->
                            val selected = selectedGateway == gateway
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(
                                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedGateway = gateway }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(gateway) {
                                        "MTN Mobile Money" -> "MTN"
                                        "Orange Money" -> "Orange"
                                        else -> "PayPal"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Reference guidelines based on gateway
                val systemAddress = when (selectedGateway) {
                    "MTN Mobile Money" -> "671551321 (Corporate MTN Momo)"
                    "Orange Money" -> "671551321 (Orange Money)"
                    else -> "payments@invexa-fintech.com (PayPal / Ref: 671551321)"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Instructions:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Send your FCFA fund transfer to: $systemAddress. Ensure saving your transfer reference ID.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount in FCFA") },
                    placeholder = { Text("Min: 5,000 FCFA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("deposit_amount_field"),
                    singleLine = true
                )

                // Number from which the money will be removed ("source number")
                OutlinedTextField(
                    value = sendingAccount,
                    onValueChange = { sendingAccount = it },
                    label = { 
                        Text(
                            if (selectedGateway == "PayPal") "PayPal Email (Source account)" 
                            else "Phone Number (Money removed from)"
                        )
                    },
                    placeholder = { 
                        Text(
                            if (selectedGateway == "PayPal") "youraccount@example.com" 
                            else "+237 6..."
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedGateway == "PayPal") KeyboardType.Email else KeyboardType.Phone
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("deposit_source_number_field"),
                    singleLine = true
                )

                // Reference ID
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Transaction Reference / Receipt ID") },
                    placeholder = { Text("e.g. TXN920188") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("deposit_ref_field"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt >= 5000 && sendingAccount.isNotBlank() && reference.isNotBlank()) {
                        // Secure reference containing sender details and transit id
                        val compoundRef = "Receipt: $reference [Source: $sendingAccount]"
                        viewModel.deposit(amt, selectedGateway, compoundRef)
                        onDismiss()
                    } else if (amt < 5000) {
                        viewModel.deposit(amt, selectedGateway, reference) // Passes to VM to show proper toast message
                    }
                },
                modifier = Modifier.testTag("submit_deposit_button")
            ) {
                Text("Verify & Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WithdrawWalletDialog(
    viewModel: InvestmentViewModel,
    walletBalance: Double,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf("MTN Mobile Money") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Cash-Out", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Request a safe balance withdrawal directly back to your local coordinates. Approvals process within 24 hours.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Gateway Selector
                Column {
                    Text("Payout Destination Gateway:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("MTN Mobile Money", "Orange Money", "PayPal").forEach { gateway ->
                            val selected = selectedGateway == gateway
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(
                                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedGateway = gateway }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(gateway) {
                                        "MTN Mobile Money" -> "MTN"
                                        "Orange Money" -> "Orange"
                                        else -> "PayPal"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Withdraw Amount (FCFA)") },
                    supportingText = { Text("Available: ${String.format(Locale.US, "%,.0f", walletBalance)} FCFA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_field")
                )

                // Coordinates
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = {
                        Text(
                            if (selectedGateway == "PayPal") "PayPal Registered Email"
                            else "Mobile Money Number (with country code)"
                        )
                    },
                    placeholder = { 
                        Text(
                            if (selectedGateway == "PayPal") "youraccount@example.com" 
                            else "+237 6..."
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("withdraw_dest_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && destination.isNotBlank()) {
                        viewModel.withdraw(amt, selectedGateway, destination)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_withdrawal_button")
            ) {
                Text("Request Cash-out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InteractiveAnalyticsChart(activeInvestmentsTotal: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "INTERACTIVE ESTIMATE YIELD FORECAST",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            var selectedForecastMonth by remember { mutableStateOf(6f) } // Default 6 months forecast slider
            val compoundYieldIndex = 0.015f // 1.5% compounding index factor

            val principalText = if (activeInvestmentsTotal > 0) activeInvestmentsTotal else 1000.0
            val forecastEndValue = principalText * Math.pow(1.0 + compoundYieldIndex, selectedForecastMonth.toDouble())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Forecast horizon:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${selectedForecastMonth.toInt()} months projection",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Projected Capital valuation:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", forecastEndValue)} FCFA",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = PositiveGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Chart Canvas Drawing using DrawScope with pure Jetpack Canvas (Compiles instantaneously!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    val w = size.width
                    val h = size.height

                    // Gridlines
                    val numGrid = 3
                    for (i in 0..numGrid) {
                        val verticalPos = h * (i.toFloat() / numGrid)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            start = Offset(0f, verticalPos),
                            end = Offset(w, verticalPos),
                            strokeWidth = 2f
                        )
                    }

                    // Render curve line representing growing interest compounding
                    val path = Path()
                    path.moveTo(0f, h * 0.8f) // Left start coordinate

                    val activePoints = 12
                    val widthStep = w / (activePoints - 1)
                    val curveFactor = selectedForecastMonth / 12f // 0 to 1

                    for (j in 1 until activePoints) {
                        val fraction = j.toFloat() / (activePoints - 1)
                        val x = fraction * w

                        // Exponential growth aesthetic
                        val yFactor = 0.8f - (0.6f * fraction * fraction * curveFactor)
                        val y = h * yFactor
                        path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF0D9488),
                        style = Stroke(width = 6f)
                    )

                    // Draw glowing accent pointer dot at selection state
                    val activeSelectorFraction = selectedForecastMonth / 12f
                    val activeX = activeSelectorFraction * w
                    val activeYRaw = 0.8f - (0.6f * activeSelectorFraction * activeSelectorFraction * curveFactor)
                    val activeY = h * activeYRaw

                    drawCircle(
                        color = Color(0xFF2DD4BF),
                        radius = 12f,
                        center = Offset(activeX, activeY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = selectedForecastMonth,
                onValueChange = { selectedForecastMonth = it },
                valueRange = 1f..12f,
                steps = 11,
                modifier = Modifier.testTag("forecast_horizon_slider")
            )

            Text(
                text = "*Forecast simulations are educational estimates reflecting general past returns of our collective pool index and do not represent guaranteed fixed payoffs.",
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Start
            )
        }
    }
}
