package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.WorkoutRoutineEntity
import com.example.ui.components.AiWorkoutAdaptationBottomSheet
import com.example.ui.components.TierBadgeChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun WorkoutsScreen(
    viewModel: FitnessViewModel,
    onStartRoutine: (WorkoutRoutineEntity) -> Unit,
    onOpenPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.allRoutines.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val aiAdaptation by viewModel.aiAdaptation.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCustomWorkoutDialog by remember { mutableStateOf(false) }
    var showAiCoachDialog by remember { mutableStateOf(false) }
    var showAiAdaptationSheet by remember { mutableStateOf(false) }

    val categories = listOf("All", "Strength", "HIIT", "Cardio", "Calisthenics", "Yoga")

    val filteredRoutines = remember(routines, searchQuery, selectedCategory) {
        routines.filter { r ->
            (selectedCategory == "All" || r.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || r.title.contains(searchQuery, ignoreCase = true) || r.targetMuscleGroups.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.testTag("workouts_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCustomWorkoutDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Custom") },
                text = { Text("Custom Routine", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_custom_routine_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & AI Smart Coach Banner
            item {
                AiSmartCoachBanner(
                    onGenerateAi = { showAiCoachDialog = true },
                    onOpenAdaptation = { showAiAdaptationSheet = true },
                    userTier = userProfile?.subscriptionTier ?: "PRO",
                    onOpenPremium = onOpenPremium
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search routines, exercises, or muscle groups...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_search_input")
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Workout Routines List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORKOUT ROUTINES (${filteredRoutines.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (filteredRoutines.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Text("No routines found", fontWeight = FontWeight.Bold)
                            Text("Try adjusting your search query or filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredRoutines) { routine ->
                    WorkoutRoutineCard(
                        routine = routine,
                        userTier = userProfile?.subscriptionTier ?: "PRO",
                        onStart = { onStartRoutine(routine) },
                        onUnlock = onOpenPremium
                    )
                }
            }
        }
    }

    // Dialogs
    if (showCustomWorkoutDialog) {
        CreateCustomRoutineDialog(
            onDismiss = { showCustomWorkoutDialog = false },
            onCreate = { title, category, diff, mins, exercises ->
                viewModel.createCustomWorkout(title, category, diff, mins, exercises)
                showCustomWorkoutDialog = false
            }
        )
    }

    if (showAiCoachDialog) {
        AiCoachGeneratorDialog(
            userTier = userProfile?.subscriptionTier ?: "PRO",
            onDismiss = { showAiCoachDialog = false },
            onGenerate = { goal, targetMuscles, durationMins, difficulty ->
                viewModel.generateAiWorkout(goal, targetMuscles, durationMins, difficulty)
                showAiCoachDialog = false
            },
            onOpenPremium = {
                showAiCoachDialog = false
                onOpenPremium()
            }
        )
    }

    if (showAiAdaptationSheet) {
        AiWorkoutAdaptationBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAiAdaptationSheet = false }
        )
    }
}

@Composable
fun AiSmartCoachBanner(
    onGenerateAi: () -> Unit,
    onOpenAdaptation: () -> Unit,
    userTier: String,
    onOpenPremium: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .testTag("ai_smart_coach_banner"),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            BentoPrimaryContainer,
                            BentoPrimaryDeep
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AI DYNAMIC COACH & ADAPTATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BentoPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    TierBadgeChip(tier = "AI POWERED", onClick = onOpenPremium)
                }

                Text(
                    text = "Dynamic Routine Adaptation & Smart Overload",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "AI analyzes your wearable recovery metrics, workout history, and muscle fatigue feedback to dynamically adjust intensity, substitute exercises, or recommend rest days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenAdaptation,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_adapt_routines_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Psychology, contentDescription = null, tint = BentoPrimaryDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Adapt Routines", color = BentoPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onGenerateAi,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_ai_routine_button")
                    ) {
                        Icon(imageVector = Icons.Filled.AddCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "New AI Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutRoutineCard(
    routine: WorkoutRoutineEntity,
    userTier: String,
    onStart: () -> Unit,
    onUnlock: () -> Unit
) {
    val isLocked = routine.isPremium && userTier == "FREE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
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
                        shape = RoundedCornerShape(8.dp),
                        color = when (routine.category.lowercase()) {
                            "strength" -> RoyalPurple.copy(alpha = 0.15f)
                            "hiit" -> ElectricOrange.copy(alpha = 0.15f)
                            "cardio" -> PulseRed.copy(alpha = 0.15f)
                            else -> NeonCyan.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = routine.category.uppercase(),
                            color = when (routine.category.lowercase()) {
                                "strength" -> RoyalPurple
                                "hiit" -> ElectricOrange
                                "cardio" -> PulseRed
                                else -> NeonCyanDark
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = routine.difficulty,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (routine.isPremium) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldStar.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(11.dp))
                                Text("PRO", fontWeight = FontWeight.Black, fontSize = 10.sp, color = GoldStar)
                            }
                        }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 ${routine.targetMuscleGroups}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isLocked) {
                    Button(
                        onClick = onUnlock,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldStar),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unlock", color = DarkBackground, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomRoutineDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int, List<Exercise>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Strength") }
    var difficulty by remember { mutableStateOf("Intermediate") }
    var durationMins by remember { mutableStateOf("40") }

    var exerciseName by remember { mutableStateOf("") }
    var exerciseMuscle by remember { mutableStateOf("Chest") }
    var exerciseSets by remember { mutableStateOf("3") }
    var exerciseReps by remember { mutableStateOf("10") }
    var exerciseWeight by remember { mutableStateOf("20") }

    val exerciseList = remember { mutableStateListOf<Exercise>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Routine", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Routine Title (e.g. Chest & Tricep Pump)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationMins,
                        onValueChange = { durationMins = it },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider()
                Text("ADD EXERCISES (${exerciseList.size} added)", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Exercise Name") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exerciseMuscle,
                        onValueChange = { exerciseMuscle = it },
                        label = { Text("Muscle") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = exerciseSets,
                        onValueChange = { exerciseSets = it },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exerciseReps,
                        onValueChange = { exerciseReps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exerciseWeight,
                        onValueChange = { exerciseWeight = it },
                        label = { Text("Kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (exerciseName.isNotBlank()) {
                                exerciseList.add(
                                    Exercise(
                                        name = exerciseName.trim(),
                                        targetMuscle = exerciseMuscle.trim(),
                                        sets = exerciseSets.toIntOrNull() ?: 3,
                                        reps = exerciseReps.toIntOrNull() ?: 10,
                                        weightKg = exerciseWeight.toFloatOrNull() ?: 0f
                                    )
                                )
                                exerciseName = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = "Add Ex", tint = NeonLimeDark)
                    }
                }

                if (exerciseList.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        items(exerciseList) { ex ->
                            Text("• ${ex.name} (${ex.sets}x${ex.reps} @ ${ex.weightKg}kg)", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && exerciseList.isNotEmpty()) {
                        onCreate(
                            title.trim(),
                            category.trim(),
                            difficulty,
                            durationMins.toIntOrNull() ?: 30,
                            exerciseList.toList()
                        )
                    }
                },
                enabled = title.isNotBlank() && exerciseList.isNotEmpty()
            ) {
                Text("Save Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AiCoachGeneratorDialog(
    userTier: String,
    onDismiss: () -> Unit,
    onGenerate: (String, String, Int, String) -> Unit,
    onOpenPremium: () -> Unit
) {
    var goal by remember { mutableStateOf("Hypertrophy Muscle Growth") }
    var targetMuscles by remember { mutableStateOf("Chest, Shoulders & Triceps") }
    var durationMins by remember { mutableStateOf(45) }
    var difficulty by remember { mutableStateOf("Intermediate") }

    val goalOptions = listOf("Hypertrophy Muscle Growth", "Maximum Fat Burn & HIIT", "Athletic Power & Strength", "Endurance & Conditioning")
    val difficultyOptions = listOf("Beginner", "Intermediate", "Advanced", "Elite")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = NeonCyan)
                Text("AI Smart Coach Generator", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select your primary workout objective:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                goalOptions.forEach { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { goal = opt }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = goal == opt, onClick = { goal = opt })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(opt, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = targetMuscles,
                    onValueChange = { targetMuscles = it },
                    label = { Text("Target Muscle Groups (e.g. Upper Body / Legs)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Session Duration: $durationMins minutes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = durationMins.toFloat(),
                    onValueChange = { durationMins = it.toInt() },
                    valueRange = 15f..90f,
                    steps = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(goal, targetMuscles, durationMins, difficulty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("✨ Generate Routine", color = DarkBackground, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
