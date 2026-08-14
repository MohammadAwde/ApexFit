package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BadgeCelebrationDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SocialScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val socialPosts by viewModel.socialPosts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val badges by viewModel.milestoneBadges.collectAsState()
    val gamificationStats by viewModel.gamificationStats.collectAsState()
    val leaderboardEntries by viewModel.leaderboardEntries.collectAsState()
    val selectedCategory by viewModel.selectedLeaderboardCategory.collectAsState()
    val newlyUnlockedBadge by viewModel.newlyUnlockedBadge.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Quests, 1 = Leaderboard & Badges, 2 = Feed
    var showSharePostDialog by remember { mutableStateOf(false) }
    var selectedBadgeDetail by remember { mutableStateOf<MilestoneBadge?>(null) }

    // Dialog for celebration
    newlyUnlockedBadge?.let { badge ->
        BadgeCelebrationDialog(
            badge = badge,
            onDismiss = { viewModel.dismissBadgeCelebration() }
        )
    }

    selectedBadgeDetail?.let { badge ->
        BadgeCelebrationDialog(
            badge = badge,
            onDismiss = { selectedBadgeDetail = null }
        )
    }

    Scaffold(
        modifier = modifier.testTag("social_screen"),
        containerColor = BentoBackground,
        floatingActionButton = {
            if (selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showSharePostDialog = true },
                    containerColor = RoyalPurple,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Filled.AddComment, contentDescription = "Share Post") },
                    text = { Text("Share Milestone", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .testTag("share_milestone_fab")
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gamification Level & XP Banner
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricGreen, NeonCyan))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(ElectricGreen, NeonCyan))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⚡", fontSize = 22.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "LEVEL ${gamificationStats.currentLevel} • ${gamificationStats.rankTitle.uppercase()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ElectricGreen,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "${gamificationStats.totalXp} Total XP",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoTextPrimary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BentoSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${gamificationStats.totalBadgesUnlocked} Badges",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Level Progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Next Rank: Level ${gamificationStats.currentLevel + 1}", fontSize = 11.sp, color = BentoMutedText)
                            Text("${gamificationStats.currentLevelXp} / 350 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { gamificationStats.levelProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectricGreen,
                            trackColor = BentoSurface
                        )
                    }
                }
            }

            // Gamification 3-Tab Navigator (Quests, Leaderboard & Badges, Feed)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BentoSurface,
                    contentColor = ElectricGreen,
                    modifier = Modifier.clip(RoundedCornerShape(18.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Quests", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("tab_quests")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Leaderboard & Badges", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("tab_leaderboard")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Feed", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("tab_feed")
                    )
                }
            }

            // TAB 0: CHALLENGES & QUESTS
            if (selectedTab == 0) {
                // Daily Quests Section
                item {
                    Text(
                        "DAILY FITNESS QUESTS ⚡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                }

                val dailyChallenges = challenges.filter { it.category == ChallengeCategory.DAILY }
                items(dailyChallenges) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onJoin = { viewModel.joinChallenge(challenge.id) },
                        onClaim = { viewModel.claimChallengeReward(challenge.id) }
                    )
                }

                // Weekly Mastery Challenges Section
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "WEEKLY ENDURANCE & STRENGTH CHALLENGES 🏆",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        letterSpacing = 1.sp
                    )
                }

                val weeklyChallenges = challenges.filter { it.category == ChallengeCategory.WEEKLY || it.category == ChallengeCategory.COMMUNITY }
                items(weeklyChallenges) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onJoin = { viewModel.joinChallenge(challenge.id) },
                        onClaim = { viewModel.claimChallengeReward(challenge.id) }
                    )
                }
            }

            // TAB 1: LEADERBOARD & TROPHY CABINET
            else if (selectedTab == 1) {
                // Leaderboard Category Filter Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "COMMUNITY LEADERBOARD 🏅",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricGreen,
                            letterSpacing = 1.sp
                        )

                        val categories = listOf("XP", "Calories", "Steps", "Streak")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ElectricGreen else BentoSurface,
                                    border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight))),
                                    modifier = Modifier
                                        .clickable { viewModel.switchLeaderboardCategory(cat) }
                                        .testTag("leaderboard_cat_$cat")
                                ) {
                                    Text(
                                        cat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else BentoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Top 3 Podium
                item {
                    PodiumView(leaderboardEntries.take(3))
                }

                // Rest of Leaderboard
                items(leaderboardEntries.drop(3)) { entry ->
                    LeaderboardEntryRow(entry = entry)
                }

                // Milestone Badges Showcase / Trophy Cabinet
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TROPHY CABINET & MILESTONE BADGES 👑",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.sp
                        )
                        Text(
                            "${badges.count { it.isUnlocked }}/${badges.size} Unlocked",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMutedText
                        )
                    }
                }

                // Badges Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        badges.chunked(2).forEach { rowBadges ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowBadges.forEach { badge ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        BadgeGridCard(
                                            badge = badge,
                                            onClick = { selectedBadgeDetail = badge }
                                        )
                                    }
                                }
                                if (rowBadges.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: COMMUNITY FEED
            else {
                items(socialPosts) { post ->
                    SocialPostCard(
                        post = post,
                        onToggleKudos = { viewModel.toggleKudos(post.id) },
                        onShareExternal = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "⚡ ${post.authorName} just shared on ApexFit: \"${post.title}\" - ${post.content} #ApexFit #Fitness")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Workout Milestone"))
                        }
                    )
                }
            }
        }
    }

    if (showSharePostDialog) {
        ShareMilestoneDialog(
            onDismiss = { showSharePostDialog = false },
            onPost = { title, notes, badge ->
                viewModel.shareWorkoutToCommunity(
                    title = title,
                    notes = notes,
                    achievementBadge = badge
                )
                showSharePostDialog = false
            }
        )
    }
}

