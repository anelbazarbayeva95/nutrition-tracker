package com.example.nutritiontracker.ui.rdi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.RDIRequirements
import com.example.nutritiontracker.ui.components.HeaderSection
import com.example.nutritiontracker.ui.theme.GrayBackground

@Composable
fun RDIResultsScreen(
    rdiRequirements: RDIRequirements,
    userName: String = "User",
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBackground)
    ) {
        HeaderSection(
            title = "Your RDI",
            showSettings = false,
            showBackButton = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User greeting card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Hello ${if (userName.isNotEmpty()) userName else "there"}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Based on your profile, here are your personalized daily nutritional requirements:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Energy section
            RDISection(title = "Energy & Macronutrients") {
                RDIItem(label = "Calories", value = "${rdiRequirements.calories}", unit = "kcal")
                RDIItem(label = "Protein", value = String.format("%.1f", rdiRequirements.protein), unit = "g")
                RDIItem(label = "Carbohydrates", value = String.format("%.1f", rdiRequirements.carbohydrates), unit = "g")
                RDIItem(label = "Fiber", value = String.format("%.1f", rdiRequirements.fiber), unit = "g")
            }

            // Minerals section
            RDISection(title = "Minerals") {
                RDIItem(label = "Calcium", value = "${rdiRequirements.calcium}", unit = "mg")
                RDIItem(label = "Iron", value = String.format("%.1f", rdiRequirements.iron), unit = "mg")
                RDIItem(label = "Magnesium", value = "${rdiRequirements.magnesium}", unit = "mg")
                RDIItem(label = "Phosphorus", value = "${rdiRequirements.phosphorus}", unit = "mg")
                RDIItem(label = "Potassium", value = "${rdiRequirements.potassium}", unit = "mg")
                RDIItem(label = "Sodium", value = "${rdiRequirements.sodium}", unit = "mg", isUpperLimit = true)
                RDIItem(label = "Zinc", value = String.format("%.1f", rdiRequirements.zinc), unit = "mg")
            }

            // Vitamins section
            RDISection(title = "Vitamins") {
                RDIItem(label = "Vitamin A", value = "${rdiRequirements.vitaminA}", unit = "mcg RAE")
                RDIItem(label = "Vitamin C", value = "${rdiRequirements.vitaminC}", unit = "mg")
                RDIItem(label = "Vitamin D", value = "${rdiRequirements.vitaminD}", unit = "mcg")
                RDIItem(label = "Vitamin E", value = String.format("%.1f", rdiRequirements.vitaminE), unit = "mg")
                RDIItem(label = "Vitamin K", value = "${rdiRequirements.vitaminK}", unit = "mcg")
                RDIItem(label = "Thiamin (B1)", value = String.format("%.1f", rdiRequirements.thiamin), unit = "mg")
                RDIItem(label = "Riboflavin (B2)", value = String.format("%.1f", rdiRequirements.riboflavin), unit = "mg")
                RDIItem(label = "Niacin (B3)", value = String.format("%.1f", rdiRequirements.niacin), unit = "mg NE")
                RDIItem(label = "Vitamin B6", value = String.format("%.1f", rdiRequirements.vitaminB6), unit = "mg")
                RDIItem(label = "Folate (B9)", value = "${rdiRequirements.folate}", unit = "mcg DFE")
                RDIItem(label = "Vitamin B12", value = String.format("%.1f", rdiRequirements.vitaminB12), unit = "mcg")
            }

            // Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Note:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "These values are based on USDA Dietary Reference Intakes (DRI). Individual needs may vary. Please consult with a healthcare professional for personalized nutrition advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D4C41)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RDISection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun RDIItem(
    label: String,
    value: String,
    unit: String,
    isUpperLimit: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            if (isUpperLimit) {
                Text(
                    text = "(Upper Limit)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF5350)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 1.dp,
        color = Color(0xFFE2E8F0)
    )
}
