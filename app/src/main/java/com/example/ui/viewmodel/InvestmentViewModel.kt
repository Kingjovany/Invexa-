package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.InvestmentRepository
import com.example.data.api.RetrofitClient
import com.example.data.api.WalletSyncPayload
import com.example.data.api.SyncStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InvestmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InvestmentRepository

    // Session Status
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Screen State
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Flows
    val allProducts: StateFlow<List<InvestmentProduct>>
    val allAnnouncements: StateFlow<List<Announcement>>

    // User Specific Flows
    private val _userInvestments = MutableStateFlow<List<UserInvestment>>(emptyList())
    val userInvestments: StateFlow<List<UserInvestment>> = _userInvestments.asStateFlow()

    private val _userTransactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val userTransactions: StateFlow<List<WalletTransaction>> = _userTransactions.asStateFlow()

    private val _linkedAccounts = MutableStateFlow<List<LinkedMobileMoneyAccount>>(emptyList())
    val linkedAccounts: StateFlow<List<LinkedMobileMoneyAccount>> = _linkedAccounts.asStateFlow()

    // API backend sync status tracking
    private val _usdToXafRate = MutableStateFlow<Double>(610.0) // Fallback default
    val usdToXafRate: StateFlow<Double> = _usdToXafRate.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncResponse = MutableStateFlow<SyncStatusResponse?>(null)
    val lastSyncResponse: StateFlow<SyncStatusResponse?> = _lastSyncResponse.asStateFlow()

    // Admin Flows
    val adminAllUsers: StateFlow<List<User>>
    val adminAllTransactions: StateFlow<List<WalletTransaction>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InvestmentRepository(
            userDao = db.userDao(),
            productDao = db.productDao(),
            investmentDao = db.investmentDao(),
            transactionDao = db.transactionDao(),
            announcementDao = db.announcementDao(),
            linkedAccountDao = db.linkedAccountDao()
        )

        // Flows Initialization
        allProducts = repository.allProducts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allAnnouncements = repository.allAnnouncements
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        adminAllUsers = repository.allUsers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        adminAllTransactions = repository.allTransactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Trigger DB seed
        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateIfNeeded()
            // Auto fetch initial financial parameters from server on birth of ViewModel
            fetchLiveRates()
        }

        // Listen for current user updates to bind investments & transactions reactively
        viewModelScope.launch {
            _currentUser.collectLatest { user ->
                if (user != null) {
                    launch {
                        repository.getUserById(user.id).collectLatest { updatedUser ->
                            _currentUser.value = updatedUser
                        }
                    }
                    launch {
                        repository.getInvestmentsForUser(user.id).collectLatest { list ->
                            _userInvestments.value = list
                        }
                    }
                    launch {
                        repository.getTransactionsForUser(user.id).collectLatest { list ->
                            _userTransactions.value = list
                        }
                    }
                    launch {
                        repository.getLinkedAccountsForUser(user.id).collectLatest { list ->
                            _linkedAccounts.value = list
                        }
                    }
                } else {
                    _userInvestments.value = emptyList()
                    _userTransactions.value = emptyList()
                    _linkedAccounts.value = emptyList()
                }
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    // ACTIONS: Authorization Login
    fun login(usernameOrEmail: String, passwordText: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.authenticate(usernameOrEmail, passwordText)
            if (user != null) {
                _currentUser.value = user
                onCompleted(true)
            } else {
                _uiMessage.value = "Invalid credentials."
                onCompleted(false)
            }
        }
    }

    // ACTIONS: Authorization Register
    fun register(username: String, email: String, fullName: String, passwordText: String, referralByText: String, onCompleted: (Boolean) -> Unit) {
        if (username.isBlank() || email.isBlank() || fullName.isBlank() || passwordText.isBlank()) {
            _uiMessage.value = "Please complete all registration fields."
            onCompleted(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.register(username, email, fullName, passwordText, referralByText)
            if (success) {
                _uiMessage.value = "Registration successful! You may log in."
                onCompleted(true)
            } else {
                _uiMessage.value = "Username or Email already taken."
                onCompleted(false)
            }
        }
    }

    // ACTIONS: Logout
    fun logout() {
        _currentUser.value = null
    }

    // ACTIONS: Submit Profile Verification (KYC)
    fun submitKyc(idType: String, idNumber: String) {
        val user = _currentUser.value ?: return
        if (idType.isBlank() || idNumber.isBlank()) {
            _uiMessage.value = "Please provide both document type and reference identification."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitVerification(user.id, idType, idNumber)
            _uiMessage.value = "KYC documents submitted successfully. Verification is pending approval."
        }
    }

    // ACTIONS: Deposit
    fun deposit(amount: Double, gateway: String, reference: String) {
        val user = _currentUser.value ?: return
        if (amount < 5000) {
            _uiMessage.value = "Minimum deposit threshold is 5,000 FCFA."
            return
        }
        if (reference.isBlank()) {
            _uiMessage.value = "Please enter the transfer reference ID for MTN, Orange, or PayPal."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestDeposit(user.id, amount, gateway, reference)
            _uiMessage.value = "Deposit request submitted. Admin will approve shortly."
        }
    }

    // ACTIONS: Withdraw
    fun withdraw(amount: Double, gateway: String, destination: String) {
        val user = _currentUser.value ?: return
        if (amount <= 0) {
            _uiMessage.value = "Invalid withdrawal amount."
            return
        }
        if (user.walletBalance < amount) {
            _uiMessage.value = "Insufficient wallet balance."
            return
        }
        if (destination.isBlank()) {
            _uiMessage.value = "Please enter payout coordinates (phone or email)."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.requestWithdrawal(user.id, amount, gateway, destination)
            if (success) {
                _uiMessage.value = "Withdrawal requested. Balance locked until admin verification."
            } else {
                _uiMessage.value = "Failed requesting withdrawal. Check your wallet balance."
            }
        }
    }

    // ACTIONS: Invest in product
    fun invest(productId: Int, amount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val err = repository.investInProduct(user.id, productId, amount)
            if (err == null) {
                _uiMessage.value = "Investment created successfully! Funds are now locked for 30 days."
            } else {
                _uiMessage.value = err
            }
        }
    }

    // ACTIONS: Claim Earnings
    fun claimEarnings(investmentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val err = repository.claimEarnings(investmentId)
            if (err == null) {
                _uiMessage.value = "Principal and earnings successfully added back to wallet!"
            } else {
                _uiMessage.value = err
            }
        }
    }

    // ACTIONS: Spin Lucky Wheel once a month
    fun spinLuckyWheel(rewardAmount: Double) {
        val user = _currentUser.value ?: return
        val now = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            repository.spinWheelAndEarn(user.id, rewardAmount, now)
        }
    }

    // ACTIONS: Link Mobile Money Account
    fun linkMobileMoney(provider: String, accountName: String, accountNumber: String) {
        val user = _currentUser.value ?: return
        if (provider.isBlank() || accountName.isBlank() || accountNumber.isBlank()) {
            _uiMessage.value = "All fields are required!"
            return
        }
        val cleanNumber = accountNumber.trim().replace("\\s".toRegex(), "")
        if (cleanNumber.length != 9 || !cleanNumber.all { it.isDigit() }) {
            _uiMessage.value = "Account number must be exactly 9 digits!"
            return
        }
        if (!cleanNumber.startsWith("6")) {
            _uiMessage.value = "Mobile money plans in Cam must start with 6!"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val account = LinkedMobileMoneyAccount(
                userId = user.id,
                provider = provider,
                accountName = accountName.trim(),
                accountNumber = cleanNumber
            )
            repository.linkMobileMoneyAccount(account)
            _uiMessage.value = "$provider Account successfully linked!"
        }
    }

    // ACTIONS: Unlink Mobile Money Account
    fun unlinkMobileMoney(accountId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.unlinkMobileMoneyAccount(accountId)
            _uiMessage.value = "Mobile money account removed securely."
        }
    }

    // ACTIONS: Claim Gift Promo Voucher
    fun claimGiftCode(codeString: String) {
        val user = _currentUser.value ?: return
        val cleanCode = codeString.trim().uppercase()
        if (cleanCode.isBlank()) {
            _uiMessage.value = "Please enter a valid gift voucher code!"
            return
        }

        val bonusReward = when (cleanCode) {
            "WELCOME1000" -> 1000.0
            "INVEXAGIFT" -> 2000.0
            "BONUS2026" -> 5000.0
            "MEGA999" -> 10000.0
            else -> {
                _uiMessage.value = "Voucher code invalid/expired. Try 'WELCOME1000' or 'BONUS2026'!"
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Update wallet balance
            val updatedUser = user.copy(walletBalance = user.walletBalance + bonusReward)
            repository.updateUser(updatedUser)

            // Register promo approved transaction for transparency
            val tx = WalletTransaction(
                userId = user.id,
                type = "REFERRAL_BONUS",
                amount = bonusReward,
                gateway = "Promo Gift Box",
                reference = "Redemption of Code $cleanCode",
                timestamp = System.currentTimeMillis(),
                status = "APPROVED"
            )
            repository.insertTransaction(tx)
            _uiMessage.value = "$cleanCode Redeemed! Free gift of ${String.format(java.util.Locale.US, "%,.0f", bonusReward)} FCFA added to wallet!"
        }
    }

    // SANDBOX - Unlock Lockups For Demo Simulations
    fun simulateUnlock(investmentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.simulateTimeExpiry(investmentId)
            _uiMessage.value = "Time-machine trigger: Sandbox shifted lock up to expired!"
        }
    }

    // ADMIN - Manage KYC
    fun adminApproveKyc(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveVerification(userId)
            _uiMessage.value = "User verification APPROVED."
        }
    }

    fun adminRejectKyc(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectVerification(userId)
            _uiMessage.value = "User verification REJECTED."
        }
    }

    // ADMIN - Manage Ledger
    fun adminApproveDeposit(txId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveDeposit(txId)
            _uiMessage.value = "Deposit transaction APPROVED."
        }
    }

    fun adminRejectDeposit(txId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectDeposit(txId)
            _uiMessage.value = "Deposit transaction REJECTED."
        }
    }

    fun adminApproveWithdrawal(txId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveWithdrawal(txId)
            _uiMessage.value = "Withdrawal transaction APPROVED."
        }
    }

    fun adminRejectWithdrawal(txId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectWithdrawal(txId)
            _uiMessage.value = "Withdrawal transaction REJECTED and refunded."
        }
    }

    // ADMIN - Create Product
    fun adminAddProduct(title: String, yield: Double, risk: String, minAmount: Double, desc: String, category: String) {
        if (title.isBlank() || desc.isBlank() || minAmount <= 0.0) {
            _uiMessage.value = "All product fields are mandatory."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.addInvestmentProduct(
                InvestmentProduct(
                    title = title,
                    expectedYieldPercent = yield,
                    riskLevel = risk,
                    minAmount = minAmount,
                    description = desc,
                    category = category
                )
            )
            _uiMessage.value = "Product '$title' listed successfully."
        }
    }

    fun adminDeleteProduct(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeProduct(id)
            _uiMessage.value = "Product removed from primary listings."
        }
    }

    // ADMIN - Broadcast Announcement
    fun adminSendAnnouncement(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            _uiMessage.value = "Please complete title and content."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.createAnnouncement(title, content)
            _uiMessage.value = "Announcement broadcast sent globally."
        }
    }

    // API: Fetch live financial exchange rates from open.er-api.com
    fun fetchLiveRates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUSDRates()
                if (response.isSuccessful && response.body() != null) {
                    val rateMap = response.body()!!.rates
                    val xafRate = rateMap["XAF"]
                    if (xafRate != null) {
                        _usdToXafRate.value = xafRate
                        _uiMessage.value = "Fetched live Central African exchange rate: 1 USD = ${String.format(java.util.Locale.US, "%.1f", xafRate)} FCFA"
                    }
                } else {
                    _uiMessage.value = "Server rate lookup returned status ${response.code()}. Using secure baseline."
                }
            } catch (e: Exception) {
                // Keep default baseline
            }
        }
    }

    // API: Sync local state securely with backend
    fun syncUserDataWithBackend() {
        val user = _currentUser.value ?: return
        val investments = _userInvestments.value
        val linked = _linkedAccounts.value
        val totalInvested = investments.sumOf { it.amount }

        _isSyncing.value = true
        _lastSyncResponse.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = WalletSyncPayload(
                    userId = user.id,
                    username = user.fullName,
                    walletBalance = user.walletBalance,
                    linkedAccountsCount = linked.size,
                    totalInvested = totalInvested
                )
                val response = RetrofitClient.apiService.syncWalletBalance(payload)
                if (response.isSuccessful && response.body() != null) {
                    _lastSyncResponse.value = response.body()
                    _uiMessage.value = "Cloud Sync complete! Ledger committed securely."
                } else {
                    _uiMessage.value = "Cloud response code: ${response.code()}. Check settings."
                }
            } catch (e: Exception) {
                _uiMessage.value = "Could not reach Invexa Cloud server. Retry later."
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
