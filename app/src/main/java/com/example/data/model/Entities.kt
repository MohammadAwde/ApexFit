package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Rivers",
    val email: String = "alex.rivers@example.com",
    val avatarUrl: String = "",
    val heightCm: Float = 180f,
    val weightKg: Float = 76.5f,
    val targetWeightKg: Float = 73.0f,
    val fitnessLevel: String = "Intermediate", // Beginner, Intermediate, Advanced, Elite
    val fitnessGoal: String = "Muscle Growth & Fat Loss", // Muscle Gain, Weight Loss, Endurance, Longevity
    val dailyCalorieGoal: Int = 2400,
    val proteinGoalG: Int = 160,
    val carbsGoalG: Int = 220,
    val fatGoalG: Int = 65,
    val dailyStepGoal: Int = 10000,
    val dailyWaterGoalMl: Int = 3000,
    val streakDays: Int = 14,
    val subscriptionTier: String = "PRO", // FREE, PRO, ELITE
    val subscriptionExpiry: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val isDarkMode: Boolean? = null, // null for system default, true/false for forced
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

data class Exercise(
    val name: String,
    val targetMuscle: String, // Chest, Back, Legs, Shoulders, Arms, Core, Full Body
    val sets: Int,
    val reps: Int,
    val weightKg: Float = 0f,
    val durationSeconds: Int = 0,
    val restSeconds: Int = 60,
    val tips: String = "",
    val animationCue: String = ""
)

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // Strength, HIIT, Cardio, Yoga, Calisthenics, Mobility
    val difficulty: String, // Beginner, Intermediate, Advanced
    val durationMinutes: Int,
    val estimatedCalories: Int,
    val exercisesJson: String, // JSON of List<Exercise>
    val isPremium: Boolean = false,
    val isCustom: Boolean = false,
    val timesCompleted: Int = 0,
    val lastCompletedTimestamp: Long? = null,
    val targetMuscleGroups: String = "Full Body"
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long?,
    val routineTitle: String,
    val category: String,
    val durationSeconds: Int,
    val caloriesBurned: Int,
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val totalVolumeKg: Float,
    val exercisesCompletedJson: String,
    val notes: String = "",
    val feelingRating: Int = 5, // 1 to 5
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val servingSize: String = "1 serving",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // YYYY-MM-DD for grouping
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // YYYY-MM-DD
)

@Entity(tableName = "wearable_devices")
data class WearableDeviceEntity(
    @PrimaryKey val id: String, // MAC or Unique ID
    val name: String,
    val type: String, // SMARTWATCH, CHEST_STRAP, FITNESS_RING, FITNESS_BAND
    val brand: String, // Garmin, Apple, WearOS, Polar, Whoop, Oura
    val isConnected: Boolean = false,
    val batteryPercent: Int = 88,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val currentHeartRateBpm: Int = 72,
    val restingHeartRateBpm: Int = 58,
    val stepsToday: Int = 6420,
    val sleepHours: Float = 7.5f,
    val sleepScore: Int = 84,
    val recoveryScore: Int = 92, // 0-100%
    val caloriesBurnedSensor: Int = 430
)

@Entity(tableName = "social_posts")
data class SocialPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAvatarBadge: String, // Initials or icon descriptor
    val authorTier: String = "PRO",
    val title: String,
    val content: String,
    val workoutSummary: String? = null,
    val achievementBadge: String? = null, // "10K Steps Master", "500kg Club", "Streak Warrior"
    val kudosCount: Int = 12,
    val isKudosGiven: Boolean = false,
    val commentsCount: Int = 3,
    val timestamp: Long = System.currentTimeMillis(),
    val isUserPost: Boolean = false
)

@Entity(tableName = "fitness_goals")
data class FitnessGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // WORKOUT, NUTRITION, STEPS, WEIGHT, HABIT
    val currentVal: Float,
    val targetVal: Float,
    val unit: String,
    val deadlineString: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val recordValue: String, // "120 kg (1RM)", "45 mins", "15 reps"
    val previousRecord: String? = null,
    val achievedDate: String,
    val category: String = "Strength"
)
