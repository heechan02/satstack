package com.example.satstack.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.SatStackDatabase
import com.example.satstack.data.Transaction
import com.example.satstack.data.appDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SatStackDatabase.getInstance(application).transactionDao()
    private val dataStore = application.appDataStore

    val transactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestoneGoal: StateFlow<Long> = dataStore.data
        .map { it[DataStoreKeys.MILESTONE_GOAL] ?: 1_000_000L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1_000_000L)

    val currency: StateFlow<String> = dataStore.data
        .map { it[DataStoreKeys.FIAT_CURRENCY] ?: "GBP" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "GBP")

    private val _isPrivate = MutableStateFlow(false)
    val isPrivate: StateFlow<Boolean> = _isPrivate.asStateFlow()

    fun togglePrivacy() {
        _isPrivate.value = !_isPrivate.value
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { dao.deleteById(transaction.id) }
    }
}
