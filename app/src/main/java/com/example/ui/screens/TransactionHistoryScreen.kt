package com.example.ui.screens

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
import com.example.data.model.WalletTransaction
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.InvestmentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    viewModel: InvestmentViewModel
) {
    val transactions by viewModel.userTransactions.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(transactions, selectedFilter) {
        if (selectedFilter == "ALL") transactions
        else transactions.filter { it.type == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(
            text = "Wallet Statement & Receipts",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Monitor chronological deposits, active allocations, and withdrawal audits",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filters scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All Logs", "DEPOSIT" to "Deposits", "WITHDRAWAL" to "Cash-Outs", "INVESTMENT" to "Allocations").forEach { (filterVal, label) ->
                val selected = selectedFilter == filterVal
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = filterVal },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("tx_filter_$filterVal")
                )
            }
        }

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1.0f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "No trans",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Receipt History Is Empty",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Initialize wallet funds to trigger transaction ledgers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1.0f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions) { transaction ->
                    TransactionCardItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCardItem(transaction: WalletTransaction) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon representing Type
            val (icon, tint) = when (transaction.type) {
                "DEPOSIT" -> Icons.Outlined.ArrowDownward to PositiveGreen
                "WITHDRAWAL" -> Icons.Outlined.ArrowUpward to Color(0xFFEF4444)
                "INVESTMENT" -> Icons.Outlined.ElectricBolt to MaterialTheme.colorScheme.primary
                "REFERRAL_BONUS" -> Icons.Outlined.CardGiftcard to Color(0xFFF59E0B)
                else -> Icons.Outlined.Savings to PositiveGreen
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = "tx icon", tint = tint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = when (transaction.type) {
                        "DEPOSIT" -> "Capital Loaded (${transaction.gateway})"
                        "WITHDRAWAL" -> "Wallet Cash-Out (${transaction.gateway})"
                        "INVESTMENT" -> "Product Subscription"
                        "REFERRAL_BONUS" -> "Affiliate Direct Credit"
                        "EARNINGS_CLAIM" -> "Holdings Maturity Redeeem"
                        else -> transaction.type
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = "Ref: " + transaction.reference,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = sdf.format(Date(transaction.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Value amount & approval status
            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (transaction.type) {
                    "DEPOSIT" -> "+"
                    "WITHDRAWAL" -> "-"
                    "INVESTMENT" -> "-"
                    "REFERRAL_BONUS" -> "+"
                    else -> "+"
                }
                
                Text(
                    text = "$prefix${String.format(Locale.US, "%,.0f", transaction.amount)} FCFA",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (prefix == "+") PositiveGreen else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                val statusColor = when (transaction.status) {
                    "APPROVED" -> PositiveGreen
                    "PENDING" -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = transaction.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}
