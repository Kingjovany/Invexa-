package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.User
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InvestmentViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: InvestmentViewModel = ViewModelProvider(this)[InvestmentViewModel::class.java]
            
            // Central Dark/Light mode state toggle (Fulfills visual theme custom demands!)
            var darkThemeSelected by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = darkThemeSelected) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScaffold(
                        viewModel = viewModel,
                        currentThemeDark = darkThemeSelected,
                        onThemeToggle = { darkThemeSelected = !darkThemeSelected }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: InvestmentViewModel,
    currentThemeDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Active bottom-navigation tab
    var currentTab by remember { mutableStateOf(0) } // 0: Wallet, 1: Offers, 2: Holdings, 3: Members Hub, 4: Admin

    // Trigger snackbar when ViewModel publishes messages
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessage()
            }
        }
    }

    if (currentUser == null) {
        AuthScreen(viewModel = viewModel, onAuthSuccess = { currentTab = 0 })
    } else {
        val user = currentUser!!

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Toll,
                                contentDescription = "Invest",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Invexa Hub",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // On-the-fly Theme Toggler (light/dark)
                        IconButton(
                            onClick = onThemeToggle,
                            modifier = Modifier.testTag("theme_picker_toggle")
                        ) {
                            Icon(
                                imageVector = if (currentThemeDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Secure Log out trigger
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Exit Hub",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(imageVector = if (currentTab == 0) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet, contentDescription = "Wallet") },
                        modifier = Modifier.testTag("nav_tab_wallet")
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(imageVector = if (currentTab == 1) Icons.Filled.TrendingUp else Icons.Outlined.TrendingUp, contentDescription = "Offers") },
                        modifier = Modifier.testTag("nav_tab_offers")
                    )

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(imageVector = if (currentTab == 2) Icons.Filled.Timeline else Icons.Outlined.Timeline, contentDescription = "Holdings") },
                        modifier = Modifier.testTag("nav_tab_holdings")
                    )

                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(imageVector = if (currentTab == 3) Icons.Filled.Widgets else Icons.Outlined.Widgets, contentDescription = "More") },
                        modifier = Modifier.testTag("nav_tab_more")
                    )

                    // Exclusive Admin view (Visible ONLY when isAdmin is true)
                    if (user.isAdmin) {
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = { Icon(imageVector = Icons.Filled.AdminPanelSettings, contentDescription = "Admin") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.error,
                                selectedTextColor = MaterialTheme.colorScheme.error,
                                indicatorColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.testTag("nav_tab_admin")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        user = user,
                        onNavigateToKYC = { currentTab = 3 } // Quick navigation to settings/KYC
                    )
                    1 -> ProductsScreen(viewModel = viewModel, user = user)
                    2 -> InvestmentsTrackerScreen(viewModel = viewModel)
                    3 -> MembersHubScreen(viewModel = viewModel, user = user)
                    4 -> {
                        if (user.isAdmin) {
                            AdminPanelScreen(viewModel = viewModel)
                        } else {
                            currentTab = 0
                        }
                    }
                }
            }
        }
    }
}

// Sleek unified Members Hub hosting Settings KYC, Affiliates, Statements, and Lucky Wheel
@Composable
fun MembersHubScreen(
    viewModel: InvestmentViewModel,
    user: User
) {
    var activeSubMode by remember { mutableStateOf(0) } // 0: Settings, 1: Affiliates, 2: Statements, 3: Lucky Wheel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab selectors - Pure icons only to satisfy 'use icon instead words for the menus please'
        TabRow(
            selectedTabIndex = activeSubMode,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeSubMode == 0,
                onClick = { activeSubMode = 0 },
                icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "KYC Verification Profile", modifier = Modifier.size(24.dp)) }
            )
            Tab(
                selected = activeSubMode == 1,
                onClick = { activeSubMode = 1 },
                icon = { Icon(imageVector = Icons.Filled.Group, contentDescription = "Affiliate Referral System", modifier = Modifier.size(24.dp)) }
            )
            Tab(
                selected = activeSubMode == 2,
                onClick = { activeSubMode = 2 },
                icon = { Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = "Transaction Ledger", modifier = Modifier.size(24.dp)) }
            )
            Tab(
                selected = activeSubMode == 3,
                onClick = { activeSubMode = 3 },
                icon = { Icon(imageVector = Icons.Filled.Casino, contentDescription = "Monthly Lucky Wheel", modifier = Modifier.size(24.dp)) }
            )
        }

        Box(modifier = Modifier.weight(1.0f).fillMaxWidth()) {
            when (activeSubMode) {
                0 -> SettingsScreen(viewModel = viewModel, user = user)
                1 -> ReferralsScreen(viewModel = viewModel, user = user)
                2 -> TransactionHistoryScreen(viewModel = viewModel)
                3 -> LuckyWheelScreen(viewModel = viewModel, user = user)
            }
        }
    }
}
