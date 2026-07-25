package com.example.debttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.debttracker.ui.debtDetails.BindDebtDetailsScreen
import com.example.debttracker.ui.debtDetails.DebtDetailsViewModelFactory
import com.example.debttracker.ui.main.BindMainScreen
import com.example.debttracker.ui.main.MainViewModelFactory
import com.example.debttracker.ui.navigation.Screen
import com.example.debttracker.ui.theme.DebtTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as DebtTrackerApplication

            DebtTrackerTheme(dynamicColor = false) {
                AppNavGraph(repository = app.repository)
            }
        }
    }
}

@Composable
fun AppNavGraph(repository: com.example.debttracker.data.repository.Repository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            val viewModel: com.example.debttracker.ui.main.MainViewModel =
                viewModel(factory = MainViewModelFactory(repository))
            BindMainScreen(
                viewModel = viewModel,
                onDebtClick = { debtId ->
                    navController.navigate(Screen.DebtDetails.createRoute(debtId))
                }
            )
        }

        composable(
            route = Screen.DebtDetails.route,
            arguments = listOf(
                navArgument("debtId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getLong("debtId") ?: return@composable
            val viewModel: com.example.debttracker.ui.debtDetails.DebtDetailsViewModel =
                viewModel(factory = DebtDetailsViewModelFactory(repository, debtId))
            BindDebtDetailsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPaymentClick = { /* TODO: handle payment click */ }
            )
        }
    }
}
