package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.ui.components.LiveHeartRateChip
import com.example.ui.components.RestTimerModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveWorkoutSession
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun ActiveWorkoutScreen(
    viewModel: FitnessViewModel,
    session: ActiveWorkoutSession,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveTelemetry by viewModel.liveTelemetry.collectAsState()
    var showFinishConfirmDialog by remember { mutableStateOf(false) }

    val currentExercise = session.currentExercise
    val nextExercise = session.nextExercise

    val minutes = session.elapsedSeconds / 60
    val seconds = session.elapsedSeconds % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("active_workout_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Routine Title, Play/Pause, Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showFinishConfirmDialog = true },
                    modifier = Modifier.testTag("exit_workout_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Exit", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = session.routine.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "EXERCISE ${session.currentExerciseIndex + 1} OF ${session.exercises.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleWorkoutPause() },
                    modifier = Modifier.testTag("pause_workout_button")
                ) {
                    Icon(
                        imageVector = if (session.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = "Pause/Resume",
                        tint = if (session.isPaused) NeonLime else Color.White
                    )
                }
            }

            // Overall Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { session.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonLime,
                    trackColor = DarkSurfaceVariant
                )
            }

            // Real-Time Telemetry & Big Timer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Big Digital Timer
                Column {
                    Text(
                        text = "ACTIVE TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextSecondary
                    )
                    Text(
                        text = timerString,
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        color = Color.White
                    )
                }

                // Heart Rate Chip
                LiveHeartRateChip(
                    bpm = liveTelemetry.heartRateBpm,
                    zone = liveTelemetry.heartRateZone,
                    isLive = true,
                    deviceName = liveTelemetry.activeDeviceName
                )
            }

            // Main Active Exercise Card
            currentExercise?.let { exercise ->
                ActiveExerciseMainCard(
                    exercise = exercise,
                    currentSet = session.currentSetIndex,
                    totalSets = exercise.sets,
                    totalVolumeKg = session.totalVolumeKg,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Actions: Complete Set & Finish Workout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.completeCurrentSet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("complete_set_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (session.currentSetIndex < (currentExercise?.sets ?: 1)) {
                            "Complete Set ${session.currentSetIndex}"
                        } else if (session.currentExerciseIndex < session.exercises.size - 1) {
                            "Next Exercise ➔"
                        } else {
                            "Finish Workout! 🏆"
                        },
                        color = DarkBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Volume: ${session.totalVolumeKg.toInt()} kg",
                        fontSize = 12.sp,
                        color = DarkTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = { viewModel.finishWorkoutSession() },
                        modifier = Modifier.testTag("finish_workout_early_button")
                    ) {
                        Text("Finish & Save", color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Rest Timer Overlay Dialog
        if (session.isResting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                RestTimerModal(
                    secondsRemaining = session.restRemainingSeconds,
                    totalSeconds = session.currentRestTotal,
                    nextExerciseName = if (session.currentSetIndex > (currentExercise?.sets ?: 1)) nextExercise?.name else currentExercise?.name,
                    onSkip = { viewModel.skipRestTimer() },
                    onAdd30s = { /* Add 30s handled in rest */ }
                )
            }
        }

        // Workout Completion Summary Overlay
        if (session.isCompleted) {
            WorkoutCompletionSummaryModal(
                session = session,
                onDismiss = {
                    viewModel.exitWorkoutPlayer()
                    onFinish()
                },
                onShare = {
                    viewModel.shareWorkoutToCommunity(
                        title = "Crushed ${session.routine.title}!",
                        notes = "Completed ${session.exercises.size} exercises with ${(session.elapsedSeconds / 60)}m time and ${session.totalVolumeKg.toInt()}kg total volume."
                    )
                    viewModel.exitWorkoutPlayer()
                    onFinish()
                }
            )
        }
    }

    if (showFinishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = { Text("Leave Active Workout?") },
            text = { Text("Would you like to finish and save your workout progress, or discard this session?") },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.finishWorkoutSession()
                    }
                ) {
                    Text("Save & Finish")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.exitWorkoutPlayer()
                        onFinish()
                    }
                ) {
                    Text("Discard", color = PulseRed)
                }
            }
        )
    }
}

@Composable
fun ActiveExerciseMainCard(
    exercise: Exercise,
    currentSet: Int,
    totalSets: Int,
    totalVolumeKg: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("active_exercise_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Muscle & Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = exercise.targetMuscle.uppercase(),
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "SET $currentSet / $totalSets",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = NeonLime
                )
            }

            // Exercise Title
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            // Target Parameters (Weight, Reps, Rest)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExerciseMetricBlock(
                    label = "WEIGHT",
                    value = if (exercise.weightKg > 0) "${exercise.weightKg} kg" else "Bodyweight",
                    highlightColor = NeonCyan
                )
                ExerciseMetricBlock(
                    label = "TARGET REPS",
                    value = "${exercise.reps} reps",
                    highlightColor = NeonLime
                )
                ExerciseMetricBlock(
                    label = "REST TIME",
                    value = "${exercise.restSeconds}s",
                    highlightColor = ElectricOrange
                )
            }

            // Visual Form Cues & Coaching Tips
            if (exercise.tips.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TipsAndUpdates,
                            contentDescription = null,
                            tint = GoldStar,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "COACH FORM CUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldStar
                            )
                            Text(
                                text = exercise.tips,
                                fontSize = 13.sp,
                                color = DarkTextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Sets Progression Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (s in 1..totalSets) {
                    val isDone = s < currentSet
                    val isCurrent = s == currentSet
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isDone -> NeonLime
                                    isCurrent -> NeonCyan
                                    else -> DarkSurfaceVariant
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseMetricBlock(
    label: String,
    value: String,
    highlightColor: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = DarkTextSecondary, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = highlightColor)
        }
    }
}

@Composable
fun WorkoutCompletionSummaryModal(
    session: ActiveWorkoutSession,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val durationMins = (session.elapsedSeconds / 60).coerceAtLeast(1)
    val estimatedCals = durationMins * 9

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("workout_completion_modal"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NeonLime.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Victory",
                        tint = NeonLime,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "WORKOUT CRUSHED! 🔥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = session.routine.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryMetricCol("DURATION", "$durationMins min", NeonCyan)
                    SummaryMetricCol("CALORIES", "$estimatedCals kcal", CaloriesColor)
                    SummaryMetricCol("VOLUME", "${session.totalVolumeKg.toInt()} kg", RoyalPurple)
                }

                HorizontalDivider(color = DarkBorder)

                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_completed_workout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share to Community", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finish_done_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Return to Dashboard", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SummaryMetricCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = DarkTextSecondary, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = color)
    }
}
