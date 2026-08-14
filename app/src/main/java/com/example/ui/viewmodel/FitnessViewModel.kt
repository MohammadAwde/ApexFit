package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FitnessRepository
import com.example.data.model.*
import com.example.notification.NotificationHelper
import com.example.wearable.DiscoveredBleDevice
import com.example.wearable.LiveHealthTelemetry
import com.example.wearable.WearableSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class ActiveWorkoutSession(
    val routine: WorkoutRoutineEntity,
    val exercises: List<Exercise>,
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 1,
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isResting: Boolean = false,
    val restRemainingSeconds: Int = 60,
    val currentRestTotal: Int = 60,
    val totalVolumeKg: Float = 0f,
    val heartRates: List<Int> = emptyList(),
    val isCompleted: Boolean = false
) {
    val currentExercise: Exercise?
        get() = exercises.getOrNull(currentExerciseIndex)

    val nextExercise: Exercise?
        get() = exercises.getOrNull(currentExerciseIndex + 1)

    val progressFraction: Float
        get() {
            val totalSets = exercises.sumOf { it.sets }
            if (totalSets == 0) return 0f
            val completedSets = exercises.take(currentExerciseIndex).sumOf { it.sets } + (currentSetIndex - 1)
            return (completedSets.toFloat() / totalSets).coerceIn(0f, 1f)
        }
}

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository
    val sensorManager = WearableSensorManager(application)

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = FitnessRepository(
            userProfileDao = database.userProfileDao(),
            workoutDao = database.workoutDao(),
            nutritionDao = database.nutritionDao(),
            wearableDao = database.wearableDao(),
            socialDao = database.socialDao(),
            goalDao = database.goalDao()
        )
        NotificationHelper.initChannels(application)
    }

    // Streams
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val allRoutines = repository.allRoutines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workoutLogs = repository.allWorkoutLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentWorkoutLogs = repository.recentWorkoutLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMeals = repository.getMealsForToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayWaterLogs = repository.getWaterLogsForToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalWaterTodayMl: StateFlow<Int> = todayWaterLogs.map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1750)

    val allWearables = repository.allWearableDevices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeWearable = repository.activeConnectedDevice.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val socialPosts = repository.allPosts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val prs = repository.allPRs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveTelemetry: StateFlow<LiveHealthTelemetry> = sensorManager.telemetry
    val isBleScanning: StateFlow<Boolean> = sensorManager.isScanning
    val discoveredDevices: StateFlow<List<DiscoveredBleDevice>> = sensorManager.discoveredDevices

    // Active Workout Player State
    private val _activeSession = MutableStateFlow<ActiveWorkoutSession?>(null)
    val activeSession: StateFlow<ActiveWorkoutSession?> = _activeSession.asStateFlow()

    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    // UI Feedback Banner
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun showFeedback(msg: String) {
        viewModelScope.launch {
            _userFeedbackMessage.value = msg
            delay(3500)
            if (_userFeedbackMessage.value == msg) {
                _userFeedbackMessage.value = null
            }
        }
    }

    // Workout Player Logic
    fun startWorkoutSession(routine: WorkoutRoutineEntity) {
        val exercises = parseExercisesJson(routine.exercisesJson)
        if (exercises.isEmpty()) return

        sensorManager.setWorkoutActiveState(true, 1.2f)
        _activeSession.value = ActiveWorkoutSession(
            routine = routine,
            exercises = exercises,
            currentExerciseIndex = 0,
            currentSetIndex = 1
        )
        startWorkoutTimers()
    }

    private fun startWorkoutTimers() {
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (_activeSession.value != null && !_activeSession.value!!.isCompleted) {
                delay(1000)
                _activeSession.value?.let { session ->
                    if (!session.isPaused) {
                        val liveBpm = sensorManager.telemetry.value.heartRateBpm
                        _activeSession.value = session.copy(
                            elapsedSeconds = session.elapsedSeconds + 1,
                            heartRates = session.heartRates + liveBpm
                        )
                    }
                }
            }
        }
    }

    fun toggleWorkoutPause() {
        _activeSession.value?.let { session ->
            _activeSession.value = session.copy(isPaused = !session.isPaused)
        }
    }

    fun completeCurrentSet() {
        val session = _activeSession.value ?: return
        val currentEx = session.currentExercise ?: return

        val addedVol = currentEx.weightKg * currentEx.reps
        val newTotalVolume = session.totalVolumeKg + addedVol

        if (session.currentSetIndex < currentEx.sets) {
            // Next set of current exercise
            startRestTimer(currentEx.restSeconds)
            _activeSession.value = session.copy(
                currentSetIndex = session.currentSetIndex + 1,
                totalVolumeKg = newTotalVolume
            )
        } else {
            // Completed all sets for this exercise
            if (session.currentExerciseIndex < session.exercises.size - 1) {
                // Move to next exercise
                startRestTimer(currentEx.restSeconds + 15)
                _activeSession.value = session.copy(
                    currentExerciseIndex = session.currentExerciseIndex + 1,
                    currentSetIndex = 1,
                    totalVolumeKg = newTotalVolume
                )
            } else {
                // Workout Completed!
                finishWorkoutSession(session.copy(totalVolumeKg = newTotalVolume))
            }
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _activeSession.value?.let { session ->
            _activeSession.value = session.copy(isResting = false, restRemainingSeconds = 0)
        }
    }

    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _activeSession.value?.let { session ->
            _activeSession.value = session.copy(
                isResting = true,
                restRemainingSeconds = seconds,
                currentRestTotal = seconds
            )
        }
        restTimerJob = viewModelScope.launch {
            var left = seconds
            while (left > 0 && _activeSession.value?.isResting == true) {
                delay(1000)
                left -= 1
                _activeSession.value?.let { session ->
                    _activeSession.value = session.copy(restRemainingSeconds = left)
                }
            }
            _activeSession.value?.let { session ->
                _activeSession.value = session.copy(isResting = false)
            }
        }
    }

    fun finishWorkoutSession(finalSession: ActiveWorkoutSession? = null) {
        val session = finalSession ?: _activeSession.value ?: return
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        sensorManager.setWorkoutActiveState(false)

        val durationSecs = session.elapsedSeconds.coerceAtLeast(60)
        val hrs = session.heartRates
        val avgHr = if (hrs.isNotEmpty()) hrs.average().toInt() else 145
        val maxHr = if (hrs.isNotEmpty()) hrs.maxOrNull() ?: 175 else 175
        val calculatedCals = ((durationSecs / 60f) * (avgHr / 160f * 9.5f)).toInt().coerceAtLeast(80)

        viewModelScope.launch {
            val log = WorkoutLogEntity(
                routineId = session.routine.id,
                routineTitle = session.routine.title,
                category = session.routine.category,
                durationSeconds = durationSecs,
                caloriesBurned = calculatedCals,
                avgHeartRate = avgHr,
                maxHeartRate = maxHr,
                totalVolumeKg = session.totalVolumeKg,
                exercisesCompletedJson = session.routine.exercisesJson,
                notes = "Completed ${session.exercises.size} exercises. Peak intensity reached.",
                feelingRating = 5
            )
            repository.logCompletedWorkout(log)
            repository.incrementStreak()
            _activeSession.value = session.copy(isCompleted = true)

            // Auto trigger motivation notification / badge
            NotificationHelper.sendAchievementNotification(
                getApplication(),
                "${session.routine.title} Finished!",
                "Crushed $calculatedCals kcal in ${(durationSecs / 60)} mins with $avgHr bpm avg HR!"
            )
            showFeedback("Workout completed! +$calculatedCals kcal logged to your progress.")
        }
    }

    fun exitWorkoutPlayer() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        sensorManager.setWorkoutActiveState(false)
        _activeSession.value = null
    }

    // Nutrition actions
    fun addMeal(name: String, mealType: String, calories: Int, protein: Int, carbs: Int, fat: Int, serving: String) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertMeal(
                MealEntryEntity(
                    name = name,
                    mealType = mealType,
                    calories = calories,
                    proteinG = protein,
                    carbsG = carbs,
                    fatG = fat,
                    servingSize = serving,
                    dateString = todayStr
                )
            )
            showFeedback("Logged $name ($calories kcal) to $mealType")
        }
    }

    fun deleteMeal(meal: MealEntryEntity) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
            showFeedback("Removed ${meal.name}")
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
            val current = totalWaterTodayMl.value + amountMl
            val target = userProfile.value?.dailyWaterGoalMl ?: 3000
            if (current >= target) {
                NotificationHelper.sendAchievementNotification(
                    getApplication(),
                    "Hydration Goal Smashed! 💧",
                    "You reached $current ml today. Excellent cellular hydration!"
                )
            }
            showFeedback("Added +$amountMl ml water")
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            repository.resetWaterToday()
            showFeedback("Reset today's water log")
        }
    }

    // Wearables & Devices
    fun scanForDevices() {
        sensorManager.scanForDevices()
    }

    fun pairAndConnectDevice(device: DiscoveredBleDevice) {
        viewModelScope.launch {
            val entity = WearableDeviceEntity(
                id = device.id,
                name = device.name,
                type = device.type,
                brand = device.brand,
                isConnected = true,
                batteryPercent = 90,
                lastSyncTimestamp = System.currentTimeMillis(),
                currentHeartRateBpm = sensorManager.telemetry.value.heartRateBpm,
                stepsToday = sensorManager.telemetry.value.steps,
                recoveryScore = 95
            )
            repository.connectDevice(entity)
            sensorManager.setActiveDevice(entity)
            showFeedback("Connected to ${device.name} via Bluetooth LE")
        }
    }

    fun disconnectWearable(deviceId: String) {
        viewModelScope.launch {
            repository.disconnectDevice(deviceId)
            showFeedback("Wearable disconnected")
        }
    }

    // Social actions
    fun toggleKudos(postId: Long) {
        viewModelScope.launch {
            repository.toggleKudos(postId)
        }
    }

    fun shareWorkoutToCommunity(title: String, notes: String, achievementBadge: String? = null) {
        val user = userProfile.value
        val name = user?.name ?: "Alex Rivers"
        val tier = user?.subscriptionTier ?: "PRO"

        viewModelScope.launch {
            val post = SocialPostEntity(
                authorName = name,
                authorAvatarBadge = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                authorTier = tier,
                title = title,
                content = notes,
                workoutSummary = "ApexFit Session • Streak: ${user?.streakDays ?: 14} Days",
                achievementBadge = achievementBadge ?: "Workout Completed 🔥",
                kudosCount = 1,
                isKudosGiven = true,
                isUserPost = true
            )
            repository.createSocialPost(post)
            showFeedback("Shared workout milestone to community feed!")
        }
    }

    fun shareNativeIntent(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Workout Achievement")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // Custom Workout Creator & AI Smart Coach Generator
    fun createCustomWorkout(
        title: String,
        category: String,
        difficulty: String,
        durationMins: Int,
        exercises: List<Exercise>
    ) {
        viewModelScope.launch {
            val json = serializeExercises(exercises)
            val entity = WorkoutRoutineEntity(
                title = title,
                description = "Personalized custom routine focused on ${exercises.joinToString(", ") { it.targetMuscle }.take(40)}",
                category = category,
                difficulty = difficulty,
                durationMinutes = durationMins,
                estimatedCalories = durationMins * 8,
                exercisesJson = json,
                isCustom = true
            )
            repository.insertRoutine(entity)
            showFeedback("Created custom workout: $title")
        }
    }

    fun generateAiWorkout(goal: String, targetMuscles: String, durationMins: Int, difficulty: String) {
        viewModelScope.launch {
            val aiExercises = when {
                targetMuscles.contains("Chest", ignoreCase = true) || targetMuscles.contains("Upper", ignoreCase = true) -> listOf(
                    Exercise("Incline Dumbbell Press", "Chest", 4, 10, 26f, 0, 75, "Controlled stretch at bottom"),
                    Exercise("Cable Chest Flyes", "Chest", 3, 12, 18f, 0, 60, "Squeeze pecs for 1 sec"),
                    Exercise("Standing Overhead Military Press", "Shoulders", 4, 8, 45f, 0, 90, "Brace core tight"),
                    Exercise("Skullcrushers / EZ Bar Extension", "Arms", 3, 12, 25f, 0, 60, "Elbows vertical"),
                    Exercise("Hanging Leg Lifts", "Core", 3, 15, 0f, 0, 45, "No momentum swinging")
                )
                targetMuscles.contains("Legs", ignoreCase = true) || targetMuscles.contains("Lower", ignoreCase = true) -> listOf(
                    Exercise("Bulgarian Split Squats", "Legs", 4, 10, 20f, 0, 75, "Slight forward torso lean"),
                    Exercise("Romanian Deadlifts (RDL)", "Legs", 4, 10, 70f, 0, 90, "Feel deep hamstring stretch"),
                    Exercise("Walking Dumbbell Lunges", "Legs", 3, 12, 18f, 0, 60, "90-degree knee bend"),
                    Exercise("Standing Calf Raises", "Legs", 4, 15, 40f, 0, 45, "Pause at full extension"),
                    Exercise("Ab Wheel Rollouts", "Core", 3, 12, 0f, 0, 60, "Hollow body positioning")
                )
                else -> listOf(
                    Exercise("Dumbbell Thrusters", "Full Body", 4, 12, 16f, 0, 60, "Explosive squat into press"),
                    Exercise("Renegade Rows with Push-Up", "Back", 3, 10, 14f, 0, 60, "Anti-rotation core brace"),
                    Exercise("Goblet Squats", "Legs", 4, 12, 24f, 0, 60, "Deep mobility squat"),
                    Exercise("Kettlebell Swings", "Full Body", 4, 20, 20f, 0, 45, "Power hip snap"),
                    Exercise("Plank with Shoulder Taps", "Core", 3, 20, 0f, 45, 30, "Keep hips parallel to floor")
                )
            }

            val routine = WorkoutRoutineEntity(
                title = "AI Smart Coach: $goal $difficulty",
                description = "Custom synthesized by ApexFit AI Coach for $targetMuscles • $durationMins mins session.",
                category = if (goal.contains("Fat", ignoreCase = true) || goal.contains("HIIT", ignoreCase = true)) "HIIT" else "Strength",
                difficulty = difficulty,
                durationMinutes = durationMins,
                estimatedCalories = durationMins * 9,
                exercisesJson = serializeExercises(aiExercises),
                isCustom = true,
                isPremium = true
            )
            repository.insertRoutine(routine)
            showFeedback("✨ AI Smart Coach generated routine: ${routine.title}")
        }
    }

    // Goals & PRs
    fun addGoal(title: String, category: String, targetVal: Float, unit: String, deadline: String) {
        viewModelScope.launch {
            repository.insertGoal(
                FitnessGoalEntity(
                    title = title,
                    category = category,
                    currentVal = 0f,
                    targetVal = targetVal,
                    unit = unit,
                    deadlineString = deadline
                )
            )
            showFeedback("New goal added: $title")
        }
    }

    fun addPersonalRecord(exercise: String, record: String, previous: String?, category: String) {
        viewModelScope.launch {
            repository.insertPR(
                PersonalRecordEntity(
                    exerciseName = exercise,
                    recordValue = record,
                    previousRecord = previous,
                    achievedDate = "Today",
                    category = category
                )
            )
            showFeedback("🏆 PR recorded for $exercise: $record")
        }
    }

    // Premium Subscription
    fun updateSubscriptionTier(tier: String) {
        viewModelScope.launch {
            repository.updateSubscriptionTier(tier)
            showFeedback("Upgraded to ApexFit $tier Plan! All features unlocked.")
        }
    }

    // Settings
    fun updateTheme(isDark: Boolean?) {
        viewModelScope.launch {
            repository.updateThemeMode(isDark)
        }
    }

    fun updateProfileInfo(height: Float, weight: Float, targetWeight: Float, dailyCals: Int, stepGoal: Int, waterGoal: Int) {
        viewModelScope.launch {
            val curr = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(
                curr.copy(
                    heightCm = height,
                    weightKg = weight,
                    targetWeightKg = targetWeight,
                    dailyCalorieGoal = dailyCals,
                    dailyStepGoal = stepGoal,
                    dailyWaterGoalMl = waterGoal
                )
            )
            showFeedback("Fitness profile updated")
        }
    }

    fun triggerTestNotification(type: String) {
        when (type) {
            "motivation" -> NotificationHelper.sendMotivationalNotification(getApplication())
            "workout" -> NotificationHelper.sendWorkoutReminder(getApplication())
            "hydration" -> NotificationHelper.sendHydrationReminder(getApplication(), totalWaterTodayMl.value, userProfile.value?.dailyWaterGoalMl ?: 3000)
            "achievement" -> NotificationHelper.sendAchievementNotification(getApplication(), "Apex Champion 🏆", "You completed 14 consecutive active days!")
        }
        showFeedback("Sent test push notification: $type")
    }

    // Utilities
    private fun parseExercisesJson(json: String): List<Exercise> {
        val list = mutableListOf<Exercise>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Exercise(
                        name = obj.optString("name", "Exercise"),
                        targetMuscle = obj.optString("targetMuscle", "Full Body"),
                        sets = obj.optInt("sets", 3),
                        reps = obj.optInt("reps", 10),
                        weightKg = obj.optDouble("weightKg", 0.0).toFloat(),
                        durationSeconds = obj.optInt("durationSeconds", 0),
                        restSeconds = obj.optInt("restSeconds", 60),
                        tips = obj.optString("tips", ""),
                        animationCue = obj.optString("animationCue", "")
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
            list.add(Exercise("Standard Push-Ups", "Chest", 3, 15, 0f, 0, 45, "Elbows at 45 degrees"))
            list.add(Exercise("Bodyweight Squats", "Legs", 3, 20, 0f, 0, 45, "Full depth"))
        }
        return list
    }

    private fun serializeExercises(exercises: List<Exercise>): String {
        val array = JSONArray()
        exercises.forEach { ex ->
            val obj = JSONObject()
            obj.put("name", ex.name)
            obj.put("targetMuscle", ex.targetMuscle)
            obj.put("sets", ex.sets)
            obj.put("reps", ex.reps)
            obj.put("weightKg", ex.weightKg.toDouble())
            obj.put("durationSeconds", ex.durationSeconds)
            obj.put("restSeconds", ex.restSeconds)
            obj.put("tips", ex.tips)
            obj.put("animationCue", ex.animationCue)
            array.put(obj)
        }
        return array.toString()
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.destroy()
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
    }
}
