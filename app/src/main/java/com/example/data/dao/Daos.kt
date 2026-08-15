package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET isLoggedIn = :loggedIn WHERE id = 1")
    suspend fun updateLoginStatus(loggedIn: Boolean)

    @Query("UPDATE user_profile SET subscriptionTier = :tier, subscriptionExpiry = :expiry WHERE id = 1")
    suspend fun updateSubscription(tier: String, expiry: Long)

    @Query("UPDATE user_profile SET isDarkMode = :isDark WHERE id = 1")
    suspend fun updateThemeMode(isDark: Boolean?)

    @Query("UPDATE user_profile SET streakDays = streakDays + 1 WHERE id = 1")
    suspend fun incrementStreak()

    @Query("UPDATE user_profile SET dailyCalorieGoal = :cals, proteinGoalG = :p, carbsGoalG = :c, fatGoalG = :f WHERE id = 1")
    suspend fun updateMacroGoals(cals: Int, p: Int, c: Int, f: Int)

    @Query("UPDATE user_profile SET dailyWaterGoalMl = :waterMl, dailyStepGoal = :steps WHERE id = 1")
    suspend fun updateDailyTargets(waterMl: Int, steps: Int)

    @Query("UPDATE user_profile SET isBiometricEnabled = :enabled WHERE id = 1")
    suspend fun updateBiometricSetting(enabled: Boolean)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun findAccountByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<UserAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerAccount(account: UserAccountEntity)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routines WHERE category = :category ORDER BY id ASC")
    fun getRoutinesByCategory(category: String): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): WorkoutRoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<WorkoutRoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: WorkoutRoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutineEntity)

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWorkoutLogs(limit: Int): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs WHERE routineId = :routineId ORDER BY timestamp DESC")
    fun getLogsForRoutine(routineId: Long): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long

    @Query("SELECT SUM(totalVolumeKg) FROM workout_logs")
    fun getTotalVolumeLifted(): Flow<Float?>

    @Query("SELECT SUM(caloriesBurned) FROM workout_logs")
    fun getTotalCaloriesBurned(): Flow<Int?>
}

@Dao
interface ExerciseLibraryDao {
    @Query("SELECT * FROM exercise_library ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseLibraryEntity>>

    @Query("SELECT * FROM exercise_library WHERE targetMuscle = :muscle ORDER BY name ASC")
    fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseLibraryEntity>>

    @Query("SELECT * FROM exercise_library WHERE equipment = :equipment ORDER BY name ASC")
    fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseLibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseLibraryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseLibraryEntity>)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseLibraryEntity)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM meal_entries WHERE dateString = :date ORDER BY timestamp DESC")
    fun getMealsForDate(date: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntryEntity): Long

    @Delete
    suspend fun deleteMeal(meal: MealEntryEntity)

    @Query("SELECT * FROM water_logs WHERE dateString = :date ORDER BY timestamp ASC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity): Long

    @Query("DELETE FROM water_logs WHERE dateString = :date")
    suspend fun clearWaterLogsForDate(date: String)
}

@Dao
interface FoodLibraryDao {
    @Query("SELECT * FROM food_library ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodLibraryEntity>>

    @Query("SELECT * FROM food_library WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: String): Flow<List<FoodLibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodLibraryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodLibraryEntity>)

    @Delete
    suspend fun deleteFood(food: FoodLibraryEntity)
}

@Dao
interface WearableDao {
    @Query("SELECT * FROM wearable_devices ORDER BY isConnected DESC, name ASC")
    fun getAllDevices(): Flow<List<WearableDeviceEntity>>

    @Query("SELECT * FROM wearable_devices WHERE isConnected = 1 LIMIT 1")
    fun getActiveConnectedDevice(): Flow<WearableDeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: WearableDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<WearableDeviceEntity>)

    @Update
    suspend fun updateDevice(device: WearableDeviceEntity)

    @Query("UPDATE wearable_devices SET isConnected = 0")
    suspend fun disconnectAllDevices()

    @Query("UPDATE wearable_devices SET isConnected = 1, lastSyncTimestamp = :syncTime WHERE id = :id")
    suspend fun connectDevice(id: String, syncTime: Long)

    @Query("UPDATE wearable_devices SET currentHeartRateBpm = :hr, stepsToday = :steps, caloriesBurnedSensor = :cals WHERE isConnected = 1")
    suspend fun updateLiveTelemetry(hr: Int, steps: Int, cals: Int)
}

@Dao
interface BiometricDao {
    @Query("SELECT * FROM biometric_logs ORDER BY timestamp DESC")
    fun getAllBiometricLogs(): Flow<List<BiometricLogEntity>>

