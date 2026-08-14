package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalRecordEntity
import com.example.ui.components.DayActivityData
import com.example.ui.components.WeeklyActivityBarChart
import com.example.ui.components.WeightTrendLineChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun AnalyticsScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val workoutLogs by viewModel.workoutLogs.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val prs by viewModel.prs.collectAsState()

    var selectedMetric by remember { mutableStateOf("Calories") } // Calories, Steps, Volume
    var showAddPrDialog by remember { mutableStateOf(false) }

    val weeklyData = remember(workoutLogs) {
        listOf(
            DayActivityData("Mon", 450, 8420, 3200f),
            DayActivityData("Tue", 520, 10200, 3800f),
            DayActivityData("Wed", 380, 7900, 2900f),
            DayActivityData("Thu", 610, 11400, 4200f),
            DayActivityData("Fri", 480, 9300, 3400f),
            DayActivityData("Sat", 680, 12800, 4600f),
            DayActivityData("Sun", 520, 7840, 3450f, isToday = true)
        )
    }

    val weightHistory = remember(userProfile) {
        val currentW = userProfile?.weightKg ?: 76.5f
        listOf(
            Pair("W1", currentW + 2.1f),
            Pair("W2", currentW + 1.6f),
            Pair("W3", currentW + 1.0f),
            Pair("W4", currentW + 0.5f),
            Pair("Today", currentW)
        )
    }

    val totalLifetimeVolume = remember(workoutLogs) {
        workoutLogs.sumOf { it.totalVolumeKg.toDouble() }.toFloat() + 18450f
    }
    val totalLifetimeCalories = remember(workoutLogs) {
        workoutLogs.sumOf { it.caloriesBurned } + 8200
    }

    Scaffold(
        modifier = modifier.testTag("analytics_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddPrDialog = true },
                containerColor = RoyalPurple,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Add PR") },
                text = { Text("Log PR Trophy", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_pr_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Metric Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Calories", "Steps", "Volume").forEach { metric ->
                        FilterChip(
                            selected = selectedMetric == metric,
                            onClick = { selectedMetric = metric },
                            label = { Text(metric) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 2. Interactive Weekly Bar Chart
            item {
                WeeklyActivityBarChart(
                    data = weeklyData,
                    selectedMetric = selectedMetric
                )
            }

            // 3. Lifetime Stats Summary
            item {
                LifetimeStatsRow(
                    totalWorkouts = workoutLogs.size + 14,
                    totalVolumeKg = totalLifetimeVolume,
                    totalCaloriesBurned = totalLifetimeCalories
                )
            }

            // 4. Weight Progress Chart
            item {
                WeightTrendLineChart(
                    weights = weightHistory,
                    targetWeight = userProfile?.targetWeightKg ?: 73.0f
                )
            }

            // 5. 30-Day Consistency Heatmap Grid
            item {
                WorkoutConsistencyHeatmapCard(
                    streakDays = userProfile?.streakDays ?: 16
                )
            }

            // 6. Personal Records (PRs) Hall of Fame
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERSONAL RECORDS (PRs) 🏆",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            items(prs) { pr ->
                PersonalRecordCard(pr = pr)
            }
        }
    }

    if (showAddPrDialog) {
        AddPersonalRecordDialog(
            onDismiss = { showAddPrDialog = false },
            onAdd = { exercise, record, prev, cat ->
                viewModel.addPersonalRecord(exercise, record, prev, cat)
                showAddPrDialog = false
            }
        )
    }
}

@Composable
fun LifetimeStatsRow(
    totalWorkouts: Int,
    totalVolumeKg: Float,
    totalCaloriesBurned: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LifetimeStatCard("WORKOUTS", "$totalWorkouts", NeonCyan, Modifier.weight(1f))
        LifetimeStatCard("VOLUME LIFTED", "${(totalVolumeKg / 1000).toInt()}k kg", RoyalPurple, Modifier.weight(1f))
        LifetimeStatCard("BURNED", "${(totalCaloriesBurned / 1000).toInt()}k kcal", CaloriesColor, Modifier.weight(1f))
    }
}

@Composable
fun LifetimeStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun WorkoutConsistencyHeatmapCard(streakDays: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("consistency_heatmap_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30-Day Training Consistency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$streakDays active days recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricOrange.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = ElectricOrange, modifier = Modifier.size(14.dp))
                        Text("$streakDays Days", fontWeight = FontWeight.Black, fontSize = 12.sp, color = ElectricOrange)
                    }
                }
            }

            // 5 rows x 6 cols grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val dayNum = row * 7 + col + 1
                            val isActive = dayNum > (28 - streakDays)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) NeonLime.copy(alpha = 0.75f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) DarkBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalRecordCard(pr: PersonalRecordEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pr_card_${pr.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldStar.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = GoldStar, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(pr.exerciseName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("${pr.category} • Achieved ${pr.achievedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(pr.recordValue, fontWeight = FontWeight.Black, fontSize = 15.sp, color = GoldStar)
                pr.previousRecord?.let {
                    Text("Prev: $it", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AddPersonalRecordDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String?, String) -> Unit
) {
    var exercise by remember { mutableStateOf("") }
    var recordValue by remember { mutableStateOf("") }
    var prevValue by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Strength") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Personal Record", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = exercise,
                    onValueChange = { exercise = it },
                    label = { Text("Exercise (e.g. Incline Bench Press)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = recordValue,
                    onValueChange = { recordValue = it },
                    label = { Text("New Record (e.g. 100 kg x 5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prevValue,
                    onValueChange = { prevValue = it },
                    label = { Text("Previous PR (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Strength, Cardio, Calisthenics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (exercise.isNotBlank() && recordValue.isNotBlank()) {
                        onAdd(exercise.trim(), recordValue.trim(), prevValue.takeIf { it.isNotBlank() }, category.trim())
                    }
                },
                enabled = exercise.isNotBlank() && recordValue.isNotBlank()
            ) {
                Text("Save PR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
