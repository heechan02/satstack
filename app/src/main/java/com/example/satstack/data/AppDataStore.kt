package com.example.satstack.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// androidx.datastore:datastore-preferences
val Context.appDataStore by preferencesDataStore(name = "settings")

object DataStoreKeys {
    val FIAT_CURRENCY = stringPreferencesKey("fiat_currency")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val MILESTONE_GOAL = longPreferencesKey("milestone_goal")
    val EXCHANGE_URL = stringPreferencesKey("exchange_url")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}
