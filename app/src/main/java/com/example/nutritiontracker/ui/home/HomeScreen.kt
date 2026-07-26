// ui/home/HomeScreen.kt
package com.example.nutritiontracker.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.camera.CameraController
import com.example.nutritiontracker.ui.components.HeaderSection
import com.example.nutritiontracker.ui.theme.GreenPrimary
import com.example.nutritiontracker.R
import com.example.nutritiontracker.data.fdc.NutritionSummary
import com.example.nutritiontracker.data.RDIRequirements

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    cameraController: CameraController,
    onScanClick: () -> Unit,
    onBarcodeEntered: (String) -> Unit,
    onManualEntry: (NutritionSummary) -> Unit,
    onSettingsClick: () -> Unit = {},
    rdiRequirements: RDIRequirements? = null,
    foodLog: List<NutritionSummary> = emptyList()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        HeaderSection(
            title = "Nutrition Tracker",
            showSettings = true,
            onSettingsClick = onSettingsClick
        )

        Spacer(Modifier.height(8.dp))

        TodayProgressCard(foodLog, rdiRequirements)
        Spacer(Modifier.height(24.dp))
        AddFoodSection(cameraController, onScanClick)
        Spacer(Modifier.height(24.dp))
        FoodActionsRow(onBarcodeEntered, onManualEntry)
        Spacer(Modifier.height(24.dp))
        TodaysLogCard(foodLog)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun TodayProgressCard(foodLog: List<NutritionSummary>, rdiRequirements: RDIRequirements?) {

    val totalProtein = foodLog.sumOf { it.protein ?: 0.0 }
    val totalCarbs = foodLog.sumOf { it.totalCarbs ?: 0.0 }
    val totalFiber = foodLog.sumOf { it.fiber ?: 0.0 }

    val totalVitaminC = foodLog.sumOf { it.vitaminC ?: 0.0 }
    val totalVitaminD = foodLog.sumOf { it.vitaminD ?: 0.0 }
    val totalCalcium = foodLog.sumOf { it.calcium ?: 0.0 }
    val totalIron = foodLog.sumOf { it.iron ?: 0.0 }

    val proteinTarget = rdiRequirements?.protein ?: 40.0
    val carbsTarget = rdiRequirements?.carbohydrates ?: 130.0
    val fiberTarget = rdiRequirements?.fiber ?: 25.0

    val vitaminCTarget = (rdiRequirements?.vitaminC?.toDouble() ?: 75.0)
    val vitaminDTarget = (rdiRequirements?.vitaminD?.toDouble() ?: 15.0)
    val calciumTarget = (rdiRequirements?.calcium?.toDouble() ?: 1000.0)
    val ironTarget = (rdiRequirements?.iron ?: 18.0)

    // Each nutrient is tracked against its own target and shown on its own bar.
    // The card previously summed raw amounts across incompatible units (e.g.
    // Vitamin C in mg + Vitamin D in µg, or calcium ~1000 mg + iron ~18 mg) into a
    // single ratio, which was physically meaningless and let the largest-magnitude
    // nutrient dominate. Per-nutrient bars keep every nutrient legible and correct.
    val macros = listOf(
        NutrientProgress("Protein", totalProtein, proteinTarget, "g"),
        NutrientProgress("Carbohydrates", totalCarbs, carbsTarget, "g"),
        NutrientProgress("Fiber", totalFiber, fiberTarget, "g")
    )
    val vitamins = listOf(
        NutrientProgress("Vitamin C", totalVitaminC, vitaminCTarget, "mg"),
        NutrientProgress("Vitamin D", totalVitaminD, vitaminDTarget, "µg")
    )
    val minerals = listOf(
        NutrientProgress("Calcium", totalCalcium, calciumTarget, "mg"),
        NutrientProgress("Iron", totalIron, ironTarget, "mg")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            NutrientGroup("Macro-nutrients", macros, Color(0xFF4285F4))
            Spacer(modifier = Modifier.height(16.dp))
            NutrientGroup("Vitamins", vitamins, Color(0xFFF9A825))
            Spacer(modifier = Modifier.height(16.dp))
            NutrientGroup("Minerals", minerals, Color(0xFFE53935))
        }
    }
}

