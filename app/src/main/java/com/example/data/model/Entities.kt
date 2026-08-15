package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Rivers",
    val email: String = "alex.fit@apexfit.io",
    val avatarUrl: String = "",
    val heightCm: Float = 180f,
    val weightKg: Float = 76.5f,
    val targetWeightKg: Float = 73.0f,
    val fitnessLevel: String = "Intermediate", // Beginner, Intermediate, Advanced, Elite
    val fitnessGoal: String = "Muscle Growth & Athleticism", // Muscle Gain, Weight Loss, Endurance, Longevity
    val dailyCalorieGoal: Int = 2450,
    val proteinGoalG: Int = 165,
    val carbsGoalG: Int = 230,
    val fatGoalG: Int = 65,
    val dailyStepGoal: Int = 10000,
    val dailyWaterGoalMl: Int = 3200,
    val streakDays: Int = 16,
    val subscriptionTier: String = "PRO", // FREE, PRO, ELITE
    val subscriptionExpiry: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val isDarkMode: Boolean? = null, // null for system default, true/false for forced
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isBiometricEnabled: Boolean = true,
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val name: String,
    val fitnessGoal: String = "Muscle Growth & Athleticism",
    val fitnessLevel: String = "Intermediate",
    val createdAt: Long = System.currentTimeMillis()
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

@Entity(tableName = "exercise_library")
data class ExerciseLibraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetMuscle: String,
    val secondaryMuscles: String = "",
    val equipment: String = "Bodyweight", // Barbell, Dumbbell, Bodyweight, Cable, Machine, Kettlebell
    val difficulty: String = "Intermediate",
    val instructions: String = "",
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultRestSeconds: Int = 60,
    val isCompound: Boolean = true
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

@Entity(tableName = "food_library")
data class FoodLibraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Protein, Carb, Healthy Fat, Snack, Fruit, Beverage
    val caloriesPerServing: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val servingUnit: String = "100g"
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

@Entity(tableName = "biometric_logs")
data class BiometricLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val avgHeartRate: Int,
    val restingHeartRate: Int,
    val hrvMs: Int,
    val recoveryScore: Int,
    val strainScore: Float,
    val sleepHours: Float,
    val sleepScore: Int,
    val steps: Int,
    val activeCalories: Int,
    val vo2Max: Float = 52.4f,
    val timestamp: Long = System.currentTimeMillis()
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

@Entity(tableName = "progress_records")
data class ProgressRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val weightKg: Float,
    val bodyFatPercent: Float? = null,
    val muscleMassKg: Float? = null,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val armsCm: Float? = null,
    val thighsCm: Float? = null,
    val notes: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fitness_challenges")
data class FitnessChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // DAILY, WEEKLY, COMMUNITY
    val metricType: String, // CALORIES, STEPS, WORKOUT_COUNT, TOTAL_VOLUME_KG, DURATION_MINUTES, HYDRATION_ML
    val targetValue: Float,
    val currentValue: Float,
    val xpReward: Int,
    val deadlineText: String,
    val isJoined: Boolean = true,
    val isClaimed: Boolean = false,
    val iconDescriptor: String = "FIRE"
)

@Entity(tableName = "milestone_badges")
data class MilestoneBadgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val rarity: String, // COMMON, RARE, EPIC, LEGENDARY
    val category: String, // STREAK, STRENGTH, CARDIO, CONSISTENCY, HYDRATION, RECOVERY, COMMUNITY
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val xpReward: Int = 100,
    val iconEmoji: String = "🏆"
)

@Entity(tableName = "user_gamification_stats")
data class UserGamificationStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 2450,
    val currentLevel: Int = 8,
    val xpToNextLevel: Int = 3000,
    val currentLevelXp: Int = 450,
    val totalBadgesUnlocked: Int = 7,
    val activeChallengesCount: Int = 4,
    val completedChallengesCount: Int = 12,
    val rankTitle: String = "Apex Master"
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // XP, CALORIES, STEPS, STREAK
    val rank: Int,
    val name: String,
    val avatarBadge: String,
    val tier: String,
    val scoreValue: Int,
    val scoreUnit: String,
    val isCurrentUser: Boolean = false,
    val level: Int = 12,
    val trend: String = "UP"
)

@Entity(tableName = "ai_adaptations")
data class AiAdaptationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val readinessScore: Int,
    val fatigueScore: Int,
    val overtrainingRisk: String, // MINIMAL, LOW, ELEVATED, HIGH_RISK
    val muscleRecoveryJson: String,
    val overallAdvice: String,
    val recommendedActionType: String, // PROGRESSIVE_OVERLOAD, DELOAD_VOLUME_REDUCTION, etc.
    val modificationsJson: String,
    val recoveryTimeHours: Int,
    val isRestDayRecommended: Boolean,
    val generatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_workout_feedback")
data class UserWorkoutFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rpeRating: Int,
    val soreMusclesCsv: String,
    val energyLevel: Int,
    val sleepQualityRating: Int,
    val userNotes: String,
    val timestamp: Long = System.currentTimeMillis()
)
