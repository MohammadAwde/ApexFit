package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FitnessRepository
import com.example.data.ai.AiWorkoutAdaptationEngine
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

    // AI Workout Adaptation State
    private val _userWorkoutFeedback = MutableStateFlow(
        UserWorkoutFeedback(
            rpeRating = 6,
            soreMuscles = setOf("Legs"),
            energyLevel = 4,
            sleepQualityRating = 4,
            userNotes = "Quads feeling fatigued from squats, ready for progressive overload on upper body"
        )
    )
    val userWorkoutFeedback: StateFlow<UserWorkoutFeedback> = _userWorkoutFeedback.asStateFlow()

    private val _aiAdaptation = MutableStateFlow<AiAdaptationAnalysis?>(null)
    val aiAdaptation: StateFlow<AiAdaptationAnalysis?> = _aiAdaptation.asStateFlow()

    private val _isAiAnalyzing = MutableStateFlow(false)
    val isAiAnalyzing: StateFlow<Boolean> = _isAiAnalyzing.asStateFlow()

    // Gamification & Challenges State
    private val _challenges = MutableStateFlow(
        listOf(
            FitnessChallenge(
                id = "daily_cals_400",
                title = "Metabolic Inferno",
                description = "Burn 400 active calories in any workout session today.",
                category = ChallengeCategory.DAILY,
                metricType = ChallengeMetricType.CALORIES,
                targetValue = 400f,
                currentValue = 430f,
                xpReward = 150,
                deadlineText = "Ends Today (Midnight)",
                isJoined = true,
                isClaimed = false,
                iconDescriptor = "FIRE"
            ),
            FitnessChallenge(
                id = "daily_steps_10k",
                title = "10,000 Step Quest",
                description = "Log 10,000 steps through wearable or sensor tracking.",
                category = ChallengeCategory.DAILY,
                metricType = ChallengeMetricType.STEPS,
                targetValue = 10000f,
                currentValue = 7840f,
                xpReward = 120,
                deadlineText = "Ends in 6 hrs",
                isJoined = true,
                isClaimed = false,
                iconDescriptor = "RUN"
            ),
            FitnessChallenge(
                id = "daily_water_hero",
                title = "Cellular Hydration 3L",
                description = "Drink and log 3,000 ml of water throughout the day.",
                category = ChallengeCategory.DAILY,
                metricType = ChallengeMetricType.HYDRATION_ML,
                targetValue = 3000f,
                currentValue = 1750f,
                xpReward = 100,
                deadlineText = "Ends in 6 hrs",
                isJoined = true,
                isClaimed = false,
                iconDescriptor = "WATER"
            ),
            FitnessChallenge(
                id = "weekly_iron_titan",
                title = "Iron Lifter 25,000 kg Volume",
                description = "Accumulate 25,000 kg in total lifted resistance volume across all routines this week.",
                category = ChallengeCategory.WEEKLY,
                metricType = ChallengeMetricType.TOTAL_VOLUME_KG,
                targetValue = 25000f,
                currentValue = 18450f,
                xpReward = 500,
                deadlineText = "3 Days Left",
                isJoined = true,
                isClaimed = false,
                iconDescriptor = "STRENGTH"
            ),
            FitnessChallenge(
                id = "weekly_zone4_cardio",
                title = "Zone 4 Cardio Crusher",
                description = "Spend at least 45 minutes in high-intensity Anaerobic Zone 4 (>150 bpm).",
                category = ChallengeCategory.WEEKLY,
                metricType = ChallengeMetricType.DURATION_MINUTES,
                targetValue = 45f,
                currentValue = 32f,
                xpReward = 450,
                deadlineText = "3 Days Left",
                isJoined = true,
                isClaimed = false,
                iconDescriptor = "HEART"
            ),
            FitnessChallenge(
                id = "community_global_million",
                title = "August 1,000,000 Steps Pool",
                description = "Join 4,200+ ApexFit athletes to collectively hit 1 Million steps this week.",
                category = ChallengeCategory.COMMUNITY,
                metricType = ChallengeMetricType.STEPS,
                targetValue = 1000000f,
                currentValue = 742500f,
                xpReward = 750,
                deadlineText = "4 Days Left",
                isJoined = true,
                isClaimed = false,
                iconDescriptor = "GLOBE"
            )
        )
    )
    val challenges: StateFlow<List<FitnessChallenge>> = _challenges.asStateFlow()

    private val _milestoneBadges = MutableStateFlow(
        listOf(
            MilestoneBadge(
                id = "badge_first_5k",
                title = "First 5k Run",
                subtitle = "Cardio Milestone",
                description = "Completed first continuous 5 Kilometer tempo run or outdoor track.",
                rarity = BadgeRarity.RARE,
                category = BadgeCategory.CARDIO,
                currentProgress = 1,
                maxProgress = 1,
                isUnlocked = true,
                unlockedDate = "3 Days ago",
                xpReward = 300,
                iconEmoji = "🏃"
            ),
            MilestoneBadge(
                id = "badge_10_streak",
                title = "10 Workout Streak",
                subtitle = "Consistency Champion",
                description = "Logged 10 or more consecutive active workout days without breaking rhythm.",
                rarity = BadgeRarity.EPIC,
                category = BadgeCategory.STREAK,
                currentProgress = 16,
                maxProgress = 10,
                isUnlocked = true,
                unlockedDate = "Yesterday",
                xpReward = 500,
                iconEmoji = "🔥"
            ),
            MilestoneBadge(
                id = "badge_iron_titan",
                title = "Iron Titan (50k Volume)",
                subtitle = "Strength Elite",
                description = "Lifted over 50,000 kg in cumulative resistance training volume.",
                rarity = BadgeRarity.LEGENDARY,
                category = BadgeCategory.STRENGTH,
                currentProgress = 38450,
                maxProgress = 50000,
                isUnlocked = false,
                unlockedDate = null,
                xpReward = 1000,
                iconEmoji = "🏋️"
            ),
            MilestoneBadge(
                id = "badge_hydration_hero",
                title = "Hydration Hero",
                subtitle = "Optimal Recovery",
                description = "Hit your daily 3,000ml water goal 7 days in a single week.",
                rarity = BadgeRarity.COMMON,
                category = BadgeCategory.HYDRATION,
                currentProgress = 6,
                maxProgress = 7,
                isUnlocked = false,
                unlockedDate = null,
                xpReward = 250,
                iconEmoji = "💧"
            ),
            MilestoneBadge(
                id = "badge_hiit_dynamo",
                title = "HIIT Dynamo",
                subtitle = "Metabolic Peak",
                description = "Burned 350+ kcal in a single high-intensity interval session.",
                rarity = BadgeRarity.RARE,
                category = BadgeCategory.CARDIO,
                currentProgress = 1,
                maxProgress = 1,
                isUnlocked = true,
                unlockedDate = "Last Week",
                xpReward = 350,
                iconEmoji = "⚡"
            ),
            MilestoneBadge(
                id = "badge_century_club",
                title = "Centurion Lifter",
                subtitle = "100 Sets Logged",
                description = "Successfully finished 100 recorded exercise sets across all routines.",
                rarity = BadgeRarity.EPIC,
                category = BadgeCategory.STRENGTH,
                currentProgress = 82,
                maxProgress = 100,
                isUnlocked = false,
                unlockedDate = null,
                xpReward = 600,
                iconEmoji = "👑"
            ),
            MilestoneBadge(
                id = "badge_early_bird",
                title = "Early Bird Dawn Crusher",
                subtitle = "Morning Discipline",
                description = "Completed and logged a full workout session before 8:00 AM.",
                rarity = BadgeRarity.COMMON,
                category = BadgeCategory.CONSISTENCY,
                currentProgress = 1,
                maxProgress = 1,
                isUnlocked = true,
                unlockedDate = "5 Days ago",
                xpReward = 200,
                iconEmoji = "🌅"
            ),
            MilestoneBadge(
                id = "badge_night_owl_recovery",
                title = "Night Owl Mobility",
                subtitle = "Joint Longevity",
                description = "Finished a 25-min dynamic flow & recovery session in the evening.",
                rarity = BadgeRarity.COMMON,
                category = BadgeCategory.RECOVERY,
                currentProgress = 1,
                maxProgress = 1,
                isUnlocked = true,
                unlockedDate = "2 Weeks ago",
                xpReward = 200,
                iconEmoji = "🌙"
            ),
            MilestoneBadge(
                id = "badge_calisthenics_ace",
                title = "Calisthenics Ace",
                subtitle = "Bodyweight Mastery",
                description = "Recorded 15+ strict pull-ups or bodyweight dip PR in single set.",
                rarity = BadgeRarity.RARE,
                category = BadgeCategory.STRENGTH,
                currentProgress = 1,
                maxProgress = 1,
                isUnlocked = true,
                unlockedDate = "Yesterday",
                xpReward = 400,
                iconEmoji = "🦾"
            )
        )
    )
    val milestoneBadges: StateFlow<List<MilestoneBadge>> = _milestoneBadges.asStateFlow()

    private val _gamificationStats = MutableStateFlow(
        UserGamificationStats(
            totalXp = 3120,
            currentLevel = 9,
            xpToNextLevel = 3600,
            currentLevelXp = 720,
            totalBadgesUnlocked = 6,
            activeChallengesCount = 6,
            completedChallengesCount = 14,
            rankTitle = "Apex Champion"
        )
    )
    val gamificationStats: StateFlow<UserGamificationStats> = _gamificationStats.asStateFlow()

    private val _newlyUnlockedBadge = MutableStateFlow<MilestoneBadge?>(null)
    val newlyUnlockedBadge: StateFlow<MilestoneBadge?> = _newlyUnlockedBadge.asStateFlow()

    private val _selectedLeaderboardCategory = MutableStateFlow("XP")
    val selectedLeaderboardCategory: StateFlow<String> = _selectedLeaderboardCategory.asStateFlow()

    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _selectedLeaderboardCategory.map { cat ->
        when (cat) {
            "Calories" -> listOf(
                LeaderboardEntry(1, "Marcus Vance", "MV", "ELITE", 6420, "kcal", isCurrentUser = false, level = 14, trend = "UP"),
                LeaderboardEntry(2, userProfile.value?.name ?: "Alex Rivers", "AR", "PRO", 5890, "kcal", isCurrentUser = true, level = 9, trend = "UP"),
                LeaderboardEntry(3, "Elena Rostova", "ER", "ELITE", 5410, "kcal", isCurrentUser = false, level = 12, trend = "SAME"),
                LeaderboardEntry(4, "David Kim", "DK", "PRO", 4980, "kcal", isCurrentUser = false, level = 8, trend = "DOWN"),
                LeaderboardEntry(5, "Sarah Jenkins", "SJ", "ATHLETE", 4620, "kcal", isCurrentUser = false, level = 7, trend = "UP"),
                LeaderboardEntry(6, "Liam Chen", "LC", "PRO", 4350, "kcal", isCurrentUser = false, level = 6, trend = "SAME")
            )
            "Steps" -> listOf(
                LeaderboardEntry(1, "Elena Rostova", "ER", "ELITE", 14820, "steps", isCurrentUser = false, level = 12, trend = "UP"),
                LeaderboardEntry(2, "Marcus Vance", "MV", "ELITE", 12400, "steps", isCurrentUser = false, level = 14, trend = "SAME"),
                LeaderboardEntry(3, userProfile.value?.name ?: "Alex Rivers", "AR", "PRO", 10240, "steps", isCurrentUser = true, level = 9, trend = "UP"),
                LeaderboardEntry(4, "Jordan Kai", "JK", "PRO", 9820, "steps", isCurrentUser = false, level = 10, trend = "UP"),
                LeaderboardEntry(5, "Emily Stone", "ES", "ATHLETE", 8940, "steps", isCurrentUser = false, level = 8, trend = "DOWN")
            )
            "Streak" -> listOf(
                LeaderboardEntry(1, "Marcus Vance", "MV", "ELITE", 42, "days", isCurrentUser = false, level = 14, trend = "SAME"),
                LeaderboardEntry(2, "Elena Rostova", "ER", "ELITE", 28, "days", isCurrentUser = false, level = 12, trend = "UP"),
                LeaderboardEntry(3, userProfile.value?.name ?: "Alex Rivers", "AR", "PRO", 16, "days", isCurrentUser = true, level = 9, trend = "UP"),
                LeaderboardEntry(4, "David Kim", "DK", "PRO", 14, "days", isCurrentUser = false, level = 8, trend = "SAME"),
                LeaderboardEntry(5, "Jordan Kai", "JK", "PRO", 12, "days", isCurrentUser = false, level = 10, trend = "UP")
            )
            else -> listOf( // XP
                LeaderboardEntry(1, "Marcus Vance", "MV", "ELITE", 4850, "XP", isCurrentUser = false, level = 14, trend = "UP"),
                LeaderboardEntry(2, userProfile.value?.name ?: "Alex Rivers", "AR", "PRO", 3120, "XP", isCurrentUser = true, level = 9, trend = "UP"),
                LeaderboardEntry(3, "Elena Rostova", "ER", "ELITE", 2980, "XP", isCurrentUser = false, level = 12, trend = "SAME"),
                LeaderboardEntry(4, "Jordan Kai", "JK", "PRO", 2450, "XP", isCurrentUser = false, level = 10, trend = "DOWN"),
                LeaderboardEntry(5, "David Kim", "DK", "PRO", 2180, "XP", isCurrentUser = false, level = 8, trend = "UP")
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // AI Dynamic Workout Adaptation Methods
    fun updateWorkoutFeedback(
        rpe: Int,
        soreMuscles: Set<String>,
        energy: Int,
        sleepQuality: Int = 4,
        notes: String = ""
    ) {
        _userWorkoutFeedback.value = UserWorkoutFeedback(
            rpeRating = rpe,
            soreMuscles = soreMuscles,
            energyLevel = energy,
            sleepQualityRating = sleepQuality,
            userNotes = notes
        )
        // Automatically re-run dynamic analysis when feedback updates
        runAiWorkoutAdaptation()
    }

    fun runAiWorkoutAdaptation(routineToAdapt: WorkoutRoutineEntity? = null) {
        viewModelScope.launch {
            _isAiAnalyzing.value = true
            val routines = allRoutines.value.ifEmpty { emptyList() }
            val logs = recentWorkoutLogs.value
            val device = activeWearable.value
            val feedback = _userWorkoutFeedback.value

            val analysis = AiWorkoutAdaptationEngine.analyzeAndAdaptWorkouts(
                routines = routines,
                recentLogs = logs,
                activeWearable = device,
                feedback = feedback
            )

            // Optional deep AI coaching enhancement
            val deepAdvice = AiWorkoutAdaptationEngine.requestGeminiAiDeepAnalysis(analysis, feedback)
            val finalAnalysis = if (deepAdvice != analysis.overallAdvice) {
                analysis.copy(overallAdvice = deepAdvice)
            } else {
                analysis
            }

            _aiAdaptation.value = finalAnalysis
            _isAiAnalyzing.value = false
            showFeedback("🧠 AI Coach: Generated dynamic workout routine adaptations!")
        }
    }

    fun applyAiWorkoutAdaptation(modification: WorkoutModification) {
        viewModelScope.launch {
            val routine = allRoutines.value.find { it.id == modification.routineId }
            if (routine != null) {
                val currentExercises = parseExercisesJson(routine.exercisesJson)
                val updatedExercises = currentExercises.map { ex ->
                    // Check if there is a substitution for this exercise
                    val sub = modification.substitutions.find { it.originalExerciseName.equals(ex.name, true) }
                    if (sub != null) {
                        ex.copy(
                            name = sub.substituteExerciseName,
                            sets = sub.modifiedSets,
                            reps = sub.modifiedReps,
                            weightKg = sub.modifiedWeightKg,
                            tips = "${sub.safetyTip} • AI Joint & Fatigue Adaptation",
                            restSeconds = (ex.restSeconds + modification.restSecondsAdjustment).coerceAtLeast(30)
                        )
                    } else {
                        // Apply progressive overload or deload adjustments
                        val adjustedWeight = (ex.weightKg + modification.weightAdjustmentKg).coerceAtLeast(0f)
                        val adjustedSets = (ex.sets + modification.setAdjustment).coerceIn(2, 6)
                        val adjustedReps = (ex.reps + modification.repAdjustment).coerceIn(4, 30)
                        val adjustedRest = (ex.restSeconds + modification.restSecondsAdjustment).coerceIn(20, 180)
                        ex.copy(
                            sets = adjustedSets,
                            reps = adjustedReps,
                            weightKg = adjustedWeight,
                            restSeconds = adjustedRest,
                            tips = if (modification.actionType == AdaptationActionType.PROGRESSIVE_OVERLOAD)
                                "${ex.tips} • AI Progressive Overload (+${modification.weightAdjustmentKg}kg)"
                            else if (modification.actionType == AdaptationActionType.DELOAD_VOLUME_REDUCTION)
                                "${ex.tips} • AI Deload (-15% volume recovery)"
                            else ex.tips
                        )
                    }
                }

                val updatedRoutine = routine.copy(
                    exercisesJson = serializeExercises(updatedExercises),
                    description = "${routine.description} (AI Adapted: ${modification.actionType.name.replace('_', ' ')})"
                )
                repository.updateRoutine(updatedRoutine)
                showFeedback("✅ Applied AI Dynamic Adaptation to ${routine.title}!")
            } else if (modification.suggestedAlternativeRoutineTitle != null) {
                showFeedback("🧘 Switched today's focus to ${modification.suggestedAlternativeRoutineTitle}")
            }
        }
    }

    // Gamification Methods
    fun joinChallenge(challengeId: String) {
        _challenges.update { list ->
            list.map { ch ->
                if (ch.id == challengeId) ch.copy(isJoined = true) else ch
            }
        }
        showFeedback("Joined fitness challenge!")
    }

    fun claimChallengeReward(challengeId: String) {
        var claimedXp = 0
        var challengeTitle = ""
        _challenges.update { list ->
            list.map { ch ->
                if (ch.id == challengeId && ch.isCompleted && !ch.isClaimed) {
                    claimedXp = ch.xpReward
                    challengeTitle = ch.title
                    ch.copy(isClaimed = true)
                } else ch
            }
        }

        if (claimedXp > 0) {
            awardXp(claimedXp, "Challenge Completed: $challengeTitle")
        }
    }

    fun awardXp(amount: Int, reason: String) {
        val current = _gamificationStats.value
        val newTotalXp = current.totalXp + amount
        val newLevel = (newTotalXp / 350) + 1
        val levelXp = newTotalXp % 350

        _gamificationStats.value = current.copy(
            totalXp = newTotalXp,
            currentLevel = newLevel,
            currentLevelXp = levelXp,
            xpToNextLevel = (newLevel) * 350 + 350
        )
        showFeedback("+$amount XP! $reason")

        // Trigger notification if leveled up
        if (newLevel > current.currentLevel) {
            NotificationHelper.sendAchievementNotification(
                getApplication(),
                "Level Up! Level $newLevel Reached 🚀",
                "Congratulations! You unlocked the Apex Rank Level $newLevel."
            )
        }
    }

    fun dismissBadgeCelebration() {
        _newlyUnlockedBadge.value = null
    }

    fun switchLeaderboardCategory(category: String) {
        _selectedLeaderboardCategory.value = category
    }

    private fun checkMilestoneUnlocks(log: WorkoutLogEntity? = null) {
        val currentStreak = userProfile.value?.streakDays ?: 14
        val totalVolume = recentWorkoutLogs.value.sumOf { it.totalVolumeKg.toDouble() }

        _milestoneBadges.update { list ->
            list.map { badge ->
                when (badge.id) {
                    "badge_10_streak" -> {
                        if (currentStreak >= 10 && !badge.isUnlocked) {
                            val unlocked = badge.copy(isUnlocked = true, unlockedDate = "Today")
                            _newlyUnlockedBadge.value = unlocked
                            unlocked
                        } else badge.copy(currentProgress = currentStreak)
                    }
                    "badge_iron_titan" -> {
                        if (totalVolume >= 50000 && !badge.isUnlocked) {
                            val unlocked = badge.copy(isUnlocked = true, unlockedDate = "Today")
                            _newlyUnlockedBadge.value = unlocked
                            unlocked
                        } else badge.copy(currentProgress = totalVolume.toInt())
                    }
                    else -> badge
                }
            }
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
