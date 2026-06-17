package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        InvestmentProduct::class,
        UserInvestment::class,
        WalletTransaction::class,
        Announcement::class,
        LinkedMobileMoneyAccount::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): InvestmentProductDao
    abstract fun investmentDao(): UserInvestmentDao
    abstract fun transactionDao(): WalletTransactionDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun linkedAccountDao(): LinkedMobileMoneyAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "investment_app_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