@Composable
fun ChallengeCard(
    challenge: FitnessChallenge,
    onJoin: () -> Unit,
    onClaim: () -> Unit
) {
    val isCompleted = challenge.isCompleted
    val isClaimed = challenge.isClaimed

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isCompleted && !isClaimed) listOf(ElectricGreen, NeonCyan)
                else listOf(BentoBorder, BentoBorderLight)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("challenge_card_${challenge.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (challenge.iconDescriptor) {
                                    "FIRE" -> Color(0xFFFF5252).copy(alpha = 0.2f)
                                    "RUN" -> NeonCyan.copy(alpha = 0.2f)
                                    "WATER" -> Color(0xFF00B0FF).copy(alpha = 0.2f)
                                    "STRENGTH" -> RoyalPurple.copy(alpha = 0.2f)
                                    "HEART" -> Color(0xFFFF4081).copy(alpha = 0.2f)
                                    else -> ElectricGreen.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (challenge.iconDescriptor) {
                                "FIRE" -> "🔥"
                                "RUN" -> "🏃"
                                "WATER" -> "💧"
                                "STRENGTH" -> "🏋️"
                                "HEART" -> "❤️"
                                else -> "🌍"
                            },
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextPrimary)
                        Text(challenge.deadlineText, fontSize = 11.sp, color = BentoMutedText)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Stars, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("+${challenge.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ElectricGreen)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(challenge.description, fontSize = 12.sp, color = BentoTextSecondary, lineHeight = 16.sp)

            Spacer(Modifier.height(12.dp))

            // Progress bar and numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${challenge.currentValue.toInt()} / ${challenge.targetValue.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    "${(challenge.progressFraction * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) ElectricGreen else NeonCyan
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { challenge.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) ElectricGreen else NeonCyan,
                trackColor = BentoSurfaceElevated
            )

            // Claim / Action Button
            Spacer(Modifier.height(12.dp))
            if (isCompleted && !isClaimed) {
                Button(
                    onClick = onClaim,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("claim_challenge_btn_${challenge.id}")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Claim +${challenge.xpReward} XP Reward!", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            } else if (isClaimed) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BentoSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reward Claimed (+${challenge.xpReward} XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoMutedText)
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumView(topThree: List<LeaderboardEntry>) {
    if (topThree.size < 3) return

    val first = topThree.getOrNull(0) ?: return
    val second = topThree.getOrNull(1) ?: return
    val third = topThree.getOrNull(2) ?: return

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                PodiumColumn(entry = second, rank = 2, height = 90.dp, color = Color(0xFFC0C0C0), crown = "🥈")
                // 1st Place
                PodiumColumn(entry = first, rank = 1, height = 120.dp, color = Color(0xFFFFD700), crown = "👑")
                // 3rd Place
                PodiumColumn(entry = third, rank = 3, height = 75.dp, color = Color(0xFFCD7F32), crown = "🥉")
            }
        }
    }
}

