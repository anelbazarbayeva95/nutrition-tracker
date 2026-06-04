
package com.example.nutritiontracker.ui.goals

import androidx.compose.foundation.Canvas
import com.example.nutritiontracker.data.fdc.NutritionSummary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritiontracker.ui.components.HeaderSection
import com.example.nutritiontracker.ui.theme.GrayBackground
import com.example.nutritiontracker.ui.theme.GreenPrimary
import com.example.nutritiontracker.ui.theme.TextPrimary
import com.example.nutritiontracker.ui.theme.TextSecondary
import com.example.nutritiontracker.data.SettingsRepository

private val DividerColor = Color(0xFFE0E3EE)

/* TABS MODEL */

private enum class GoalsTab(val label: String) {
    Daily("Daily Goals"),
    Weekly("Weekly Goals"),
    Monthly("Monthly Goals")
}

/* ROOT SCREEN */


@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    foodLog: List<NutritionSummary> = emptyList(),
    viewModel: GoalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val rdi = remember { settingsRepository.getCurrentRdiRequirements() }

    LaunchedEffect(Unit) {
        viewModel.refreshGoals()
    }

    var selectedTab by remember { mutableStateOf(GoalsTab.Daily) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GrayBackground)
    ) {
        // Green header
        HeaderSection(
            title = "Goals",
            onSettingsClick = onSettingsClick
        )

        // White content area with rounded top corners
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(top = 16.dp)
        ) {
            GoalsTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(Modifier.height(16.dp))

            // Scrollable content below the pill nav.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp)
            ) {
                when (selectedTab) {
                    GoalsTab.Daily -> {
                        // 1) Aggregate today's intake from the foodLog list
                        val totalCalories = foodLog.sumOf { it.calories?.toInt() ?: 0 }
                        val totalProtein  = foodLog.sumOf { it.protein?.toInt() ?: 0 }
                        val totalCarbs    = foodLog.sumOf { it.totalCarbs?.toInt() ?: 0 }
                        val totalFiber    = foodLog.sumOf { it.fiber?.toInt() ?: 0 }

                        // Micronutrients for "Today's Intake vs RDI"
                        val totalVitaminC = foodLog.sumOf { it.vitaminC?.toInt() ?: 0 }
                        val totalVitaminD = foodLog.sumOf { it.vitaminD?.toInt() ?: 0 }
                        val totalCalcium  = foodLog.sumOf { it.calcium?.toInt() ?: 0 }

                        // 2) Take the targets (RDI) from the ViewModel state,
                        //    but override the "current" values with these sums.
                        val dailyForUi = uiState.daily.copy(
                            caloriesCurrent = totalCalories,
                            proteinCurrent  = totalProtein,
                            carbsCurrent    = totalCarbs,
                            fiberCurrent    = totalFiber,
                            progressToCalorieGoal =
                                if (uiState.daily.caloriesTarget > 0)
                                    totalCalories.toFloat() / uiState.daily.caloriesTarget.toFloat()
                                else 0f
                        )

                        DailyGoalsContent(daily = dailyForUi)

                        Spacer(Modifier.height(16.dp))

                        DailyRdiIntakeSection(
                            vitaminCIntake = totalVitaminC,
                            vitaminCTarget = rdi.vitaminC,
                            vitaminDIntake = totalVitaminD,
                            vitaminDTarget = rdi.vitaminD,
                            calciumIntake = totalCalcium,
                            calciumTarget = rdi.calcium,
                            ironIntake = null,
                            ironTarget = null,
                        )
                    }

                    GoalsTab.Weekly  -> WeeklyGoalsContent(weekly = uiState.weekly)
                    GoalsTab.Monthly -> MonthlyGoalsContent(monthly = uiState.monthly)
                }

                Spacer(Modifier.height(24.dp))

                GoalsProgressSummary(selectedTab)

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

/* PROGRESS SUMMARY */

@Composable
private fun GoalsProgressSummary(selectedTab: GoalsTab) {
    val text = when (selectedTab) {
        GoalsTab.Daily   -> "Daily goals progress overview"
        GoalsTab.Weekly  -> "Weekly intake vs daily goals"
        GoalsTab.Monthly -> "Monthly consistency overview"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    )
}

/* TAB SWITCHER */

@Composable
private fun GoalsTabSwitcher(
    selectedTab: GoalsTab,
    onTabSelected: (GoalsTab) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GoalsTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (isSelected) GreenPrimary else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label.substringBefore(" "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/* COMMON RING DRAWING */

@Composable
private fun CircularRing(
    progress: Float,            // 0f..1f
    ringColor: Color,           // dark arc (progress)
    backgroundColor: Color,     // light track (100%)
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp
) {
    Canvas(modifier = modifier) {
        val sweepAngleMax = 360f
        val startAngle = -90f

        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )

        // Background track = full circle
        drawArc(
            color = backgroundColor,
            startAngle = startAngle,
            sweepAngle = sweepAngleMax,
            useCenter = false,
            style = stroke
        )

        // Foreground progress arc
        drawArc(
            color = ringColor,
            startAngle = startAngle,
            sweepAngle = sweepAngleMax * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke
        )
    }
}

/* DAILY GOALS */

@Composable
fun DailyGoalsContent(daily: DailyGoalsUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Daily Goals",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            // Calories ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BigRingStat(
                    value = daily.caloriesCurrent.toString(),
                    unit = "kcal",
                    label = "Calories",
                    subtitle = "${daily.caloriesCurrent} / ${daily.caloriesTarget} kcal",
                    progress = daily.progressToCalorieGoal.coerceIn(0f, 1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Macro rings (Protein / Carbs / Fiber)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallRingStat(
                    label = "Protein",
                    value = "${daily.proteinCurrent}/${daily.proteinTarget}",
                    unit = "g",
                    ringColor = Color(0xFF5C9DFF),
                    progress = if (daily.proteinTarget > 0) {
                        daily.proteinCurrent.toFloat() / daily.proteinTarget.toFloat()
                    } else 0f
                )
                SmallRingStat(
                    label = "Carbs",
                    value = "${daily.carbsCurrent}/${daily.carbsTarget}",
                    unit = "g",
                    ringColor = Color(0xFFFFB74D),
                    progress = if (daily.carbsTarget > 0) {
                        daily.carbsCurrent.toFloat() / daily.carbsTarget.toFloat()
                    } else 0f
                )
                SmallRingStat(
                    label = "Fiber",
                    value = "${daily.fiberCurrent}/${daily.fiberTarget}",
                    unit = "g",
                    ringColor = Color(0xFFBA68C8),
                    progress = if (daily.fiberTarget > 0) {
                        daily.fiberCurrent.toFloat() / daily.fiberTarget.toFloat()
                    } else 0f
                )
            }

            Spacer(Modifier.height(16.dp))

            Divider(color = DividerColor)

            Spacer(Modifier.height(12.dp))

            Column {
                Text(
                    text = "Great Progress!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "You're ${(daily.progressToCalorieGoal * 100).toInt()}% to your calorie goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

/* DAILY HELPERS */

@Composable
private fun BigRingStat(
    value: String,
    unit: String,
    label: String,
    subtitle: String,
    progress: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularRing(
                progress = progress,
                ringColor = GreenPrimary,
                backgroundColor = Color(0xFFE0F5EA),
                strokeWidth = 12.dp,
                modifier = Modifier.fillMaxSize()
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SmallRingStat(
    label: String,
    value: String,
    unit: String,
    ringColor: Color,
    progress: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularRing(
                progress = progress,
                ringColor = ringColor,
                backgroundColor = ringColor.copy(alpha = 0.18f),
                strokeWidth = 8.dp,
                modifier = Modifier.fillMaxSize()
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun RdiProgressRow(
    label: String,
    current: String,
    progress: Float,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Text(current, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFEFF1F5), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun DailyRdiIntakeSection(
    vitaminCIntake: Int,
    vitaminCTarget: Int,
    vitaminDIntake: Int,
    vitaminDTarget: Int,
    calciumIntake: Int,
    calciumTarget: Int,
    ironIntake: Int?,
    ironTarget: Int?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Today's Intake vs RDI",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            RdiProgressRow(
                label = "Vitamin C",
                current = "${vitaminCIntake}/${vitaminCTarget} mg",
                progress = if (vitaminCTarget > 0)
                    vitaminCIntake.toFloat() / vitaminCTarget.toFloat()
                else 0f,
                barColor = Color(0xFFFFA726)
            )

            Spacer(Modifier.height(8.dp))

            RdiProgressRow(
                label = "Vitamin D",
                current = "${vitaminDIntake}/${vitaminDTarget} µg",
                progress = if (vitaminDTarget > 0)
                    vitaminDIntake.toFloat() / vitaminDTarget.toFloat()
                else 0f,
                barColor = Color(0xFFFFD54F)
            )

            Spacer(Modifier.height(8.dp))

            RdiProgressRow(
                label = "Calcium",
                current = "${calciumIntake}/${calciumTarget} mg",
                progress = if (calciumTarget > 0)
                    calciumIntake.toFloat() / calciumTarget.toFloat()
                else 0f,
                barColor = Color(0xFF64B5F6)
            )

            Spacer(Modifier.height(8.dp))
            
        }
    }
}

/* WEEKLY GOALS */

@Composable
fun WeeklyGoalsContent(weekly: WeeklyGoalsUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title + “This Week” chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Performance",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Total nutrient intake vs daily goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                WeeklyRangeChip()
            }

            Spacer(Modifier.height(16.dp))

            if (weekly.days.isEmpty()) {
                Text(
                    text = "No data yet. Log today’s intake to see weekly performance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                WeeklyPerformanceChart(days = weekly.days)
            }

            Spacer(Modifier.height(16.dp))

            WeeklyLegend()
        }
    }
}

@Composable
private fun WeeklyPerformanceChart(days: List<WeeklyDayUi>) {
    if (days.isEmpty()) return

    val maxPercent = (days.maxOf { it.percentOfGoal }.coerceAtLeast(120f))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        WeeklyYAxis()

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    val rawPercent = day.percentOfGoal
                    val fraction = (rawPercent / maxPercent).coerceIn(0f, 1f)
                    val barColor = when {
                        rawPercent < 80f  -> Color(0xFFE57373)   // red: below target
                        rawPercent < 100f -> Color(0xFFFFB74D)   // orange: close
                        else              -> Color(0xFF4CAF50)   // green: goal met
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .height(140.dp)
                                .width(24.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(fraction)
                                    .fillMaxWidth()
                                    .background(
                                        color = barColor,
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp
                                        )
                                    )
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = day.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyRangeChip() {
    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFE0E3EE),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "This Week",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun WeeklyLegend() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LegendDotRow(
            color = Color(0xFF4CAF50),
            label = "Goal Met (100%+)"
        )
        Spacer(Modifier.height(4.dp))
        LegendDotRow(
            color = Color(0xFFFFB74D),
            label = "Close (80–99%)"
        )
        Spacer(Modifier.height(4.dp))
        LegendDotRow(
            color = Color(0xFFE57373),
            label = "Below Target (<80%)"
        )
    }
}

@Composable
private fun WeeklyYAxis() {
    val labels = listOf(0, 30, 60, 90, 120)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        // top → bottom: 120, 90, 60, 30, 0
        labels.reversed().forEach { value ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(1.dp)
                        .background(Color(0xFFCFD8DC))
                )
            }
        }
    }
}

@Composable
private fun DashedTargetLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(16) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(3.dp)
                    .background(
                        color = Color(0xFFB0BEC5).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun LegendDotRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/* MONTHLY GOALS */

@Composable
fun MonthlyGoalsContent(monthly: MonthlyGoalsUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title + chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Consistency",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Days per week you met your daily goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                MonthlyRangeChip()
            }

            Spacer(Modifier.height(12.dp))

            MonthlySummaryCard(
                totalDaysText = "${monthly.daysMetGoal}/${monthly.totalDaysTracked}",
                successRateText = "${monthly.successRatePercent}%"
            )

            Spacer(Modifier.height(16.dp))

            if (monthly.weeks.isEmpty()) {
                Text(
                    text = "No data recorded yet for this month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                MonthlyConsistencyChart(weeks = monthly.weeks)
            }

            Spacer(Modifier.height(16.dp))

            MonthlyLegendItem(
                color = Color(0xFF7C4DFF),
                label = "Perfect (7 days)"
            )
            Spacer(Modifier.height(4.dp))
            MonthlyLegendItem(
                color = Color(0xFF9575CD),
                label = "Great (5–6 days)"
            )
            Spacer(Modifier.height(4.dp))
            MonthlyLegendItem(
                color = Color(0xFFB39DDB),
                label = "Good (3–4 days)"
            )
            Spacer(Modifier.height(4.dp))
            MonthlyLegendItem(
                color = Color(0xFFE0E0E0),
                label = "Needs Work (<3 days)"
            )
        }
    }
}

/* MONTHLY SUBCOMPONENTS */

@Composable
private fun MonthlyRangeChip() {
    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFE0E3EE),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "This Month",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun MonthlySummaryCard(
    totalDaysText: String,
    successRateText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF3F2FF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = totalDaysText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Success Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = successRateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7C4DFF)
                )
            }
        }
    }
}

@Composable
private fun MonthlyLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/* MONTHLY Y-AXIS */

@Composable
private fun MonthlyYAxis(maxDays: Int) {
    val labels = (0..maxDays).toList()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        labels.reversed().forEach { value ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(1.dp)
                        .background(Color(0xFFCFD8DC))
                )
            }
        }
    }
}

/* MONTHLY BAR CHART */

@Composable
private fun MonthlyConsistencyChart(weeks: List<MonthlyWeekUi>) {
    if (weeks.isEmpty()) return

    val maxDays = weeks.maxOf { it.daysMetGoal }.coerceAtLeast(7)
    val maxBarHeight = 140.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxBarHeight + 32.dp)
            .padding(top = 4.dp)
    ) {
        MonthlyYAxis(maxDays = maxDays)

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeks.forEach { week ->
                    val fraction = (week.daysMetGoal.toFloat() / maxDays.toFloat())
                        .coerceIn(0f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .height(maxBarHeight)
                                .width(30.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(fraction)
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF7C4DFF),
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp
                                        )
                                    )
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = week.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}