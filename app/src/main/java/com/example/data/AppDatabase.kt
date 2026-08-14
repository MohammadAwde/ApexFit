package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutRoutineEntity::class,
        WorkoutLogEntity::class,
        MealEntryEntity::class,
        WaterLogEntity::class,
        WearableDeviceEntity::class,
        SocialPostEntity::class,
        FitnessGoalEntity::class,
        PersonalRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun wearableDao(): WearableDao
    abstract fun socialDao(): SocialDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apexfit_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userProfileDao = database.userProfileDao()
            val workoutDao = database.workoutDao()
            val nutritionDao = database.nutritionDao()
            val wearableDao = database.wearableDao()
            val socialDao = database.socialDao()
            val goalDao = database.goalDao()

            // 1. Initial User Profile
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Alex Rivers",
                    email = "alex.fit@apexfit.io",
                    heightCm = 180f,
                    weightKg = 76.5f,
                    targetWeightKg = 73.0f,
                    fitnessLevel = "Intermediate",
                    fitnessGoal = "Muscle Growth & Athleticism",
                    dailyCalorieGoal = 2450,
                    proteinGoalG = 165,
                    carbsGoalG = 230,
                    fatGoalG = 65,
                    dailyStepGoal = 10000,
                    dailyWaterGoalMl = 3200,
                    streakDays = 16,
                    subscriptionTier = "PRO"
                )
            )

            // 2. Initial Workout Routines
            val fullBodyStrengthExercises = """[
                {"name":"Barbell Back Squats","targetMuscle":"Legs","sets":4,"reps":8,"weightKg":85.0,"restSeconds":90,"tips":"Keep chest elevated and drive through mid-foot.","animationCue":"Squat deep to parallel"},
                {"name":"Incline Dumbbell Press","targetMuscle":"Chest","sets":4,"reps":10,"weightKg":28.0,"restSeconds":75,"tips":"Retract scapulae and press up in an arc.","animationCue":"Controlled eccentric tempo"},
                {"name":"Overhand Barbell Rows","targetMuscle":"Back","sets":4,"reps":10,"weightKg":65.0,"restSeconds":75,"tips":"Hinge hips at 45 degrees and pull to lower sternum.","animationCue":"Squeeze shoulder blades together"},
                {"name":"Dumbbell Overhead Press","targetMuscle":"Shoulders","sets":3,"reps":12,"weightKg":20.0,"restSeconds":60,"tips":"Keep core tight and elbows angled slightly forward.","animationCue":"Lockout overhead smoothly"},
                {"name":"Hanging Knee Raises","targetMuscle":"Core","sets":3,"reps":15,"weightKg":0.0,"restSeconds":45,"tips":"Curl pelvis upwards without swinging.","animationCue":"Engage lower abdominals"}
            ]"""

            val hiitBurnerExercises = """[
                {"name":"High Knees Sprint","targetMuscle":"Full Body","sets":4,"reps":45,"durationSeconds":45,"restSeconds":20,"tips":"Explosive hip drive with synchronized arm pumps.","animationCue":"Fast sprint cadence"},
                {"name":"Burpees with Jump","targetMuscle":"Full Body","sets":4,"reps":15,"durationSeconds":45,"restSeconds":30,"tips":"Chest to deck, pop feet wide and jump high.","animationCue":"Maximum intensity vertical jump"},
                {"name":"Kettlebell Swings","targetMuscle":"Legs","sets":4,"reps":20,"weightKg":20.0,"durationSeconds":45,"restSeconds":25,"tips":"Hip hinge power snap, glute contraction.","animationCue":"Float weight to eye level"},
                {"name":"Mountain Climbers","targetMuscle":"Core","sets":4,"reps":40,"durationSeconds":45,"restSeconds":20,"tips":"Maintain steady plank and sprint knees to chest.","animationCue":"Rapid alternating piston drive"},
                {"name":"Box / Tuck Jumps","targetMuscle":"Legs","sets":3,"reps":12,"durationSeconds":40,"restSeconds":30,"tips":"Soft landing with absorbed knee bend.","animationCue":"Explosive triple extension"}
            ]"""

            val upperBodyHypertrophyExercises = """[
                {"name":"Bench Press (Flat Barbell)","targetMuscle":"Chest","sets":4,"reps":8,"weightKg":80.0,"restSeconds":90,"tips":"Plant feet firmly, control the bar to mid-chest.","animationCue":"Drive up explosively"},
                {"name":"Weighted Pull-Ups","targetMuscle":"Back","sets":4,"reps":8,"weightKg":10.0,"restSeconds":90,"tips":"Full dead hang to chin clearly over bar.","animationCue":"Lats engaging at bottom"},
                {"name":"Lateral Dumbbell Raises","targetMuscle":"Shoulders","sets":4,"reps":15,"weightKg":12.0,"restSeconds":45,"tips":"Lead with elbows with slight forward lean.","animationCue":"Constant tension on side delts"},
                {"name":"Incline Dumbbell Bicep Curls","targetMuscle":"Arms","sets":3,"reps":12,"weightKg":14.0,"restSeconds":60,"tips":"Full supination at peak contraction.","animationCue":"Deep stretch at bottom"},
                {"name":"Tricep Rope Pushdowns","targetMuscle":"Arms","sets":3,"reps":15,"weightKg":25.0,"restSeconds":45,"tips":"Spread ropes at bottom and lock out triceps.","animationCue":"Keep elbows pinned to sides"}
            ]"""

            val calisthenicsMasteryExercises = """[
                {"name":"Strict Ring / Bar Dips","targetMuscle":"Chest","sets":4,"reps":12,"weightKg":0.0,"restSeconds":60,"tips":"Lean forward for chest bias, full lockout.","animationCue":"Smooth rhythmic descent"},
                {"name":"Archer Push-Ups","targetMuscle":"Chest","sets":3,"reps":10,"weightKg":0.0,"restSeconds":45,"tips":"Slide horizontally across wide pushup base.","animationCue":"Single arm power focus"},
                {"name":"Pistol Squats (Single Leg)","targetMuscle":"Legs","sets":3,"reps":8,"weightKg":0.0,"restSeconds":60,"tips":"Extend counter-leg forward, maintain balance.","animationCue":"Deep single-leg stability"},
                {"name":"L-Sit Hold on Parallettes","targetMuscle":"Core","sets":3,"reps":30,"durationSeconds":30,"restSeconds":45,"tips":"Depress shoulders and point toes forward.","animationCue":"Lock legs 90 degree angle"}
            ]"""

            val yogaRecoveryMobility = """[
                {"name":"World's Greatest Stretch","targetMuscle":"Full Body","sets":3,"reps":8,"durationSeconds":60,"restSeconds":15,"tips":"Deep thoracic spine rotation with hip opener.","animationCue":"Fluid sweeping arm rotation"},
                {"name":"Pigeon Pose Hip Opener","targetMuscle":"Legs","sets":2,"reps":60,"durationSeconds":60,"restSeconds":15,"tips":"Square hips to floor, breathe into tight glutes.","animationCue":"Gentle deep hip release"},
                {"name":"Cat-Cow Spinal Waves","targetMuscle":"Back","sets":3,"reps":12,"durationSeconds":45,"restSeconds":10,"tips":"Synchronize breath with spinal flexion and extension.","animationCue":"Slow rolling vertebra wave"},
                {"name":"Deep Malasana Squat Hold","targetMuscle":"Legs","sets":2,"reps":60,"durationSeconds":60,"restSeconds":15,"tips":"Elbows inside knees, spine tall and lengthened.","animationCue":"Pelvic floor & ankle decompression"}
            ]"""

            val routines = listOf(
                WorkoutRoutineEntity(
                    title = "Apex Full-Body Power",
                    description = "High-efficiency compound strength session targeting power, core stability, and lean muscle mass.",
                    category = "Strength",
                    difficulty = "Intermediate",
                    durationMinutes = 48,
                    estimatedCalories = 420,
                    exercisesJson = fullBodyStrengthExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 8,
                    targetMuscleGroups = "Full Body (Legs, Chest, Back, Shoulders)"
                ),
                WorkoutRoutineEntity(
                    title = "Inferno HIIT Cardio Blast",
                    description = "High-octane interval circuits designed for maximum VO2 max elevation and rapid metabolic conditioning.",
                    category = "HIIT",
                    difficulty = "Advanced",
                    durationMinutes = 32,
                    estimatedCalories = 390,
                    exercisesJson = hiitBurnerExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 5,
                    targetMuscleGroups = "Cardiovascular & Full Body"
                ),
                WorkoutRoutineEntity(
                    title = "Upper Body Hypertrophy Sculpt",
                    description = "Precision hypertrophy targeting chest, upper back, 3D deltoids, and arms with strict tempo.",
                    category = "Strength",
                    difficulty = "Intermediate",
                    durationMinutes = 45,
                    estimatedCalories = 380,
                    exercisesJson = upperBodyHypertrophyExercises,
                    isPremium = true,
                    isCustom = false,
                    timesCompleted = 12,
                    targetMuscleGroups = "Chest, Back, Delts, Arms"
                ),
                WorkoutRoutineEntity(
                    title = "Elite Calisthenics & Core",
                    description = "Gymnastic bodyweight mastery for explosive relative strength, shoulder stability, and steel core.",
                    category = "Calisthenics",
                    difficulty = "Advanced",
                    durationMinutes = 40,
                    estimatedCalories = 340,
                    exercisesJson = calisthenicsMasteryExercises,
                    isPremium = true,
                    isCustom = false,
                    timesCompleted = 3,
                    targetMuscleGroups = "Relative Strength, Core, Chest"
                ),
                WorkoutRoutineEntity(
                    title = "Dynamic Flow & Joint Recovery",
                    description = "Athletic mobility sequence to restore connective tissue flexibility, open tight hips, and accelerate recovery.",
                    category = "Yoga",
                    difficulty = "Beginner",
                    durationMinutes = 25,
                    estimatedCalories = 160,
                    exercisesJson = yogaRecoveryMobility,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 9,
                    targetMuscleGroups = "Hips, Thoracic Spine, Ankles"
                )
            )
            workoutDao.insertRoutines(routines)

            // 3. Initial Workout Logs (History)
            val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = todayFormat.format(Date())
            val cal = Calendar.getInstance()

            val initialLogs = mutableListOf<WorkoutLogEntity>()
            for (i in 1..4) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -i * 2)
                initialLogs.add(
                    WorkoutLogEntity(
                        routineId = 1L,
                        routineTitle = if (i % 2 == 0) "Apex Full-Body Power" else "Inferno HIIT Cardio Blast",
                        category = if (i % 2 == 0) "Strength" else "HIIT",
                        durationSeconds = 2700 - i * 120,
                        caloriesBurned = 430 - i * 25,
                        avgHeartRate = 142 + (i % 3) * 6,
                        maxHeartRate = 176 + (i % 2) * 5,
                        totalVolumeKg = 3450f - i * 180f,
                        exercisesCompletedJson = fullBodyStrengthExercises,
                        notes = "Felt great energy, pushed through last superset!",
                        feelingRating = 5,
                        timestamp = cal.timeInMillis
                    )
                )
            }
            initialLogs.forEach { workoutDao.insertWorkoutLog(it) }

            // 4. Initial Nutrition (Meals & Water) for today
            nutritionDao.insertMeal(
                MealEntryEntity(
                    name = "Greek Yogurt Bowl & Organic Berries",
                    mealType = "Breakfast",
                    calories = 380,
                    proteinG = 34,
                    carbsG = 42,
                    fatG = 8,
                    servingSize = "1 large bowl (300g)",
                    dateString = todayStr
                )
            )
            nutritionDao.insertMeal(
                MealEntryEntity(
                    name = "Grilled Chicken Breast with Quinoa & Avocado",
                    mealType = "Lunch",
                    calories = 620,
                    proteinG = 52,
                    carbsG = 58,
                    fatG = 18,
                    servingSize = "1 plate (400g)",
                    dateString = todayStr
                )
            )
            nutritionDao.insertMeal(
                MealEntryEntity(
                    name = "Whey Isolate Protein Shake & Banana",
                    mealType = "Snack",
                    calories = 240,
                    proteinG = 28,
                    carbsG = 30,
                    fatG = 2,
                    servingSize = "1 shaker (400ml)",
                    dateString = todayStr
                )
            )

            // Initial Water Logs
            nutritionDao.insertWaterLog(WaterLogEntity(amountMl = 500, dateString = todayStr))
            nutritionDao.insertWaterLog(WaterLogEntity(amountMl = 500, dateString = todayStr))
            nutritionDao.insertWaterLog(WaterLogEntity(amountMl = 750, dateString = todayStr))

            // 5. Initial Wearable Devices
            val wearables = listOf(
                WearableDeviceEntity(
                    id = "GARMIN_965_448",
                    name = "Garmin Forerunner 965",
                    type = "SMARTWATCH",
                    brand = "Garmin",
                    isConnected = true,
                    batteryPercent = 84,
                    lastSyncTimestamp = System.currentTimeMillis() - 4 * 60 * 1000,
                    currentHeartRateBpm = 74,
                    restingHeartRateBpm = 54,
                    stepsToday = 7840,
                    sleepHours = 7.8f,
                    sleepScore = 88,
                    recoveryScore = 94,
                    caloriesBurnedSensor = 520
                ),
                WearableDeviceEntity(
                    id = "POLAR_H10_991",
                    name = "Polar H10 Heart Rate Strap",
                    type = "CHEST_STRAP",
                    brand = "Polar",
                    isConnected = false,
                    batteryPercent = 95,
                    lastSyncTimestamp = System.currentTimeMillis() - 24 * 3600 * 1000,
                    currentHeartRateBpm = 0,
                    restingHeartRateBpm = 52,
                    stepsToday = 0,
                    sleepHours = 0f,
                    sleepScore = 0,
                    recoveryScore = 90,
                    caloriesBurnedSensor = 0
                ),
                WearableDeviceEntity(
                    id = "WEAROS_PIXEL_3",
                    name = "Pixel Watch 3 / WearOS",
                    type = "SMARTWATCH",
                    brand = "WearOS",
                    isConnected = false,
                    batteryPercent = 72,
                    lastSyncTimestamp = System.currentTimeMillis() - 48 * 3600 * 1000,
                    currentHeartRateBpm = 68,
                    restingHeartRateBpm = 56,
                    stepsToday = 3400,
                    sleepHours = 7.1f,
                    sleepScore = 79,
                    recoveryScore = 82,
                    caloriesBurnedSensor = 310
                ),
                WearableDeviceEntity(
                    id = "WHOOP_40_BAND",
                    name = "Whoop 4.0 Bio-Strap",
                    type = "FITNESS_BAND",
                    brand = "Whoop",
                    isConnected = false,
                    batteryPercent = 60,
                    lastSyncTimestamp = System.currentTimeMillis() - 72 * 3600 * 1000,
                    currentHeartRateBpm = 70,
                    restingHeartRateBpm = 51,
                    stepsToday = 5200,
                    sleepHours = 8.1f,
                    sleepScore = 91,
                    recoveryScore = 96,
                    caloriesBurnedSensor = 480
                )
            )
            wearableDao.insertDevices(wearables)

            // 6. Initial Social Feed Posts
            val posts = listOf(
                SocialPostEntity(
                    authorName = "Elena Rostova",
                    authorAvatarBadge = "ER",
                    authorTier = "ELITE",
                    title = "Crushed New 100kg Squat Milestone! 🚀",
                    content = "Consistency over months finally paid off. Hit 4 sets of 8 at 100kg on the Apex Full-Body Power routine. High recovery score this morning made the difference!",
                    workoutSummary = "Apex Full-Body Power • 52m • 460 kcal • Total Vol: 4,200 kg",
                    achievementBadge = "Century Squat Club (100kg)",
                    kudosCount = 38,
                    isKudosGiven = true,
                    commentsCount = 7,
                    timestamp = System.currentTimeMillis() - 2 * 3600 * 1000,
                    isUserPost = false
                ),
                SocialPostEntity(
                    authorName = "Marcus Vance",
                    authorAvatarBadge = "MV",
                    authorTier = "PRO",
                    title = "Early Morning 10K & Zone 4 Cardio Session 🏃‍♂️",
                    content = "Started the day connected with Garmin 965. Averaged 156 bpm through a breezy tempo run followed by 15 min mobility. Keep the streak alive team!",
                    workoutSummary = "Inferno HIIT Cardio Blast • 42m • 510 kcal • Avg HR: 156 bpm",
                    achievementBadge = "14-Day Workout Streak 🔥",
                    kudosCount = 29,
                    isKudosGiven = false,
                    commentsCount = 4,
                    timestamp = System.currentTimeMillis() - 6 * 3600 * 1000,
                    isUserPost = false
                ),
                SocialPostEntity(
                    authorName = "Jordan Kai",
                    authorAvatarBadge = "JK",
                    authorTier = "PRO",
                    title = "Nutrition goals locked in for 30 consecutive days 🥑",
                    content = "Hit 170g protein target every single day this month. Clean whole foods, hydration on point. Energy levels have never been higher!",
                    workoutSummary = "Nutrition Consistency • 2,400 kcal avg • 100% Macro Hit",
                    achievementBadge = "Macro Master (30 Days)",
                    kudosCount = 45,
                    isKudosGiven = true,
                    commentsCount = 11,
                    timestamp = System.currentTimeMillis() - 18 * 3600 * 1000,
                    isUserPost = false
                )
            )
            socialDao.insertPosts(posts)

            // 7. Initial Goals
            val goals = listOf(
                FitnessGoalEntity(
                    title = "Complete 20 Workouts This Month",
                    category = "WORKOUT",
                    currentVal = 14f,
                    targetVal = 20f,
                    unit = "workouts",
                    deadlineString = "End of Month",
                    isCompleted = false
                ),
                FitnessGoalEntity(
                    title = "Target Weight 73.0 kg",
                    category = "WEIGHT",
                    currentVal = 76.5f,
                    targetVal = 73.0f,
                    unit = "kg",
                    deadlineString = "In 6 Weeks",
                    isCompleted = false
                ),
                FitnessGoalEntity(
                    title = "Daily 10,000 Step Milestone",
                    category = "STEPS",
                    currentVal = 7840f,
                    targetVal = 10000f,
                    unit = "steps",
                    deadlineString = "Daily",
                    isCompleted = false
                ),
                FitnessGoalEntity(
                    title = "Hydration 3,200 ml Daily",
                    category = "NUTRITION",
                    currentVal = 1750f,
                    targetVal = 3200f,
                    unit = "ml",
                    deadlineString = "Daily",
                    isCompleted = false
                )
            )
            goalDao.insertGoals(goals)

            // 8. Initial Personal Records (PRs)
            val prs = listOf(
                PersonalRecordEntity(
                    exerciseName = "Barbell Back Squat",
                    recordValue = "115 kg (1RM)",
                    previousRecord = "105 kg",
                    achievedDate = "Last Week",
                    category = "Strength"
                ),
                PersonalRecordEntity(
                    exerciseName = "Flat Barbell Bench Press",
                    recordValue = "95 kg (1RM)",
                    previousRecord = "90 kg",
                    achievedDate = "2 Weeks ago",
                    category = "Strength"
                ),
                PersonalRecordEntity(
                    exerciseName = "Deadlift (Conventional)",
                    recordValue = "150 kg (1RM)",
                    previousRecord = "140 kg",
                    achievedDate = "1 Month ago",
                    category = "Strength"
                ),
                PersonalRecordEntity(
                    exerciseName = "5K Tempo Run",
                    recordValue = "21m 45s",
                    previousRecord = "23m 10s",
                    achievedDate = "3 Weeks ago",
                    category = "Cardio"
                ),
                PersonalRecordEntity(
                    exerciseName = "Strict Pull-Ups (Max Reps)",
                    recordValue = "18 reps",
                    previousRecord = "15 reps",
                    achievedDate = "Yesterday",
                    category = "Calisthenics"
                )
            )
            goalDao.insertPRs(prs)
        }
    }
}
