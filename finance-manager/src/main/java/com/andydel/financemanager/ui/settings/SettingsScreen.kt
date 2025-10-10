package com.andydel.financemanager.ui.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onNameChanged: (String) -> Unit,
    onCurrencySelected: (Long) -> Unit,
    onExchangeRateApiKeyChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Profile")
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") }
        )

        val currencies = state.currencies
        DropdownField(
            label = "Base currency",
            currentValue = currencies.firstOrNull { it.id == state.selectedCurrencyId }?.displayName ?: "Select currency",
            options = currencies.map { it.displayName },
            onOptionSelected = { index -> currencies.getOrNull(index)?.let { onCurrencySelected(it.id) } }
        )

        OutlinedTextField(
            value = state.exchangeRateApiKey,
            onValueChange = onExchangeRateApiKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Exchange rate API key") },
            supportingText = { Text("Leave blank to disable currency conversions") }
        )

        state.message?.let { Text(text = it) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            enabled = !state.isSaving && state.name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (state.isSaving) "Saving..." else "Save")
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
