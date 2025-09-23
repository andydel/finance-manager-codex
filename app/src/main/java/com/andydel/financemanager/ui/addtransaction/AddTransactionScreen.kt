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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionUiState,
    onAmountChanged: (String) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onTransactionTypeChange: (TransactionType) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
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
