package com.example.satstack.ui.addentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.satstack.data.currencySymbol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntrySheetContent(
    onDismiss: () -> Unit,
    vm: AddEntryViewModel = viewModel()
) {
    val satsInput by vm.satsInput.collectAsState()
    val fiatInput by vm.fiatInput.collectAsState()
    val currency by vm.currency.collectAsState()
    val selectedDateMs by vm.selectedDateMs.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMs)
    val dateLabel = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMs))
    val currencySymbol = currencySymbol(currency)
    val satsLong = satsInput.toLongOrNull() ?: 0L
    val fiatDouble = fiatInput.toDoubleOrNull() ?: 0.0
    val impliedPricePerBtc = if (satsLong > 0) fiatDouble / (satsLong / 100_000_000.0) else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            "Add DCA Entry",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // ── Sats section ──────────────────────────────────────────────
        Text("Amount (Sats)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = satsInput,
            onValueChange = { vm.setSats(it.filter(Char::isDigit)) },
            placeholder = { Text("0", fontSize = 32.sp) },
            suffix = { Text("Sats", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,   // always orange
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            )
        )
        // Preview line — shows implied BTC price once both fields have values
        if (satsLong > 0 && fiatDouble > 0.0) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Implied price: $currencySymbol${"%,.0f".format(impliedPricePerBtc)} / BTC",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // ── Fiat section ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Amount (Fiat)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(currency, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = fiatInput,
            onValueChange = { vm.setFiat(it) },
            placeholder = { Text("0.00") },
            prefix = { Text(currencySymbol) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // ── Date section ──────────────────────────────────────────────
        Text("Date of Purchase", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(dateLabel)
        }

        // Validation error
        validationError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(20.dp))

        // ── Save button ───────────────────────────────────────────────
        Button(
            onClick = {
                if (vm.save()) {
                    onDismiss()
                } else {
                    validationError = "Enter valid sats and fiat values greater than zero."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
        ) {
            Text("Save Entry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "All entries are manual and stored locally.\nVerify details before saving.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Date picker dialog — androidx.compose.material3:material3
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { vm.setDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
