package com.example.ui.screens

import java.util.Locale

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentProduct
import com.example.data.model.User
import com.example.ui.viewmodel.InvestmentViewModel

@Composable
fun ProductsScreen(
    viewModel: InvestmentViewModel,
    user: User
) {
    val products by viewModel.allProducts.collectAsState()
    var selectedProductForInvestment by remember { mutableStateOf<InvestmentProduct?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Explore Investment Yields",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Vetted real-world assets & infrastructure programs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Compliance warning
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Risk Check",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Disclaimer: Principal & yield are subject to natural asset cycles. Valuations are not fixed-guarantees. High-yield products carry high volatility risks.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }

            // Product grid (auto-adapts dynamically to compact vs tablet layout widths!)
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1.0f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sourcing live investment packages...", fontSize = 12.sp)
                    }
                }
            } else {
                BoxWithConstraints(modifier = Modifier.weight(1.0f).fillMaxWidth()) {
                    val cols = if (maxWidth > 600.dp) 2 else 1
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(cols),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(products) { product ->
                            ProductCardItem(
                                product = product,
                                userVerified = user.verificationStatus == "VERIFIED",
                                onInvestTrigger = { selectedProductForInvestment = product }
                            )
                        }
                    }
                }
            }
        }

        // Investment execution Modal sheet / Popover Dialog
        if (selectedProductForInvestment != null) {
            InvestSubscriptionDialog(
                product = selectedProductForInvestment!!,
                walletBalance = user.walletBalance,
                onDismiss = { selectedProductForInvestment = null },
                onSubmitInvestment = { amount ->
                    viewModel.invest(selectedProductForInvestment!!.id, amount)
                    selectedProductForInvestment = null
                }
            )
        }
    }
}

@Composable
fun ProductCardItem(
    product: InvestmentProduct,
    userVerified: Boolean,
    onInvestTrigger: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_item_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Category Badge & Yield Accent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Label with Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val categoryIcon = when (product.category.lowercase()) {
                        "infrastructure" -> Icons.Outlined.CorporateFare
                        "digital assets" -> Icons.Outlined.Token
                        "agriculture" -> Icons.Outlined.Agriculture
                        else -> Icons.Outlined.CastConnected
                    }
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = "Category",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Expected Interest Rate (Annualized Rate)
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${product.expectedYieldPercent}% (30D)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Details
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp,
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Minimum Ticket",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", product.minAmount)} FCFA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Risk Profile
                Column {
                    Text(
                        text = "Disclosed Risk",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    val riskColor = when (product.riskLevel.uppercase()) {
                        "LOW" -> Color(0xFF10B981)
                        "MEDIUM" -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Box(
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.riskLevel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = riskColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Button (Slight greyed if KYC Pending or rejected)
            Button(
                onClick = onInvestTrigger,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("invest_button_${product.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userVerified) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (userVerified) Icons.Filled.ElectricBolt else Icons.Filled.Lock,
                        contentDescription = "Lock icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (userVerified) "Invest in Capital" else "Verify KYC to Invest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InvestSubscriptionDialog(
    product: InvestmentProduct,
    walletBalance: Double,
    onDismiss: () -> Unit,
    onSubmitInvestment: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var agreedRiskTerms by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Capital Allocation: " + product.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Your Balance", fontSize = 11.sp)
                        Text(String.format(Locale.US, "%,.0f", walletBalance) + " FCFA", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Min Ticket Required", fontSize = 11.sp)
                        Text(String.format(Locale.US, "%,.0f", product.minAmount) + " FCFA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Amount Textfield
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Enter Allocation Amount (FCFA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("investment_ticket_field")
                )

                // Computed Return
                val ticketAmt = amountStr.toDoubleOrNull() ?: 0.0
                if (ticketAmt >= product.minAmount) {
                    val projectedReward = ticketAmt * (product.expectedYieldPercent / 100.0)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Est. Return after 30-days:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = String.format(Locale.US, "%,.0f", ticketAmt + projectedReward) + " FCFA (+" + String.format(Locale.US, "%,.0f", projectedReward) + " FCFA)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Terms Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { agreedRiskTerms = !agreedRiskTerms }
                ) {
                    Checkbox(
                        checked = agreedRiskTerms,
                        onCheckedChange = { agreedRiskTerms = it },
                        modifier = Modifier.testTag("agree_terms_checkbox")
                    )
                    Text(
                        text = "I acknowledge that yields fluctuate and values are subject to risk parameters.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ticketAmt = amountStr.toDoubleOrNull() ?: 0.0
                    if (ticketAmt >= product.minAmount && ticketAmt <= walletBalance && agreedRiskTerms) {
                        onSubmitInvestment(ticketAmt)
                    }
                },
                enabled = agreedRiskTerms && (amountStr.toDoubleOrNull() ?: 0.0) >= product.minAmount && (amountStr.toDoubleOrNull() ?: 0.0) <= walletBalance,
                modifier = Modifier.testTag("confirm_investment_button")
            ) {
                Text("Confirm Allocation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
