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

    @Query("UPDATE user_profile SET subscriptionTier = :tier, subscriptionExpiry = :expiry WHERE id = 1")
    suspend fun updateSubscription(tier: String, expiry: Long)

    @Query("UPDATE user_profile SET isDarkMode = :isDark WHERE id = 1")
    suspend fun updateThemeMode(isDark: Boolean?)

    @Query("UPDATE user_profile SET streakDays = streakDays + 1 WHERE id = 1")
    suspend fun incrementStreak()
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<WorkoutRoutineEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long
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
interface SocialDao {
    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<SocialPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPostEntity>)

    @Query("UPDATE social_posts SET kudosCount = kudosCount + (CASE WHEN isKudosGiven = 1 THEN -1 ELSE 1 END), isKudosGiven = (CASE WHEN isKudosGiven = 1 THEN 0 ELSE 1 END) WHERE id = :postId")
    suspend fun toggleKudos(postId: Long)
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
}
