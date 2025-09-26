package com.andydel.financemanager.ui.addaccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andydel.financemanager.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    state: AddAccountUiState,
    onNameChanged: (String) -> Unit,
    onBalanceChanged: (String) -> Unit,
    onTypeSelected: (AccountType) -> Unit,
    onCurrencySelected: (Long) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val titleLabel = if (state.mode == AccountFormMode.EDIT) "Edit account" else "Account details"
    val actionLabel = when {
        state.isSaving -> "Saving..."
        state.mode == AccountFormMode.EDIT -> "Update"
        else -> "Save"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = titleLabel)
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") }
        )
        OutlinedTextField(
            value = state.initialBalance,
            onValueChange = onBalanceChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Opening balance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        DropdownField(
            label = "Account type",
            currentValue = state.selectedType.displayName,
            options = AccountType.values().toList().map { it.displayName },
            onOptionSelected = { index -> onTypeSelected(AccountType.values()[index]) }
        )

        val currencies = state.availableCurrencies
        DropdownField(
            label = "Currency",
            currentValue = currencies.firstOrNull { it.id == state.selectedCurrencyId }?.displayName
                ?: "Select currency",
            options = currencies.map { it.displayName },
            onOptionSelected = { index -> currencies.getOrNull(index)?.let { onCurrencySelected(it.id) } }
        )

        state.errorMessage?.let {
            Text(text = it)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            enabled = state.canSave && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = actionLabel)
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
