package com.example.satstack.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// androidx.compose.material:material-icons-extended
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import android.content.Intent
// androidx.core:core-ktx
import androidx.core.net.toUri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
// androidx.lifecycle:lifecycle-viewmodel-compose
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.satstack.data.ALL_CURRENCIES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val darkMode by viewModel.darkMode.collectAsState()
    val milestoneGoal by viewModel.milestoneGoal.collectAsState()
    val exchangeUrl by viewModel.exchangeUrl.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // ── Appearance ────────────────────────────────────────────────
        SettingsSectionLabel("Appearance")
        SettingsCard {
            SettingsRow(label = "Dark Mode") {
                Switch(
                    checked = darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Milestone Goal ────────────────────────────────────────────
        SettingsSectionLabel("Milestone Goal")
        SettingsCard {
            SettingsRow(label = "Target Sats") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%,d".format(milestoneGoal),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit milestone goal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Exchange URL ──────────────────────────────────────────────
        SettingsSectionLabel("Exchange")
        SettingsCard {
            ExchangeUrlRow(
                url = exchangeUrl,
                onUrlChange = { viewModel.setExchangeUrl(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Fiat Currency ─────────────────────────────────────────────
        SettingsSectionLabel("Fiat Currency")
        SettingsCard {
            CurrencyPickerRow(
                selected = currency,
                onSelect = { viewModel.setCurrency(it) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Changing currency affects how totals are displayed.\nYour entries keep their original currency.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showGoalDialog) {
        MilestoneGoalDialog(
            current = milestoneGoal,
            onConfirm = { goal ->
                viewModel.setMilestoneGoal(goal)
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        trailing()
    }
}

@Composable
private fun ExchangeUrlRow(url: String, onUrlChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var draft by remember(url) { mutableStateOf(url) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Exchange URL") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onUrlChange(draft)
                focusManager.clearFocus()
            })
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = {
            onUrlChange(draft)
            val intent = Intent(Intent.ACTION_VIEW, draft.toUri())
            context.startActivity(intent)
        }) {
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open exchange URL",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerRow(selected: String, onSelect: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    // Pending holds the user's pick before they confirm — doesn't save until Confirm is tapped
    var pending by remember(selected) { mutableStateOf(selected) }

    val filtered = remember(query) {
        if (query.isBlank()) ALL_CURRENCIES
        else ALL_CURRENCIES.filter { (code, name) ->
            code.contains(query, ignoreCase = true) || name.contains(query, ignoreCase = true)
        }
    }

    fun resetToReadOnly() {
        editing = false
        expanded = false
        query = ""
        pending = selected
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (!editing) {
            // Read-only display — transparent overlay captures taps since OutlinedTextField
            // consumes pointer events internally
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selected,
                    onValueChange = {},
                    label = { Text("Currency") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = "Change currency")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { editing = true; expanded = true }
                )
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                    if (!it) query = ""
                }
            ) {
                OutlinedTextField(
                    value = if (expanded) query else pending,
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
                                onClick = { pending = code; expanded = false; query = "" }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { resetToReadOnly() },
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                Button(
                    onClick = { onSelect(pending); editing = false },
                    enabled = pending != selected,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Confirm — $pending") }
            }
        }
    }
}

@Composable
private fun MilestoneGoalDialog(
    current: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(current.toString()) }
    val parsed = input.filter { it.isDigit() }.toLongOrNull()
    val isValid = parsed != null && parsed > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Milestone Goal") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() } },
                label = { Text("Target (sats)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = !isValid && input.isNotEmpty(),
                supportingText = {
                    if (!isValid && input.isNotEmpty()) Text("Enter a positive number")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid) onConfirm(parsed) },
                enabled = isValid
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