/** A single nutrient's logged amount measured against its daily target. */
private data class NutrientProgress(
    val label: String,
    val value: Double,
    val target: Double,
    val unit: String
) {
    val progress: Float = if (target > 0.0) (value / target).coerceIn(0.0, 1.0).toFloat() else 0f
    val valueText: String = "${value.toInt()} / ${target.toInt()} $unit"
    val remainingText: String =
        if (progress >= 1f) "Goal met"
        else "${(target - value).coerceAtLeast(0.0).toInt()} $unit to goal"
}

@Composable
private fun NutrientGroup(title: String, nutrients: List<NutrientProgress>, barColor: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
    Spacer(modifier = Modifier.height(8.dp))
    nutrients.forEachIndexed { index, nutrient ->
        NutrientProgressRow(
            label = nutrient.label,
            valueText = nutrient.valueText,
            remainingText = nutrient.remainingText,
            progress = nutrient.progress,
            barColor = barColor
        )
        if (index != nutrients.lastIndex) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NutrientProgressRow(
    label: String,
    valueText: String,
    remainingText: String,
    progress: Float,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = remainingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AddFoodSection(cameraController: CameraController, onScanClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Add Food",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onScanClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Scan",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Scan Food",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Use camera to scan barcode",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun FoodActionsRow(onBarcodeEntered: (String) -> Unit, onManualEntry: (NutritionSummary) -> Unit) {
    var showEnterCodeDialog by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var codeInput by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var totalCarbs by remember { mutableStateOf("") }
    var totalFat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var vitaminC by remember { mutableStateOf("") }
    var vitaminD by remember { mutableStateOf("") }
    var calcium by remember { mutableStateOf("") }
    var iron by remember { mutableStateOf("") }

    // Reset every manual-entry field. Using one helper avoids the earlier bug where
    // some dismiss/confirm handlers forgot to clear `iron`/`totalFat`, leaving stale
    // values in the dialog the next time it was opened.
    val resetManualFields = {
        description = ""; calories = ""; protein = ""; totalCarbs = ""
        totalFat = ""; fiber = ""; vitaminC = ""; vitaminD = ""; calcium = ""; iron = ""
    }

    // Derive dialog colors from the active theme so they read correctly in both
    // light and dark mode instead of being hardcoded to a dark palette.
    val dialogContainerColor = MaterialTheme.colorScheme.surface
    val dialogFieldColor = MaterialTheme.colorScheme.surfaceVariant
    val dialogContentColor = MaterialTheme.colorScheme.onSurface
    val dialogSecondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val manualTextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = dialogContentColor,
        unfocusedTextColor = dialogContentColor,
        focusedContainerColor = dialogFieldColor,
        unfocusedContainerColor = dialogFieldColor,
        disabledContainerColor = dialogFieldColor,
        focusedLabelColor = dialogContentColor,
        unfocusedLabelColor = dialogSecondaryTextColor,
        focusedPlaceholderColor = dialogSecondaryTextColor,
        unfocusedPlaceholderColor = dialogSecondaryTextColor,
        focusedIndicatorColor = dialogContentColor.copy(alpha = 0.75f),
        unfocusedIndicatorColor = dialogContentColor.copy(alpha = 0.55f),
        cursorColor = GreenPrimary,
        focusedTrailingIconColor = dialogContentColor,
        unfocusedTrailingIconColor = dialogContentColor
    )
    val manualOutlinedTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = dialogContentColor,
        unfocusedTextColor = dialogContentColor,
        focusedLabelColor = dialogContentColor,
        unfocusedLabelColor = dialogSecondaryTextColor,
        focusedPlaceholderColor = dialogSecondaryTextColor,
        unfocusedPlaceholderColor = dialogSecondaryTextColor,
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.55f),
        focusedContainerColor = dialogContainerColor,
        unfocusedContainerColor = dialogContainerColor,
        cursorColor = GreenPrimary,
        focusedTrailingIconColor = dialogContentColor,
        unfocusedTrailingIconColor = dialogContentColor
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FoodActionCard(
            title = "Enter Code",
            subtitle = "UPC/GTIN",
            icon = Icons.Filled.Tag,
            modifier = Modifier
                .weight(1f)
                .clickable { showEnterCodeDialog = true }
        )
        FoodActionCard(
            title = "Manual Entry",
            subtitle = "Enter facts",
            icon = Icons.Filled.Description,
            modifier = Modifier
                .weight(1f)
                .clickable { showManualEntryDialog = true }
        )
    }
    if (showEnterCodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showEnterCodeDialog = false
                codeInput = ""
            },
            confirmButton = {
                TextButton(
                    enabled = codeInput.isNotBlank(),
                    onClick = {
                        val cleanedCode = codeInput.trim().filter { it.isDigit() }
                        if (cleanedCode.isNotEmpty()) {
                            onBarcodeEntered(cleanedCode)
                            codeInput = ""
                            showEnterCodeDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Submit",
                        color = if (codeInput.isNotBlank()) GreenPrimary else dialogSecondaryTextColor.copy(alpha = 0.45f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEnterCodeDialog = false
                    codeInput = ""
                }) { Text("Cancel", color = dialogSecondaryTextColor) }
            },
            containerColor = dialogContainerColor,
            titleContentColor = dialogContentColor,
            textContentColor = dialogContentColor,
            title = { Text("Enter UPC/GTIN", color = dialogContentColor) },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(R.drawable.barcode_gtin_example),
                            contentDescription = "Barcode GTIN number example",
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Enter the Barcode number below:", color = dialogSecondaryTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("UPC/GTIN code", color = dialogSecondaryTextColor) },
                        placeholder = { Text("Example: 123456789123", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = manualOutlinedTextFieldColors
                    )
                }
            },
        )
    }
    if (showManualEntryDialog) {
        AlertDialog(
            onDismissRequest = {
                showManualEntryDialog = false
                resetManualFields()
            },
            confirmButton = {
                TextButton(onClick = {
                    val summary = NutritionSummary(
                        description = description,
                        calories = calories.toDoubleOrNull(),
                        protein = protein.toDoubleOrNull(),
                        totalCarbs = totalCarbs.toDoubleOrNull(),
                        totalFat = totalFat.toDoubleOrNull(),
                        fiber = fiber.toDoubleOrNull(),
                        vitaminC = vitaminC.toDoubleOrNull(),
                        vitaminD = vitaminD.toDoubleOrNull(),
                        calcium = calcium.toDoubleOrNull(),
                        iron = iron.toDoubleOrNull(),
                    )
                    onManualEntry(summary)
                    resetManualFields()
                    showManualEntryDialog = false
                }) { Text("Submit", color = GreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManualEntryDialog = false
                    resetManualFields()
                }) { Text("Cancel", color = dialogSecondaryTextColor) }
            },
            containerColor = dialogContainerColor,
            titleContentColor = dialogContentColor,
            textContentColor = dialogContentColor,
            title = { Text("Manual Entry", color = dialogContentColor) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Enter the Nutritional Facts Below:", color = dialogSecondaryTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Food Name", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualOutlinedTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = totalCarbs,
                        onValueChange = { totalCarbs = it },
                        label = { Text("Carbohydrates", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Fiber", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = vitaminC,
                        onValueChange = { vitaminC = it },
                        label = { Text("Vitamin C", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = vitaminD,
                        onValueChange = { vitaminD = it },
                        label = { Text("Vitamin D", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = calcium,
                        onValueChange = { calcium = it },
                        label = { Text("Calcium", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = iron,
                        onValueChange = { iron = it },
                        label = { Text("Iron", color = dialogSecondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = manualTextFieldColors
                    )
                }
            },
        )
    }
}

@Composable
fun FoodActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(96.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun TodaysLogCard(foodLog: List<NutritionSummary> = emptyList()) {
    val totalCalories = foodLog.sumOf { it.calories ?: 0.0 }.toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Today's Log", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "$totalCalories kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            if (foodLog.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                foodLog.forEach { food ->
                    LogItemRow(
                        title = food.description ?: "Unknown Food",
                        subtitle = "1 serving",
                        kcal = "${food.calories?.toInt() ?: 0} kcal",
                        macros = "P: ${food.protein?.toInt() ?: 0}g · C: ${food.totalCarbs?.toInt() ?: 0}g · F: ${food.totalFat?.toInt() ?: 0}g"
                    )
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "No food logged yet today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LogItemRow(
    title: String,
    subtitle: String,
    kcal: String,
    macros: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(140.dp)) {
            Text(text = kcal, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = macros, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Details", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}