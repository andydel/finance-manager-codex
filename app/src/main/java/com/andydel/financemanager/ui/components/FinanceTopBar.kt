package com.andydel.financemanager.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTopBar(
    title: String,
    onNavigateHome: () -> Unit,
    onAddAccount: () -> Unit,
    onShowSummary: () -> Unit,
    onShowSettings: () -> Unit,
    showManageAccounts: Boolean = false,
    isManagingAccounts: Boolean = false,
    onManageAccounts: (() -> Unit)? = null,
    onDoneManaging: (() -> Unit)? = null,
    onCancelManaging: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onNavigateHome) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Go home")
            }
        },
        actions = {
            if (isManagingAccounts && onDoneManaging != null && onCancelManaging != null) {
                TextButton(onClick = {
                    expanded = false
                    onCancelManaging()
                }) {
                    Text("Cancel")
                }
                TextButton(onClick = {
                    expanded = false
                    onDoneManaging()
                }) {
                    Text("Done")
                }
            } else {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Add account") },
                        onClick = {
                            expanded = false
                            onAddAccount()
                        }
                    )
                    if (showManageAccounts && onManageAccounts != null) {
                        DropdownMenuItem(
                            text = { Text("Manage accounts") },
                            onClick = {
                                expanded = false
                                onManageAccounts()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Summary") },
                        onClick = {
                            expanded = false
                            onShowSummary()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            expanded = false
                            onShowSettings()
                        }
                    )
                }
            }
        }
    )
}
