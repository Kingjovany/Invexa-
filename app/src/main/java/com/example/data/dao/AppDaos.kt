package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :identifier OR email = :identifier LIMIT 1")
    suspend fun getUserByUsernameOrEmail(identifier: String): User?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): User?

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface InvestmentProductDao {
    @Query("SELECT * FROM investment_products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<InvestmentProduct>>

    @Query("SELECT * FROM investment_products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSync(id: Int): InvestmentProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: InvestmentProduct)

    @Delete
    suspend fun deleteProduct(product: InvestmentProduct)
}

@Dao
interface UserInvestmentDao {
    @Query("SELECT * FROM user_investments WHERE userId = :userId ORDER BY startDate DESC")
    fun getInvestmentsByUserId(userId: Int): Flow<List<UserInvestment>>

    @Query("SELECT * FROM user_investments ORDER BY startDate DESC")
    fun getAllInvestments(): Flow<List<UserInvestment>>

    @Query("SELECT * FROM user_investments WHERE id = :id LIMIT 1")
    suspend fun getInvestmentByIdSync(id: Int): UserInvestment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: UserInvestment): Long

    @Update
    suspend fun updateInvestment(investment: UserInvestment)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserId(userId: Int): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM wallet_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionByIdSync(id: Int): WalletTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: WalletTransaction)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)
}

@Dao
interface LinkedMobileMoneyAccountDao {
    @Query("SELECT * FROM linked_mobile_money_accounts WHERE userId = :userId ORDER BY dateLinked DESC")
    fun getAccountsByUserId(userId: Int): Flow<List<LinkedMobileMoneyAccount>>

    @Query("SELECT * FROM linked_mobile_money_accounts WHERE userId = :userId ORDER BY dateLinked DESC")
    suspend fun getAccountsByUserIdSync(userId: Int): List<LinkedMobileMoneyAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: LinkedMobileMoneyAccount): Long

    @Query("DELETE FROM linked_mobile_money_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Int)
}
