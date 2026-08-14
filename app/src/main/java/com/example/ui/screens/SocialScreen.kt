package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocialPostEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.*

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val score: String,
    val badge: String,
    val isCurrentUser: Boolean = false
)

@Composable
fun SocialScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val socialPosts by viewModel.socialPosts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Feed, 1 = Leaderboard
    var showSharePostDialog by remember { mutableStateOf(false) }

    val leaderboard = listOf(
        LeaderboardUser(1, "Marcus Vance", "6,420 kcal", "👑 ELITE"),
        LeaderboardUser(2, userProfile?.name ?: "Alex Rivers (You)", "5,890 kcal", "🔥 STREAK 14", isCurrentUser = true),
        LeaderboardUser(3, "Elena Rostova", "5,410 kcal", "⚡ PRO"),
        LeaderboardUser(4, "David Kim", "4,980 kcal", "🎯 PRO"),
        LeaderboardUser(5, "Sarah Jenkins", "4,620 kcal", "🏃 ATHLETE")
    )

    Scaffold(
        modifier = modifier.testTag("social_screen"),
        floatingActionButton = {
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs: Community Feed vs Leaderboard
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Community Feed", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Weekly Leaderboard", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Feed list
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
            } else {
                // Leaderboard list
                item {
                    Text(
                        text = "WEEKLY CALORIE BURN PODIUM 🏅",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                items(leaderboard) { user ->
                    LeaderboardUserCard(user = user)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
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
                            .background(
                                Brush.linearGradient(listOf(NeonCyan, RoyalPurple))
                            ),
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
                            Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
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
                        Text(timeFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                IconButton(onClick = onShareExternal) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            // Headline & Achievement Badge
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                post.achievementBadge?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonLime.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = NeonLimeDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Workout Summary banner
            post.workoutSummary?.let { summary ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Text(summary, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                        tint = if (post.isKudosGiven) PulseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.kudosCount} Kudos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isKudosGiven) PulseRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${post.commentsCount} comments",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardUserCard(user: LeaderboardUser) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser) RoyalPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (user.isCurrentUser) CardDefaults.outlinedCardBorder() else null
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (user.rank) {
                                1 -> GoldStar
                                2 -> NeonCyan
                                3 -> ElectricOrange
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${user.rank}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (user.rank <= 3) DarkBackground else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isCurrentUser) NeonLimeDark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = user.score,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = if (user.rank == 1) GoldStar else MaterialTheme.colorScheme.onSurface
            )
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
        title = { Text("Share Workout Milestone", fontWeight = FontWeight.Black) },
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
                enabled = title.isNotBlank()
            ) {
                Text("Publish to Community")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
