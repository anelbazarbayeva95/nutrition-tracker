// ui/goals/GoalsViewModel.kt
package com.example.nutritiontracker.ui.goals

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.data.RDIRequirements
import com.example.nutritiontracker.data.SettingsRepository
import com.example.nutritiontracker.data.goals.GoalsRepository
import com.example.nutritiontracker.data.local.DailyLogEntity
import com.example.nutritiontracker.data.local.NutritionDatabase
import com.example.nutritiontracker.utils.RDICalculator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/* DATA MODELS FOR GOALS UI */

data class DailyGoalsUi(
    val caloriesCurrent: Int,
    val caloriesTarget: Int,
    val progressToCalorieGoal: Float,

    val proteinCurrent: Int,
    val proteinTarget: Int,

    val carbsCurrent: Int,
    val carbsTarget: Int,

    val fiberCurrent: Int,
    val fiberTarget: Int
)

data class WeeklyDayUi(
    val label: String,
    val percentOfGoal: Float
)

data class WeeklyGoalsUi(
    val days: List<WeeklyDayUi>
)

data class MonthlyWeekUi(
    val label: String,
    val daysMetGoal: Int
)

data class MonthlyGoalsUi(
    val totalDaysTracked: Int,
    val daysMetGoal: Int,
    val successRatePercent: Int,
    val weeks: List<MonthlyWeekUi>
)

data class GoalsUiState(
    val daily: DailyGoalsUi,
    val weekly: WeeklyGoalsUi,
    val monthly: MonthlyGoalsUi
)

/**
 * ViewModel that connects Daily, Weekly and Monthly goals.
 *
 * Source of truth:
 *  - Personalized RDI target  -> SettingsRepository + RDICalculator
 *  - Actual intake per day    -> DailyLogEntity in Room via GoalsRepository
 */
