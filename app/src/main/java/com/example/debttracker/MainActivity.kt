package com.example.debttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debttracker.ui.main.MainScreen
import com.example.debttracker.ui.main.MainViewModel
import com.example.debttracker.ui.main.MainViewModelFactory
import com.example.debttracker.ui.theme.DebtTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as DebtTrackerApplication
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(app.repository))
            DebtTrackerTheme(dynamicColor = false) {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DebtTrackerTheme(dynamicColor = false) {
        Greeting("Android")
    }
}