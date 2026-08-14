package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.wearable.HeartRateZone

@Composable
fun AppHeaderTopBar(
    title: String,
    streakDays: Int,
    subscriptionTier: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_top_bar"),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento Initials Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BentoPrimary)
                        .clickable { onOpenProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        color = BentoPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Column {
                    Text(
                        text = "Good Morning,",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (title == "ApexFit") "A-Z Fitness" else title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bento Premium Pill
                TierBadgeChip(tier = subscriptionTier, onClick = onOpenPremium)

                // Dark/Light Theme Toggle Button
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = BentoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TierBadgeChip(
    tier: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = BentoPrimaryContainer,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (tier == "FREE") Icons.Filled.LockOpen else Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = BentoPrimary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (tier.uppercase() == "PRO" || tier.uppercase() == "ELITE") tier.uppercase() else "PREMIUM",
                color = BentoPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun LiveHeartRateChip(
    bpm: Int,
    zone: HeartRateZone,
    isLive: Boolean,
    deviceName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (bpm > 100) 1.22f else 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween((60000 / bpm.coerceIn(50, 190)), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("live_heart_rate_chip"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = BentoSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BentoHeartRateRed)
                        .scale(if (isLive && bpm > 0) scale else 1.0f)
                )
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (bpm > 0) "$bpm" else "--",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "bpm",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text(
                    text = "${zone.label} • $deviceName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MultiRingProgressCard(
    caloriesCurrent: Int,
    caloriesTarget: Int,
    stepsCurrent: Int,
    stepsTarget: Int,
    activeMinsCurrent: Int,
    activeMinsTarget: Int,
    waterMlCurrent: Int,
    waterMlTarget: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("multi_ring_progress_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = BentoSurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Concentric Canvas Rings
            Box(
                modifier = Modifier.size(126.dp),
                contentAlignment = Alignment.Center
            ) {
                ConcentricFitnessRings(
                    calProgress = (caloriesCurrent.toFloat() / caloriesTarget.coerceAtLeast(1)).coerceIn(0f, 1f),
                    stepProgress = (stepsCurrent.toFloat() / stepsTarget.coerceAtLeast(1)).coerceIn(0f, 1f),
                    activeProgress = (activeMinsCurrent.toFloat() / activeMinsTarget.coerceAtLeast(1)).coerceIn(0f, 1f),
                    waterProgress = (waterMlCurrent.toFloat() / waterMlTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = BentoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "$caloriesCurrent",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend Breakdown
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricRowLegend(
                    color = BentoPrimary,
                    icon = Icons.Filled.LocalFireDepartment,
                    label = "Calories",
                    value = "$caloriesCurrent / $caloriesTarget kcal"
                )
                MetricRowLegend(
                    color = BentoPrimaryHighlight,
                    icon = Icons.Filled.DirectionsRun,
                    label = "Steps",
                    value = "$stepsCurrent / $stepsTarget"
                )
                MetricRowLegend(
                    color = ElectricOrange,
                    icon = Icons.Filled.Timer,
                    label = "Active Time",
                    value = "$activeMinsCurrent / $activeMinsTarget min"
                )
                MetricRowLegend(
                    color = WaterColor,
                    icon = Icons.Filled.WaterDrop,
                    label = "Hydration",
                    value = "$waterMlCurrent / $waterMlTarget ml"
                )
            }
        }
    }
}

@Composable
fun ConcentricFitnessRings(
    calProgress: Float,
    stepProgress: Float,
    activeProgress: Float,
    waterProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = 7.dp.toPx()
        val spacing = 10.dp.toPx()

        val rings = listOf(
            Triple(size.minDimension / 2 - strokeWidth / 2, BentoPrimary, calProgress),
            Triple(size.minDimension / 2 - strokeWidth / 2 - spacing, BentoPrimaryHighlight, stepProgress),
            Triple(size.minDimension / 2 - strokeWidth / 2 - spacing * 2, ElectricOrange, activeProgress),
            Triple(size.minDimension / 2 - strokeWidth / 2 - spacing * 3, WaterColor, waterProgress)
        )

        rings.forEach { (radius, color, progress) ->
            // Background track
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            // Foreground progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MetricRowLegend(
    color: Color,
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
        }
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RestTimerModal(
    secondsRemaining: Int,
    totalSeconds: Int,
    nextExerciseName: String?,
    onSkip: () -> Unit,
    onAdd30s: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("rest_timer_modal"),
        shape = RoundedCornerShape(28.dp),
        color = BentoSurface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "REST & RECOVERY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = BentoPrimary,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2 - 8.dp.toPx()
                    drawCircle(
                        color = BentoSurfaceVariant,
                        radius = radius,
                        style = Stroke(width = 10.dp.toPx())
                    )
                    val sweep = (secondsRemaining.toFloat() / totalSeconds.coerceAtLeast(1)) * 360f
                    drawArc(
                        color = BentoPrimary,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${secondsRemaining}s",
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "REMAINING",
                        fontSize = 11.sp,
                        color = DarkTextSecondary
                    )
                }
            }

            nextExerciseName?.let { next ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.FitnessCenter, contentDescription = null, tint = BentoPrimary)
                        Column {
                            Text("UP NEXT", fontSize = 10.sp, color = DarkTextSecondary, fontWeight = FontWeight.Bold)
                            Text(next, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAdd30s,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary)
                ) {
                    Text("+30s", color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("skip_rest_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Skip Rest", color = BentoPrimaryDark, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
