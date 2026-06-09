package com.example.vibespendanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.vibespendanalyzer.data.AiSettingsStore
import com.example.vibespendanalyzer.data.ExpenseRepositoryProvider
import com.example.vibespendanalyzer.navigation.AppNavigation
import com.example.vibespendanalyzer.ui.theme.VibeSpendAnalyzerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AiSettingsStore.init(applicationContext)
        ExpenseRepositoryProvider.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            VibeSpendAnalyzerTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    VibeSpendAnalyzerTheme {
        AppNavigation()
    }
}
