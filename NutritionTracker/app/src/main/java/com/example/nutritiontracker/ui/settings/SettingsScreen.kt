// ui/settings/SettingsScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.nutritiontracker.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritiontracker.data.SettingsData
import com.example.nutritiontracker.ui.components.HeaderSection
import com.example.nutritiontracker.ui.theme.GrayBackground
import com.example.nutritiontracker.utils.RDICalculator

@Composable
fun SettingsScreen(
    onCustomizeClick: () -> Unit = {},
    onRDICalculate: (com.example.nutritiontracker.data.RDIRequirements, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val savedSettings by viewModel.settings.observeAsState(SettingsData())
    val saveSuccess by viewModel.saveSuccess.observeAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }

    var isEditMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(savedSettings.name) }
    var age by remember { mutableStateOf(savedSettings.age) }
    var gender by remember { mutableStateOf(savedSettings.gender) }
    var height by remember { mutableStateOf(savedSettings.height) }
    var weight by remember { mutableStateOf(savedSettings.weight) }
    var bodyFat by remember { mutableStateOf(savedSettings.bodyFat) }
    var activityLevel by remember { mutableStateOf(savedSettings.activityLevel) }

    LaunchedEffect(savedSettings) {
        name = savedSettings.name
        age = savedSettings.age
        gender = savedSettings.gender
        height = savedSettings.height
        weight = savedSettings.weight
        bodyFat = savedSettings.bodyFat
        activityLevel = savedSettings.activityLevel

        // Check if any settings are saved
        if (name.isNotEmpty() || age.isNotEmpty() || gender.isNotEmpty()) {
            isEditMode = false
        } else {
            isEditMode = true
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Settings saved successfully!",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSaveSuccessFlag()
            isEditMode = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GrayBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBackground)
                .padding(paddingValues)
        ) {
            HeaderSection(
                title = "Settings",
                showSettings = false,
                showBackButton = true,
                onBackClick = onBackClick
            )

            if (isEditMode) {
                // Edit Mode - Show editable sections
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 120.dp),  // Extra padding for navigation bar and dropdowns
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PersonalInformationSection(
                        name = name,
                        onNameChange = { name = it },
                        age = age,
                        onAgeChange = { age = it },
                        gender = gender,
                        onGenderChange = { gender = it }
                    )
                    PhysicalDetailsSection(
                        height = height,
                        onHeightChange = { height = it },
                        weight = weight,
                        onWeightChange = { weight = it },
                        bodyFat = bodyFat,
                        onBodyFatChange = { bodyFat = it },
                        activityLevel = activityLevel,
                        onActivityLevelChange = { activityLevel = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveSettings(
                                SettingsData(
                                    name = name,
                                    age = age,
                                    gender = gender,
                                    height = height,
                                    weight = weight,
                                    bodyFat = bodyFat,
                                    activityLevel = activityLevel
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Save Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // View Mode - Show summary with customize button
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 100.dp),  // Extra padding for navigation bar
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSummaryView(
                        name = name,
                        age = age,
                        gender = gender,
                        height = height,
                        weight = weight,
                        bodyFat = bodyFat,
                        activityLevel = activityLevel
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // RDI Calculator Button
                    Button(
                        onClick = {
                            val ageInt = age.toIntOrNull() ?: 25
                            val weightDouble = weight.toDoubleOrNull() ?: 70.0
                            val heightDouble = height.toDoubleOrNull() ?: 170.0

                            val rdi = RDICalculator.calculateRDI(
                                age = ageInt,
                                gender = gender,
                                weight = weightDouble,
                                height = heightDouble,
                                activityLevel = activityLevel
                            )
                            onRDICalculate(rdi, name)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Personalized RDI Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Customize Button
                    Button(
                        onClick = { isEditMode = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Customize Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PersonalInformationSection(
    name: String,
    onNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female")

    ExpandableSection(
        title = "Personal Information",
        expanded = expanded,
        onExpandToggle = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            OutlinedTextField(
                value = age,
                onValueChange = onAgeChange,
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onGenderChange(option)
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhysicalDetailsSection(
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    bodyFat: String,
    onBodyFatChange: (String) -> Unit,
    activityLevel: String,
    onActivityLevelChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var activityLevelExpanded by remember { mutableStateOf(false) }

    val activityLevels = listOf(
        "Sedentary (little or no exercise)",
        "Lightly Active (1-3 days/week)",
        "Moderately Active (3-5 days/week)",
        "Very Active (6-7 days/week)",
        "Extra Active (very intense exercise)"
    )

    ExpandableSection(
        title = "Physical Details",
        expanded = expanded,
        onExpandToggle = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            OutlinedTextField(
                value = bodyFat,
                onValueChange = onBodyFatChange,
                label = { Text("Body Fat (%)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            ExposedDropdownMenuBox(
                expanded = activityLevelExpanded,
                onExpandedChange = { activityLevelExpanded = !activityLevelExpanded }
            ) {
                OutlinedTextField(
                    value = activityLevel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity Level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityLevelExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                ExposedDropdownMenu(
                    expanded = activityLevelExpanded,
                    onDismissRequest = { activityLevelExpanded = false }
                ) {
                    activityLevels.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onActivityLevelChange(option)
                                activityLevelExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                   // modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSummaryView(
    name: String,
    age: String,
    gender: String,
    height: String,
    weight: String,
    bodyFat: String,
    activityLevel: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Personal Information Summary
        SummaryCard(title = "Personal Information") {
            if (name.isNotEmpty()) SummaryItem(label = "Name", value = name)
            if (age.isNotEmpty()) SummaryItem(label = "Age", value = age)
            if (gender.isNotEmpty()) SummaryItem(label = "Gender", value = gender)
        }

        // Physical Details Summary
        SummaryCard(title = "Physical Details") {
            if (height.isNotEmpty()) SummaryItem(label = "Height", value = "$height cm")
            if (weight.isNotEmpty()) SummaryItem(label = "Weight", value = "$weight kg")
            if (bodyFat.isNotEmpty()) SummaryItem(label = "Body Fat", value = "$bodyFat%")
            if (activityLevel.isNotEmpty()) SummaryItem(label = "Activity Level", value = activityLevel)
        }
    }
}

@Composable
fun SummaryCard(
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
fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF1E293B),
            fontWeight = FontWeight.SemiBold
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 1.dp,
        color = Color(0xFFE2E8F0)
    )
}