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
        UserAccountEntity::class,
        WorkoutRoutineEntity::class,
        WorkoutLogEntity::class,
        ExerciseLibraryEntity::class,
        MealEntryEntity::class,
        FoodLibraryEntity::class,
        WaterLogEntity::class,
        WearableDeviceEntity::class,
        BiometricLogEntity::class,
        SocialPostEntity::class,
        FitnessGoalEntity::class,
        PersonalRecordEntity::class,
        ProgressRecordEntity::class,
        FitnessChallengeEntity::class,
        MilestoneBadgeEntity::class,
        UserGamificationStatsEntity::class,
        LeaderboardEntryEntity::class,
        AiAdaptationEntity::class,
        UserWorkoutFeedbackEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseLibraryDao(): ExerciseLibraryDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun foodLibraryDao(): FoodLibraryDao
    abstract fun wearableDao(): WearableDao
    abstract fun biometricDao(): BiometricDao
    abstract fun socialDao(): SocialDao
    abstract fun goalDao(): GoalDao
    abstract fun progressDao(): ProgressDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun aiAdaptationDao(): AiAdaptationDao

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
            val userAccountDao = database.userAccountDao()
            val workoutDao = database.workoutDao()
            val exerciseLibraryDao = database.exerciseLibraryDao()
            val nutritionDao = database.nutritionDao()
            val foodLibraryDao = database.foodLibraryDao()
            val wearableDao = database.wearableDao()
            val biometricDao = database.biometricDao()
            val socialDao = database.socialDao()
            val goalDao = database.goalDao()
            val progressDao = database.progressDao()
            val gamificationDao = database.gamificationDao()
            val aiAdaptationDao = database.aiAdaptationDao()

            // 0. Seed Demo Account
            userAccountDao.registerAccount(
                UserAccountEntity(
                    email = "alex.fit@apexfit.io",
                    passwordHash = "apexfit123",
                    name = "Alex Rivers",
                    fitnessGoal = "Muscle Growth & Athleticism",
                    fitnessLevel = "Intermediate"
                )
            )

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
                    subscriptionTier = "PRO",
                    isLoggedIn = true
                )
            )

            // 2. Exercise Library
            val exerciseCatalog = listOf(
                // Legs
                ExerciseLibraryEntity(name = "Barbell Back Squat", targetMuscle = "Legs", secondaryMuscles = "Glutes, Lower Back, Core", equipment = "Barbell", difficulty = "Intermediate", instructions = "Feet shoulder-width apart, brace core, descend to parallel and drive up.", defaultSets = 4, defaultReps = 8, defaultRestSeconds = 90, isCompound = true),
                ExerciseLibraryEntity(name = "Romanian Deadlift", targetMuscle = "Legs", secondaryMuscles = "Hamstrings, Glutes, Lower Back", equipment = "Barbell", difficulty = "Intermediate", instructions = "Hinge hips back with slight knee bend, feeling deep hamstring stretch.", defaultSets = 4, defaultReps = 10, defaultRestSeconds = 90, isCompound = true),
                ExerciseLibraryEntity(name = "Bulgarian Split Squats", targetMuscle = "Legs", secondaryMuscles = "Quads, Glutes", equipment = "Dumbbell", difficulty = "Intermediate", instructions = "Rear foot elevated on bench, descend front thigh parallel to floor.", defaultSets = 3, defaultReps = 10, defaultRestSeconds = 60, isCompound = true),
                ExerciseLibraryEntity(name = "Leg Press 45-Degree", targetMuscle = "Legs", secondaryMuscles = "Quads, Adductors", equipment = "Machine", difficulty = "Beginner", instructions = "Place feet hip-width on sled, lower smoothly without rounding lower back.", defaultSets = 4, defaultReps = 12, defaultRestSeconds = 75, isCompound = true),
                ExerciseLibraryEntity(name = "Standing Calf Raises", targetMuscle = "Legs", secondaryMuscles = "Gastrocnemius, Soleus", equipment = "Machine", difficulty = "Beginner", instructions = "Full ankle stretch at bottom, explosive rise onto balls of feet.", defaultSets = 4, defaultReps = 15, defaultRestSeconds = 45, isCompound = false),

                // Chest
                ExerciseLibraryEntity(name = "Barbell Flat Bench Press", targetMuscle = "Chest", secondaryMuscles = "Front Delts, Triceps", equipment = "Barbell", difficulty = "Intermediate", instructions = "Grip slightly wider than shoulders, touch lower sternum and press firmly.", defaultSets = 4, defaultReps = 8, defaultRestSeconds = 90, isCompound = true),
                ExerciseLibraryEntity(name = "Incline Dumbbell Bench Press", targetMuscle = "Chest", secondaryMuscles = "Upper Chest, Front Delts", equipment = "Dumbbell", difficulty = "Intermediate", instructions = "Set bench to 30 degrees, press dumbbells upward in a gentle converging arc.", defaultSets = 4, defaultReps = 10, defaultRestSeconds = 75, isCompound = true),
                ExerciseLibraryEntity(name = "Standing Cable Chest Flyes", targetMuscle = "Chest", secondaryMuscles = "Inner Pecs, Front Delts", equipment = "Cable", difficulty = "Beginner", instructions = "Bring cables together in a hugging motion with slight elbow bend.", defaultSets = 3, defaultReps = 12, defaultRestSeconds = 60, isCompound = false),
                ExerciseLibraryEntity(name = "Chest Dips (Forward Lean)", targetMuscle = "Chest", secondaryMuscles = "Triceps, Shoulders", equipment = "Bodyweight", difficulty = "Intermediate", instructions = "Lean torso forward, lower until shoulders are below elbows, press up powerfully.", defaultSets = 3, defaultReps = 12, defaultRestSeconds = 60, isCompound = true),
                ExerciseLibraryEntity(name = "Push-Up Finisher", targetMuscle = "Chest", secondaryMuscles = "Core, Triceps", equipment = "Bodyweight", difficulty = "Beginner", instructions = "Plank alignment, touch chest to floor with elbows at 45 degrees.", defaultSets = 3, defaultReps = 20, defaultRestSeconds = 45, isCompound = true),

                // Back
                ExerciseLibraryEntity(name = "Conventional Deadlift", targetMuscle = "Back", secondaryMuscles = "Hamstrings, Glutes, Forearms", equipment = "Barbell", difficulty = "Advanced", instructions = "Hinge at hips, grip bar firmly, drive feet through floor while keeping spine neutral.", defaultSets = 4, defaultReps = 6, defaultRestSeconds = 120, isCompound = true),
                ExerciseLibraryEntity(name = "Strict Pull-Ups", targetMuscle = "Back", secondaryMuscles = "Biceps, Rear Delts", equipment = "Bodyweight", difficulty = "Intermediate", instructions = "Full dead hang to chin over bar with active scapular depression.", defaultSets = 4, defaultReps = 10, defaultRestSeconds = 90, isCompound = true),
                ExerciseLibraryEntity(name = "Overhand Barbell Bent-Over Row", targetMuscle = "Back", secondaryMuscles = "Rhomboids, Lats, Biceps", equipment = "Barbell", difficulty = "Intermediate", instructions = "Hinge hips at 45 degrees, pull barbell to lower ribcage while squeezing shoulder blades.", defaultSets = 4, defaultReps = 8, defaultRestSeconds = 75, isCompound = true),
                ExerciseLibraryEntity(name = "Lat Pulldown (Wide Grip)", targetMuscle = "Back", secondaryMuscles = "Lats, Teres Major", equipment = "Cable", difficulty = "Beginner", instructions = "Pull bar downward to upper chest with proud chest and retracted scapula.", defaultSets = 4, defaultReps = 12, defaultRestSeconds = 60, isCompound = false),
                ExerciseLibraryEntity(name = "Seated Cable Row", targetMuscle = "Back", secondaryMuscles = "Mid-Back, Biceps", equipment = "Cable", difficulty = "Beginner", instructions = "Keep spine upright, drive elbows backward and pinch shoulder blades together.", defaultSets = 3, defaultReps = 12, defaultRestSeconds = 60, isCompound = false),

                // Shoulders
                ExerciseLibraryEntity(name = "Overhead Standing Barbell Press", targetMuscle = "Shoulders", secondaryMuscles = "Triceps, Upper Chest, Core", equipment = "Barbell", difficulty = "Intermediate", instructions = "Press bar directly overhead locking out arms with glutes and abs engaged.", defaultSets = 4, defaultReps = 8, defaultRestSeconds = 90, isCompound = true),
                ExerciseLibraryEntity(name = "Lateral Dumbbell Raises", targetMuscle = "Shoulders", secondaryMuscles = "Traps", equipment = "Dumbbell", difficulty = "Beginner", instructions = "Raise arms outward leading with elbows until parallel to ground.", defaultSets = 4, defaultReps = 15, defaultRestSeconds = 45, isCompound = false),
                ExerciseLibraryEntity(name = "Seated Arnold Press", targetMuscle = "Shoulders", secondaryMuscles = "Front Delts, Lateral Delts", equipment = "Dumbbell", difficulty = "Intermediate", instructions = "Rotate palms from facing chest outwards as you press smoothly overhead.", defaultSets = 3, defaultReps = 10, defaultRestSeconds = 60, isCompound = true),
                ExerciseLibraryEntity(name = "Rear Delt Rope Face Pulls", targetMuscle = "Shoulders", secondaryMuscles = "Rear Delts, Rotator Cuff", equipment = "Cable", difficulty = "Beginner", instructions = "Pull rope toward eye level with elbows flared high and externally rotated.", defaultSets = 4, defaultReps = 15, defaultRestSeconds = 45, isCompound = false),

                // Arms
                ExerciseLibraryEntity(name = "Barbell EZ-Bar Bicep Curl", targetMuscle = "Arms", secondaryMuscles = "Brachialis, Forearms", equipment = "Barbell", difficulty = "Beginner", instructions = "Keep upper arms locked against ribs, curl smoothly without swinging torso.", defaultSets = 3, defaultReps = 10, defaultRestSeconds = 60, isCompound = false),
                ExerciseLibraryEntity(name = "Close-Grip Bench Press", targetMuscle = "Arms", secondaryMuscles = "Triceps, Inner Chest", equipment = "Barbell", difficulty = "Intermediate", instructions = "Grip shoulder-width apart, tuck elbows close to ribcage on descent.", defaultSets = 4, defaultReps = 8, defaultRestSeconds = 75, isCompound = true),
                ExerciseLibraryEntity(name = "Incline Dumbbell Hammer Curl", targetMuscle = "Arms", secondaryMuscles = "Brachioradialis, Biceps", equipment = "Dumbbell", difficulty = "Beginner", instructions = "Neutral grip with palms facing each other, full stretch at bottom of incline.", defaultSets = 3, defaultReps = 12, defaultRestSeconds = 45, isCompound = false),
                ExerciseLibraryEntity(name = "Tricep Overhead Rope Extension", targetMuscle = "Arms", secondaryMuscles = "Triceps Long Head", equipment = "Cable", difficulty = "Beginner", instructions = "Extend cable forward overhead with flared ropes at completion.", defaultSets = 3, defaultReps = 15, defaultRestSeconds = 45, isCompound = false),

                // Core
                ExerciseLibraryEntity(name = "Hanging Leg / Knee Raises", targetMuscle = "Core", secondaryMuscles = "Hip Flexors, Lower Abs", equipment = "Bodyweight", difficulty = "Beginner", instructions = "Hang from bar, tilt pelvis upward without swinging momentum.", defaultSets = 3, defaultReps = 15, defaultRestSeconds = 45, isCompound = false),
                ExerciseLibraryEntity(name = "Ab Wheel Rollouts", targetMuscle = "Core", secondaryMuscles = "Lats, Shoulders", equipment = "Bodyweight", difficulty = "Advanced", instructions = "From knees, roll wheel forward maintaining hollow body posture, pull back with core.", defaultSets = 3, defaultReps = 10, defaultRestSeconds = 60, isCompound = false),
                ExerciseLibraryEntity(name = "Cable Standing Woodchopper", targetMuscle = "Core", secondaryMuscles = "Obliques, Rotators", equipment = "Cable", difficulty = "Intermediate", instructions = "Rotate torso diagonally downward using oblique contraction.", defaultSets = 3, defaultReps = 12, defaultRestSeconds = 45, isCompound = false),
                ExerciseLibraryEntity(name = "Weighted Plank Hold", targetMuscle = "Core", secondaryMuscles = "Shoulders, Glutes", equipment = "Bodyweight", difficulty = "Intermediate", instructions = "Forearms on deck, glutes squeezed tight, straight line from heels to crown.", defaultSets = 3, defaultReps = 60, defaultRestSeconds = 45, isCompound = false)
            )
            exerciseLibraryDao.insertExercises(exerciseCatalog)

            // 3. Initial Workout Routines for ALL Body Parts
            val chestForgeExercises = """[
                {"name":"Barbell Flat Bench Press","targetMuscle":"Chest","sets":4,"reps":8,"weightKg":80.0,"restSeconds":90,"tips":"Keep shoulders pinned to bench, drive feet into floor.","animationCue":"Explosive concentric press"},
                {"name":"Incline Dumbbell Bench Press","targetMuscle":"Chest","sets":4,"reps":10,"weightKg":28.0,"restSeconds":75,"tips":"30-degree incline, feel deep upper pec stretch.","animationCue":"Smooth converging arc"},
                {"name":"Chest Dips (Forward Lean)","targetMuscle":"Chest","sets":3,"reps":12,"weightKg":0.0,"restSeconds":60,"tips":"Lean forward 30 degrees to bias lower/outer chest.","animationCue":"Controlled descent"},
                {"name":"Standing Cable Chest Flyes","targetMuscle":"Chest","sets":3,"reps":15,"weightKg":14.0,"restSeconds":45,"tips":"Squeeze pecs hard for 1 full second at peak.","animationCue":"Peak contraction squeeze"},
                {"name":"Push-Up Finisher","targetMuscle":"Chest","sets":3,"reps":20,"weightKg":0.0,"restSeconds":45,"tips":"Fast controlled reps until muscular burnout.","animationCue":"Full chest to ground"}
            ]"""

            val backDestroyerExercises = """[
                {"name":"Conventional Deadlift","targetMuscle":"Back","sets":4,"reps":6,"weightKg":120.0,"restSeconds":120,"tips":"Brace abs, engage lats, drive hips forward.","animationCue":"Powerful hip lockout"},
                {"name":"Strict Pull-Ups","targetMuscle":"Back","sets":4,"reps":8,"weightKg":0.0,"restSeconds":90,"tips":"Depress scapula before initiating the pull.","animationCue":"Chin clear above bar"},
                {"name":"Overhand Barbell Bent-Over Row","targetMuscle":"Back","sets":4,"reps":10,"weightKg":70.0,"restSeconds":75,"tips":"Pull to navel, keep torso locked at 45 degrees.","animationCue":"Elbows driving past ribs"},
                {"name":"Lat Pulldown (Wide Grip)","targetMuscle":"Back","sets":3,"reps":12,"weightKg":60.0,"restSeconds":60,"tips":"Keep chest arched and pull elbows straight down.","animationCue":"Full stretch at top"},
                {"name":"Seated Cable Row","targetMuscle":"Back","sets":3,"reps":12,"weightKg":55.0,"restSeconds":60,"tips":"Pause and pinch shoulder blades on every rep.","animationCue":"Squeeze mid-traps"}
            ]"""

            val legsIronDayExercises = """[
                {"name":"Barbell Back Squat","targetMuscle":"Legs","sets":4,"reps":8,"weightKg":90.0,"restSeconds":120,"tips":"Brace core 360 degrees, knees track over toes.","animationCue":"Deep parallel depth"},
                {"name":"Romanian Deadlift","targetMuscle":"Legs","sets":4,"reps":10,"weightKg":80.0,"restSeconds":90,"tips":"Push hips straight back with soft knees.","animationCue":"Hamstring tension stretch"},
                {"name":"Bulgarian Split Squats","targetMuscle":"Legs","sets":3,"reps":10,"weightKg":18.0,"restSeconds":60,"tips":"Keep front foot flat and torso slightly leaned.","animationCue":"Unilateral quad burn"},
                {"name":"Leg Press 45-Degree","targetMuscle":"Legs","sets":3,"reps":15,"weightKg":160.0,"restSeconds":75,"tips":"Do not lock out knees at top to keep continuous tension.","animationCue":"Continuous quad tension"},
                {"name":"Standing Calf Raises","targetMuscle":"Legs","sets":4,"reps":15,"weightKg":60.0,"restSeconds":45,"tips":"2-second pause at bottom stretch, explosive raise.","animationCue":"Full ankle extension"}
            ]"""

            val shouldersForgeExercises = """[
                {"name":"Overhead Standing Barbell Press","targetMuscle":"Shoulders","sets":4,"reps":8,"weightKg":50.0,"restSeconds":90,"tips":"Squeeze glutes and abs, press directly overhead.","animationCue":"Head through the window"},
                {"name":"Lateral Dumbbell Raises","targetMuscle":"Shoulders","sets":4,"reps":15,"weightKg":12.0,"restSeconds":45,"tips":"Pour the pitcher slightly, lead with elbows.","animationCue":"Constant lateral delt load"},
                {"name":"Seated Arnold Press","targetMuscle":"Shoulders","sets":3,"reps":10,"weightKg":18.0,"restSeconds":60,"tips":"Full rotation from front to overhead lockout.","animationCue":"Rotational delt engagement"},
                {"name":"Rear Delt Rope Face Pulls","targetMuscle":"Shoulders","sets":4,"reps":15,"weightKg":25.0,"restSeconds":45,"tips":"Pull to forehead with high elbows for posture.","animationCue":"External rotator cuff activation"}
            ]"""

            val armsPumpExercises = """[
                {"name":"Barbell EZ-Bar Bicep Curl","targetMuscle":"Arms","sets":4,"reps":10,"weightKg":32.0,"restSeconds":60,"tips":"Pin elbows at sides, eliminate swing momentum.","animationCue":"Peak bicep contraction"},
                {"name":"Close-Grip Bench Press","targetMuscle":"Arms","sets":4,"reps":8,"weightKg":65.0,"restSeconds":75,"tips":"Hands 30cm apart, lower to mid-chest.","animationCue":"Tricep extension lockout"},
                {"name":"Incline Dumbbell Hammer Curl","targetMuscle":"Arms","sets":3,"reps":12,"weightKg":14.0,"restSeconds":45,"tips":"Builds the brachialis and forearm thickness.","animationCue":"Neutral grip curl"},
                {"name":"Tricep Overhead Rope Extension","targetMuscle":"Arms","sets":3,"reps":15,"weightKg":22.0,"restSeconds":45,"tips":"Flare ropes outward at full extension.","animationCue":"Long head tricep stretch"}
            ]"""

            val coreAbsExercises = """[
                {"name":"Hanging Leg / Knee Raises","targetMuscle":"Core","sets":3,"reps":15,"weightKg":0.0,"restSeconds":45,"tips":"Curl pelvis up into chest without swinging.","animationCue":"Lower abs contraction"},
                {"name":"Ab Wheel Rollouts","targetMuscle":"Core","sets":3,"reps":10,"weightKg":0.0,"restSeconds":60,"tips":"Maintain posterior pelvic tilt throughout.","animationCue":"Anti-extension core brace"},
                {"name":"Cable Standing Woodchopper","targetMuscle":"Core","sets":3,"reps":12,"weightKg":20.0,"restSeconds":45,"tips":"Drive rotation from obliques, not arms.","animationCue":"High-to-low diagonal power"},
                {"name":"Weighted Plank Hold","targetMuscle":"Core","sets":3,"reps":60,"durationSeconds":60,"restSeconds":45,"tips":"Total body tension, squeeze quads and glutes.","animationCue":"Rock solid isometric hold"}
            ]"""

            val fullBodyStrengthExercises = """[
                {"name":"Barbell Back Squats","targetMuscle":"Legs","sets":4,"reps":8,"weightKg":85.0,"restSeconds":90,"tips":"Keep chest elevated and drive through mid-foot.","animationCue":"Squat deep to parallel"},
                {"name":"Incline Dumbbell Press","targetMuscle":"Chest","sets":4,"reps":10,"weightKg":28.0,"restSeconds":75,"tips":"Retract scapulae and press up in an arc.","animationCue":"Controlled eccentric tempo"},
                {"name":"Overhand Barbell Rows","targetMuscle":"Back","sets":4,"reps":10,"weightKg":65.0,"restSeconds":75,"tips":"Hinge hips at 45 degrees and pull to lower sternum.","animationCue":"Squeeze shoulder blades together"},
                {"name":"Overhead Standing Barbell Press","targetMuscle":"Shoulders","sets":3,"reps":10,"weightKg":45.0,"restSeconds":60,"tips":"Press bar directly overhead locking out arms.","animationCue":"Smooth overhead lockout"},
                {"name":"Hanging Leg / Knee Raises","targetMuscle":"Core","sets":3,"reps":15,"weightKg":0.0,"restSeconds":45,"tips":"Curl pelvis upwards without swinging.","animationCue":"Engage lower abdominals"}
            ]"""

            val hiitBurnerExercises = """[
                {"name":"High Knees Sprint","targetMuscle":"Full Body","sets":4,"reps":45,"durationSeconds":45,"restSeconds":20,"tips":"Explosive hip drive with synchronized arm pumps.","animationCue":"Fast sprint cadence"},
                {"name":"Burpees with Jump","targetMuscle":"Full Body","sets":4,"reps":15,"durationSeconds":45,"restSeconds":30,"tips":"Chest to deck, pop feet wide and jump high.","animationCue":"Maximum intensity vertical jump"},
                {"name":"Kettlebell Swings","targetMuscle":"Legs","sets":4,"reps":20,"weightKg":20.0,"durationSeconds":45,"restSeconds":25,"tips":"Hip hinge power snap, glute contraction.","animationCue":"Float weight to eye level"},
                {"name":"Mountain Climbers","targetMuscle":"Core","sets":4,"reps":40,"durationSeconds":45,"restSeconds":20,"tips":"Maintain steady plank and sprint knees to chest.","animationCue":"Rapid alternating piston drive"}
            ]"""

            val yogaRecoveryMobility = """[
                {"name":"World's Greatest Stretch","targetMuscle":"Full Body","sets":3,"reps":8,"durationSeconds":60,"restSeconds":15,"tips":"Deep thoracic spine rotation with hip opener.","animationCue":"Fluid sweeping arm rotation"},
                {"name":"Pigeon Pose Hip Opener","targetMuscle":"Legs","sets":2,"reps":60,"durationSeconds":60,"restSeconds":15,"tips":"Square hips to floor, breathe into tight glutes.","animationCue":"Gentle deep hip release"},
                {"name":"Cat-Cow Spinal Waves","targetMuscle":"Back","sets":3,"reps":12,"durationSeconds":45,"restSeconds":10,"tips":"Synchronize breath with spinal flexion and extension.","animationCue":"Slow rolling vertebra wave"},
                {"name":"Deep Malasana Squat Hold","targetMuscle":"Legs","sets":2,"reps":60,"durationSeconds":60,"restSeconds":15,"tips":"Elbows inside knees, spine tall and lengthened.","animationCue":"Pelvic floor & ankle decompression"}
            ]"""

            val routines = listOf(
                WorkoutRoutineEntity(
                    title = "Titan Chest & Pectoral Forge",
                    description = "Comprehensive chest hypertrophy routine targeting upper, mid, and lower pectoral fibers with heavy pressing and cable flyes.",
                    category = "Chest",
                    difficulty = "Intermediate",
                    durationMinutes = 45,
                    estimatedCalories = 390,
                    exercisesJson = chestForgeExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 10,
                    targetMuscleGroups = "Chest (Upper, Mid, Lower Pectorals)"
                ),
                WorkoutRoutineEntity(
                    title = "V-Taper Lats & Back Destroyer",
                    description = "Heavy-duty back protocol for wide lat wingspan, thick rhomboids, and bulletproof posterior chain.",
                    category = "Back",
                    difficulty = "Advanced",
                    durationMinutes = 50,
                    estimatedCalories = 440,
                    exercisesJson = backDestroyerExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 14,
                    targetMuscleGroups = "Back (Lats, Traps, Rhomboids, Lower Back)"
                ),
                WorkoutRoutineEntity(
                    title = "Iron Quads & Glutes Leg Day",
                    description = "High-volume lower body builder focusing on barbell back squats, heavy Romanian deadlifts, and unilateral Bulgarian split squats.",
                    category = "Legs",
                    difficulty = "Advanced",
                    durationMinutes = 52,
                    estimatedCalories = 480,
                    exercisesJson = legsIronDayExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 8,
                    targetMuscleGroups = "Legs (Quads, Hamstrings, Glutes, Calves)"
                ),
                WorkoutRoutineEntity(
                    title = "3D Boulder Shoulders Forge",
                    description = "Complete deltoid isolation targeting anterior, lateral, and posterior heads for wide, capped shoulders.",
                    category = "Shoulders",
                    difficulty = "Intermediate",
                    durationMinutes = 40,
                    estimatedCalories = 340,
                    exercisesJson = shouldersForgeExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 6,
                    targetMuscleGroups = "Shoulders (Front, Lateral, Rear Deltoids)"
                ),
                WorkoutRoutineEntity(
                    title = "Armageddon Biceps & Triceps",
                    description = "Dedicated arm day designed for massive biceps peaks, dense horseshoe triceps, and iron grip strength.",
                    category = "Arms",
                    difficulty = "Intermediate",
                    durationMinutes = 38,
                    estimatedCalories = 320,
                    exercisesJson = armsPumpExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 9,
                    targetMuscleGroups = "Arms (Biceps, Triceps, Forearms)"
                ),
                WorkoutRoutineEntity(
                    title = "Six-Pack Core & Oblique Engine",
                    description = "Dynamic rotational and anti-extension core routine for deep abdominal definition and lumbar spine stability.",
                    category = "Core",
                    difficulty = "Intermediate",
                    durationMinutes = 28,
                    estimatedCalories = 240,
                    exercisesJson = coreAbsExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 16,
                    targetMuscleGroups = "Core (Upper Abs, Lower Abs, Obliques)"
                ),
                WorkoutRoutineEntity(
                    title = "Apex Full-Body Power",
                    description = "High-efficiency compound strength session targeting power, core stability, and lean muscle mass across all body parts.",
                    category = "Full Body",
                    difficulty = "Intermediate",
                    durationMinutes = 48,
                    estimatedCalories = 420,
                    exercisesJson = fullBodyStrengthExercises,
                    isPremium = false,
                    isCustom = false,
                    timesCompleted = 12,
                    targetMuscleGroups = "Full Body (Legs, Chest, Back, Shoulders, Core)"
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
                    targetMuscleGroups = "Full Body & Cardiovascular"
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
                    targetMuscleGroups = "Full Body (Hips, Thoracic Spine, Ankles)"
                )
            )
            workoutDao.insertRoutines(routines)

            // 4. Initial Workout Logs (History)
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

            // 5. Food Library
            val foods = listOf(
                FoodLibraryEntity(name = "Grilled Chicken Breast", category = "Protein", caloriesPerServing = 165, proteinG = 31, carbsG = 0, fatG = 4, servingUnit = "100g"),
                FoodLibraryEntity(name = "Wild Atlantic Salmon", category = "Protein", caloriesPerServing = 208, proteinG = 22, carbsG = 0, fatG = 13, servingUnit = "100g"),
                FoodLibraryEntity(name = "Organic Whole Eggs (2 large)", category = "Protein", caloriesPerServing = 140, proteinG = 12, carbsG = 1, fatG = 10, servingUnit = "2 eggs"),
                FoodLibraryEntity(name = "Greek Yogurt 0% Plain", category = "Protein", caloriesPerServing = 120, proteinG = 22, carbsG = 6, fatG = 0, servingUnit = "200g"),
                FoodLibraryEntity(name = "Whey Isolate Protein Powder", category = "Protein", caloriesPerServing = 120, proteinG = 25, carbsG = 2, fatG = 1, servingUnit = "1 scoop (30g)"),
                FoodLibraryEntity(name = "Cooked Jasmine / Brown Rice", category = "Carb", caloriesPerServing = 130, proteinG = 3, carbsG = 28, fatG = 0, servingUnit = "100g"),
                FoodLibraryEntity(name = "Rolled Oats / Oatmeal", category = "Carb", caloriesPerServing = 150, proteinG = 5, carbsG = 27, fatG = 3, servingUnit = "40g"),
                FoodLibraryEntity(name = "Hass Avocado", category = "Healthy Fat", caloriesPerServing = 160, proteinG = 2, carbsG = 9, fatG = 15, servingUnit = "1/2 medium"),
                FoodLibraryEntity(name = "Raw Almonds", category = "Healthy Fat", caloriesPerServing = 160, proteinG = 6, carbsG = 6, fatG = 14, servingUnit = "28g (1 oz)")
            )
            foodLibraryDao.insertFoods(foods)

            // 6. Initial Nutrition (Meals & Water) for today
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

            // 7. Initial Wearable Devices
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

            // 8. Historical 7-Day Biometrics Logs
            val biometricLogs = mutableListOf<BiometricLogEntity>()
            for (i in 0..6) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dStr = todayFormat.format(cal.time)
                biometricLogs.add(
                    BiometricLogEntity(
                        dateString = dStr,
                        avgHeartRate = 72 + (i % 3) * 2,
                        restingHeartRate = 54 + (i % 2),
                        hrvMs = 68 - i * 2,
                        recoveryScore = 94 - i * 3,
                        strainScore = 14.5f - i * 0.8f,
                        sleepHours = 7.8f - (i % 3) * 0.3f,
                        sleepScore = 88 - i * 2,
                        steps = 8200 - i * 350,
                        activeCalories = 540 - i * 30,
                        vo2Max = 52.4f,
                        timestamp = cal.timeInMillis
                    )
                )
            }
            biometricDao.insertBiometricLogs(biometricLogs)

            // 9. Initial Social Feed Posts
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

            // 10. Initial Goals
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

            // 11. Initial Personal Records (PRs)
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

            // Progress Body Composition Records
            progressDao.insertProgressRecord(
                ProgressRecordEntity(
                    dateString = todayStr,
                    weightKg = 76.5f,
                    bodyFatPercent = 14.8f,
                    muscleMassKg = 38.2f,
                    chestCm = 104.5f,
                    waistCm = 81.0f,
                    armsCm = 39.0f,
                    thighsCm = 58.5f,
                    notes = "Energy high, recovery 94%, feeling lean and strong"
                )
            )

            // 12. Gamification Challenges
            val initialChallenges = listOf(
                FitnessChallengeEntity(
                    id = "daily_cals_400",
                    title = "Metabolic Inferno",
                    description = "Burn 400 active calories in any workout session today.",
                    category = "DAILY",
                    metricType = "CALORIES",
                    targetValue = 400f,
                    currentValue = 430f,
                    xpReward = 150,
                    deadlineText = "Ends Today (Midnight)",
                    isJoined = true,
                    isClaimed = false,
                    iconDescriptor = "FIRE"
                ),
                FitnessChallengeEntity(
                    id = "daily_steps_10k",
                    title = "10,000 Step Quest",
                    description = "Log 10,000 steps through wearable or sensor tracking.",
                    category = "DAILY",
                    metricType = "STEPS",
                    targetValue = 10000f,
                    currentValue = 7840f,
                    xpReward = 120,
                    deadlineText = "Ends in 6 hrs",
                    isJoined = true,
                    isClaimed = false,
                    iconDescriptor = "RUN"
                ),
                FitnessChallengeEntity(
                    id = "daily_water_hero",
                    title = "Cellular Hydration 3L",
                    description = "Drink and log 3,000 ml of water throughout the day.",
                    category = "DAILY",
                    metricType = "HYDRATION_ML",
                    targetValue = 3000f,
                    currentValue = 1750f,
                    xpReward = 100,
                    deadlineText = "Ends in 6 hrs",
                    isJoined = true,
                    isClaimed = false,
                    iconDescriptor = "WATER"
                ),
                FitnessChallengeEntity(
                    id = "weekly_iron_titan",
                    title = "Iron Lifter 25,000 kg Volume",
                    description = "Accumulate 25,000 kg in total lifted resistance volume across all routines this week.",
                    category = "WEEKLY",
                    metricType = "TOTAL_VOLUME_KG",
                    targetValue = 25000f,
                    currentValue = 18450f,
                    xpReward = 500,
                    deadlineText = "3 Days Left",
                    isJoined = true,
                    isClaimed = false,
                    iconDescriptor = "STRENGTH"
                ),
                FitnessChallengeEntity(
                    id = "weekly_zone4_cardio",
                    title = "Zone 4 Cardio Crusher",
                    description = "Spend at least 45 minutes in high-intensity Anaerobic Zone 4 (>150 bpm).",
                    category = "WEEKLY",
                    metricType = "DURATION_MINUTES",
                    targetValue = 45f,
                    currentValue = 32f,
                    xpReward = 450,
                    deadlineText = "3 Days Left",
                    isJoined = true,
                    isClaimed = false,
                    iconDescriptor = "HEART"
                ),
                FitnessChallengeEntity(
                    id = "community_global_million",
                    title = "August 1,000,000 Steps Pool",
                    description = "Join 4,200+ ApexFit athletes to collectively hit 1 Million steps this week.",
                    category = "COMMUNITY",
                    metricType = "STEPS",
                    targetValue = 1000000f,
                    currentValue = 742500f,
                    xpReward = 750,
                    deadlineText = "4 Days Left",
                    isJoined = true,
                    isClaimed = false,
                    iconDescriptor = "GLOBE"
                )
            )
            gamificationDao.insertChallenges(initialChallenges)

            // 13. Milestone Badges
            val initialBadges = listOf(
                MilestoneBadgeEntity(id = "badge_streak_7", title = "7-Day Ignition", subtitle = "Week of Discipline", description = "Completed 7 continuous active training days.", rarity = "COMMON", category = "STREAK", currentProgress = 7, maxProgress = 7, isUnlocked = true, unlockedDate = "2 Weeks ago", xpReward = 150, iconEmoji = "🔥"),
                MilestoneBadgeEntity(id = "badge_streak_14", title = "Relentless Fortitude", subtitle = "Two Weeks Strong", description = "Maintained an unbroken 14-day training streak.", rarity = "RARE", category = "STREAK", currentProgress = 16, maxProgress = 14, isUnlocked = true, unlockedDate = "Yesterday", xpReward = 300, iconEmoji = "⚡"),
                MilestoneBadgeEntity(id = "badge_iron_50k", title = "Iron Titan 50,000 kg", subtitle = "Cumulative Tonnage", description = "Lifted over 50,000 kg in cumulative workout volume.", rarity = "EPIC", category = "STRENGTH", currentProgress = 42800, maxProgress = 50000, isUnlocked = false, xpReward = 600, iconEmoji = "🏋️"),
                MilestoneBadgeEntity(id = "badge_5k_speed", title = "Sub-22m 5K Speedster", subtitle = "Cardio Milestone", description = "Clocked a 5K tempo run under 22 minutes.", rarity = "RARE", category = "CARDIO", currentProgress = 1, maxProgress = 1, isUnlocked = true, unlockedDate = "3 Weeks ago", xpReward = 250, iconEmoji = "🏃‍♂️"),
                MilestoneBadgeEntity(id = "badge_century_club", title = "Centurion Lifter", subtitle = "100 Workouts Completed", description = "Logged 100 full workout sessions in ApexFit.", rarity = "LEGENDARY", category = "CONSISTENCY", currentProgress = 37, maxProgress = 100, isUnlocked = false, xpReward = 1500, iconEmoji = "👑"),
                MilestoneBadgeEntity(id = "badge_hydro_30d", title = "Hydration Master", subtitle = "30-Day Target Hit", description = "Met daily water goals for 30 consecutive days.", rarity = "EPIC", category = "HYDRATION", currentProgress = 22, maxProgress = 30, isUnlocked = false, xpReward = 400, iconEmoji = "💧"),
                MilestoneBadgeEntity(id = "badge_recovery_sage", title = "Recovery Sage", subtitle = "Optimum Sleep & HRV", description = "Achieved 90%+ recovery score on 5 consecutive days.", rarity = "RARE", category = "RECOVERY", currentProgress = 5, maxProgress = 5, isUnlocked = true, unlockedDate = "Last Week", xpReward = 300, iconEmoji = "🧘")
            )
            gamificationDao.insertBadges(initialBadges)

            // 14. User Gamification Stats
            gamificationDao.insertOrUpdateStats(
                UserGamificationStatsEntity(
                    id = 1,
                    totalXp = 2450,
                    currentLevel = 8,
                    xpToNextLevel = 3000,
                    currentLevelXp = 450,
                    totalBadgesUnlocked = 4,
                    activeChallengesCount = 6,
                    completedChallengesCount = 14,
                    rankTitle = "Apex Master"
                )
            )

            // 15. Leaderboards
            val leaderboardData = listOf(
                LeaderboardEntryEntity(category = "XP", rank = 1, name = "Valeriya K.", avatarBadge = "VK", tier = "TITAN", scoreValue = 4850, scoreUnit = "XP", level = 16, trend = "SAME"),
                LeaderboardEntryEntity(category = "XP", rank = 2, name = "Alex Rivers", avatarBadge = "AR", tier = "ELITE", scoreValue = 2450, scoreUnit = "XP", isCurrentUser = true, level = 8, trend = "UP"),
                LeaderboardEntryEntity(category = "XP", rank = 3, name = "David 'Iron' S.", avatarBadge = "DS", tier = "PRO", scoreValue = 2310, scoreUnit = "XP", level = 7, trend = "DOWN"),
                LeaderboardEntryEntity(category = "XP", rank = 4, name = "Sarah Chen", avatarBadge = "SC", tier = "PRO", scoreValue = 2190, scoreUnit = "XP", level = 7, trend = "UP"),
                LeaderboardEntryEntity(category = "XP", rank = 5, name = "Marcus Vance", avatarBadge = "MV", tier = "PRO", scoreValue = 1980, scoreUnit = "XP", level = 6, trend = "SAME"),

                LeaderboardEntryEntity(category = "CALORIES", rank = 1, name = "Valeriya K.", avatarBadge = "VK", tier = "TITAN", scoreValue = 6400, scoreUnit = "kcal", level = 16, trend = "UP"),
                LeaderboardEntryEntity(category = "CALORIES", rank = 2, name = "Marcus Vance", avatarBadge = "MV", tier = "PRO", scoreValue = 4820, scoreUnit = "kcal", level = 6, trend = "UP"),
                LeaderboardEntryEntity(category = "CALORIES", rank = 3, name = "Alex Rivers", avatarBadge = "AR", tier = "ELITE", scoreValue = 4350, scoreUnit = "kcal", isCurrentUser = true, level = 8, trend = "SAME"),
                LeaderboardEntryEntity(category = "CALORIES", rank = 4, name = "Sarah Chen", avatarBadge = "SC", tier = "PRO", scoreValue = 3800, scoreUnit = "kcal", level = 7, trend = "DOWN"),

                LeaderboardEntryEntity(category = "STEPS", rank = 1, name = "Sarah Chen", avatarBadge = "SC", tier = "PRO", scoreValue = 84200, scoreUnit = "steps", level = 7, trend = "UP"),
                LeaderboardEntryEntity(category = "STEPS", rank = 2, name = "Alex Rivers", avatarBadge = "AR", tier = "ELITE", scoreValue = 71400, scoreUnit = "steps", isCurrentUser = true, level = 8, trend = "UP"),
                LeaderboardEntryEntity(category = "STEPS", rank = 3, name = "Valeriya K.", avatarBadge = "VK", tier = "TITAN", scoreValue = 68900, scoreUnit = "steps", level = 16, trend = "DOWN"),

                LeaderboardEntryEntity(category = "STREAK", rank = 1, name = "Valeriya K.", avatarBadge = "VK", tier = "TITAN", scoreValue = 42, scoreUnit = "days", level = 16, trend = "SAME"),
                LeaderboardEntryEntity(category = "STREAK", rank = 2, name = "Elena Rostova", avatarBadge = "ER", tier = "ELITE", scoreValue = 28, scoreUnit = "days", level = 11, trend = "UP"),
                LeaderboardEntryEntity(category = "STREAK", rank = 3, name = "Alex Rivers", avatarBadge = "AR", tier = "ELITE", scoreValue = 16, scoreUnit = "days", isCurrentUser = true, level = 8, trend = "UP")
            )
            gamificationDao.insertLeaderboardEntries(leaderboardData)

            // 16. Initial User Feedback & AI Adaptation
            aiAdaptationDao.insertFeedback(
                UserWorkoutFeedbackEntity(
                    rpeRating = 6,
                    soreMusclesCsv = "Legs",
                    energyLevel = 4,
                    sleepQualityRating = 4,
                    userNotes = "Quads feeling fatigued from squats, ready for progressive overload on upper body"
                )
            )

            aiAdaptationDao.insertAdaptation(
                AiAdaptationEntity(
                    readinessScore = 94,
                    fatigueScore = 28,
                    overtrainingRisk = "LOW",
                    muscleRecoveryJson = """{"Legs":55,"Chest":95,"Back":88,"Shoulders":92,"Core":90}""",
                    overallAdvice = "HRV and resting heart rate indicate strong neurological recovery. Muscle soreness localized to lower extremities. Recommending upper-body progressive overload (+8% load) while allowing quad recovery.",
                    recommendedActionType = "PROGRESSIVE_OVERLOAD",
                    modificationsJson = """[
                        {
                            "routineId": 3,
                            "routineTitle": "Upper Body Hypertrophy Sculpt",
                            "actionType": "PROGRESSIVE_OVERLOAD",
                            "intensityPercentChange": 8,
                            "setAdjustment": 0,
                            "repAdjustment": 1,
                            "weightAdjustmentKg": 2.5,
                            "restSecondsAdjustment": 0,
                            "substitutions": [],
                            "isRestDayRecommended": false,
                            "scientificRationale": "Recovery index is 94% with balanced sympathetic tone. Safe to increase intensity and resistance load by 2.5kg across compound chest and lat movements."
                        }
                    ]""",
                    recoveryTimeHours = 18,
                    isRestDayRecommended = false
                )
            )
        }
    }
}
