package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfileEntity
import com.example.data.model.WorkoutLogEntity
import com.example.data.model.WorkoutRoutineEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FitnessViewModel,
    onNavigateToWorkouts: () -> Unit,
    onStartRoutine: (WorkoutRoutineEntity) -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToWearables: () -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val routines by viewModel.allRoutines.collectAsState()
    val recentLogs by viewModel.recentWorkoutLogs.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val totalWaterMl by viewModel.totalWaterTodayMl.collectAsState()
    val liveTelemetry by viewModel.liveTelemetry.collectAsState()
    val activeWearable by viewModel.activeWearable.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val aiAdaptation by viewModel.aiAdaptation.collectAsState()

    var showAiAdaptationSheet by remember { mutableStateOf(false) }

    val totalCalsConsumed = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val recommendedRoutine = remember(routines) {
        routines.firstOrNull { it.id == 1L } ?: routines.firstOrNull()
    }
    val activeDailyQuest = remember(challenges) {
        challenges.firstOrNull { it.category == com.example.data.model.ChallengeCategory.DAILY }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Bento Hero Routine Banner ("Today's Routine")
        item {
            BentoHeroRoutineCard(
                routine = recommendedRoutine,
                streakDays = userProfile?.streakDays ?: 14,
                onStartWorkout = { recommendedRoutine?.let { onStartRoutine(it) } ?: onNavigateToWorkouts() }
            )
        }

        // 2. Bento Modular Grid (2 Columns: Left = Nutrition, Right = Heart Rate + Social Goals)
        item {
            BentoModularGrid(
                calsConsumed = if (totalCalsConsumed > 0) totalCalsConsumed else 1840,
                calsTarget = userProfile?.dailyCalorieGoal ?: 2400,
                telemetry = liveTelemetry,
                onOpenNutrition = onNavigateToNutrition,
                onOpenWearables = onNavigateToWearables,
                onOpenSocial = onNavigateToSocial
            )
        }

        // 2.5. AI Dynamic Workout Adaptation & Readiness Bento Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricGreen, NeonCyan))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAiAdaptationSheet = true }
                    .testTag("dashboard_ai_adaptation_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "AI DYNAMIC COACH READINESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricGreen,
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "88% OPTIMAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Dynamic Routine Adaptation Ready",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        "AI analyzed your sleep, heart rate, and fatigue to adapt today's sets, weights, and exercises.",
                        fontSize = 12.sp,
                        color = BentoTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Overload +2.5kg • Legs Protected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        }

                        Button(
                            onClick = { showAiAdaptationSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Review AI Plan", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // 2.7. Active Daily Quest Challenge Card
        activeDailyQuest?.let { quest ->
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSocial() }
                        .testTag("dashboard_daily_quest_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "DAILY QUEST: ${quest.title.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF5252),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "+${quest.xpReward} XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(quest.description, fontSize = 12.sp, color = BentoTextSecondary)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${quest.currentValue.toInt()} / ${quest.targetValue.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                            Text("${(quest.progressFraction * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricGreen)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { quest.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricGreen,
                            trackColor = BentoSurfaceElevated
                        )
                    }
                }
            }
        }

        // 3. Wearable Sync Device Bar
        item {
            BentoWearableSyncBar(
                deviceName = activeWearable?.name ?: liveTelemetry.activeDeviceName,
                isConnected = liveTelemetry.isConnected,
                battery = activeWearable?.batteryPercent ?: 88,
                onOpenWearables = onNavigateToWearables
            )
        }

        // 4. Multi-Ring Daily Goal Progress Card
        item {
            MultiRingProgressCard(
                caloriesCurrent = liveTelemetry.activeCalories,
                caloriesTarget = 650,
                stepsCurrent = liveTelemetry.steps,
                stepsTarget = userProfile?.dailyStepGoal ?: 10000,
                activeMinsCurrent = 42,
                activeMinsTarget = 45,
                waterMlCurrent = totalWaterMl,
                waterMlTarget = userProfile?.dailyWaterGoalMl ?: 3000
            )
        }

        // 5. Quick Action Shortcuts
        item {
            BentoQuickActionsRow(
                onLogWater250 = { viewModel.logWater(250) },
                onLogWater500 = { viewModel.logWater(500) },
                onLogMeal = onNavigateToNutrition,
                onShare = onNavigateToSocial
            )
        }

        // 6. Routine Highlight List
        item {
            recommendedRoutine?.let { routine ->
                Text(
                    text = "FEATURED WORKOUT ROUTINE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                RoutineHighlightCard(
                    routine = routine,
                    onStart = { onStartRoutine(routine) }
                )
            }
        }

        // 7. Recent Workout Activity History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${recentLogs.size} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (recentLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
                ) {
                    Text(
                        text = "No recent workouts yet. Start your first session above!",
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentLogs.take(3).forEach { log ->
                        RecentActivityLogCard(log = log)
                    }
                }
            }
        }
    }

    if (showAiAdaptationSheet) {
        AiWorkoutAdaptationBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAiAdaptationSheet = false }
        )
    }
}

