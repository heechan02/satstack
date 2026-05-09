package com.example.satstack.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import kotlinx.coroutines.runBlocking

// Custom ContentProvider exposing the Room transactions table.
// ContentProvider callbacks run on binder threads, so runBlocking is safe here.
class TransactionProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.satstack.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/transactions")

        private const val TRANSACTIONS = 1
        private const val TRANSACTION_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "transactions", TRANSACTIONS)
            addURI(AUTHORITY, "transactions/#", TRANSACTION_ID)
        }

        val COLUMNS = arrayOf("id", "date", "fiatAmount", "currency", "sats")
    }

    private lateinit var dao: TransactionDao

    override fun onCreate(): Boolean {
        dao = SatStackDatabase.getInstance(context!!).transactionDao()
        return true
    }

    override fun getType(uri: Uri): String = when (uriMatcher.match(uri)) {
        TRANSACTIONS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.transactions"
        TRANSACTION_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.transactions"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        if (uriMatcher.match(uri) != TRANSACTIONS) throw IllegalArgumentException("Unknown URI: $uri")
        val rows = dao.getAllSync()
        val cursor = MatrixCursor(COLUMNS)
        rows.forEach { t ->
            cursor.addRow(arrayOf<Any>(t.id, t.date, t.fiatAmount, t.currency, t.sats))
        }
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        if (uriMatcher.match(uri) != TRANSACTIONS) throw IllegalArgumentException("Unknown URI: $uri")
        requireNotNull(values) { "ContentValues must not be null" }

        val transaction = Transaction(
            date = values.getAsLong("date") ?: System.currentTimeMillis(),
            fiatAmount = values.getAsDouble("fiatAmount") ?: 0.0,
            currency = values.getAsString("currency") ?: "GBP",
            sats = values.getAsLong("sats") ?: 0L
        )
        val newId = runBlocking { dao.insert(transaction) }
        context!!.contentResolver.notifyChange(CONTENT_URI, null)
        return ContentUris.withAppendedId(CONTENT_URI, newId)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (uriMatcher.match(uri) != TRANSACTION_ID) throw IllegalArgumentException("Unknown URI: $uri")
        val id = ContentUris.parseId(uri).toInt()
        val count = dao.deleteByIdSync(id)
        context!!.contentResolver.notifyChange(CONTENT_URI, null)
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = throw UnsupportedOperationException("Update not supported")
}
