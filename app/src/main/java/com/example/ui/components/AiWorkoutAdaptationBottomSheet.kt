package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiWorkoutAdaptationBottomSheet(
    viewModel: FitnessViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aiAdaptation by viewModel.aiAdaptation.collectAsState()
    val isAnalyzing by viewModel.isAiAnalyzing.collectAsState()
    val feedback by viewModel.userWorkoutFeedback.collectAsState()

    var rpeRating by remember { mutableFloatStateOf(feedback.rpeRating.toFloat()) }
    var selectedSoreMuscles by remember { mutableStateOf(feedback.soreMuscles) }
    var energyLevel by remember { mutableIntStateOf(feedback.energyLevel) }
    var sleepQuality by remember { mutableIntStateOf(feedback.sleepQualityRating) }
    var userNotes by remember { mutableStateOf(feedback.userNotes) }

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = BentoSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(5.dp),
                shape = CircleShape,
                color = BentoMutedText.copy(alpha = 0.4f),
                content = {}
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("ai_adaptation_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(NeonCyan, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "AI DYNAMIC COACH",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            "Workout Routine Adaptation",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = BentoMutedText)
                    }
                }
            }

            // Wearable Biometric Readiness & Overtraining Risk Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "BIOMETRIC READINESS & RECOVERY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoMutedText,
                                letterSpacing = 0.5.sp
                            )
                            val risk = aiAdaptation?.overtrainingRisk ?: OvertrainingRiskLevel.MINIMAL
                            val riskColor = when (risk) {
                                OvertrainingRiskLevel.HIGH_RISK -> Color(0xFFFF5252)
                                OvertrainingRiskLevel.ELEVATED -> Color(0xFFFFAB00)
                                OvertrainingRiskLevel.LOW -> ElectricGreen
                                OvertrainingRiskLevel.MINIMAL -> NeonCyan
                            }
                            val riskBg = riskColor.copy(alpha = 0.15f)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = riskBg,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    "RISK: ${risk.name.replace('_', ' ')}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Score Rings / Numbers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Readiness Score
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(BentoSurfaceElevated, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("READINESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoMutedText)
                                Text(
                                    "${aiAdaptation?.readinessScore ?: 88}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricGreen
                                )
                                Text("HRV: 68ms • Rest: 54bpm", fontSize = 9.sp, color = BentoMutedText)
                            }

                            Spacer(Modifier.width(10.dp))

                            // Cumulative Strain
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(BentoSurfaceElevated, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("FATIGUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoMutedText)
                                Text(
                                    "${aiAdaptation?.fatigueScore ?: 32}/100",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if ((aiAdaptation?.fatigueScore ?: 32) > 70) Color(0xFFFF5252) else NeonCyan
                                )
                                Text("Recovery: ${aiAdaptation?.recoveryTimeHours ?: 24}h", fontSize = 9.sp, color = BentoMutedText)
                            }

                            Spacer(Modifier.width(10.dp))

                            // Recommended Action
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(BentoSurfaceElevated, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ACTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoMutedText)
                                val action = aiAdaptation?.recommendedActionType ?: AdaptationActionType.PROGRESSIVE_OVERLOAD
                                Text(
                                    when (action) {
                                        AdaptationActionType.PROGRESSIVE_OVERLOAD -> "+8% OVERLOAD"
                                        AdaptationActionType.DELOAD_VOLUME_REDUCTION -> "-15% DELOAD"
                                        AdaptationActionType.EXERCISE_SUBSTITUTION -> "SWAP JOINTS"
                                        AdaptationActionType.ACTIVE_RECOVERY_DAY -> "ACTIVE REC"
                                        AdaptationActionType.COMPLETE_REST_DAY -> "REST DAY"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (action == AdaptationActionType.PROGRESSIVE_OVERLOAD) ElectricGreen else Color(0xFFFFAB00)
                                )
                                Text("Calibrated", fontSize = 9.sp, color = BentoMutedText)
                            }
                        }

                        // Muscle Group Recovery Barometers
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "MUSCLE GROUP RECOVERY CAPACITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMutedText
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val defaultMuscles = mapOf("Chest" to 95, "Legs" to 55, "Back" to 85, "Shoulders" to 90, "Core" to 90)
                            val recoveryMap = aiAdaptation?.muscleRecoveryMap ?: defaultMuscles
                            recoveryMap.entries.take(5).forEach { entry ->
                                val muscle = entry.key
                                val pct = entry.value
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(12.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BentoSurfaceElevated),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight((pct.coerceIn(0, 100)) / 100f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (pct < 60) Color(0xFFFF5252)
                                                    else if (pct < 80) Color(0xFFFFAB00)
                                                    else ElectricGreen
                                                )
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(muscle, fontSize = 9.sp, color = BentoMutedText, maxLines = 1)
                                    Text("$pct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // User Feedback Input Section (RPE, Muscle Soreness, Energy)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "YOUR ATHLETE FEEDBACK (RPE & SORENESS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                "RPE: ${rpeRating.toInt()}/10",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricGreen
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Rate difficulty & fatigue of recent sessions:",
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )

                        Slider(
                            value = rpeRating,
                            onValueChange = { rpeRating = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricGreen,
                                activeTrackColor = ElectricGreen,
                                inactiveTrackColor = BentoSurfaceElevated
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rpe_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 (Warmup)", fontSize = 10.sp, color = BentoMutedText)
                            Text("5 (Moderate)", fontSize = 10.sp, color = BentoMutedText)
                            Text("8 (High Strain)", fontSize = 10.sp, color = BentoMutedText)
                            Text("10 (Failure)", fontSize = 10.sp, color = BentoMutedText)
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Tap any sore or fatigued muscle groups:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))

                        val availableMuscles = listOf("Legs", "Lower Back", "Shoulders", "Chest", "Arms", "Knees/Joints", "Core")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableMuscles) { muscle ->
                                val isSore = selectedSoreMuscles.contains(muscle)
                                FilterChip(
                                    selected = isSore,
                                    onClick = {
                                        val updated = selectedSoreMuscles.toMutableSet()
                                        if (isSore) updated.remove(muscle) else updated.add(muscle)
                                        selectedSoreMuscles = updated
                                    },
                                    label = { Text(muscle, fontSize = 11.sp) },
                                    leadingIcon = if (isSore) {
                                        { Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF5252).copy(alpha = 0.2f),
                                        selectedLabelColor = Color(0xFFFF5252),
                                        containerColor = BentoSurfaceElevated,
                                        labelColor = BentoTextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Energy Level Selector (1 to 5)
                        Text("Energy & Sleep State:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val energyOptions = listOf(2 to "😴 Drained", 4 to "⚡ Normal", 5 to "🚀 Charged")
                            energyOptions.forEach { (lvl, label) ->
                                val isSel = energyLevel == lvl
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) RoyalPurple else BentoSurfaceElevated,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { energyLevel = lvl }
                                ) {
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else BentoTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateWorkoutFeedback(
                                    rpe = rpeRating.toInt(),
                                    soreMuscles = selectedSoreMuscles,
                                    energy = energyLevel,
                                    sleepQuality = sleepQuality,
                                    notes = userNotes
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("run_ai_adaptation_btn"),
                            enabled = !isAnalyzing
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing Biometrics...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Re-Calculate AI Adaptations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // AI Generated Modifications & Recommendations
            item {
                Text(
                    "AI PROPOSED MODIFICATIONS FOR UPCOMING ROUTINES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricGreen,
                    letterSpacing = 0.5.sp
                )
            }

            val modifications = aiAdaptation?.modifications ?: emptyList()
            if (modifications.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Tap 'Re-Calculate AI Adaptations' above to dynamically calibrate your upcoming workouts based on your latest wearable telemetry and feedback.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                    }
                }
            } else {
                items(modifications) { mod ->
                    WorkoutModificationCard(
                        mod = mod,
                        onApply = { viewModel.applyAiWorkoutAdaptation(mod) }
                    )
                }
            }

            // AI Coach Diagnostic Summary
            aiAdaptation?.overallAdvice?.let { advice ->
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceElevated),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonCyan, RoyalPurple))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("AI COACH DIAGNOSTIC NOTE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(advice, fontSize = 12.sp, color = BentoTextPrimary, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutModificationCard(
    mod: WorkoutModification,
    onApply: () -> Unit
) {
    val (typeLabel, typeColor) = when (mod.actionType) {
        AdaptationActionType.PROGRESSIVE_OVERLOAD -> "INTENSITY OVERLOAD ⚡" to ElectricGreen
        AdaptationActionType.DELOAD_VOLUME_REDUCTION -> "FATIGUE DELOAD 🛡️" to Color(0xFFFFAB00)
        AdaptationActionType.EXERCISE_SUBSTITUTION -> "EXERCISE SWAP 🔄" to NeonCyan
        AdaptationActionType.ACTIVE_RECOVERY_DAY -> "ACTIVE RECOVERY 🛌" to Color(0xFF80D8FF)
        AdaptationActionType.COMPLETE_REST_DAY -> "COMPLETE REST 🛑" to Color(0xFFFF5252)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(typeColor.copy(alpha = 0.5f), BentoBorder))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mod_card_${mod.routineId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        typeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(mod.routineTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
            }

            Spacer(Modifier.height(10.dp))
            Text(mod.scientificRationale, fontSize = 12.sp, color = BentoTextSecondary, lineHeight = 16.sp)

            Spacer(Modifier.height(10.dp))

            // Substituted / Overloaded details
            if (mod.substitutions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BentoSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Substitutions applied:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        mod.substitutions.forEach { sub ->
                            Text("• ${sub.originalExerciseName} ➔ ${sub.substituteExerciseName} (${sub.reason})", fontSize = 11.sp, color = BentoTextPrimary)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Button(
                onClick = onApply,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = typeColor, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("apply_mod_btn_${mod.routineId}")
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Apply AI Adaptation to Routine", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}
