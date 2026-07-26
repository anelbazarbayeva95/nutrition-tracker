package com.example.nutritiontracker.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nutritiontracker.camera.CameraController
import com.example.nutritiontracker.camera.CameraScreen
import com.example.nutritiontracker.data.RDIRequirements
import com.example.nutritiontracker.ui.goals.GoalsScreen
import com.example.nutritiontracker.ui.home.AddFoodSection
import com.example.nutritiontracker.ui.home.HomeScreen
import com.example.nutritiontracker.ui.rdi.RDIResultsScreen
import com.example.nutritiontracker.ui.settings.SettingsScreen
import com.example.nutritiontracker.data.fdc.FDCHelper
import com.example.nutritiontracker.data.fdc.FdcIdDetails
import com.example.nutritiontracker.data.fdc.NutritionResults
import com.example.nutritiontracker.data.fdc.NutritionSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.platform.LocalContext
import com.example.nutritiontracker.data.local.DailyLogEntity
import com.example.nutritiontracker.data.local.NutritionDatabase




@Composable
fun NutritionApp(cameraController: CameraController) {   // still passed from MainActivity
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var rdiRequirements by remember { mutableStateOf<RDIRequirements?>(null) }
    var userName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val fdcHelper = remember { FDCHelper() }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    // This is where the Scanned food's Name and FDC ID will be stored
    var scannedFoodFound by remember { mutableStateOf<FdcIdDetails?>(null)}
    // This is where the Scanned food's Nutritional facts will be stored
    var nutritionalFacts by remember {mutableStateOf<NutritionSummary?>(null)}
    // Stores any Errors obtained from processing the barcode with the FDC API
    var obtainedErrors by remember { mutableStateOf<String?>(null) }
    // List of today's logged food items
    var todaysFoodLog by remember { mutableStateOf<List<NutritionSummary>>(emptyList()) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val dailyLogDao = remember {
        NutritionDatabase.getInstance(context).dailyLogDao()
    }

    fun upsertToday(foodLog: List<NutritionSummary>) {
        val today = LocalDate.now().toString()
        val calories = foodLog.sumOf { (it.calories ?: 0).toInt() }

        coroutineScope.launch(Dispatchers.IO) {
            dailyLogDao.upsertLog(
                DailyLogEntity(
                    date = today,
                    caloriesConsumed = calories,
                    caloriesTarget = 0
                )
            )
        }
    }



    cameraController.barcodeScannedReturnHome { barcode ->
        scannedBarcode = barcode
        selectedScreen = Screen.Home

    }

    val manualEntryCallback: (NutritionSummary) -> Unit = {summary ->
        nutritionalFacts = summary
        scannedFoodFound = null
        selectedScreen = Screen.Home

        // Add to today's food log
        todaysFoodLog = todaysFoodLog + summary
        upsertToday(todaysFoodLog)

        Log.i("Manual Entry Success", "Food found: ${summary.description}")
        Log.i(
            "Manual Entry Success",
            "Calories: ${summary.calories}," +
                    "Protein: ${summary.protein}, " +
                    "Carbs ${summary.totalCarbs}, " +
                    "Fat: ${summary.totalFat}, " +
                    "Fiber: ${summary.fiber}, " +
                    "VitaminC ${summary.vitaminC}, " +
                    "VitaminD: ${summary.vitaminD}, " +
                    "Calcium: ${summary.calcium}, "
        )
    }

    val processBarcodeCallback: (String) -> Unit = { barcode ->
        scannedBarcode = barcode
        selectedScreen = Screen.Home
        obtainedErrors = null
        scannedFoodFound = null
        nutritionalFacts = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val results: NutritionResults = fdcHelper.getNutritionFactsFromBarcodeType(barcode)

                //Logging is for testing to ensure data is populated correctly
                Log.i("API_SUCCESS", "Food found: ${results.details.description}, FDC ID: ${results.details.fdcId}")
                Log.i("API_SUCCESS", "Food found: ${results.summary.description}, Calories: ${results.summary.calories}")
                Log.i(
                    "API_SUCCESS",
                    "Calories: ${results.summary.calories}," +
                            "Protein: ${results.summary.protein}, " +
                            "Carbs ${results.summary.totalCarbs}, " +
                            "Fat: ${results.summary.totalFat}, " +
                            "Fiber: ${results.summary.fiber}, " +
                            "VitaminC ${results.summary.vitaminC}, " +
                            "VitaminD: ${results.summary.vitaminD}, " +
                            "Calcium: ${results.summary.calcium}, "
                )

                scannedFoodFound = results.details
                nutritionalFacts = results.summary

                // Add to today's food log
                todaysFoodLog = todaysFoodLog + results.summary
                upsertToday(todaysFoodLog)
            } catch (e: Exception) {
                obtainedErrors = when (e) {
                    is NoSuchElementException -> "Food not Found"
                    is IllegalArgumentException -> "Invalid barcode"
                    else -> "Network error: ${e.message}"
                }
                scannedFoodFound = null
            }
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        cameraController.barcodeScannedReturnHome(processBarcodeCallback)
        onDispose {
        }
    }


    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedScreen.route == item.screen.route,
                        onClick = { selectedScreen = item.screen },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.screen.label
                            )
                        },
                        label = { Text(item.screen.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedScreen) {
            Screen.Home -> HomeScreen(
                onSettingsClick = { selectedScreen = Screen.Settings },
                modifier = Modifier.padding(paddingValues),
                cameraController = cameraController,
                onScanClick = { selectedScreen = Screen.Camera },
                onBarcodeEntered = processBarcodeCallback,
                onManualEntry = manualEntryCallback,
                foodLog = todaysFoodLog
            )

            //TODO: Remove this call and reference to the AddFood screen as this is not a screen but
            // a function on the home screen
            Screen.AddFood -> AddFoodSection(
                cameraController = TODO(),
                onScanClick = TODO()
            )

            Screen.Camera -> CameraScreen(cameraController)

            Screen.Goals -> GoalsScreen(
                onSettingsClick = { selectedScreen = Screen.Settings },
                foodLog = todaysFoodLog
            )

            Screen.Settings -> SettingsScreen(
                onRDICalculate = { rdi, name ->
                    rdiRequirements = rdi
                    userName = name
                    selectedScreen = Screen.RDIResults
                },
                onBackClick = { selectedScreen = Screen.Home }
            )

            Screen.RDIResults -> {
                rdiRequirements?.let { rdi ->
                    RDIResultsScreen(
                        rdiRequirements = rdi,
                        userName = userName,
                        onBack = { selectedScreen = Screen.Settings }
                    )
                }
            }
        }
    }
}