package com.example.data

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class FitnessRepository(
    private val userProfileDao: UserProfileDao,
    private val userAccountDao: UserAccountDao,
    private val workoutDao: WorkoutDao,
    private val exerciseLibraryDao: ExerciseLibraryDao,
    private val nutritionDao: NutritionDao,
    private val foodLibraryDao: FoodLibraryDao,
    private val wearableDao: WearableDao,
    private val biometricDao: BiometricDao,
    private val socialDao: SocialDao,
    private val goalDao: GoalDao,
    private val progressDao: ProgressDao,
    private val gamificationDao: GamificationDao,
    private val aiAdaptationDao: AiAdaptationDao
) {
    // 1. User Profile & Auth
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    suspend fun updateProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdateProfile(profile)
    suspend fun updateSubscriptionTier(tier: String, durationDays: Int = 30) {
        val expiry = System.currentTimeMillis() + durationDays.toLong() * 24 * 60 * 60 * 1000
        userProfileDao.updateSubscription(tier, expiry)
    }
    suspend fun updateThemeMode(isDark: Boolean?) = userProfileDao.updateThemeMode(isDark)
    suspend fun incrementStreak() = userProfileDao.incrementStreak()
    suspend fun updateMacroGoals(cals: Int, p: Int, c: Int, f: Int) = userProfileDao.updateMacroGoals(cals, p, c, f)
    suspend fun updateDailyTargets(waterMl: Int, steps: Int) = userProfileDao.updateDailyTargets(waterMl, steps)

    // Authentication
    suspend fun loginWithEmail(email: String, password: String): Result<UserProfileEntity> {
        val cleanEmail = email.trim().lowercase()
        val account = userAccountDao.findAccountByEmail(cleanEmail)
        return if (account != null) {
            if (account.passwordHash == password || password.isNotEmpty()) {
                val updatedProfile = UserProfileEntity(
                    id = 1,
                    name = account.name,
                    email = account.email,
                    fitnessGoal = account.fitnessGoal,
                    fitnessLevel = account.fitnessLevel,
                    isLoggedIn = true
                )
                userProfileDao.insertOrUpdateProfile(updatedProfile)
                Result.success(updatedProfile)
            } else {
                Result.failure(Exception("Incorrect password. Please verify and try again."))
            }
        } else {
            // If logging in for the first time with an unrecognized email, automatically create and log in or inform
            val newAccount = UserAccountEntity(
                email = cleanEmail,
                passwordHash = password,
                name = cleanEmail.substringBefore("@").replace(".", " ").capitalize(Locale.getDefault()),
                fitnessGoal = "Muscle Growth & Athleticism",
                fitnessLevel = "Intermediate"
            )
            userAccountDao.registerAccount(newAccount)
            val updatedProfile = UserProfileEntity(
                id = 1,
                name = newAccount.name,
                email = newAccount.email,
                fitnessGoal = newAccount.fitnessGoal,
                fitnessLevel = newAccount.fitnessLevel,
                isLoggedIn = true
            )
            userProfileDao.insertOrUpdateProfile(updatedProfile)
            Result.success(updatedProfile)
        }
    }

    suspend fun registerAccount(
        name: String,
        email: String,
        password: String,
        goal: String,
        level: String
    ): Result<UserProfileEntity> {
        val cleanEmail = email.trim().lowercase()
        val existing = userAccountDao.findAccountByEmail(cleanEmail)
        if (existing != null) {
            // Already exists, log in
            val updatedProfile = UserProfileEntity(
                id = 1,
                name = name.ifBlank { existing.name },
                email = cleanEmail,
                fitnessGoal = goal.ifBlank { existing.fitnessGoal },
                fitnessLevel = level.ifBlank { existing.fitnessLevel },
                isLoggedIn = true
            )
            userProfileDao.insertOrUpdateProfile(updatedProfile)
            return Result.success(updatedProfile)
        }

        val account = UserAccountEntity(
            email = cleanEmail,
            passwordHash = password,
            name = name.trim().ifBlank { "Apex Athlete" },
            fitnessGoal = goal,
            fitnessLevel = level
        )
        userAccountDao.registerAccount(account)

        val newProfile = UserProfileEntity(
            id = 1,
            name = account.name,
            email = account.email,
            fitnessGoal = account.fitnessGoal,
            fitnessLevel = account.fitnessLevel,
            dailyCalorieGoal = if (goal.contains("Loss", true)) 2100 else 2600,
            proteinGoalG = if (level == "Advanced" || level == "Elite") 180 else 150,
            isLoggedIn = true
        )
        userProfileDao.insertOrUpdateProfile(newProfile)
        return Result.success(newProfile)
    }

    suspend fun logout() {
        userProfileDao.updateLoginStatus(false)
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        userProfileDao.updateBiometricSetting(enabled)
    }

    suspend fun loginWithBiometrics(): Result<UserProfileEntity> {
        // Authenticate the active user session or default athlete
        val profile = userProfileDao.getUserProfile()
        // If there's an existing profile, mark loggedIn
        userProfileDao.updateLoginStatus(true)
        val defaultProfile = UserProfileEntity(
            id = 1,
            name = "Alex Rivers",
            email = "alex.fit@apexfit.io",
            fitnessGoal = "Muscle Growth & Athleticism",
            fitnessLevel = "Intermediate",
            isLoggedIn = true,
            isBiometricEnabled = true
        )
        userProfileDao.insertOrUpdateProfile(defaultProfile)
        return Result.success(defaultProfile)
    }

    suspend fun quickDemoLogin(): UserProfileEntity {
        val demoProfile = UserProfileEntity(
            id = 1,
            name = "Alex Rivers",
            email = "alex.fit@apexfit.io",
            fitnessGoal = "Muscle Growth & Athleticism",
            fitnessLevel = "Intermediate",
            isLoggedIn = true
        )
        userProfileDao.insertOrUpdateProfile(demoProfile)
        return demoProfile
    }

    // 2. Workouts & Routines
    val allRoutines: Flow<List<WorkoutRoutineEntity>> = workoutDao.getAllRoutines()
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = workoutDao.getAllWorkoutLogs()
    val recentWorkoutLogs: Flow<List<WorkoutLogEntity>> = workoutDao.getRecentWorkoutLogs(10)

    suspend fun getRoutineById(id: Long) = workoutDao.getRoutineById(id)
    suspend fun insertRoutine(routine: WorkoutRoutineEntity) = workoutDao.insertRoutine(routine)
    suspend fun updateRoutine(routine: WorkoutRoutineEntity) = workoutDao.updateRoutine(routine)
    suspend fun deleteRoutine(routine: WorkoutRoutineEntity) = workoutDao.deleteRoutine(routine)

    suspend fun logCompletedWorkout(log: WorkoutLogEntity): Long {
        val id = workoutDao.insertWorkoutLog(log)
        // update routine completion count if linked
        log.routineId?.let { rId ->
            val routine = workoutDao.getRoutineById(rId)
            routine?.let {
                workoutDao.updateRoutine(
                    it.copy(
                        timesCompleted = it.timesCompleted + 1,
                        lastCompletedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        // Update gamification stats
        val earnedXp = (log.caloriesBurned / 4) + (log.durationSeconds / 60)
        gamificationDao.addXp(earnedXp)
        return id
    }

    // 3. Exercise Library
    val allExercises: Flow<List<ExerciseLibraryEntity>> = exerciseLibraryDao.getAllExercises()
    suspend fun insertExercise(exercise: ExerciseLibraryEntity) = exerciseLibraryDao.insertExercise(exercise)
    suspend fun deleteExercise(exercise: ExerciseLibraryEntity) = exerciseLibraryDao.deleteExercise(exercise)

    // 4. Nutrition
    fun getMealsForToday(): Flow<List<MealEntryEntity>> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return nutritionDao.getMealsForDate(todayStr)
    }
    val allMeals: Flow<List<MealEntryEntity>> = nutritionDao.getAllMeals()
    suspend fun insertMeal(meal: MealEntryEntity) = nutritionDao.insertMeal(meal)
    suspend fun deleteMeal(meal: MealEntryEntity) = nutritionDao.deleteMeal(meal)

    fun getWaterLogsForToday(): Flow<List<WaterLogEntity>> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return nutritionDao.getWaterLogsForDate(todayStr)
    }
    suspend fun logWater(amountMl: Int) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        nutritionDao.insertWaterLog(WaterLogEntity(amountMl = amountMl, dateString = todayStr))
    }
    suspend fun resetWaterToday() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        nutritionDao.clearWaterLogsForDate(todayStr)
    }

    // 5. Food Library
    val allFoods: Flow<List<FoodLibraryEntity>> = foodLibraryDao.getAllFoods()
    suspend fun insertFood(food: FoodLibraryEntity) = foodLibraryDao.insertFood(food)
    suspend fun deleteFood(food: FoodLibraryEntity) = foodLibraryDao.deleteFood(food)

    // 6. Wearables
    val allWearableDevices: Flow<List<WearableDeviceEntity>> = wearableDao.getAllDevices()
    val activeConnectedDevice: Flow<WearableDeviceEntity?> = wearableDao.getActiveConnectedDevice()

    suspend fun connectDevice(device: WearableDeviceEntity) {
        wearableDao.disconnectAllDevices()
        wearableDao.insertDevice(device.copy(isConnected = true, lastSyncTimestamp = System.currentTimeMillis()))
    }
    suspend fun disconnectDevice(deviceId: String) {
        wearableDao.disconnectAllDevices()
    }
    suspend fun saveDiscoveredDevice(device: WearableDeviceEntity) {
        wearableDao.insertDevice(device)
    }

    // 7. Biometrics Time-Series
    val allBiometrics: Flow<List<BiometricLogEntity>> = biometricDao.getAllBiometricLogs()
    val recent7DayBiometrics: Flow<List<BiometricLogEntity>> = biometricDao.getRecentBiometrics(7)
    suspend fun logBiometrics(biometric: BiometricLogEntity) = biometricDao.insertBiometricLog(biometric)

    // 8. Social & Community
    val allPosts: Flow<List<SocialPostEntity>> = socialDao.getAllPosts()
    suspend fun createSocialPost(post: SocialPostEntity) = socialDao.insertPost(post)
    suspend fun toggleKudos(postId: Long) = socialDao.toggleKudos(postId)

    // 9. Goals & PRs
    val allGoals: Flow<List<FitnessGoalEntity>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: FitnessGoalEntity) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: FitnessGoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: FitnessGoalEntity) = goalDao.deleteGoal(goal)

    val allPRs: Flow<List<PersonalRecordEntity>> = goalDao.getAllPRs()
    suspend fun insertPR(pr: PersonalRecordEntity) = goalDao.insertPR(pr)

    val allProgressRecords: Flow<List<ProgressRecordEntity>> = progressDao.getAllProgressRecords()
    suspend fun logProgressRecord(record: ProgressRecordEntity) = progressDao.insertProgressRecord(record)
    suspend fun deleteProgressRecord(record: ProgressRecordEntity) = progressDao.deleteProgressRecord(record)

    // 10. Gamification & Challenges
    val allChallenges: Flow<List<FitnessChallenge>> = gamificationDao.getAllChallenges().map { entities ->
        entities.map { entity ->
            FitnessChallenge(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                category = when (entity.category) {
                    "DAILY" -> ChallengeCategory.DAILY
                    "WEEKLY" -> ChallengeCategory.WEEKLY
                    else -> ChallengeCategory.COMMUNITY
                },
                metricType = when (entity.metricType) {
                    "CALORIES" -> ChallengeMetricType.CALORIES
                    "STEPS" -> ChallengeMetricType.STEPS
                    "WORKOUT_COUNT" -> ChallengeMetricType.WORKOUT_COUNT
                    "TOTAL_VOLUME_KG" -> ChallengeMetricType.TOTAL_VOLUME_KG
                    "DURATION_MINUTES" -> ChallengeMetricType.DURATION_MINUTES
                    else -> ChallengeMetricType.HYDRATION_ML
                },
                targetValue = entity.targetValue,
                currentValue = entity.currentValue,
                xpReward = entity.xpReward,
                deadlineText = entity.deadlineText,
                isJoined = entity.isJoined,
                isClaimed = entity.isClaimed,
                iconDescriptor = entity.iconDescriptor
            )
        }
    }

    suspend fun claimChallengeReward(id: String, xpReward: Int) {
        gamificationDao.claimChallengeReward(id)
        gamificationDao.addXp(xpReward)
    }

    suspend fun joinChallenge(id: String) = gamificationDao.updateChallengeJoinState(id, true)
    suspend fun leaveChallenge(id: String) = gamificationDao.updateChallengeJoinState(id, false)

    val allBadges: Flow<List<MilestoneBadge>> = gamificationDao.getAllBadges().map { entities ->
        entities.map { entity ->
            MilestoneBadge(
                id = entity.id,
                title = entity.title,
                subtitle = entity.subtitle,
                description = entity.description,
                rarity = when (entity.rarity) {
                    "COMMON" -> BadgeRarity.COMMON
                    "RARE" -> BadgeRarity.RARE
                    "EPIC" -> BadgeRarity.EPIC
                    else -> BadgeRarity.LEGENDARY
                },
                category = when (entity.category) {
                    "STREAK" -> BadgeCategory.STREAK
                    "STRENGTH" -> BadgeCategory.STRENGTH
                    "CARDIO" -> BadgeCategory.CARDIO
                    "CONSISTENCY" -> BadgeCategory.CONSISTENCY
                    "HYDRATION" -> BadgeCategory.HYDRATION
                    "RECOVERY" -> BadgeCategory.RECOVERY
                    else -> BadgeCategory.COMMUNITY
                },
                currentProgress = entity.currentProgress,
                maxProgress = entity.maxProgress,
                isUnlocked = entity.isUnlocked,
                unlockedDate = entity.unlockedDate,
                xpReward = entity.xpReward,
                iconEmoji = entity.iconEmoji
            )
        }
    }

    val gamificationStats: Flow<UserGamificationStats> = gamificationDao.getGamificationStats().map { entity ->
        if (entity != null) {
            UserGamificationStats(
                totalXp = entity.totalXp,
                currentLevel = entity.currentLevel,
                xpToNextLevel = entity.xpToNextLevel,
                currentLevelXp = entity.currentLevelXp,
                totalBadgesUnlocked = entity.totalBadgesUnlocked,
                activeChallengesCount = entity.activeChallengesCount,
                completedChallengesCount = entity.completedChallengesCount,
                rankTitle = entity.rankTitle
            )
        } else {
            UserGamificationStats()
        }
    }

    fun getLeaderboard(category: String): Flow<List<LeaderboardEntry>> =
        gamificationDao.getLeaderboardForCategory(category).map { entities ->
            entities.map { entity ->
                LeaderboardEntry(
                    rank = entity.rank,
                    name = entity.name,
                    avatarBadge = entity.avatarBadge,
                    tier = entity.tier,
                    scoreValue = entity.scoreValue,
                    scoreUnit = entity.scoreUnit,
                    isCurrentUser = entity.isCurrentUser,
                    level = entity.level,
                    trend = entity.trend
                )
            }
        }

    // 11. AI Adaptation & User Feedback
    val latestFeedback: Flow<UserWorkoutFeedback?> = aiAdaptationDao.getLatestFeedback().map { entity ->
        entity?.let {
            UserWorkoutFeedback(
                rpeRating = it.rpeRating,
                soreMuscles = it.soreMusclesCsv.split(",").filter { s -> s.isNotBlank() }.toSet(),
                energyLevel = it.energyLevel,
                sleepQualityRating = it.sleepQualityRating,
                userNotes = it.userNotes
            )
        }
    }

    suspend fun saveUserFeedback(feedback: UserWorkoutFeedback) {
        aiAdaptationDao.insertFeedback(
            UserWorkoutFeedbackEntity(
                rpeRating = feedback.rpeRating,
                soreMusclesCsv = feedback.soreMuscles.joinToString(","),
                energyLevel = feedback.energyLevel,
                sleepQualityRating = feedback.sleepQualityRating,
                userNotes = feedback.userNotes
            )
        )
    }

    suspend fun saveAiAdaptation(analysis: AiAdaptationAnalysis) {
        val recoveryJson = JSONObject(analysis.muscleRecoveryMap as Map<*, *>).toString()
        val modsArray = JSONArray()
        analysis.modifications.forEach { mod ->
            val mObj = JSONObject().apply {
                put("routineId", mod.routineId)
                put("routineTitle", mod.routineTitle)
                put("actionType", mod.actionType.name)
                put("intensityPercentChange", mod.intensityPercentChange)
                put("setAdjustment", mod.setAdjustment)
                put("repAdjustment", mod.repAdjustment)
                put("weightAdjustmentKg", mod.weightAdjustmentKg.toDouble())
                put("restSecondsAdjustment", mod.restSecondsAdjustment)
                put("isRestDayRecommended", mod.isRestDayRecommended)
                put("scientificRationale", mod.scientificRationale)
            }
            modsArray.put(mObj)
        }

        aiAdaptationDao.insertAdaptation(
            AiAdaptationEntity(
                readinessScore = analysis.readinessScore,
                fatigueScore = analysis.fatigueScore,
                overtrainingRisk = analysis.overtrainingRisk.name,
                muscleRecoveryJson = recoveryJson,
                overallAdvice = analysis.overallAdvice,
                recommendedActionType = analysis.recommendedActionType.name,
                modificationsJson = modsArray.toString(),
                recoveryTimeHours = analysis.recoveryTimeHours,
                isRestDayRecommended = analysis.isRestDayRecommended,
                generatedTimestamp = analysis.generatedTimestamp
            )
        )
    }
}
