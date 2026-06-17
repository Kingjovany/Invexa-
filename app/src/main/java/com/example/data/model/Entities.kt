package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String, // Kept simple for standard local persistence sandbox
    val fullName: String,
    val referralCode: String,
    val referredBy: String? = null,
    val walletBalance: Double = 0.0,
    val referralEarnings: Double = 0.0,
    // Verification state: UNVERIFIED, PENDING, VERIFIED, REJECTED
    val verificationStatus: String = "VERIFIED",
    val idType: String = "Auto-Verified",
    val idNumber: String = "9999",
    val isAdmin: Boolean = false,
    val lastSpinTimestamp: Long = 0L
) : Serializable

@Entity(tableName = "investment_products")
data class InvestmentProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val expectedYieldPercent: Double,  // e.g. 15.0 for 15%
    val riskLevel: String,             // LOW, MEDIUM, HIGH
    val termDays: Int = 30,            // Lock period
    val minAmount: Double,
    val description: String,
    val category: String               // Real Estate, Telecom, Crypto, Agriculture
) : Serializable

@Entity(tableName = "user_investments")
data class UserInvestment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val productTitle: String,
    val yieldPercent: Double,
    val amount: Double,
    val startDate: Long,               // Timestamp (ms)
    val lockUntilDate: Long,           // Timestamp (ms, 30 days after startDate)
    val projectedEarnings: Double,      // Yield projection
    val status: String = "ACTIVE",     // ACTIVE, UNLOCKED, CLAIMED
) : Serializable

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String,                  // DEPOSIT, WITHDRAWAL, INVESTMENT, REFERRAL_BONUS, EARNINGS_CLAIM
    val amount: Double,
    val gateway: String,               // MTN Mobile Money, Orange Money, PayPal, Wallet
    val reference: String,             // TX ref ID
    val timestamp: Long,
    val status: String = "PENDING"     // PENDING, APPROVED, REJECTED
) : Serializable

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long
) : Serializable

@Entity(tableName = "linked_mobile_money_accounts")
data class LinkedMobileMoneyAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val provider: String,      // MTN Mobile Money or Orange Money
    val accountName: String,   // User's name
    val accountNumber: String, // Cameroon 9-digit mobile plan number
    val dateLinked: Long = System.currentTimeMillis()
) : Serializable
