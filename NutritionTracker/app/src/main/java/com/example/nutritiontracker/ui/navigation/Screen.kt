package com.example.nutritiontracker.ui.navigation

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object AddFood : Screen("add_food", "Add Food")
    object Goals : Screen("goals", "Goals")
    object Settings : Screen("settings", "Settings")

    object Camera : Screen("camera", "Camera")
    object RDIResults : Screen("rdi_results", "RDI Results")
}