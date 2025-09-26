package com.andydel.financemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.andydel.financemanager.ui.FinanceManagerApp
import com.andydel.financemanager.ui.theme.FinanceManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as FinanceManagerApplication).appContainer.repository
        setContent {
            FinanceManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinanceManagerApp(repository = repository)
                }
            }
        }
    }
}