class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    // Internal model used for calculations
    private data class DailyLog(
        val dayIndex: Int,
        val caloriesConsumed: Int,
        val caloriesTarget: Int
    )

    private val goalsRepository: GoalsRepository
    private val settingsRepository: SettingsRepository

    private val _uiState = mutableStateOf(createEmptyState(rdi = null))
    val uiState: State<GoalsUiState> = _uiState

    init {
        val appContext = application.applicationContext
        val db = NutritionDatabase.getInstance(appContext)
        goalsRepository = GoalsRepository(db.dailyLogDao())
        settingsRepository = SettingsRepository(appContext)

        // Initial load
        refreshGoals()
    }
    /**
     * Reload goals using the latest Settings (RDI) and DB logs.
     * Call this when the user updates their profile or RDI.
     */
    fun refreshGoals() {
        viewModelScope.launch {
            // 1) Get personalized RDI for this user (current settings)
            val rdi: RDIRequirements = loadCurrentRdi()
            val calorieTarget = rdi.calories

            // 2) Load last up-to-31 days from DB
            val entities: List<DailyLogEntity> =
                goalsRepository.getLastDailyLogs(maxDays = 31)

            if (entities.isEmpty()) {
                // No logs yet: show RDI-based targets but 0 consumed
                _uiState.value = createEmptyState(rdi)
            } else {
                // 3) Map DB entities into internal DailyLog model
                val logs: List<DailyLog> = entities
                    .sortedBy { it.date } // oldest -> newest
                    .mapIndexed { index, e ->
                        DailyLog(
                            dayIndex         = index,
                            caloriesConsumed = e.caloriesConsumed,
                            // Always use CURRENT RDI target (not older stored target)
                            caloriesTarget   = calorieTarget
                        )
                    }

                _uiState.value = buildUiStateFromLogs(
                    logs = logs,
                    rdi  = rdi
                )
            }
        }
    }

    /** Empty state but with the proper calorie target from RDI. */
    private fun createEmptyState(rdi: RDIRequirements?): GoalsUiState {
        val calorieTarget = rdi?.calories ?: 0
        val proteinTarget = rdi?.protein?.roundToInt() ?: 0
        val carbsTarget   = rdi?.carbohydrates?.roundToInt() ?: 0
        val fiberTarget   = rdi?.fiber?.roundToInt() ?: 0

        val daily = DailyGoalsUi(
            caloriesCurrent       = 0,
            caloriesTarget        = calorieTarget,
            progressToCalorieGoal = 0f,

            proteinCurrent = 0,
            proteinTarget  = proteinTarget,

            carbsCurrent   = 0,
            carbsTarget    = carbsTarget,

            fiberCurrent   = 0,
            fiberTarget    = fiberTarget
        )

        val weekly = WeeklyGoalsUi(days = emptyList())

        val monthly = MonthlyGoalsUi(
            totalDaysTracked   = 0,
            daysMetGoal        = 0,
            successRatePercent = 0,
            weeks              = emptyList()
        )

        return GoalsUiState(
            daily   = daily,
            weekly  = weekly,
            monthly = monthly
        )
    }

    /** Load the current RDI based on saved user settings. */
    private fun loadCurrentRdi(): RDIRequirements {
        val settings = settingsRepository.loadSettings()

        val ageInt = settings.age.toIntOrNull() ?: 25
        val weightDouble = settings.weight.toDoubleOrNull() ?: 70.0
        val heightDouble = settings.height.toDoubleOrNull() ?: 170.0
        val gender = settings.gender
        val activityLevel = settings.activityLevel

        return RDICalculator.calculateRDI(
            age = ageInt,
            gender = gender,
            weight = weightDouble,
            height = heightDouble,
            activityLevel = activityLevel
        )
    }

    // ---- Mapping + aggregation logic ----

    private fun buildUiStateFromLogs(
        logs: List<DailyLog>,
        rdi: RDIRequirements
    ): GoalsUiState {
        require(logs.isNotEmpty())

        val latest = logs.last()

        // Targets from RDI
        val proteinTarget = rdi.protein.roundToInt()
        val carbsTarget   = rdi.carbohydrates.roundToInt()
        val fiberTarget   = rdi.fiber.roundToInt()

        // For now, macro CURRENT values are 0 – later we’ll wire them
        // from today’s food log totals.
        val dailyUi = DailyGoalsUi(
            caloriesCurrent       = latest.caloriesConsumed,
            caloriesTarget        = latest.caloriesTarget,
            progressToCalorieGoal =
                latest.caloriesConsumed.toFloat() / latest.caloriesTarget.toFloat(),

            proteinCurrent = 0,
            proteinTarget  = proteinTarget,

            carbsCurrent   = 0,
            carbsTarget    = carbsTarget,

            fiberCurrent   = 0,
            fiberTarget    = fiberTarget
        )

        val weeklyUi  = buildWeeklyUi(logs)
        val monthlyUi = buildMonthlyUi(logs)

        return GoalsUiState(
            daily   = dailyUi,
            weekly  = weeklyUi,
            monthly = monthlyUi
        )
    }

    private fun buildWeeklyUi(logs: List<DailyLog>): WeeklyGoalsUi {
        val last7 = logs.takeLast(7)
        val startIndex = logs.size - last7.size
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        val days = last7.mapIndexed { index, log ->
            val rawPercent =
                (log.caloriesConsumed.toFloat() / log.caloriesTarget.toFloat()) * 100f
            WeeklyDayUi(
                label = labels.getOrNull(index) ?: "D${startIndex + index + 1}",
                percentOfGoal = rawPercent
            )
        }

        return WeeklyGoalsUi(days = days)
    }

    private fun buildMonthlyUi(logs: List<DailyLog>): MonthlyGoalsUi {
        val lastDays = logs.takeLast(31)
        val totalDays = lastDays.size
        if (totalDays == 0) {
            return MonthlyGoalsUi(
                totalDaysTracked = 0,
                daysMetGoal = 0,
                successRatePercent = 0,
                weeks = emptyList()
            )
        }

        // Define “goal met” as being within 90–110% of calorie target.
        fun DailyLog.goalMet(): Boolean {
            val ratio = caloriesConsumed.toFloat() / caloriesTarget.toFloat()
            return ratio in 0.9f..1.1f
        }

        val daysMet = lastDays.count { it.goalMet() }
        val successRate = (daysMet * 100f / totalDays).toInt()

        val chunks = lastDays.chunked(7)
        val weeks = chunks.mapIndexed { index, weekLogs ->
            MonthlyWeekUi(
                label = "Week ${index + 1}",
                daysMetGoal = weekLogs.count { it.goalMet() }
            )
        }

        return MonthlyGoalsUi(
            totalDaysTracked = totalDays,
            daysMetGoal = daysMet,
            successRatePercent = successRate,
            weeks = weeks
        )
    }
}