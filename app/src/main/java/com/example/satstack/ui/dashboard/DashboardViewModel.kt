package com.example.satstack.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.satstack.data.SatStackDatabase
import com.example.satstack.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SatStackDatabase.getInstance(application).transactionDao()

    val transactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Placeholder until DataStore is wired in step 8 (Settings)
    val milestoneGoal: Long = 1_000_000L

    fun delete(transaction: Transaction) {
        viewModelScope.launch { dao.deleteById(transaction.id) }
    }
}
