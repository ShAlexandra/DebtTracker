package com.example.debttracker.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Main : Screen("main")
    object DebtDetails : Screen("debt_details/{debtId}") {
        fun createRoute(debtId: Long) = "debt_details/$debtId"
    }
}
