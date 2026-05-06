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

    // Synchronous query used by TransactionProvider (ContentProvider runs on binder thread)
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllSync(): List<Transaction>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Synchronous delete used by TransactionProvider
    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteByIdSync(id: Int): Int
}
