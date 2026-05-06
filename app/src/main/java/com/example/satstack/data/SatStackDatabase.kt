package com.example.satstack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

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
                ).addCallback(SeedCallback()).build().also { instance = it }
            }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert demo data on first launch so the Dashboard has something to show.
                // Uses raw SQL because the DAO isn't available until after build() completes.
                val rows = listOf(
                    Triple(dateOf(2025, 1, 5),  50.00  to 52_400L,  "GBP"),
                    Triple(dateOf(2025, 2, 3),  50.00  to 55_100L,  "GBP"),
                    Triple(dateOf(2025, 3, 1),  100.00 to 108_700L, "GBP"),
                    Triple(dateOf(2025, 4, 7),  50.00  to 53_800L,  "GBP"),
                    Triple(dateOf(2025, 5, 5),  75.00  to 79_200L,  "GBP"),
                    Triple(dateOf(2025, 6, 2),  50.00  to 51_600L,  "GBP"),
                    Triple(dateOf(2025, 7, 7),  100.00 to 103_500L, "GBP"),
                    Triple(dateOf(2025, 8, 4),  50.00  to 49_900L,  "GBP"),
                    Triple(dateOf(2025, 9, 1),  50.00  to 48_300L,  "GBP"),
                    Triple(dateOf(2025, 10, 6), 75.00  to 71_500L,  "GBP"),
                )
                rows.forEach { (date, amountSats, currency) ->
                    val (fiat, sats) = amountSats
                    db.execSQL(
                        "INSERT INTO transactions (date, fiatAmount, currency, sats) VALUES (?, ?, ?, ?)",
                        arrayOf(date, fiat, currency, sats)
                    )
                }
            }

            // Returns epoch millis for the given date (month is 1-based)
            private fun dateOf(year: Int, month: Int, day: Int): Long {
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month - 1, day, 0, 0, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                return cal.timeInMillis
            }
        }
    }
}
