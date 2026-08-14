package com.example.data

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class FitnessRepository(
    private val userProfileDao: UserProfileDao,
    private val workoutDao: WorkoutDao,
    private val nutritionDao: NutritionDao,
    private val wearableDao: WearableDao,
    private val socialDao: SocialDao,
    private val goalDao: GoalDao
) {
    // User Profile
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    suspend fun updateProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdateProfile(profile)
    suspend fun updateSubscriptionTier(tier: String, durationDays: Int = 30) {
        val expiry = System.currentTimeMillis() + durationDays.toLong() * 24 * 60 * 60 * 1000
        userProfileDao.updateSubscription(tier, expiry)
    }
    suspend fun updateThemeMode(isDark: Boolean?) = userProfileDao.updateThemeMode(isDark)
    suspend fun incrementStreak() = userProfileDao.incrementStreak()

    // Workouts
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
        return id
    }

    // Nutrition
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

    // Wearables
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

    // Social & Community
    val allPosts: Flow<List<SocialPostEntity>> = socialDao.getAllPosts()
    suspend fun createSocialPost(post: SocialPostEntity) = socialDao.insertPost(post)
    suspend fun toggleKudos(postId: Long) = socialDao.toggleKudos(postId)

    // Goals & PRs
    val allGoals: Flow<List<FitnessGoalEntity>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: FitnessGoalEntity) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: FitnessGoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: FitnessGoalEntity) = goalDao.deleteGoal(goal)

    val allPRs: Flow<List<PersonalRecordEntity>> = goalDao.getAllPRs()
    suspend fun insertPR(pr: PersonalRecordEntity) = goalDao.insertPR(pr)
}
