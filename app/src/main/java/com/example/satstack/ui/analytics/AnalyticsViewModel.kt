package com.example.satstack.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.SatStackDatabase
import com.example.satstack.data.appDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class FngEntry(val value: Int, val label: String, val timestamp: Long)

data class PortfolioState(
    val btcPriceGbp: Double?,
    val btcPriceUsd: Double?,
    val totalSats: Long,
    val totalSpent: Double,
    val lastUpdated: String
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SatStackDatabase.getInstance(application).transactionDao()
    private val dataStore = application.appDataStore

    val currency: StateFlow<String> = dataStore.data
        .map { it[DataStoreKeys.FIAT_CURRENCY] ?: "GBP" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "GBP")

    val exchangeUrl: StateFlow<String> = dataStore.data
        .map { it[DataStoreKeys.EXCHANGE_URL] ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _fngEntries = MutableStateFlow<List<FngEntry>>(emptyList())
    val fngEntries: StateFlow<List<FngEntry>> = _fngEntries.asStateFlow()

    private val _portfolio = MutableStateFlow<PortfolioState?>(null)
    val portfolio: StateFlow<PortfolioState?> = _portfolio.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    fun setConnectivity(isConnected: Boolean) {
        _isOffline.value = !isConnected
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadFailed.value = false
            launch { fetchFearAndGreed() }
            launch { fetchPortfolio() }
        }
    }

    // 5-minute auto-refresh loop for portfolio price
    fun startPriceRefreshLoop() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L)
                fetchPortfolio()
            }
        }
    }

    // Fetch Fear & Greed Index — api.alternative.me
    private suspend fun fetchFearAndGreed() {
        try {
            val json = withContext(Dispatchers.IO) {
                // java.net.URL — standard library, no 3rd party needed
                URL("https://api.alternative.me/fng/?limit=31").readText()
            }
            val data = JSONObject(json).getJSONArray("data")
            fun entry(i: Int) = data.getJSONObject(i).let {
                FngEntry(
                    value = it.getInt("value"),
                    label = it.getString("value_classification"),
                    timestamp = it.getLong("timestamp") * 1000L
                )
            }
            // Pick now (0), yesterday (1), last week (7), last month (30)
            val entries = listOf(0, 1, 7, 30)
                .filter { it < data.length() }
                .map { entry(it) }
            _fngEntries.value = entries
            _isOffline.value = false
        } catch (_: Exception) {
            _loadFailed.value = true
        } finally {
            _isLoading.value = false
        }
    }

    // Fetch BTC price — CoinGecko free API, no key required
    // https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=gbp,usd
    private suspend fun fetchPortfolio() {
        // Use getAllSync() to avoid race condition where StateFlow hasn't emitted yet on tab open
        val txList = withContext(Dispatchers.IO) { dao.getAllSync() }
        val totalSats = txList.sumOf { it.sats }

        try {
            val json = withContext(Dispatchers.IO) {
                URL("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=gbp,usd")
                    .readText()
            }
            val bitcoin = JSONObject(json).getJSONObject("bitcoin")
            val priceGbp = bitcoin.getDouble("gbp")
            val priceUsd = bitcoin.getDouble("usd")

            val cur = currency.value
            val totalSpent = txList.filter { it.currency == cur }.sumOf { it.fiatAmount }

            val now = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())

            _portfolio.value = PortfolioState(
                btcPriceGbp = priceGbp,
                btcPriceUsd = priceUsd,
                totalSats = totalSats,
                totalSpent = totalSpent,
                lastUpdated = now
            )
        } catch (_: Exception) {
            // network or API error — connectivity state managed by NetworkReceiver
        }
    }
}
