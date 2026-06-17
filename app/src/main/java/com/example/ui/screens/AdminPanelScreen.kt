package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentProduct
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.InvestmentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: InvestmentViewModel
) {
    val users by viewModel.adminAllUsers.collectAsState()
    val transactions by viewModel.adminAllTransactions.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("Pending Queue", "Platform Stats", "New Products", "Broadsheet")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Flat Admin Colored Header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Admin Area",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "INVEST-PLUS ADMINISTRATION SUITE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Compliance Officer Console • Sandbox Overrides Active",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Tab Row Control
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabLabels.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    modifier = Modifier.testTag("admin_tab_$index")
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content switching based on tabs
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> PendingQueueTab(viewModel = viewModel, users = users, transactions = transactions)
                1 -> PlatformStatsTab(users = users, transactions = transactions, products = products)
                2 -> CreateProductsTab(viewModel = viewModel, products = products)
                3 -> BroadcastingTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PendingQueueTab(
    viewModel: InvestmentViewModel,
    users: List<User>,
    transactions: List<WalletTransaction>
) {
    val pendingKYC = users.filter { it.verificationStatus == "PENDING" }
    val pendingTX = transactions.filter { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pending KYC Sector
        item {
            SectionHeader(title = "Pending Profile Screenings (${pendingKYC.size})", icon = Icons.Filled.PeopleOutline)
        }

        if (pendingKYC.isEmpty()) {
            item {
                EmptyStateSmall(message = "No profile verifications are currently queued.")
            }
        } else {
            items(pendingKYC) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("pending_kyc_item_${user.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Applicant: ${user.fullName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Doc Type: ${user.idType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(text = "Reference No: ${user.idNumber}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.adminApproveKyc(user.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen),
                                modifier = Modifier.weight(1.0f).height(36.dp).testTag("approve_kyc_button_${user.id}")
                            ) {
                                Text("Approve", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.adminRejectKyc(user.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1.0f).height(36.dp).testTag("reject_kyc_button_${user.id}")
                            ) {
                                Text("Reject", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Pending deposit/withdrawal receipts verification
        item {
            SectionHeader(title = "Receipt approvals & Withdrawal Audits (${pendingTX.size})", icon = Icons.Filled.ReceiptLong)
        }

        if (pendingTX.isEmpty()) {
            item {
                EmptyStateSmall(message = "All deposit receipts and withdrawal cash-outs processed.")
            }
        } else {
            items(pendingTX) { tx ->
                val applicant = users.firstOrNull { it.id == tx.userId }?.fullName ?: "Unknown User"
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("pending_tx_item_${tx.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val typeLabel = if (tx.type == "DEPOSIT") "CREDIT TOP-UP" else "BALANCE DEBIT (CASH-OUT)"
                            val labelColor = if (tx.type == "DEPOSIT") PositiveGreen else Color(0xFFEF4444)
                            Text(
                                text = typeLabel,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = labelColor
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,.0f", tx.amount)} FCFA",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = labelColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = "User: $applicant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Gateway: " + tx.gateway, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(text = "Channel Ref: " + tx.reference, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (tx.type == "DEPOSIT") viewModel.adminApproveDeposit(tx.id)
                                    else viewModel.adminApproveWithdrawal(tx.id)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen),
                                modifier = Modifier.weight(1.0f).height(36.dp).testTag("approve_tx_button_${tx.id}")
                            ) {
                                Text("Accept Proof", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (tx.type == "DEPOSIT") viewModel.adminRejectDeposit(tx.id)
                                    else viewModel.adminRejectWithdrawal(tx.id)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1.0f).height(36.dp).testTag("reject_tx_button_${tx.id}")
                            ) {
                                Text("Reject / Void", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlatformStatsTab(
    users: List<User>,
    transactions: List<WalletTransaction>,
    products: List<InvestmentProduct>
) {
    // Computations
    val totalDepositsVal = transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount }
    val totalWithdrawVal = transactions.filter { it.type == "WITHDRAWAL" && it.status == "APPROVED" }.sumOf { it.amount }
    val totalInvestedVal = transactions.filter { it.type == "INVESTMENT" && it.status == "APPROVED" }.sumOf { it.amount }
    val referralPayoutsVal = users.sumOf { it.referralEarnings }

    var showFinancialReport by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Primary Ledger Audits", icon = Icons.Filled.QueryStats)

        // Balance summaries grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatAuditBox(label = "Total Inflow Assets", value = "${String.format(Locale.US, "%,.0f", totalDepositsVal)} FCFA", modifier = Modifier.weight(1.0f))
            StatAuditBox(label = "Total Outflows Paid", value = "${String.format(Locale.US, "%,.0f", totalWithdrawVal)} FCFA", modifier = Modifier.weight(1.0f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatAuditBox(label = "Active Allocations", value = "${String.format(Locale.US, "%,.0f", totalInvestedVal)} FCFA", modifier = Modifier.weight(1.0f))
            StatAuditBox(label = "Affiliate Commissions", value = "${String.format(Locale.US, "%,.0f", referralPayoutsVal)} FCFA", modifier = Modifier.weight(1.0f))
        }

        // Registry Sizes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CENSUS REGISTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                DetailRow(label = "Total Registered Accounts", value = "${users.size} users")
                DetailRow(label = "Listed Asset Catalogs", value = "${products.size} options")
                DetailRow(label = "Archived Receipts Logs", value = "${transactions.size} records")
            }
        }

        // Financial report generator button
        Button(
            onClick = { showFinancialReport = true },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_report_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Summarize, contentDescription = "Report")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Financial Outbreak Report", fontWeight = FontWeight.Bold)
            }
        }

        // Interactive sheet Overlay matching report generator
        if (showFinancialReport) {
            AlertDialog(
                onDismissRequest = { showFinancialReport = false },
                title = { Text("AUDITED COMPLIANCE REPORT SUMMARY", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Statement Audit Date: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
                        Text("This certificate verifies legal reserves are compliant with regional mobile money regulations.", fontSize = 11.sp)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow(label = "Gross Liquid Inflow", value = String.format(Locale.US, "%,.0f FCFA", totalDepositsVal))
                        DetailRow(label = "Gross Exit Outflow", value = String.format(Locale.US, "%,.0f FCFA", totalWithdrawVal))
                        DetailRow(label = "Active Placements Pool", value = String.format(Locale.US, "%,.0f FCFA", totalInvestedVal))
                        DetailRow(label = "Commissions Paid", value = String.format(Locale.US, "%,.0f FCFA", referralPayoutsVal))
                        DetailRow(label = "Liquidity Settle Ratio", value = "98.42% Secure")
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Disclaimer: Returns on physical telecom infrastructure and agricultural microloans are variable. Asset depreciation holds negative outcomes.",
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showFinancialReport = false }) {
                        Text("Export & Print")
                    }
                }
            )
        }
    }
}

@Composable
fun StatAuditBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CreateProductsTab(
    viewModel: InvestmentViewModel,
    products: List<InvestmentProduct>
) {
    var title by remember { mutableStateOf("") }
    var yieldStr by remember { mutableStateOf("") }
    var minAmtStr by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Infrastructure") }
    var risk by remember { mutableStateOf("LOW") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "List New Product", icon = Icons.Filled.AddBusiness)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Product Title") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("admin_prod_title")
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = yieldStr,
                onValueChange = { yieldStr = it },
                label = { Text("Yield Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.0f).testTag("admin_prod_yield")
            )
            OutlinedTextField(
                value = minAmtStr,
                onValueChange = { minAmtStr = it },
                label = { Text("Min Entry ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.0f).testTag("admin_prod_min")
            )
        }

        // Risk & Category Row spinners
        Column {
            Text("Product Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Infrastructure", "Digital Assets", "Agriculture", "Telecom").forEach { cat ->
                    val selected = category == cat
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Column {
            Text("Security Disclosed Risk Rank:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("LOW", "MEDIUM", "HIGH").forEach { r ->
                    val selected = risk == r
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { risk = r }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(r, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Asset Descriptors & Risk Disclosures") },
            placeholder = { Text("Disclose risks specifically to protect compliance rules...") },
            minLines = 3,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("admin_prod_desc")
        )

        Button(
            onClick = {
                val yield = yieldStr.toDoubleOrNull() ?: 0.0
                val minAmt = minAmtStr.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && yield > 0 && minAmt > 0 && desc.isNotBlank()) {
                    viewModel.adminAddProduct(title, yield, risk, minAmt, desc, category)
                    // Reset fields
                    title = ""
                    yieldStr = ""
                    minAmtStr = ""
                    desc = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_product_submit"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Publish Asset Option")
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // Existing product list featuring delete option
        Text("CURRENT RUNNING PRODUCT LINES (${products.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        products.forEach { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(p.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${p.category} | ${p.expectedYieldPercent}% YoY | Risk: ${p.riskLevel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    IconButton(
                        onClick = { viewModel.adminDeleteProduct(p.id) },
                        modifier = Modifier.testTag("admin_delete_product_${p.id}")
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastingTab(
    viewModel: InvestmentViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Push Community Announcement", icon = Icons.Filled.Campaign)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Announcement Title / Broad Header") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("announcement_title_field")
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Announcement Body Notification") },
            placeholder = { Text("Publish systemic wallet delays, payout logs, or network warnings...") },
            minLines = 4,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("announcement_body_field")
        )

        Button(
            onClick = {
                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.adminSendAnnouncement(title, content)
                    title = ""
                    content = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_announcement_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Send, contentDescription = "Broadcast")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast Payout Alert", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = "Icon header", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title.uppercase(), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun EmptyStateSmall(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}
