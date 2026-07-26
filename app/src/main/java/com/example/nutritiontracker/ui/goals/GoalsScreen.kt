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
import com.example.nutritiontracker.ui.theme.GreenPrimary
import com.example.nutritiontracker.data.SettingsRepository

private enum class GoalsTab(val label: String) {
    Daily("Daily Goals"),
    Weekly("Weekly Goals"),
    Monthly("Monthly Goals")
}

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

    LaunchedEffect(Unit) { viewModel.refreshGoals() }

    var selectedTab by remember { mutableStateOf(GoalsTab.Daily) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection(title = "Goals", onSettingsClick = onSettingsClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(top = 16.dp)
        ) {
            GoalsTabSwitcher(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp)
            ) {
                when (selectedTab) {
                    GoalsTab.Daily -> {
                        val totalCalories = foodLog.sumOf { it.calories?.toInt() ?: 0 }
                        val totalProtein  = foodLog.sumOf { it.protein?.toInt() ?: 0 }
                        val totalCarbs    = foodLog.sumOf { it.totalCarbs?.toInt() ?: 0 }
                        val totalFiber    = foodLog.sumOf { it.fiber?.toInt() ?: 0 }
                        val totalVitaminC = foodLog.sumOf { it.vitaminC?.toInt() ?: 0 }
                        val totalVitaminD = foodLog.sumOf { it.vitaminD?.toInt() ?: 0 }
                        val totalCalcium  = foodLog.sumOf { it.calcium?.toInt() ?: 0 }

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
                            vitaminCIntake = totalVitaminC, vitaminCTarget = rdi.vitaminC,
                            vitaminDIntake = totalVitaminD, vitaminDTarget = rdi.vitaminD,
                            calciumIntake = totalCalcium, calciumTarget = rdi.calcium,
                            ironIntake = null, ironTarget = null,
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    )
}

@Composable
private fun GoalsTabSwitcher(selectedTab: GoalsTab, onTabSelected: (GoalsTab) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GoalsTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f).height(44.dp).padding(horizontal = 4.dp)
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
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularRing(
    progress: Float,
    ringColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(color = backgroundColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
        drawArc(color = ringColor, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false, style = stroke)
    }
}

@Composable
fun DailyGoalsContent(daily: DailyGoalsUi) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Daily Goals", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                BigRingStat(
                    value = daily.caloriesCurrent.toString(), unit = "kcal", label = "Calories",
                    subtitle = "${daily.caloriesCurrent} / ${daily.caloriesTarget} kcal",
                    progress = daily.progressToCalorieGoal.coerceIn(0f, 1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SmallRingStat("Protein", "${daily.proteinCurrent}/${daily.proteinTarget}", "g", Color(0xFF5C9DFF),
                    if (daily.proteinTarget > 0) daily.proteinCurrent.toFloat() / daily.proteinTarget.toFloat() else 0f)
                SmallRingStat("Carbs", "${daily.carbsCurrent}/${daily.carbsTarget}", "g", Color(0xFFFFB74D),
                    if (daily.carbsTarget > 0) daily.carbsCurrent.toFloat() / daily.carbsTarget.toFloat() else 0f)
                SmallRingStat("Fiber", "${daily.fiberCurrent}/${daily.fiberTarget}", "g", Color(0xFFBA68C8),
                    if (daily.fiberTarget > 0) daily.fiberCurrent.toFloat() / daily.fiberTarget.toFloat() else 0f)
            }
            Spacer(Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            Text("Great Progress!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "You're ${(daily.progressToCalorieGoal * 100).toInt()}% to your calorie goal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun BigRingStat(value: String, unit: String, label: String, subtitle: String, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            CircularRing(progress = progress, ringColor = GreenPrimary, backgroundColor = Color(0xFFE0F5EA), strokeWidth = 12.dp, modifier = Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (unit.isNotBlank()) Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun SmallRingStat(label: String, value: String, unit: String, ringColor: Color, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            CircularRing(progress = progress, ringColor = ringColor, backgroundColor = ringColor.copy(alpha = 0.18f), strokeWidth = 8.dp, modifier = Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RdiProgressRow(label: String, current: String, progress: Float, barColor: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(current, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))) {
            Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(barColor, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun DailyRdiIntakeSection(
    vitaminCIntake: Int, vitaminCTarget: Int,
    vitaminDIntake: Int, vitaminDTarget: Int,
    calciumIntake: Int, calciumTarget: Int,
    ironIntake: Int?, ironTarget: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Today's Intake vs RDI", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            RdiProgressRow("Vitamin C", "${vitaminCIntake}/${vitaminCTarget} mg",
                if (vitaminCTarget > 0) vitaminCIntake.toFloat() / vitaminCTarget else 0f, Color(0xFFFFA726))
            Spacer(Modifier.height(8.dp))
            RdiProgressRow("Vitamin D", "${vitaminDIntake}/${vitaminDTarget} µg",
                if (vitaminDTarget > 0) vitaminDIntake.toFloat() / vitaminDTarget else 0f, Color(0xFFFFD54F))
            Spacer(Modifier.height(8.dp))
            RdiProgressRow("Calcium", "${calciumIntake}/${calciumTarget} mg",
                if (calciumTarget > 0) calciumIntake.toFloat() / calciumTarget else 0f, Color(0xFF64B5F6))
        }
    }
}

@Composable
fun WeeklyGoalsContent(weekly: WeeklyGoalsUi) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Weekly Performance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Text("Total nutrient intake vs daily goals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                WeeklyRangeChip()
            }
            Spacer(Modifier.height(16.dp))
            if (weekly.days.isEmpty()) {
                Text("No data yet. Log today's intake to see weekly performance.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
    val maxPercent = days.maxOf { it.percentOfGoal }.coerceAtLeast(120f)
    Row(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        WeeklyYAxis()
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                days.forEach { day ->
                    val rawPercent = day.percentOfGoal
                    val fraction = (rawPercent / maxPercent).coerceIn(0f, 1f)
                    val barColor = when {
                        rawPercent < 80f  -> Color(0xFFE57373)
                        rawPercent < 100f -> Color(0xFFFFB74D)
                        else              -> Color(0xFF4CAF50)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(modifier = Modifier.height(140.dp).width(24.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(modifier = Modifier.fillMaxHeight(fraction).fillMaxWidth().background(barColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(day.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyRangeChip() {
    Row(
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("This Week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun WeeklyLegend() {
    Column(modifier = Modifier.fillMaxWidth()) {
        LegendDotRow(Color(0xFF4CAF50), "Goal Met (100%+)")
        Spacer(Modifier.height(4.dp))
        LegendDotRow(Color(0xFFFFB74D), "Close (80–99%)")
        Spacer(Modifier.height(4.dp))
        LegendDotRow(Color(0xFFE57373), "Below Target (<80%)")
    }
}

@Composable
private fun WeeklyYAxis() {
    Column(modifier = Modifier.fillMaxHeight().padding(start = 4.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
        listOf(0, 30, 60, 90, 120).reversed().forEach { value ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.width(4.dp))
                Box(modifier = Modifier.width(10.dp).height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            }
        }
    }
}

@Composable
private fun LegendDotRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun MonthlyGoalsContent(monthly: MonthlyGoalsUi) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Monthly Consistency", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Text("Days per week you met your daily goal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                MonthlyRangeChip()
            }
            Spacer(Modifier.height(12.dp))
            MonthlySummaryCard(totalDaysText = "${monthly.daysMetGoal}/${monthly.totalDaysTracked}", successRateText = "${monthly.successRatePercent}%")
            Spacer(Modifier.height(16.dp))
            if (monthly.weeks.isEmpty()) {
                Text("No data recorded yet for this month.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                MonthlyConsistencyChart(weeks = monthly.weeks)
            }
            Spacer(Modifier.height(16.dp))
            MonthlyLegendItem(Color(0xFF7C4DFF), "Perfect (7 days)")
            Spacer(Modifier.height(4.dp))
            MonthlyLegendItem(Color(0xFF9575CD), "Great (5–6 days)")
            Spacer(Modifier.height(4.dp))
            MonthlyLegendItem(Color(0xFFB39DDB), "Good (3–4 days)")
            Spacer(Modifier.height(4.dp))
            MonthlyLegendItem(Color(0xFFE0E0E0), "Needs Work (<3 days)")
        }
    }
}

@Composable
private fun MonthlyRangeChip() {
    Row(
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("This Month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MonthlySummaryCard(totalDaysText: String, successRateText: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(totalDaysText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Success Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(successRateText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7C4DFF))
            }
        }
    }
}

@Composable
private fun MonthlyLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun MonthlyYAxis(maxDays: Int) {
    Column(modifier = Modifier.fillMaxHeight().padding(start = 4.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
        (0..maxDays).toList().reversed().forEach { value ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.width(4.dp))
                Box(modifier = Modifier.width(10.dp).height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            }
        }
    }
}

@Composable
private fun MonthlyConsistencyChart(weeks: List<MonthlyWeekUi>) {
    if (weeks.isEmpty()) return
    val maxDays = weeks.maxOf { it.daysMetGoal }.coerceAtLeast(7)
    val maxBarHeight = 140.dp
    Row(modifier = Modifier.fillMaxWidth().height(maxBarHeight + 32.dp).padding(top = 4.dp)) {
        MonthlyYAxis(maxDays = maxDays)
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                weeks.forEach { week ->
                    val fraction = (week.daysMetGoal.toFloat() / maxDays.toFloat()).coerceIn(0f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(modifier = Modifier.height(maxBarHeight).width(30.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(modifier = Modifier.fillMaxHeight(fraction).fillMaxWidth().background(Color(0xFF7C4DFF), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(week.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}