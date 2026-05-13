package com.example.satstack.ui.addentry

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.pm.PackageManager
// androidx.core:core-ktx
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
// androidx.lifecycle:lifecycle-runtime-ktx
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.satstack.MainActivity
import com.example.satstack.R
import com.example.satstack.data.appDataStore
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.SatStackDatabase
import com.example.satstack.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
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

    /** Returns true if validation passed and insert was launched. */
    fun save(): Boolean {
        val satsVal = satsInput.value.toLongOrNull() ?: return false
        val fiatVal = fiatInput.value.toDoubleOrNull() ?: return false
        if (satsVal <= 0 || fiatVal <= 0.0) return false
        viewModelScope.launch {
            val totalBefore = dao.totalSats()
            dao.insert(
                Transaction(
                    date = selectedDateMs.value,
                    fiatAmount = fiatVal,
                    currency = currency.value,
                    sats = satsVal
                )
            )
            val totalAfter = dao.totalSats()
            val milestone = dataStore.data.first()[DataStoreKeys.MILESTONE_GOAL] ?: 1_000_000L
            if (totalBefore < milestone && totalAfter >= milestone) {
                sendMilestoneNotification(milestone)
            }
        }
        return true
    }

    private fun sendMilestoneNotification(milestone: Long) {
        val app = getApplication<Application>()
        if (app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(app, MainActivity.MILESTONE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Milestone reached!")
            .setContentText("Your stack hit ${"%,d".format(milestone)} sats!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        app.getSystemService(NotificationManager::class.java).notify(1, notification)
    }
}
