package com.example.satstack.ui.addentry

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.satstack.data.appDataStore
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.SatStackDatabase
import com.example.satstack.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEntryViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val dao = SatStackDatabase.getInstance(application).transactionDao()
    private val dataStore = application.appDataStore

    val satsInput = savedStateHandle.getStateFlow("sats", "")
    val fiatInput = savedStateHandle.getStateFlow("fiat", "")
    val selectedDateMs = savedStateHandle.getStateFlow("date", System.currentTimeMillis())

    val currency = dataStore.data
        .map { prefs -> prefs[DataStoreKeys.FIAT_CURRENCY] ?: "GBP" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "GBP")

    fun setSats(v: String) { savedStateHandle["sats"] = v }
    fun setFiat(v: String) { savedStateHandle["fiat"] = v }
    fun setDate(ms: Long) { savedStateHandle["date"] = ms }

    fun setCurrency(c: String) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[DataStoreKeys.FIAT_CURRENCY] = c }
        }
    }

    /** Returns true if validation passed and insert was launched. */
    fun save(): Boolean {
        val satsVal = satsInput.value.toLongOrNull() ?: return false
        val fiatVal = fiatInput.value.toDoubleOrNull() ?: return false
        if (satsVal <= 0 || fiatVal <= 0.0) return false
        viewModelScope.launch {
            dao.insert(
                Transaction(
                    date = selectedDateMs.value,
                    fiatAmount = fiatVal,
                    currency = currency.value,
                    sats = satsVal
                )
            )
        }
        return true
    }
}
