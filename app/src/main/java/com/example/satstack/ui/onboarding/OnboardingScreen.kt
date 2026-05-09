package com.example.satstack.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
// androidx.compose.foundation:foundation — HorizontalPager, PagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.example.satstack.R
import com.example.satstack.data.ALL_CURRENCIES
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.SatStackDatabase
import com.example.satstack.data.Transaction
import com.example.satstack.data.appDataStore
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    var selectedCurrency by remember { mutableStateOf("GBP") }
    var milestoneGoal by remember { mutableStateOf("1000000") }
    var loadSampleData by remember { mutableStateOf(false) }
    var exchangeUrl by remember { mutableStateOf("") }

    fun finish() {
        scope.launch {
            context.appDataStore.edit { prefs ->
                prefs[DataStoreKeys.FIAT_CURRENCY] = selectedCurrency
                prefs[DataStoreKeys.MILESTONE_GOAL] = milestoneGoal.toLongOrNull() ?: 1_000_000L
                prefs[DataStoreKeys.EXCHANGE_URL] = exchangeUrl
                prefs[DataStoreKeys.ONBOARDING_COMPLETE] = true
            }
            if (loadSampleData) {
                insertSampleData(SatStackDatabase.getInstance(context), selectedCurrency)
            }
            onComplete()
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> WelcomePage(onNext = { scope.launch { pagerState.animateScrollToPage(1) } })
            1 -> CurrencyPage(
                selected = selectedCurrency,
                onSelect = { selectedCurrency = it },
                onNext = { scope.launch { pagerState.animateScrollToPage(2) } }
            )
            2 -> GoalPage(
                milestoneGoal = milestoneGoal,
                onGoalChange = { milestoneGoal = it },
                loadSampleData = loadSampleData,
                onLoadSampleDataChange = { loadSampleData = it },
                onNext = { scope.launch { pagerState.animateScrollToPage(3) } },
                onSkip = { scope.launch { pagerState.animateScrollToPage(3) } }
            )
            3 -> ExchangePage(
                exchangeUrl = exchangeUrl,
                onUrlChange = { exchangeUrl = it },
                onDone = { finish() },
                onSkip = { finish() }
            )
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Sat Stack",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(10.dp))
            Image(
                painter = painterResource(R.drawable.satstack_logo),
                contentDescription = "Sat Stack logo",
                modifier = Modifier.size(52.dp)
            )
        }

        Text(
            text = "Track your Bitcoin DCA Journey",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.weight(0.8f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Get Started", modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(Modifier.weight(0.4f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPage(selected: String, onSelect: (String) -> Unit, onNext: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) ALL_CURRENCIES
        else ALL_CURRENCIES.filter { (code, name) ->
            code.contains(query, ignoreCase = true) || name.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "What currency do you use?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(32.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it; if (!it) query = "" }
        ) {
            OutlinedTextField(
                value = if (expanded) query else selected,
                onValueChange = { query = it; expanded = true },
                label = { Text("Currency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false; query = "" }
            ) {
                if (filtered.isEmpty()) {
                    DropdownMenuItem(text = { Text("No results") }, onClick = {})
                } else {
                    filtered.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = {
                                Row {
                                    Text(code, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = { onSelect(code); expanded = false; query = "" }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Continue", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun GoalPage(
    milestoneGoal: String,
    onGoalChange: (String) -> Unit,
    loadSampleData: Boolean,
    onLoadSampleDataChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Set your first milestone",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = milestoneGoal,
            onValueChange = { onGoalChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Target (sats)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = loadSampleData,
                onCheckedChange = onLoadSampleDataChange
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Load sample data so I can explore the app first",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                Text("Skip")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun ExchangePage(
    exchangeUrl: String,
    onUrlChange: (String) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Where do you buy Bitcoin?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Add your exchange URL for quick access from Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = exchangeUrl,
            onValueChange = onUrlChange,
            label = { Text("Exchange URL (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                Text("Skip")
            }
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Done")
            }
        }
    }
}

private fun dateOf(month: Int, day: Int): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(2025, month - 1, day, 0, 0, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private suspend fun insertSampleData(db: SatStackDatabase, currency: String) {
    val dao = db.transactionDao()
    listOf(
        Transaction(date = dateOf(1, 5),  fiatAmount = 50.00,  currency = currency, sats = 52_400L),
        Transaction(date = dateOf(2, 3),  fiatAmount = 50.00,  currency = currency, sats = 55_100L),
        Transaction(date = dateOf(3, 1),  fiatAmount = 100.00, currency = currency, sats = 108_700L),
        Transaction(date = dateOf(4, 7),  fiatAmount = 50.00,  currency = currency, sats = 53_800L),
        Transaction(date = dateOf(5, 5),  fiatAmount = 75.00,  currency = currency, sats = 79_200L),
        Transaction(date = dateOf(6, 2),  fiatAmount = 50.00,  currency = currency, sats = 51_600L),
        Transaction(date = dateOf(7, 7),  fiatAmount = 100.00, currency = currency, sats = 103_500L),
        Transaction(date = dateOf(8, 4),  fiatAmount = 50.00,  currency = currency, sats = 49_900L),
        Transaction(date = dateOf(9, 1),  fiatAmount = 50.00,  currency = currency, sats = 48_300L),
        Transaction(date = dateOf(10, 6), fiatAmount = 75.00,  currency = currency, sats = 71_500L),
    ).forEach { dao.insert(it) }
}
