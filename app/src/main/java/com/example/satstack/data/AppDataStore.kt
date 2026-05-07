package com.example.satstack.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// androidx.datastore:datastore-preferences
val Context.appDataStore by preferencesDataStore(name = "settings")

object DataStoreKeys {
    val FIAT_CURRENCY = stringPreferencesKey("fiat_currency")
}
