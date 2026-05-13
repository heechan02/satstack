package com.example.satstack.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// androidx.compose.material:material-icons-extended
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.foundation.clickable
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.TooltipState
import com.example.satstack.ui.addentry.AddEntrySheetContent
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// androidx.lifecycle:lifecycle-viewmodel-compose
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.satstack.R
import com.example.satstack.data.Transaction
import com.example.satstack.data.currencySymbol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val isPrivate by viewModel.isPrivate.collectAsState()
    val milestoneGoal by viewModel.milestoneGoal.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Scroll to top whenever a new entry is added
    LaunchedEffect(transactions.size) {
        if (transactions.isNotEmpty()) listState.animateScrollToItem(0)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val totalSats = transactions.sumOf { it.sats }
    // Use the user's selected fiat currency from DataStore (set in Settings).
    // Each history item renders its own currency symbol independently.
    val stackSymbol = currencySymbol(currency)
    val totalFiat = transactions.filter { it.currency == currency }.sumOf { it.fiatAmount }
    val progress = if (milestoneGoal > 0) (totalSats.toFloat() / milestoneGoal).coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()

    if (isLandscape) {
        // Landscape: single scrollable LazyColumn — everything scrolls together
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                item {
                    StackCard(isPrivate, totalSats, totalFiat, stackSymbol, tooltipState, scope) {
                        viewModel.togglePrivacy()
                    }
                }
                item { MilestoneProgress(progress, progressPercent, milestoneGoal) }
                item { Text("DCA History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
                transactionItems(transactions, onDelete = { viewModel.delete(it) })
            }
            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add DCA Entry")
            }
        }
    } else {
        // Portrait: stack card + progress bar pinned, DCA history scrolls independently
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            StackCard(isPrivate, totalSats, totalFiat, stackSymbol, tooltipState, scope) {
                viewModel.togglePrivacy()
            }
            Spacer(modifier = Modifier.height(16.dp))
            MilestoneProgress(progress, progressPercent, milestoneGoal)
            Spacer(modifier = Modifier.height(16.dp))
            Text("DCA History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No entries yet.\nTap + to add your first DCA.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        transactionItems(transactions, onDelete = { viewModel.delete(it) })
                    }
                }
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 4.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add DCA Entry")
                }
            }
        }
    }

    // Add DCA Entry bottom sheet — slides up over Dashboard without navigating away
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            dragHandle = {
                androidx.compose.material3.BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            AddEntrySheetContent(onDismiss = { showAddSheet = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StackCard(
    isPrivate: Boolean,
    totalSats: Long,
    totalFiat: Double,
    stackSymbol: String,
    tooltipState: TooltipState,
    scope: CoroutineScope,
    onDoubleTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Stack", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isPrivate) "****** SATS" else "%,d SATS".format(totalSats),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Double-tap card to hide balance") } },
                    state = tooltipState
                ) {
                    Icon(
                        if (isPrivate) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { scope.launch { tooltipState.show() } }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (isPrivate) "Double-tap to reveal your balance"
                else "($stackSymbol${"%.2f".format(totalFiat)} ≈ ${"%.5f".format(totalSats / 100_000_000.0)} BTC)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MilestoneProgress(progress: Float, progressPercent: Int, milestoneGoal: Long) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Stacking Milestone Progress: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Text("$progressPercent%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Goal: ${"%,d".format(milestoneGoal)} Sats",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.transactionItems(
    transactions: List<Transaction>,
    onDelete: (Transaction) -> Unit
) {
    if (transactions.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No entries yet.\nTap + to add your first DCA.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        items(transactions, key = { it.id }) { transaction ->
            SwipeToDismissBox(
                state = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onDelete(transaction); true
                        } else false
                    }
                ),
                enableDismissFromStartToEnd = false,
                modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFD32F2F))
                            .padding(end = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            ) {
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val currencySymbol = currencySymbol(transaction.currency)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bitcoin logo — already orange, no extra circle needed
            Image(
                painter = painterResource(R.drawable.satstack_logo),
                contentDescription = "Bitcoin",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Date + fiat — centred
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    formatDate(transaction.date),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$currencySymbol${"%.2f".format(transaction.fiatAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Sats in green
            Text(
                "+ %,d Sats".format(transaction.sats),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Green
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
