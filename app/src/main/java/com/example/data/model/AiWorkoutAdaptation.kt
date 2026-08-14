package com.example.data.model

enum class AdaptationActionType {
    PROGRESSIVE_OVERLOAD,   // High recovery + low RPE -> Increase intensity/load
    DELOAD_VOLUME_REDUCTION,// Elevated fatigue -> Decrease sets or weight
    EXERCISE_SUBSTITUTION,  // Joint/muscle soreness -> Biomechanical swaps
    ACTIVE_RECOVERY_DAY,    // Mild overtraining risk -> Low-impact mobility
    COMPLETE_REST_DAY       // High overtraining risk -> CNS rest & recovery
}

enum class OvertrainingRiskLevel {
    MINIMAL,
    LOW,
    ELEVATED,
    HIGH_RISK
}

data class UserWorkoutFeedback(
    val rpeRating: Int = 5, // 1 (Very Light) to 10 (Maximal Effort)
    val soreMuscles: Set<String> = emptySet(), // Legs, Lower Back, Chest, Shoulders, Arms, Knees/Joints
    val energyLevel: Int = 4, // 1 to 5
    val sleepQualityRating: Int = 4, // 1 to 5
    val userNotes: String = ""
)

data class ExerciseSubstitution(
    val originalExerciseName: String,
    val substituteExerciseName: String,
    val targetMuscle: String,
    val reason: String,
    val modifiedSets: Int,
    val modifiedReps: Int,
    val modifiedWeightKg: Float,
    val safetyTip: String
)

data class WorkoutModification(
    val routineId: Long,
    val routineTitle: String,
    val actionType: AdaptationActionType,
    val intensityPercentChange: Int, // e.g. +8% or -15%
    val setAdjustment: Int, // e.g. +0, -1
    val repAdjustment: Int, // e.g. +2, -2
    val weightAdjustmentKg: Float, // e.g. +2.5f or -5.0f
    val restSecondsAdjustment: Int, // e.g. -15 or +30
    val substitutions: List<ExerciseSubstitution> = emptyList(),
    val isRestDayRecommended: Boolean = false,
    val suggestedAlternativeRoutineTitle: String? = null,
    val scientificRationale: String
)

data class AiAdaptationAnalysis(
    val readinessScore: Int, // 0-100%
    val fatigueScore: Int, // 0-100%
    val overtrainingRisk: OvertrainingRiskLevel,
    val muscleRecoveryMap: Map<String, Int>, // "Legs" -> 45, "Chest" -> 90, "Back" -> 85, etc.
    val overallAdvice: String,
    val recommendedActionType: AdaptationActionType,
    val modifications: List<WorkoutModification>,
    val recoveryTimeHours: Int,
    val isRestDayRecommended: Boolean,
    val generatedTimestamp: Long = System.currentTimeMillis()
)
