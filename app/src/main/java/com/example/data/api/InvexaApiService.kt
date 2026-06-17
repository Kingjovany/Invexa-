package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.Serializable

// Real exchange rate API payload from https://open.er-api.com/v6/latest/USD
data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
) : Serializable

// Data payloads to synchronize user stats with cloud servers
data class WalletSyncPayload(
    val userId: Int,
    val username: String,
    val walletBalance: Double,
    val linkedAccountsCount: Int,
    val totalInvested: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class SyncStatusResponse(
    val status: String,      // SUCCESS, ERROR
    val serverMessage: String,
    val syncId: String,
    val backupSize: Int,
    val timestamp: Long
) : Serializable

interface InvexaApiService {

    // Real-world public financial exchange rates (CFA Franc / XAF)
    @GET("v6/latest/USD")
    suspend fun getUSDRates(): Response<ExchangeRateResponse>

    // Backup & Sync dynamic data securely on cloud backend
    @POST("v1/sync/wallet")
    suspend fun syncWalletBalance(@Body payload: WalletSyncPayload): Response<SyncStatusResponse>
}
