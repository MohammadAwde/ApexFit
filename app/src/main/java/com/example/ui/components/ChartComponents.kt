package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class DayActivityData(
    val dayLabel: String,
    val calories: Int,
    val steps: Int,
    val volumeKg: Float,
    val isToday: Boolean = false
)

@Composable
fun WeeklyActivityBarChart(
    data: List<DayActivityData>,
    selectedMetric: String = "Calories", // Calories, Steps, Volume
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf(data.indexOfFirst { it.isToday }.takeIf { it >= 0 } ?: (data.size - 1)) }
    val maxVal = remember(data, selectedMetric) {
        when (selectedMetric) {
            "Steps" -> data.maxOfOrNull { it.steps }?.toFloat()?.coerceAtLeast(10000f) ?: 10000f
            "Volume" -> data.maxOfOrNull { it.volumeKg }?.coerceAtLeast(4000f) ?: 4000f
            else -> data.maxOfOrNull { it.calories }?.toFloat()?.coerceAtLeast(600f) ?: 600f
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_activity_chart"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Activity Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val selectedItem = data.getOrNull(selectedDayIndex)
                    Text(
                        text = when (selectedMetric) {
                            "Steps" -> "${selectedItem?.steps ?: 0} steps"
                            "Volume" -> "${selectedItem?.volumeKg?.toInt() ?: 0} kg lifted"
                            else -> "${selectedItem?.calories ?: 0} kcal burned"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = when (selectedMetric) {
                            "Steps" -> NeonCyan
                            "Volume" -> RoyalPurple
                            else -> CaloriesColor
                        }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = selectedMetric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Canvas Bar Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val barColor = when (selectedMetric) {
                    "Steps" -> NeonCyan
                    "Volume" -> RoyalPurple
                    else -> CaloriesColor
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, day ->
                        val value = when (selectedMetric) {
                            "Steps" -> day.steps.toFloat()
                            "Volume" -> day.volumeKg
                            else -> day.calories.toFloat()
                        }
                        val heightFraction = (value / maxVal).coerceIn(0.08f, 1f)
                        val isSelected = index == selectedDayIndex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedDayIndex = index }
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.verticalGradient(
                                                listOf(barColor, barColor.copy(alpha = 0.5f))
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(barColor.copy(alpha = 0.4f), barColor.copy(alpha = 0.15f))
                                            )
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = day.dayLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) barColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroBreakdownCard(
    caloriesCurrent: Int,
    caloriesTarget: Int,
    proteinCurrentG: Int,
    proteinTargetG: Int,
    carbsCurrentG: Int,
    carbsTargetG: Int,
    fatCurrentG: Int,
    fatTargetG: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("macro_breakdown_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Macro Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$caloriesCurrent / $caloriesTarget kcal total intake",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val remainingCals = caloriesTarget - caloriesCurrent
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (remainingCals >= 0) NeonLime.copy(alpha = 0.15f) else PulseRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (remainingCals >= 0) "$remainingCals kcal left" else "+${-remainingCals} kcal over",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (remainingCals >= 0) NeonLimeDark else PulseRed,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Macro Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroProgressBar(
                    label = "Protein",
                    current = proteinCurrentG,
                    target = proteinTargetG,
                    unit = "g",
                    color = ProteinColor
                )
                MacroProgressBar(
                    label = "Carbohydrates",
                    current = carbsCurrentG,
                    target = carbsTargetG,
                    unit = "g",
                    color = CarbsColor
                )
                MacroProgressBar(
                    label = "Fats",
                    current = fatCurrentG,
                    target = fatTargetG,
                    unit = "g",
                    color = FatColor
                )
            }
        }
    }
}

@Composable
fun MacroProgressBar(
    label: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val progress = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$current / $target $unit (${(progress * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun WeightTrendLineChart(
    weights: List<Pair<String, Float>>,
    targetWeight: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weight_trend_chart"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weight & Body Composition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current: ${weights.lastOrNull()?.second ?: 76.5f} kg • Target: $targetWeight kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val minW = (weights.minOfOrNull { it.second } ?: 70f) - 1.5f
                    val maxW = (weights.maxOfOrNull { it.second } ?: 80f) + 1.5f
                    val range = (maxW - minW).coerceAtLeast(1f)

                    val stepX = size.width / (weights.size - 1).coerceAtLeast(1)

                    // Target dotted line
                    val targetY = size.height - ((targetWeight - minW) / range) * size.height
                    drawLine(
                        color = NeonLime.copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(size.width, targetY),
                        strokeWidth = 2.dp.toPx()
                    )

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    weights.forEachIndexed { index, pair ->
                        val x = index * stepX
                        val y = size.height - ((pair.second - minW) / range) * size.height
                        points.add(Offset(x, y))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    // Stroke
                    drawPath(
                        path = path,
                        color = NeonCyan,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Points
                    points.forEach { pt ->
                        drawCircle(color = NeonCyan, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = DarkBackground, radius = 2.5.dp.toPx(), center = pt)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weights.forEach { (label, _) ->
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
