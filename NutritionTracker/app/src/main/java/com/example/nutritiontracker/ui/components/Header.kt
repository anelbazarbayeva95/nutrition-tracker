// ui/components/Header.kt
package com.example.nutritiontracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.ui.theme.GreenPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HeaderSection(
    title: String,
    showSettings: Boolean = true,
    showBackButton: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val todayText = remember {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
        LocalDate.now().format(formatter)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(GreenPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = todayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                if (showSettings) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}