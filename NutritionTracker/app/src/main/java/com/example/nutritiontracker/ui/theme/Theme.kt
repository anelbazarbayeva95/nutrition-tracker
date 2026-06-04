package com.example.nutritiontracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = OrangePrimary,
    tertiary = PurplePrimary,
    error = RedError,
    background = GrayBackground,
    onBackground = TextPrimary
)

@Composable
fun NutritionTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}