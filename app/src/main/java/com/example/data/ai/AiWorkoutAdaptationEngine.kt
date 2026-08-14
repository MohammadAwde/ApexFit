package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiWorkoutAdaptationEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Synthesizes an intelligent, personalized workout adaptation based on:
     * - User Workout History (volume, intensity, frequency, perceived fatigue)
     * - Wearable Health Telemetry (recovery score, sleep, resting HR, strain)
     * - Direct User Feedback (RPE difficulty rating 1-10, muscle soreness, energy)
     */
    fun analyzeAndAdaptWorkouts(
        routines: List<WorkoutRoutineEntity>,
        recentLogs: List<WorkoutLogEntity>,
        activeWearable: WearableDeviceEntity?,
        feedback: UserWorkoutFeedback
    ): AiAdaptationAnalysis {
        // 1. Wearable signals
        val recoveryScore = activeWearable?.recoveryScore ?: 88
        val sleepScore = activeWearable?.sleepScore ?: 84
        val sleepHours = activeWearable?.sleepHours ?: 7.5f
        val restingHr = activeWearable?.restingHeartRateBpm ?: 54

        // 2. Workout history signals
        val last3DaysLogs = recentLogs.take(4)
        val recentVolumeTotal = last3DaysLogs.sumOf { it.totalVolumeKg.toDouble() }.toFloat()
        val recentAvgHr = if (last3DaysLogs.isNotEmpty()) last3DaysLogs.map { it.avgHeartRate }.average().toInt() else 140
        val recentHighIntensityCount = last3DaysLogs.count { it.avgHeartRate > 155 || it.caloriesBurned > 400 }

        // 3. User feedback signals
        val rpe = feedback.rpeRating.coerceIn(1, 10)
        val soreMuscles = feedback.soreMuscles
        val energy = feedback.energyLevel.coerceIn(1, 5)

        // 4. Calculate Physiological Readiness & Fatigue
        // Base readiness from wearable + sleep
        var readiness = (recoveryScore * 0.45f + sleepScore * 0.25f + (energy * 20f) * 0.30f).toInt().coerceIn(10, 99)
        // High RPE and soreness penalize readiness
        if (rpe >= 8) readiness -= 15
        if (rpe >= 9) readiness -= 10
        if (soreMuscles.size >= 3) readiness -= 12
        if (recentHighIntensityCount >= 3) readiness -= 10

        val computedReadiness = readiness.coerceIn(15, 98)
        val computedFatigue = (100 - computedReadiness).coerceIn(5, 95)

        // 5. Determine Overtraining Risk
        val overtrainingRisk = when {
            computedFatigue >= 75 || (recoveryScore < 45 && rpe >= 8) -> OvertrainingRiskLevel.HIGH_RISK
            computedFatigue >= 55 || soreMuscles.size >= 4 -> OvertrainingRiskLevel.ELEVATED
            computedFatigue >= 35 -> OvertrainingRiskLevel.LOW
            else -> OvertrainingRiskLevel.MINIMAL
        }

        // 6. Muscle Recovery Matrix calculation
        val muscleRecovery = mutableMapOf(
            "Chest" to 92,
            "Back" to 88,
            "Legs" to 85,
            "Shoulders" to 90,
            "Core" to 94,
            "Arms" to 91
        )

        // Adjust based on user reported soreness
        soreMuscles.forEach { muscle ->
            when {
                muscle.contains("Leg", ignoreCase = true) || muscle.contains("Knee", ignoreCase = true) -> muscleRecovery["Legs"] = (35 + (energy * 5)).coerceAtMost(55)
                muscle.contains("Chest", ignoreCase = true) -> muscleRecovery["Chest"] = (40 + (energy * 4)).coerceAtMost(60)
                muscle.contains("Back", ignoreCase = true) || muscle.contains("Spine", ignoreCase = true) -> muscleRecovery["Back"] = (38 + (energy * 5)).coerceAtMost(58)
                muscle.contains("Shoulder", ignoreCase = true) -> muscleRecovery["Shoulders"] = (42 + (energy * 4)).coerceAtMost(62)
                muscle.contains("Arm", ignoreCase = true) -> muscleRecovery["Arms"] = (45 + (energy * 5)).coerceAtMost(65)
                muscle.contains("Core", ignoreCase = true) -> muscleRecovery["Core"] = (48 + (energy * 5)).coerceAtMost(68)
            }
        }

        // 7. Decide Primary Action Type
        val actionType = when {
            overtrainingRisk == OvertrainingRiskLevel.HIGH_RISK -> AdaptationActionType.COMPLETE_REST_DAY
            overtrainingRisk == OvertrainingRiskLevel.ELEVATED -> AdaptationActionType.ACTIVE_RECOVERY_DAY
            soreMuscles.isNotEmpty() && soreMuscles.any { it.contains("Knee") || it.contains("Shoulder") || it.contains("Back") } -> AdaptationActionType.EXERCISE_SUBSTITUTION
            computedReadiness >= 82 && rpe <= 5 -> AdaptationActionType.PROGRESSIVE_OVERLOAD
            computedFatigue >= 50 || rpe >= 7 -> AdaptationActionType.DELOAD_VOLUME_REDUCTION
            else -> AdaptationActionType.PROGRESSIVE_OVERLOAD
        }

        // 8. Generate Specific Modifications for available routines
        val modifications = mutableListOf<WorkoutModification>()

        routines.forEach { routine ->
            val isLegRoutine = routine.title.contains("Squat", true) || routine.targetMuscleGroups.contains("Legs", true)
            val isUpperRoutine = routine.title.contains("Upper", true) || routine.title.contains("Chest", true)
            val isHiit = routine.category.equals("HIIT", true)

            when (actionType) {
                AdaptationActionType.PROGRESSIVE_OVERLOAD -> {
                    modifications.add(
                        WorkoutModification(
                            routineId = routine.id,
                            routineTitle = routine.title,
                            actionType = AdaptationActionType.PROGRESSIVE_OVERLOAD,
                            intensityPercentChange = +8,
                            setAdjustment = 0,
                            repAdjustment = if (routine.category == "Strength") 0 else 2,
                            weightAdjustmentKg = if (routine.category == "Strength") 2.5f else 0f,
                            restSecondsAdjustment = -10,
                            substitutions = emptyList(),
                            isRestDayRecommended = false,
                            scientificRationale = "High recovery score (${recoveryScore}%) and optimal readiness indicate peak CNS freshness. Applied +2.5kg progressive overload on compound lifts to stimulate hypertrophy and strength adaptations."
                        )
                    )
                }

                AdaptationActionType.DELOAD_VOLUME_REDUCTION -> {
                    modifications.add(
                        WorkoutModification(
                            routineId = routine.id,
                            routineTitle = routine.title,
                            actionType = AdaptationActionType.DELOAD_VOLUME_REDUCTION,
                            intensityPercentChange = -15,
                            setAdjustment = -1,
                            repAdjustment = 0,
                            weightAdjustmentKg = -5.0f,
                            restSecondsAdjustment = +20,
                            substitutions = emptyList(),
                            isRestDayRecommended = false,
                            scientificRationale = "Fatigue score is elevated (${computedFatigue}%). Reducing total working sets by 1 set per movement and backing off load by 15% to clear metabolic waste while maintaining motor recruitment."
                        )
                    )
                }

                AdaptationActionType.EXERCISE_SUBSTITUTION -> {
                    val subs = mutableListOf<ExerciseSubstitution>()
                    if (soreMuscles.any { it.contains("Leg", true) || it.contains("Knee", true) }) {
                        subs.add(
                            ExerciseSubstitution(
                                originalExerciseName = "Barbell Back Squats",
                                substituteExerciseName = "Leg Press / Bulgarian Split Squats",
                                targetMuscle = "Legs",
                                reason = "Reduces spinal compression and sheer force on knee patella while targeting quads safely.",
                                modifiedSets = 3,
                                modifiedReps = 12,
                                modifiedWeightKg = 60f,
                                safetyTip = "Maintain slow 3-second eccentric tempo"
                            )
                        )
                    }
                    if (soreMuscles.any { it.contains("Shoulder", true) || it.contains("Chest", true) }) {
                        subs.add(
                            ExerciseSubstitution(
                                originalExerciseName = "Dumbbell Overhead Press",
                                substituteExerciseName = "Incline Cable Lateral Raises",
                                targetMuscle = "Shoulders",
                                reason = "Prevents subacromial shoulder impingement while maintaining lateral delt tension.",
                                modifiedSets = 3,
                                modifiedReps = 15,
                                modifiedWeightKg = 10f,
                                safetyTip = "Keep arm slightly forward in scapular plane"
                            )
                        )
                    }
                    if (soreMuscles.any { it.contains("Back", true) || it.contains("Spine", true) }) {
                        subs.add(
                            ExerciseSubstitution(
                                originalExerciseName = "Overhand Barbell Rows",
                                substituteExerciseName = "Chest-Supported Dumbbell Rows",
                                targetMuscle = "Back",
                                reason = "Eliminates lower back lumbar shear load while providing maximum lat isolation.",
                                modifiedSets = 4,
                                modifiedReps = 10,
                                modifiedWeightKg = 22f,
                                safetyTip = "Chest firm against incline bench"
                            )
                        )
                    }

                    modifications.add(
                        WorkoutModification(
                            routineId = routine.id,
                            routineTitle = routine.title,
                            actionType = AdaptationActionType.EXERCISE_SUBSTITUTION,
                            intensityPercentChange = -5,
                            setAdjustment = 0,
                            repAdjustment = 0,
                            weightAdjustmentKg = 0f,
                            restSecondsAdjustment = +15,
                            substitutions = subs,
                            isRestDayRecommended = false,
                            scientificRationale = "Biomechanical joint-friendly substitutions selected based on your reported soreness areas (${soreMuscles.joinToString(", ")}). Preserves target stimulus while protecting fatigued stabilizers."
                        )
                    )
                }

                AdaptationActionType.ACTIVE_RECOVERY_DAY -> {
                    modifications.add(
                        WorkoutModification(
                            routineId = routine.id,
                            routineTitle = routine.title,
                            actionType = AdaptationActionType.ACTIVE_RECOVERY_DAY,
                            intensityPercentChange = -50,
                            setAdjustment = -2,
                            repAdjustment = 0,
                            weightAdjustmentKg = 0f,
                            restSecondsAdjustment = +45,
                            substitutions = emptyList(),
                            isRestDayRecommended = true,
                            suggestedAlternativeRoutineTitle = "Dynamic Flow & Joint Recovery",
                            scientificRationale = "Elevated systemic fatigue detected from ${recentLogs.size} recent high-intensity sessions. Switching today to light joint mobility and zone 1 recovery flow to accelerate lymphatic clearance."
                        )
                    )
                }

                AdaptationActionType.COMPLETE_REST_DAY -> {
                    modifications.add(
                        WorkoutModification(
                            routineId = routine.id,
                            routineTitle = routine.title,
                            actionType = AdaptationActionType.COMPLETE_REST_DAY,
                            intensityPercentChange = -100,
                            setAdjustment = 0,
                            repAdjustment = 0,
                            weightAdjustmentKg = 0f,
                            restSecondsAdjustment = 0,
                            substitutions = emptyList(),
                            isRestDayRecommended = true,
                            suggestedAlternativeRoutineTitle = "Full Rest & Supercompensation Day",
                            scientificRationale = "High overtraining risk flagged. HRV/Recovery score is low (${recoveryScore}%) and RPE rating is elevated. Taking a full rest day today prevents chronic overtraining syndrome and tendon strain."
                        )
                    )
                }
            }
        }

        val overallAdvice = when (actionType) {
            AdaptationActionType.PROGRESSIVE_OVERLOAD -> "🚀 Peak Readiness (${computedReadiness}%): Your nervous system and biometric recovery are in an optimal state. AI recommends advancing progressive overload (+2.5kg to +5kg) on your primary lifts today!"
            AdaptationActionType.DELOAD_VOLUME_REDUCTION -> "⚡ Deload Optimization: Moderate fatigue accumulated. AI has trimmed 1 set per exercise and reduced working load by 15% to maintain adaptation momentum without inducing overtraining."
            AdaptationActionType.EXERCISE_SUBSTITUTION -> "🛡️ Joint & Muscle Protection: AI adjusted your exercise selection for ${soreMuscles.joinToString(", ")} to bypass joint stress while maximizing hypertrophy efficiency."
            AdaptationActionType.ACTIVE_RECOVERY_DAY -> "🧘 Active Recovery Recommended: Your biometric strain is elevated. AI suggests swapping high-impact lifts for 'Dynamic Flow & Joint Recovery' today to accelerate muscle tissue repair."
            AdaptationActionType.COMPLETE_REST_DAY -> "🛑 Full Rest & Rejuvenation: High fatigue detected (${computedFatigue}%). AI advises taking a dedicated rest day with high protein and hydration to optimize supercompensation."
        }

        return AiAdaptationAnalysis(
            readinessScore = computedReadiness,
            fatigueScore = computedFatigue,
            overtrainingRisk = overtrainingRisk,
            muscleRecoveryMap = muscleRecovery,
            overallAdvice = overallAdvice,
            recommendedActionType = actionType,
            modifications = modifications,
            recoveryTimeHours = if (computedReadiness > 75) 24 else if (computedReadiness > 50) 36 else 48,
            isRestDayRecommended = actionType == AdaptationActionType.ACTIVE_RECOVERY_DAY || actionType == AdaptationActionType.COMPLETE_REST_DAY
        )
    }

    /**
     * Optional cloud Gemini analysis call via REST with prompt summarizing user history.
     */
    suspend fun requestGeminiAiDeepAnalysis(
        analysis: AiAdaptationAnalysis,
        feedback: UserWorkoutFeedback
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext analysis.overallAdvice
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val prompt = """
                You are ApexFit Elite AI Sports Science Coach. Analyze this athlete's data and provide a concise 2-sentence actionable coaching recommendation:
                - Readiness Score: ${analysis.readinessScore}%
                - Fatigue Score: ${analysis.fatigueScore}%
                - Overtraining Risk: ${analysis.overtrainingRisk}
                - User RPE Feedback: ${feedback.rpeRating}/10
                - Sore Muscles: ${feedback.soreMuscles.joinToString(", ")}
                - Recommended Action: ${analysis.recommendedActionType}
                Keep it motivational, scientific, and clear.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val rootJson = JSONObject(respBody)
                val text = rootJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""
                if (text.isNotBlank()) return@withContext text.trim()
            }
        } catch (_: Exception) {
            // Fallback to local deterministic analysis
        }

        return@withContext analysis.overallAdvice
    }
}