@Composable
fun BentoHeroRoutineCard(
    routine: WorkoutRoutineEntity?,
    streakDays: Int,
    onStartWorkout: () -> Unit
) {
    val routineTitle = routine?.title ?: "Full Body HIIT"
    val routineDetails = if (routine != null) {
        "${routine.durationMinutes} mins • ${routine.estimatedCalories} kcal • ${routine.difficulty}"
    } else {
        "45 mins • 320 kcal • Advanced"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(BentoPrimary)
            .testTag("hero_motivation_card")
    ) {
        // Decorative blurred ambient bubble
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 24.dp, y = 24.dp)
                .size(130.dp)
                .clip(CircleShape)
                .background(BentoPrimaryHighlight.copy(alpha = 0.55f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TODAY'S ROUTINE",
                    color = BentoPrimaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = routineTitle,
                    color = BentoPrimaryDeep,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )
                Text(
                    text = routineDetails,
                    color = BentoPrimaryDark.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onStartWorkout,
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryDeep),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                modifier = Modifier
                    .testTag("start_quick_workout_button")
                    .height(40.dp)
            ) {
                Text(
                    text = "Start Workout",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun BentoModularGrid(
    calsConsumed: Int,
    calsTarget: Int,
    telemetry: com.example.wearable.LiveHealthTelemetry,
    onOpenNutrition: () -> Unit,
    onOpenWearables: () -> Unit,
    onOpenSocial: () -> Unit
) {
    val nutritionProgress = (calsConsumed.toFloat() / calsTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
    val percentage = (nutritionProgress * 100).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Nutrition Bento Tile
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .clickable { onOpenNutrition() }
                .testTag("bento_nutrition_tile"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "NUTRITION",
                        color = BentoTextPrimary.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%,d", calsConsumed),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kcal",
                            fontSize = 11.sp,
                            color = BentoTextPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Bento styled progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BentoBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(nutritionProgress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BentoPrimary)
                        )
                    }
                    Text(
                        text = "$percentage% of daily target",
                        fontSize = 10.sp,
                        color = BentoTextPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Right Column: Heart Rate & Social Goals Mini Tiles
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Heart Rate Tile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenWearables() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BentoHeartRateRed)
                        )
                        Text(
                            text = "HEART RATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoTextPrimary.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${if (telemetry.heartRateBpm > 0) telemetry.heartRateBpm else 72}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "bpm",
                            fontSize = 11.sp,
                            color = BentoTextPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            // Social Goals Tile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenSocial() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SOCIAL GOALS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryDark,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "3 friends active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Overlapping avatars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy((-6).dp)
                    ) {
                        listOf("MV", "ER", "DK").forEachIndexed { idx, initial ->
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, BentoPrimary, CircleShape)
                                    .background(
                                        when (idx) {
                                            0 -> Color(0xFF6B7280)
                                            1 -> Color(0xFF4B5563)
                                            else -> Color(0xFF374151)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initial, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoWearableSyncBar(
    deviceName: String,
    isConnected: Boolean,
    battery: Int,
    onOpenWearables: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onOpenWearables() }
            .testTag("bento_wearable_bar"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Watch,
                        contentDescription = null,
                        tint = BentoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (deviceName.isNotBlank()) deviceName else "Watch Ultra 2",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "Synced 2 mins ago • Offline Mode On",
                        fontSize = 10.sp,
                        color = BentoTextPrimary.copy(alpha = 0.5f)
                    )
                }
            }

            // Bento Styled Pill Toggle/Indicator
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoPrimary)
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterEnd)
                        .clip(CircleShape)
                        .background(BentoPrimaryDark)
                )
            }
        }
    }
}

@Composable
fun BentoQuickActionsRow(
    onLogWater250: () -> Unit,
    onLogWater500: () -> Unit,
    onLogMeal: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BentoQuickActionButton(
            label = "+250ml 💧",
            icon = Icons.Filled.WaterDrop,
            color = WaterColor,
            onClick = onLogWater250,
            modifier = Modifier.weight(1f)
        )
        BentoQuickActionButton(
            label = "+500ml 💧",
            icon = Icons.Filled.LocalDrink,
            color = WaterColor,
            onClick = onLogWater500,
            modifier = Modifier.weight(1f)
        )
        BentoQuickActionButton(
            label = "Log Meal",
            icon = Icons.Filled.Restaurant,
            color = BentoPrimary,
            onClick = onLogMeal,
            modifier = Modifier.weight(1f)
        )
        BentoQuickActionButton(
            label = "Share",
            icon = Icons.Filled.Share,
            color = BentoPrimaryHighlight,
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BentoQuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RoutineHighlightCard(
    routine: WorkoutRoutineEntity,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_highlight_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoPrimaryContainer
                    ) {
                        Text(
                            text = routine.category.uppercase(),
                            color = BentoPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoSurfaceVariant
                    ) {
                        Text(
                            text = routine.difficulty,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "${routine.durationMinutes} min • ~${routine.estimatedCalories} kcal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_routine_highlight_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = BentoPrimaryDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Start Workout Session", color = BentoPrimaryDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentActivityLogCard(log: WorkoutLogEntity) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
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
                        .background(BentoPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = BentoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = log.routineTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$dateStr • ${(log.durationSeconds / 60)} mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${log.caloriesBurned} kcal",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = BentoPrimary
                )
                Text(
                    text = "${log.avgHeartRate} bpm avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoHeartRateRed
                )
            }
        }
    }
}
