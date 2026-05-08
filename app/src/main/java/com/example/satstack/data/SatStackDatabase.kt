package com.example.satstack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// androidx.room:room-runtime
@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class SatStackDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var instance: SatStackDatabase? = null

        fun getInstance(context: Context): SatStackDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SatStackDatabase::class.java,
                    "satstack.db"
                ).build().also { instance = it }
            }
    }
}
