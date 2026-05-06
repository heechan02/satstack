package com.example.satstack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// androidx.room:room-runtime
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,           // epoch milliseconds
    val fiatAmount: Double,
    val currency: String,     // "GBP" or "USD"
    val sats: Long
)
