package com.example.satstack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// androidx.room:room-ktx
@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)
}
