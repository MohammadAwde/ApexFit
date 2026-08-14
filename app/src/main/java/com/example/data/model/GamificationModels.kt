package com.example.data.model

enum class ChallengeCategory {
    DAILY,
    WEEKLY,
    COMMUNITY
}

enum class ChallengeMetricType {
    CALORIES,
    STEPS,
    WORKOUT_COUNT,
    TOTAL_VOLUME_KG,
    DURATION_MINUTES,
    HYDRATION_ML
}

data class FitnessChallenge(
    val id: String,
    val title: String,
    val description: String,
    val category: ChallengeCategory,
    val metricType: ChallengeMetricType,
    val targetValue: Float,
    val currentValue: Float,
    val xpReward: Int,
    val deadlineText: String,
    val isJoined: Boolean = true,
    val isClaimed: Boolean = false,
    val iconDescriptor: String = "FIRE"
) {
    val progressFraction: Float
        get() = if (targetValue > 0f) (currentValue / targetValue).coerceIn(0f, 1f) else 0f

    val isCompleted: Boolean
        get() = currentValue >= targetValue
}

enum class BadgeRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}

enum class BadgeCategory {
    STREAK,
    STRENGTH,
    CARDIO,
    CONSISTENCY,
    HYDRATION,
    RECOVERY,
    COMMUNITY
}

data class MilestoneBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val rarity: BadgeRarity,
    val category: BadgeCategory,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val xpReward: Int = 100,
    val iconEmoji: String = "🏆"
) {
    val progressFraction: Float
        get() = if (maxProgress > 0) (currentProgress.toFloat() / maxProgress).coerceIn(0f, 1f) else 0f
}

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val avatarBadge: String,
    val tier: String,
    val scoreValue: Int,
    val scoreUnit: String,
    val isCurrentUser: Boolean = false,
    val level: Int = 12,
    val trend: String = "UP" // UP, DOWN, SAME
)

data class UserGamificationStats(
    val totalXp: Int = 2450,
    val currentLevel: Int = 8,
    val xpToNextLevel: Int = 3000,
    val currentLevelXp: Int = 450,
    val totalBadgesUnlocked: Int = 7,
    val activeChallengesCount: Int = 4,
    val completedChallengesCount: Int = 12,
    val rankTitle: String = "Apex Master"
) {
    val levelProgressFraction: Float
        get() {
            val totalInLevel = xpToNextLevel - (currentLevel * 300)
            return if (totalInLevel > 0) (currentLevelXp.toFloat() / totalInLevel).coerceIn(0f, 1f) else 0.65f
        }
}
