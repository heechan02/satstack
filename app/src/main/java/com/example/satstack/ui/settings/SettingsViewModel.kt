package com.example.satstack.ui.settings

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.appDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.appDataStore

    val darkMode = dataStore.data
        .map { it[DataStoreKeys.DARK_MODE] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val milestoneGoal = dataStore.data
        .map { it[DataStoreKeys.MILESTONE_GOAL] ?: 1_000_000L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1_000_000L)

    val exchangeUrl = dataStore.data
        .map { it[DataStoreKeys.EXCHANGE_URL] ?: "https://strike.me" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://strike.me")

    val currency = dataStore.data
        .map { it[DataStoreKeys.FIAT_CURRENCY] ?: "GBP" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "GBP")

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[DataStoreKeys.DARK_MODE] = enabled }
        }
    }

    fun setMilestoneGoal(goal: Long) {
        viewModelScope.launch {
            dataStore.edit { it[DataStoreKeys.MILESTONE_GOAL] = goal }
        }
    }

    fun setExchangeUrl(url: String) {
        viewModelScope.launch {
            dataStore.edit { it[DataStoreKeys.EXCHANGE_URL] = url }
        }
    }

    fun setCurrency(c: String) {
        viewModelScope.launch {
            dataStore.edit { it[DataStoreKeys.FIAT_CURRENCY] = c }
        }
    }
}
