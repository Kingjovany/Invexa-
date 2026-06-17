package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class InvestmentRepository(
    private val userDao: UserDao,
    private val productDao: InvestmentProductDao,
    private val investmentDao: UserInvestmentDao,
    private val transactionDao: WalletTransactionDao,
    private val announcementDao: AnnouncementDao,
    private val linkedAccountDao: LinkedMobileMoneyAccountDao
) {
    // Exposing reactive fields
    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id)
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allProducts: Flow<List<InvestmentProduct>> = productDao.getAllProducts()
    val allTransactions: Flow<List<WalletTransaction>> = transactionDao.getAllTransactions()
    val allAnnouncements: Flow<List<Announcement>> = announcementDao.getAllAnnouncements()

    fun getInvestmentsForUser(userId: Int): Flow<List<UserInvestment>> {
        return investmentDao.getInvestmentsByUserId(userId)
    }

    fun getTransactionsForUser(userId: Int): Flow<List<WalletTransaction>> {
        return transactionDao.getTransactionsByUserId(userId)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    // Prepopulate initial core products and admin if none exist
    suspend fun prepopulateIfNeeded() {
        val products = productDao.getAllProducts().firstOrNull()
        if (products.isNullOrEmpty()) {
            // Seed default investment packages starting from 10,000 FCFA, doubling until 50,000 FCFA, then adding 20,000 FCFA
            productDao.insertProduct(
                InvestmentProduct(
                    title = "Vanguard Solar Grid",
                    expectedYieldPercent = 12.0,
                    riskLevel = "LOW",
                    termDays = 30,
                    minAmount = 10000.0,
                    category = "Infrastructure",
                    description = "A diversified asset portfolio in clean solar generation fields in sub-Saharan regions. Offers highly stable projected returns with low market volatility."
                )
            )
            productDao.insertProduct(
                InvestmentProduct(
                    title = "Pan-African Telecoms Expansion",
                    expectedYieldPercent = 16.5,
                    riskLevel = "MEDIUM",
                    termDays = 30,
                    minAmount = 20000.0,
                    category = "Telecommunications",
                    description = "Equity-backed crowdfunding of rural towers and fiber pathways. Medium risk profile correlating to regional telecommunication infrastructure demand."
                )
            )
            productDao.insertProduct(
                InvestmentProduct(
                    title = "Apex Crypto Arbitrage Pool",
                    expectedYieldPercent = 28.0,
                    riskLevel = "HIGH",
                    termDays = 30,
                    minAmount = 40000.0,
                    category = "Digital Assets",
                    description = "Algorithmic high-frequency spread capturing across major African exchange order books. Yields are highly attractive but capital fluctuates with high risk."
                )
            )
            productDao.insertProduct(
                InvestmentProduct(
                    title = "Agribusiness Supply Ledger",
                    expectedYieldPercent = 18.0,
                    riskLevel = "LOW",
                    termDays = 30,
                    minAmount = 60000.0,
                    category = "Agriculture",
                    description = "Agricultural ledger funding cold chain distribution grids. Offers attractive gains of 18% with strong commodity support."
                )
            )
            productDao.insertProduct(
                InvestmentProduct(
                    title = "Sovereign Development Fund",
                    expectedYieldPercent = 22.5,
                    riskLevel = "MEDIUM",
                    termDays = 30,
                    minAmount = 80000.0,
                    category = "Infrastructure",
                    description = "Regional developmental bonds backed by sovereign infrastructure assets."
                )
            )
            productDao.insertProduct(
                InvestmentProduct(
                    title = "Invexa Blue-Chip Alliance",
                    expectedYieldPercent = 32.0,
                    riskLevel = "HIGH",
                    termDays = 30,
                    minAmount = 100000.0,
                    category = "Digital Assets",
                    description = "Exclusive high-scale portfolio pool optimizing diversified premium asset clusters."
                )
            )

            // Seed default System Announcement
            announcementDao.insertAnnouncement(
                Announcement(
                    title = "Welcome to Invexa Portfolio Suite!",
                    content = "Enjoy premium assets, high-yield infrastructure products, and transparent payouts. Remember all assets carry risk; please review documentation carefully.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Check if admin user exists
        val oldAdmin = userDao.getUserByUsername("admin")
        if (oldAdmin == null) {
            userDao.insertUser(
                User(
                    username = "admin",
                    email = "admin@invexa.com",
                    passwordHash = "admin", // Simple for local sandbox verification
                    fullName = "Platform Admin",
                    referralCode = "SYSTEM100",
                    verificationStatus = "VERIFIED",
                    idType = "Auto-Verified",
                    idNumber = "9999",
                    isAdmin = true
                )
            )
        }
    }

    // AUTH - Log In
    suspend fun authenticate(usernameOrEmail: String, passwordText: String): User? {
        val user = userDao.getUserByUsernameOrEmail(usernameOrEmail)
        return if (user != null && user.passwordHash == passwordText) {
            user
        } else null
    }

    // AUTH - Sign Up
    suspend fun register(username: String, email: String, fullName: String, passwordText: String, referredBy: String?): Boolean {
        val existing = userDao.getUserByUsernameOrEmail(username) ?: userDao.getUserByUsernameOrEmail(email)
        if (existing != null) return false

        // Generate clean referral code
        val randomRef = UUID.randomUUID().toString().take(6).uppercase()

        // Check if referral exists
        var refCodeClean: String? = null
        if (!referredBy.isNullOrBlank()) {
            val referrer = userDao.getUserByReferralCode(referredBy.trim().uppercase())
            if (referrer != null) {
                refCodeClean = referrer.referralCode
            }
        }

        val newUser = User(
            username = username.trim().lowercase(),
            email = email.trim().lowercase(),
            fullName = fullName.trim(),
            passwordHash = passwordText,
            referralCode = randomRef,
            referredBy = refCodeClean,
            walletBalance = 0.0,
            referralEarnings = 0.0,
            verificationStatus = "VERIFIED",
            idType = "Auto-Verified",
            idNumber = "9999"
        )
        userDao.insertUser(newUser)
        return true
    }

    // KYC - Submit ID Info
    suspend fun submitVerification(userId: Int, idType: String, idNumber: String) {
        val user = userDao.getUserByIdSync(userId) ?: return
        val updated = user.copy(
            idType = idType,
            idNumber = idNumber,
            verificationStatus = "PENDING"
        )
        userDao.updateUser(updated)
    }

    // WALLET - Request Deposit
    suspend fun requestDeposit(userId: Int, amount: Double, gateway: String, reference: String) {
        if (amount <= 0) return
        val transaction = WalletTransaction(
            userId = userId,
            type = "DEPOSIT",
            amount = amount,
            gateway = gateway,
            reference = reference,
            timestamp = System.currentTimeMillis(),
            status = "PENDING"
        )
        transactionDao.insertTransaction(transaction)
    }

    // WALLET - Request Withdrawal
    suspend fun requestWithdrawal(userId: Int, amount: Double, gateway: String, destination: String): Boolean {
        val user = userDao.getUserByIdSync(userId) ?: return false
        if (amount <= 0 || user.walletBalance < amount) return false

        // Safe double-entry: deduct balance immediately and place in PENDING
        val updatedUser = user.copy(walletBalance = user.walletBalance - amount)
        userDao.updateUser(updatedUser)

        val transaction = WalletTransaction(
            userId = userId,
            type = "WITHDRAWAL",
            amount = amount,
            gateway = gateway,
            reference = destination, // Stored target payment info (PayPal email, phone etc.)
            timestamp = System.currentTimeMillis(),
            status = "PENDING"
        )
        transactionDao.insertTransaction(transaction)
        return true
    }

    // INVESTMENT - Subscribe
    suspend fun investInProduct(userId: Int, productId: Int, amount: Double): String? {
        val user = userDao.getUserByIdSync(userId) ?: return "User not found."
        val product = productDao.getProductByIdSync(productId) ?: return "Product not found."

        if (user.verificationStatus != "VERIFIED") {
            return "Profile verification required. Please complete profile verification first."
        }

        if (amount < product.minAmount) {
            return "Minimum investment amount is ${String.format(java.util.Locale.US, "%,.0f", product.minAmount)} FCFA."
        }

        if (user.walletBalance < amount) {
            return "Insufficient wallet balance. Please deposit funds."
        }

        // Subtract wallet balance
        val updatedUser = user.copy(walletBalance = user.walletBalance - amount)
        userDao.updateUser(updatedUser)

        // Log INVESTMENT transaction instantly as APPROVED
        val tx = WalletTransaction(
            userId = userId,
            type = "INVESTMENT",
            amount = amount,
            gateway = "Wallet",
            reference = "Inv: ${product.title}",
            timestamp = System.currentTimeMillis(),
            status = "APPROVED"
        )
        transactionDao.insertTransaction(tx)

        // Create the active investment
        val daysMs = 30L * 24L * 60L * 60L * 1000L
        val yieldAmount = amount * (product.expectedYieldPercent / 100.0)
        val investment = UserInvestment(
            userId = userId,
            productId = productId,
            productTitle = product.title,
            yieldPercent = product.expectedYieldPercent,
            amount = amount,
            startDate = System.currentTimeMillis(),
            lockUntilDate = System.currentTimeMillis() + daysMs,
            projectedEarnings = yieldAmount,
            status = "ACTIVE"
        )
        investmentDao.insertInvestment(investment)
        return null
    }

    // EARNINGS CLAIM - Unlock and Claim
    suspend fun claimEarnings(investmentId: Int): String? {
        val investment = investmentDao.getInvestmentByIdSync(investmentId) ?: return "Investment not found."
        if (investment.status == "CLAIMED") return "Earnings have already been claimed."

        val now = System.currentTimeMillis()
        if (now < investment.lockUntilDate) {
            return "Earnings are locked! Must elapse lockup end date."
        }

        val user = userDao.getUserByIdSync(investment.userId) ?: return "User not found."

        // Return original principal + accrued yield back into user's wallet
        val totalPayout = investment.amount + investment.projectedEarnings
        val updatedUser = user.copy(walletBalance = user.walletBalance + totalPayout)
        userDao.updateUser(updatedUser)

        // Update Investment status to CLAIMED
        val updatedInv = investment.copy(status = "CLAIMED")
        investmentDao.updateInvestment(updatedInv)

        // Insert Transaction log
        val tx = WalletTransaction(
            userId = user.id,
            type = "EARNINGS_CLAIM",
            amount = totalPayout,
            gateway = "Wallet",
            reference = "Claim: ${investment.productTitle}",
            timestamp = System.currentTimeMillis(),
            status = "APPROVED"
        )
        transactionDao.insertTransaction(tx)
        return null
    }

    // SIMULATED TIME EXPIRY helper to bypass 30-day lock constraints in demo builds
    suspend fun simulateTimeExpiry(investmentId: Int) {
        val investment = investmentDao.getInvestmentByIdSync(investmentId) ?: return
        val oneDayMs = 24L * 60L * 60L * 1000L
        // Shift start date back 31 days to exceed lock period
        val shiftedStart = System.currentTimeMillis() - (31L * oneDayMs)
        val shiftedEnd = System.currentTimeMillis() - oneDayMs
        val updated = investment.copy(
            startDate = shiftedStart,
            lockUntilDate = shiftedEnd,
            status = "UNLOCKED"
        )
        investmentDao.updateInvestment(updated)
    }

    // ADMIN - Approve Deposit
    suspend fun approveDeposit(transactionRefId: Int) {
        val tx = transactionDao.getTransactionByIdSync(transactionRefId) ?: return
        if (tx.status != "PENDING" || tx.type != "DEPOSIT") return

        val user = userDao.getUserByIdSync(tx.userId) ?: return
        var updatedBalance = user.walletBalance + tx.amount

        // Handle referral bonus: 5% of this deposit as referralEarnings
        if (!user.referredBy.isNullOrBlank()) {
            val referrer = userDao.getUserByReferralCode(user.referredBy)
            if (referrer != null) {
                val commission = tx.amount * 0.05
                val updatedReferrer = referrer.copy(
                    referralEarnings = referrer.referralEarnings + commission,
                    walletBalance = referrer.walletBalance + commission // payout directly to balance
                )
                userDao.updateUser(updatedReferrer)

                // Log a REFERRAL_BONUS transaction log for the referrer
                transactionDao.insertTransaction(
                    WalletTransaction(
                        userId = referrer.id,
                        type = "REFERRAL_BONUS",
                        amount = commission,
                        gateway = "Wallet System",
                        reference = "Ref bonus from @${user.username}",
                        timestamp = System.currentTimeMillis(),
                        status = "APPROVED"
                    )
                )
            }
        }

        // Apply wallet update
        userDao.updateUser(user.copy(walletBalance = updatedBalance))
        transactionDao.updateTransaction(tx.copy(status = "APPROVED"))
    }

    // ADMIN - Reject Deposit
    suspend fun rejectDeposit(transactionRefId: Int) {
        val tx = transactionDao.getTransactionByIdSync(transactionRefId) ?: return
        if (tx.status != "PENDING" || tx.type != "DEPOSIT") return
        transactionDao.updateTransaction(tx.copy(status = "REJECTED"))
    }

    // ADMIN - Approve Withdrawal
    suspend fun approveWithdrawal(transactionRefId: Int) {
        val tx = transactionDao.getTransactionByIdSync(transactionRefId) ?: return
        if (tx.status != "PENDING" || tx.type != "WITHDRAWAL") return
        // Balance was deducted when requesting, simply approve transaction status
        transactionDao.updateTransaction(tx.copy(status = "APPROVED"))
    }

    // ADMIN - Reject Withdrawal & Refund Wallet Balance
    suspend fun rejectWithdrawal(transactionRefId: Int) {
        val tx = transactionDao.getTransactionByIdSync(transactionRefId) ?: return
        if (tx.status != "PENDING" || tx.type != "WITHDRAWAL") return

        val user = userDao.getUserByIdSync(tx.userId) ?: return
        userDao.updateUser(user.copy(walletBalance = user.walletBalance + tx.amount))
        transactionDao.updateTransaction(tx.copy(status = "REJECTED"))
    }

    // ADMIN - Manage KYC
    suspend fun approveVerification(userId: Int) {
        val user = userDao.getUserByIdSync(userId) ?: return
        userDao.updateUser(user.copy(verificationStatus = "VERIFIED"))
    }

    suspend fun rejectVerification(userId: Int) {
        val user = userDao.getUserByIdSync(userId) ?: return
        userDao.updateUser(user.copy(verificationStatus = "REJECTED"))
    }

    // ADMIN - Create Product
    suspend fun addInvestmentProduct(product: InvestmentProduct) {
        productDao.insertProduct(product)
    }

    // ADMIN - Delete Product
    suspend fun removeProduct(productId: Int) {
        val product = productDao.getProductByIdSync(productId)
        if (product != null) {
            productDao.deleteProduct(product)
        }
    }

    // ADMIN - Send Announcement
    suspend fun createAnnouncement(title: String, content: String) {
        val ann = Announcement(
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        announcementDao.insertAnnouncement(ann)
    }

    // WALLET & WHEEL - Spin Wheel and earn a bonus once a month
    suspend fun spinWheelAndEarn(userId: Int, amount: Double, timestamp: Long) {
        val user = userDao.getUserByIdSync(userId) ?: return
        // Add reward to wallet balance
        val updatedUser = user.copy(
            walletBalance = user.walletBalance + amount,
            lastSpinTimestamp = timestamp // Records the last spin timestamp!
        )
        userDao.updateUser(updatedUser)

        // Record a transaction for audit visibility
        val tx = WalletTransaction(
            userId = userId,
            type = "REFERRAL_BONUS",
            amount = amount,
            gateway = "Invexa Lucky Wheel",
            reference = "Monthly Wheel Spin Reward",
            timestamp = timestamp,
            status = "APPROVED"
        )
        transactionDao.insertTransaction(tx)
    }

    // MOBILE MONEY LINKS
    fun getLinkedAccountsForUser(userId: Int): Flow<List<LinkedMobileMoneyAccount>> {
        return linkedAccountDao.getAccountsByUserId(userId)
    }

    suspend fun linkMobileMoneyAccount(account: LinkedMobileMoneyAccount): Long {
        return linkedAccountDao.insertAccount(account)
    }

    suspend fun unlinkMobileMoneyAccount(accountId: Int) {
        linkedAccountDao.deleteAccountById(accountId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun insertTransaction(transaction: WalletTransaction): Long {
        return transactionDao.insertTransaction(transaction)
    }
}
