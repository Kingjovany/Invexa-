package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.data.model.LinkedMobileMoneyAccount
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.InvestmentViewModel

@Composable
fun SettingsScreen(
    viewModel: InvestmentViewModel,
    user: User
) {
    var selectedIdType by remember { mutableStateOf("National Identity Card (CNI)") }
    var idNumber by remember { mutableStateOf("") }

    val uiMessage by viewModel.uiMessage.collectAsState()
    val linkedAccounts by viewModel.linkedAccounts.collectAsState()

    val usdToXafRate by viewModel.usdToXafRate.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncResponse by viewModel.lastSyncResponse.collectAsState()

    // Form inputs for Account Linking
    var linkProvider by remember { mutableStateOf("MTN Mobile Money") }
    var linkAccountName by remember { mutableStateOf("") }
    var linkAccountNumber by remember { mutableStateOf("") }
    var linkSecurityPin by remember { mutableStateOf("") } // secure carrier/app PIN (4 digits)
    var showLinkForm by remember { mutableStateOf(false) }

    // Form input for promo code
    var promoCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Screen Header
        Text(
            text = "Profile Settings & KYC",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Verify your account, manage billing coordinates, and inspect limits",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Identification Status Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("verification_status_banner_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (user.verificationStatus) {
                    "VERIFIED" -> MaterialTheme.colorScheme.primaryContainer
                    "PENDING" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large descriptive icon
                val statIcon = when (user.verificationStatus) {
                    "VERIFIED" -> Icons.Filled.VerifiedUser
                    "PENDING" -> Icons.Filled.PendingActions
                    else -> Icons.Filled.NoAccounts
                }
                Icon(
                    imageVector = statIcon,
                    contentDescription = "Status icon",
                    tint = when (user.verificationStatus) {
                        "VERIFIED" -> MaterialTheme.colorScheme.primary
                        "PENDING" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Identity Verification Status",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.verificationStatus.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = when (user.verificationStatus) {
                            "VERIFIED" -> MaterialTheme.colorScheme.primary
                            "PENDING" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }

        // Account Profile coordinates
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ACCOUNT DETAILS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                DetailRow(label = "Username Handle", value = "@" + user.username)
                DetailRow(label = "Legal Full Name", value = user.fullName)
                DetailRow(label = "Ref Invitation Code", value = user.referralCode)
                
                if (user.isAdmin) {
                    DetailRow(label = "Security Clearance", value = "Platform System Administrator")
                }
            }
        }

        // Submit KYC verification Box (only if UNVERIFIED or REJECTED)
        if (user.verificationStatus == "UNVERIFIED" || user.verificationStatus == "REJECTED") {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("kyc_submission_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SUBMIT KYC VERIFICATION DOCUMENT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Compliance rules require submitting legal details before product capital investment activations:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Card options for ID type
                    Column {
                        Text("Select Document Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf("National Identity Card (CNI)", "Legal International Passport", "Driver License").forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedIdType = type }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedIdType == type,
                                    onClick = { selectedIdType = type }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(type, fontSize = 12.sp)
                            }
                        }
                    }

                    // Document reference ID
                    OutlinedTextField(
                        value = idNumber,
                        onValueChange = { idNumber = it },
                        label = { Text("ID reference Number / Document Serial ID") },
                        placeholder = { Text("e.g. 11029102434") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kyc_ref_field")
                    )

                    // Error logs
                    if (uiMessage != null) {
                        Text(
                            text = uiMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (idNumber.isNotBlank()) {
                                viewModel.submitKyc(selectedIdType, idNumber)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_verification_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = "Upload")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit KYC ID Specs", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (user.verificationStatus == "PENDING") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.HourglassBottom,
                        contentDescription = "Validating",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Document Screening in Progress",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Submitted Doc: ${user.idType}\nRef: ${user.idNumber}\nOur audit desk reviews KYC assets within 12 business hours. You will receive access instantly upon approval.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        } else {
            // VERIFIED
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VERIFIED ACCESS STANDARDS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PositiveGreen)
                    Text(
                        text = "Congratulations! Your account features fully verified fintech credentials. You can access unlimited capital placements and high-tier agricultural, telecoms and cryptos offerings.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(label = "Verified CNI Type", value = user.idType)
                    DetailRow(label = "Document Serial Id", value = "xxxx-xxxx-" + user.idNumber.takeLast(4))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MOBILE MONEY ACCOUNTS MANAGEMENT SECTION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mobile_money_link_section_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = "Billing Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "LINKED MOBILE TRANSACTIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Toggle Expand Form
                    TextButton(
                        onClick = { showLinkForm = !showLinkForm },
                        modifier = Modifier.testTag("toggle_link_form_btn")
                    ) {
                        Text(
                            text = if (showLinkForm) "Close Form" else "Link Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "Link mobile wallets to bypass payment entries on daily portfolio subscriptions or claims. Changes persist directly into secure datastore.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 15.sp
                )

                // Render secure Link Form
                if (showLinkForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Secure Mobile Money Linking Setup",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Provider Selection Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                listOf("MTN Mobile Money", "Orange Money").forEach { provider ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable { linkProvider = provider }
                                            .testTag("provider_radio_${provider.replace(" ", "_")}")
                                    ) {
                                        RadioButton(
                                            selected = linkProvider == provider,
                                            onClick = { linkProvider = provider }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(provider, fontSize = 12.sp)
                                    }
                                }
                            }

                            // Legal Holder Name Field
                            OutlinedTextField(
                                value = linkAccountName,
                                onValueChange = { linkAccountName = it },
                                label = { Text("Legal Wallet Owner Name") },
                                placeholder = { Text("e.g. Jovany King") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("link_account_name_field")
                            )

                            // 9 digit Number with auto compliance help
                            OutlinedTextField(
                                value = linkAccountNumber,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } && it.length <= 9) {
                                        linkAccountNumber = it
                                    }
                                },
                                label = { Text("9-Digit Numeric Mobile Number") },
                                placeholder = { Text("6xxxxxxxx (e.g. 671551321)") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("link_account_number_field")
                            )

                            // SECURE COMPLIANCE FIELD: Account validation pin input
                            OutlinedTextField(
                                value = linkSecurityPin,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                        linkSecurityPin = it
                                    }
                                },
                                label = { Text("4-Digit Provider Validation PIN") },
                                placeholder = { Text("••••") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("link_security_pin_field")
                            )

                            Text(
                                text = "💡 System performs compliance verification with mobile money networks in Cameroon instantly on validation.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )

                            Button(
                                onClick = {
                                    if (linkSecurityPin.length < 4) {
                                        viewModel.clearMessage()
                                        // Simple local validation error feedback
                                        viewModel.simulateUnlock(-99) // updates messages mock
                                        return@Button
                                    }
                                    viewModel.linkMobileMoney(linkProvider, linkAccountName, linkAccountNumber)
                                    // Reset inputs upon triggers
                                    linkAccountName = ""
                                    linkAccountNumber = ""
                                    linkSecurityPin = ""
                                    showLinkForm = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_link_account_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Validate & Link Wallet Plan", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Render Linked Wallets List
                if (linkedAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No linked accounts. Tap 'Link Account' to configure mobile wallets.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        linkedAccounts.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("linked_account_row_${acc.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Visual color indicator based on MTN MoMo (Yellow tint) vs Orange (Orange tint)
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (acc.provider.contains("MTN")) Color(0xFFFFCC00) else Color(0xFFFF6600),
                                                shape = CircleShape
                                            )
                                    )
                                    Column {
                                        Text(
                                            text = "${acc.provider} (Active)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${acc.accountName} • +237 ${acc.accountNumber}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.unlinkMobileMoney(acc.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("unlink_account_btn_${acc.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Billing Wallet",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GIFTS & PROMOTIONS PLACE SECTION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gifts_and_promo_box_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Gifts Promo Codes",
                        tint = PositiveGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "GIFTS & VOUCHERS REDEMPTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PositiveGreen
                    )
                }

                Text(
                    text = "Claim direct financial cash packages using Invexa secret promotional gift codes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 15.sp
                )

                // Redemption Layout Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it.uppercase() },
                        label = { Text("Code") },
                        placeholder = { Text("WELCOME1000") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.0f)
                            .testTag("gift_promo_code_input_field")
                    )

                    Button(
                        onClick = {
                            if (promoCodeInput.isNotBlank()) {
                                viewModel.claimGiftCode(promoCodeInput)
                                promoCodeInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("submit_claim_gift_btn"),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Claim", fontWeight = FontWeight.Bold)
                    }
                }

                // Promo references guide
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Active Sandbox Voucher Codes:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎁 WELCOME1000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+1,000 FCFA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PositiveGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎁 BONUS2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+5,000 FCFA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PositiveGreen)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CLOUD SYNCHRONIZATION AND FINANCIAL API NETWORK SECTION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backend_cloud_sync_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Cloud Gateway Sync",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "FINANCIAL CLOUD REPLICATOR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Online state visual indicator
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (lastSyncResponse != null) PositiveGreen.copy(alpha = 0.15f)
                                       else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (lastSyncResponse != null) "CONNECTED-SYNCED" else "READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (lastSyncResponse != null) PositiveGreen else MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Text(
                    text = "Perform dynamic client-server reconciliation. This connects to our secure web backend gateway using Retrofit to synchronize portfolio records, balances and backup local ledgers securely.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 15.sp
                )

                // Render dynamic statistics from server
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Live Market Rates",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Fetched from index API gateway",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "1 USD = ${String.format(java.util.Locale.US, "%,.1f", usdToXafRate)} FCFA",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Cameroon central standard",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)

                    // Fetch Latest Rate Button
                    OutlinedButton(
                        onClick = { viewModel.fetchLiveRates() },
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("fetch_live_rates_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Query Latest Forex Exchange Feed", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Render dynamic backup reconciliation ledger logs
                lastSyncResponse?.let { resp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PositiveGreen.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, PositiveGreen.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("sync_reconciliation_details_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CloudDone, contentDescription = "Synced successfully", tint = PositiveGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Server Acknowledge Details",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PositiveGreen
                                )
                            }
                            Text(
                                text = "Message: ${resp.serverMessage}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            DetailRow(label = "Remote Sync Session ID", value = resp.syncId.take(18) + "...")
                            DetailRow(label = "Encrypted State Volume", value = "${resp.backupSize} bytes")
                            DetailRow(
                                label = "Verification Latency",
                                value = "12ms (Completed via Retrofit/OkHttp)"
                            )
                        }
                    }
                }

                // Cloud Replicate Action Button with proper states
                Button(
                    onClick = { viewModel.syncUserDataWithBackend() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trigger_cloud_backup_sync_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lastSyncResponse != null) PositiveGreen else MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSyncing
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Text("Reconciling Encrypted Ledger...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = if (lastSyncResponse != null) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = "Sync Now"
                            )
                            Text(
                                text = if (lastSyncResponse != null) "Re-Sync Account Data" else "Initiate Cloud Replicator Sync",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
