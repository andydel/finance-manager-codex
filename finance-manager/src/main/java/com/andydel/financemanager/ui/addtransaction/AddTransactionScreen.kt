package com.andydel.financemanager.ui.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andydel.financemanager.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionUiState,
    onAmountChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onTransactionTypeChange: (TransactionType) -> Unit,
    onTransactionDateSelected: (Instant) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val displayDate = remember(state.transactionInstant) {
        val date = state.transactionInstant.atZone(ZoneOffset.UTC).toLocalDate()
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Transaction details")
        OutlinedTextField(
            value = state.amount,
            onValueChange = onAmountChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") }
        )

        val accounts = state.accounts
        DropdownField(
            label = "Account",
            currentValue = accounts.firstOrNull { it.id == state.selectedAccountId }?.name ?: "Select account",
            options = accounts.map { it.name },
            onOptionSelected = { index -> accounts.getOrNull(index)?.let { onAccountSelected(it.id) } }
        )

        val categories = state.categories
        DropdownField(
            label = "Category",
            currentValue = categories.firstOrNull { it.id == state.selectedCategoryId }?.name ?: "Select category",
            options = categories.map { it.name },
            onOptionSelected = { index -> categories.getOrNull(index)?.let { onCategorySelected(it.id) } }
        )

        DateSelector(
            label = "Date",
            displayValue = displayDate,
            onClick = { showDatePicker = true }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TransactionType.values().forEach { type ->
                FilterChip(
                    selected = state.transactionType == type,
                    onClick = { onTransactionTypeChange(type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        state.errorMessage?.let { Text(text = it) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            enabled = state.canSave && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (state.isSaving) "Saving..." else "Save")
        }

        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Cancel")
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(state.transactionInstant) {
            val utcDate = state.transactionInstant.atZone(ZoneOffset.UTC).toLocalDate()
            utcDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis,
            yearRange = DatePickerDefaults.YearRange
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val localDate = Instant.ofEpochMilli(selected)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val utcInstant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                            onTransactionDateSelected(utcInstant)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateSelector(
    label: String,
    displayValue: String,
    onClick: () -> Unit
) {
    Column {
        Text(text = label)
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = displayValue)
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel by rememberUpdatedState(newValue = currentValue)

    Column {
        Text(text = label)
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
            enabled = options.isNotEmpty()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = displayLabel)
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