    @Query("SELECT * FROM biometric_logs WHERE dateString = :dateString LIMIT 1")
    fun getBiometricForDate(dateString: String): Flow<BiometricLogEntity?>

    @Query("SELECT * FROM biometric_logs ORDER BY timestamp DESC LIMIT :days")
    fun getRecentBiometrics(days: Int): Flow<List<BiometricLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometricLog(log: BiometricLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometricLogs(logs: List<BiometricLogEntity>)
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<SocialPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPostEntity>)

    @Query("UPDATE social_posts SET kudosCount = kudosCount + (CASE WHEN isKudosGiven = 1 THEN -1 ELSE 1 END), isKudosGiven = (CASE WHEN isKudosGiven = 1 THEN 0 ELSE 1 END) WHERE id = :postId")
    suspend fun toggleKudos(postId: Long)

    @Delete
    suspend fun deletePost(post: SocialPostEntity)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM fitness_goals ORDER BY isCompleted ASC, id DESC")
    fun getAllGoals(): Flow<List<FitnessGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FitnessGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<FitnessGoalEntity>)

    @Update
    suspend fun updateGoal(goal: FitnessGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: FitnessGoalEntity)

    @Query("SELECT * FROM personal_records ORDER BY id DESC")
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPR(pr: PersonalRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPRs(prs: List<PersonalRecordEntity>)

    @Delete
    suspend fun deletePR(pr: PersonalRecordEntity)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_records ORDER BY timestamp DESC")
    fun getAllProgressRecords(): Flow<List<ProgressRecordEntity>>

    @Query("SELECT * FROM progress_records WHERE dateString = :date LIMIT 1")
    fun getProgressRecordForDate(date: String): Flow<ProgressRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressRecord(record: ProgressRecordEntity): Long

    @Delete
    suspend fun deleteProgressRecord(record: ProgressRecordEntity)
}

@Dao
interface GamificationDao {
    // Challenges
    @Query("SELECT * FROM fitness_challenges ORDER BY isClaimed ASC, id ASC")
    fun getAllChallenges(): Flow<List<FitnessChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: FitnessChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<FitnessChallengeEntity>)

    @Query("UPDATE fitness_challenges SET isClaimed = 1 WHERE id = :id")
    suspend fun claimChallengeReward(id: String)

    @Query("UPDATE fitness_challenges SET isJoined = :joined WHERE id = :id")
    suspend fun updateChallengeJoinState(id: String, joined: Boolean)

    @Query("UPDATE fitness_challenges SET currentValue = :value WHERE id = :id")
    suspend fun updateChallengeProgress(id: String, value: Float)

    // Badges
    @Query("SELECT * FROM milestone_badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadges(): Flow<List<MilestoneBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: MilestoneBadgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<MilestoneBadgeEntity>)

    @Query("UPDATE milestone_badges SET isUnlocked = 1, unlockedDate = :date, currentProgress = maxProgress WHERE id = :id")
    suspend fun unlockBadge(id: String, date: String)

    // User Gamification Stats
    @Query("SELECT * FROM user_gamification_stats WHERE id = 1 LIMIT 1")
    fun getGamificationStats(): Flow<UserGamificationStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserGamificationStatsEntity)

    @Query("UPDATE user_gamification_stats SET totalXp = totalXp + :xpToAdd, currentLevelXp = currentLevelXp + :xpToAdd WHERE id = 1")
    suspend fun addXp(xpToAdd: Int)

    // Leaderboards
    @Query("SELECT * FROM leaderboard_entries WHERE category = :category ORDER BY rank ASC")
    fun getLeaderboardForCategory(category: String): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntryEntity>)
}

@Dao
interface AiAdaptationDao {
    @Query("SELECT * FROM ai_adaptations ORDER BY generatedTimestamp DESC LIMIT 1")
    fun getLatestAdaptation(): Flow<AiAdaptationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdaptation(adaptation: AiAdaptationEntity): Long

    @Query("SELECT * FROM user_workout_feedback ORDER BY timestamp DESC LIMIT 1")
    fun getLatestFeedback(): Flow<UserWorkoutFeedbackEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: UserWorkoutFeedbackEntity): Long

    @Query("SELECT * FROM user_workout_feedback ORDER BY timestamp DESC LIMIT 10")
    fun getRecentFeedbackHistory(): Flow<List<UserWorkoutFeedbackEntity>>
}
