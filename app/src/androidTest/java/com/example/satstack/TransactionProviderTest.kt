package com.example.satstack

import android.content.ContentValues
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.satstack.data.TransactionProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented tests for TransactionProvider (ContentProvider)
// Verifies insert / query / delete via the content:// URI interface
@RunWith(AndroidJUnit4::class)
class TransactionProviderTest {

    private val contentResolver by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
    }
    private val uri = TransactionProvider.CONTENT_URI

    @Before
    fun clearTable() {
        // Query all and delete each so tests start with a clean state
        val cursor = contentResolver.query(uri, null, null, null, null) ?: return
        val idIndex = cursor.getColumnIndexOrThrow("id")
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idIndex)
            contentResolver.delete(Uri.withAppendedPath(uri, id.toString()), null, null)
        }
        cursor.close()
    }

    @Test
    fun insert_addsRowToProvider() {
        val values = ContentValues().apply {
            put("date", 1_700_000_000_000L)
            put("fiatAmount", 50.0)
            put("currency", "GBP")
            put("sats", 100_000L)
        }
        val resultUri = contentResolver.insert(uri, values)
        assertNotNull("Insert should return a non-null URI", resultUri)
    }

    @Test
    fun query_returnsInsertedRow() {
        val values = ContentValues().apply {
            put("date", 1_700_000_001_000L)
            put("fiatAmount", 25.0)
            put("currency", "USD")
            put("sats", 50_000L)
        }
        contentResolver.insert(uri, values)

        val cursor = contentResolver.query(uri, null, null, null, null)
        assertNotNull(cursor)
        assertEquals("Cursor should contain exactly 1 row", 1, cursor!!.count)

        cursor.moveToFirst()
        assertEquals(25.0, cursor.getDouble(cursor.getColumnIndexOrThrow("fiatAmount")), 0.001)
        assertEquals("USD", cursor.getString(cursor.getColumnIndexOrThrow("currency")))
        assertEquals(50_000L, cursor.getLong(cursor.getColumnIndexOrThrow("sats")))
        cursor.close()
    }

    @Test
    fun delete_removesRow() {
        val values = ContentValues().apply {
            put("date", 1_700_000_002_000L)
            put("fiatAmount", 10.0)
            put("currency", "GBP")
            put("sats", 20_000L)
        }
        val insertedUri = contentResolver.insert(uri, values)!!
        val id = insertedUri.lastPathSegment

        val rowsDeleted = contentResolver.delete(Uri.withAppendedPath(uri, id), null, null)
        assertEquals("Should delete exactly 1 row", 1, rowsDeleted)

        val cursor = contentResolver.query(uri, null, null, null, null)
        assertNotNull(cursor)
        assertEquals("Table should be empty after delete", 0, cursor!!.count)
        cursor.close()
    }
}
