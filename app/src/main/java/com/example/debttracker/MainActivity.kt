package com.example.debttracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.debttracker.ui.debtDetails.BindDebtDetailsScreen
import com.example.debttracker.ui.debtDetails.DebtDetailsViewModelFactory
import com.example.debttracker.ui.main.BindMainScreen
import com.example.debttracker.ui.navigation.Screen
import com.example.debttracker.ui.theme.DebtTrackerTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Запрос разрешения на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val deepLinkDebtId = intent?.getLongExtra("open_debt_id", -1L)?.takeIf { it != -1L }

        setContent {
            val app = application as DebtTrackerApplication

            DebtTrackerTheme(dynamicColor = false) {
                AppNavGraph(
                    repository = app.repository,
                    deepLinkDebtId = deepLinkDebtId
                )
            }
        }
    }
}

@Composable
fun AppNavGraph(
    repository: com.example.debttracker.data.repository.Repository,
    deepLinkDebtId: Long? = null
) {
    val navController = rememberNavController()

    if (deepLinkDebtId != null) {
        navController.navigate(Screen.DebtDetails.createRoute(deepLinkDebtId))
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            val app = LocalContext.current.applicationContext as DebtTrackerApplication
            val viewModel: com.example.debttracker.ui.main.MainViewModel =
                viewModel(factory = com.example.debttracker.ui.main.MainViewModel.factory(app, repository))
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