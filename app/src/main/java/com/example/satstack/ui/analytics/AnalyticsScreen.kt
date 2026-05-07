package com.example.satstack.ui.analytics

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = viewModel()) {
    val context = LocalContext.current
    val fngEntries by vm.fngEntries.collectAsState()
    val portfolio by vm.portfolio.collectAsState()
    val currency by vm.currency.collectAsState()
    val exchangeUrl by vm.exchangeUrl.collectAsState()
    val isOffline by vm.isOffline.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    // Fetch on tab open and start 5-min refresh loop
    LaunchedEffect(Unit) {
        vm.refresh()
        vm.startPriceRefreshLoop()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Analytics",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )

        // Offline banner
        if (isOffline) {
            Surface(
                color = Color(0xFFD32F2F),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Offline Mode — data may be outdated",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Fear & Greed card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Market Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading && fngEntries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (fngEntries.isNotEmpty()) {
                    val current = fngEntries.first()
                    val fngColor = fngColor(current.value)

                    Text(
                        text = "Fear & Greed Index",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(current.value / 100f)
                                .fillMaxHeight()
                                .background(fngColor, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = current.label,
                            fontWeight = FontWeight.Bold,
                            color = fngColor,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${current.value}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Historical Values card
        if (fngEntries.size >= 4) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Historical Values", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    val labels = listOf("Now", "Yesterday", "Last Week", "Last Month")
                    fngEntries.forEachIndexed { i, entry ->
                        if (i > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(labels.getOrElse(i) { "" }, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    formatDate(entry.timestamp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(fngColor(entry.value), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = entry.label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text("${entry.value}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Portfolio card (5a)
        portfolio?.let { p ->
            val curSymbol = if (currency == "GBP") "£" else "$"
            val currentPrice = if (currency == "GBP") p.btcPriceGbp else p.btcPriceUsd
            val stackValue = currentPrice?.let { it * p.totalSats / 100_000_000.0 }
            val pnl = stackValue?.minus(p.totalSpent)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your Portfolio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    PortfolioRow(
                        label = "BTC Price",
                        value = currentPrice?.let { "$curSymbol${"%,.2f".format(it)}" } ?: "--"
                    )
                    PortfolioRow(
                        label = "Stack Value",
                        value = stackValue?.let { "$curSymbol${"%,.2f".format(it)}" } ?: "--"
                    )
                    PortfolioRow(
                        label = "Total Spent",
                        value = "$curSymbol${"%,.2f".format(p.totalSpent)}"
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (pnl != null) {
                        val pnlColor = if (pnl >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        val pnlPct = if (p.totalSpent > 0) pnl / p.totalSpent * 100 else 0.0
                        val sign = if (pnl >= 0) "+" else ""
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("P&L", fontWeight = FontWeight.Bold)
                            Text(
                                text = "$sign$curSymbol${"%,.2f".format(pnl)} ($sign${"%.1f".format(pnlPct)}%)",
                                color = pnlColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Last updated ${p.lastUpdated}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Buy More Sats button
        Button(
            onClick = {
                val url = exchangeUrl.ifBlank { "https://www.coinbase.com" }
                // android.content.Intent.ACTION_VIEW to open exchange URL in browser
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7931A))
        ) {
            Text("Buy More Sats", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PortfolioRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun fngColor(value: Int): Color = when {
    value <= 25 -> Color(0xFFF44336)   // Extreme Fear — red
    value <= 45 -> Color(0xFFFF6600)   // Fear — orange
    value <= 55 -> Color(0xFFFFCC00)   // Neutral — yellow
    value <= 75 -> Color(0xFF8BC34A)   // Greed — light green
    else        -> Color(0xFF4CAF50)   // Extreme Greed — green
}

private fun formatDate(epochMs: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMs))