@Composable
fun PodiumColumn(entry: LeaderboardEntry, rank: Int, height: androidx.compose.ui.unit.Dp, color: Color, crown: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Text(crown, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.3f), BentoSurfaceElevated))),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.avatarBadge, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            entry.name.split(" ").firstOrNull() ?: entry.name,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = BentoTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${entry.scoreValue} ${entry.scoreUnit}",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = color
        )
        Spacer(Modifier.height(6.dp))

        // Step Pillar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.25f), BentoSurfaceElevated)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("#$rank", fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun LeaderboardEntryRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) RoyalPurple.copy(alpha = 0.15f) else BentoSurface
        ),
        border = if (entry.isCurrentUser) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RoyalPurple, NeonCyan))) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "#${entry.rank}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = BentoMutedText
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BentoSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(entry.avatarBadge, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BentoTextPrimary
                        )
                        if (entry.isCurrentUser) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ElectricGreen.copy(alpha = 0.2f)
                            ) {
                                Text("YOU", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ElectricGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text("Level ${entry.level} • ${entry.tier}", fontSize = 11.sp, color = BentoMutedText)
                }
            }

            Text(
                "${entry.scoreValue} ${entry.scoreUnit}",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = if (entry.isCurrentUser) ElectricGreen else BentoTextPrimary
            )
        }
    }
}

@Composable
fun BadgeGridCard(
    badge: MilestoneBadge,
    onClick: () -> Unit
) {
    val rarityColor = when (badge.rarity) {
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700)
        BadgeRarity.EPIC -> RoyalPurple
        BadgeRarity.RARE -> NeonCyan
        BadgeRarity.COMMON -> ElectricGreen
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (badge.isUnlocked) Brush.horizontalGradient(listOf(rarityColor, BentoBorder)) else Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) Brush.radialGradient(listOf(rarityColor.copy(alpha = 0.35f), BentoSurfaceElevated))
                        else Brush.radialGradient(listOf(BentoSurfaceElevated, BentoSurface))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.iconEmoji,
                    fontSize = 24.sp,
                    modifier = Modifier.then(if (!badge.isUnlocked) Modifier.alpha(0.4f) else Modifier)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) BentoTextPrimary else BentoMutedText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                if (badge.isUnlocked) (badge.unlockedDate ?: "Unlocked") else "${badge.currentProgress}/${badge.maxProgress}",
                fontSize = 10.sp,
                color = if (badge.isUnlocked) ElectricGreen else BentoMutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SocialPostCard(
    post: SocialPostEntity,
    onToggleKudos: () -> Unit,
    onShareExternal: () -> Unit
) {
    val timeFormatted = remember(post.timestamp) {
        val diff = System.currentTimeMillis() - post.timestamp
        when {
            diff < 60 * 1000 -> "Just now"
            diff < 3600 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 3600 * 1000 -> "${diff / (3600 * 1000)}h ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(post.timestamp))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BentoBorder, BentoBorderLight)))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonCyan, RoyalPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorAvatarBadge.ifBlank { post.authorName.take(2) }.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextPrimary)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (post.authorTier == "ELITE") GoldStar.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = post.authorTier,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (post.authorTier == "ELITE") GoldStar else NeonCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(timeFormatted, fontSize = 11.sp, color = BentoMutedText)
                    }
                }

                IconButton(onClick = onShareExternal) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = BentoMutedText, modifier = Modifier.size(18.dp))
                }
            }

            // Headline & Achievement Badge
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = BentoTextPrimary
                )
                post.achievementBadge?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = ElectricGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = BentoTextSecondary
            )

            // Workout Summary banner
            post.workoutSummary?.let { summary ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BentoSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Text(summary, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }
                }
            }

            // Kudos & Comments footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleKudos() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (post.isKudosGiven) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Kudos",
                        tint = if (post.isKudosGiven) PulseRed else BentoMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.kudosCount} Kudos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isKudosGiven) PulseRed else BentoMutedText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = BentoMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${post.commentsCount} comments",
                        fontSize = 12.sp,
                        color = BentoMutedText
                    )
                }
            }
        }
    }
}

@Composable
fun ShareMilestoneDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var badge by remember { mutableStateOf("100kg Squat Milestone 🏆") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Workout Milestone", fontWeight = FontWeight.Black, color = BentoTextPrimary) },
        containerColor = BentoSurface,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Headline (e.g. New PR on Deadlifts!)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("What did you achieve today?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                OutlinedTextField(
                    value = badge,
                    onValueChange = { badge = it },
                    label = { Text("Achievement Badge Tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onPost(title.trim(), notes.trim(), badge.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = Color.Black),
                enabled = title.isNotBlank()
            ) {
                Text("Publish to Community", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = BentoMutedText) }
        }
    )
}
