package com.example.nutritiontracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val screen: Screen,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    NavItem(Screen.Home, Icons.Filled.Home),
    NavItem(Screen.Goals, Icons.Filled.CheckCircle)
)