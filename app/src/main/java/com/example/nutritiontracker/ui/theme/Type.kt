package com.example.nutritiontracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// NOTE: text styles intentionally do NOT set a color. A hardcoded color here
// (e.g. TextPrimary) becomes part of the field's default text style and overrides
// the color scheme, so input text rendered dark-on-dark in dark mode. Leaving the
// color unspecified lets each component fall back to the theme (onSurface) or an
// explicitly provided color.
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
)