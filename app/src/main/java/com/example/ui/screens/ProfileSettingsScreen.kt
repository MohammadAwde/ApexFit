package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TierBadgeChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun ProfileSettingsScreen(
    viewModel: FitnessViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onOpenPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var notificationSentMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Header & Tier Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(NeonCyan, RoyalPurple))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (userProfile?.name?.take(2) ?: "AR").uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = userProfile?.name ?: "Alex Rivers",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = userProfile?.email ?: "alex.rivers@fitness.ai",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TierBadgeChip(
                            tier = userProfile?.subscriptionTier ?: "PRO",
                            onClick = onOpenPremium
                        )
                    }

                    // Biometrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BiometricPill(label = "WEIGHT", value = "${userProfile?.weightKg ?: 76.5f} kg")
                        BiometricPill(label = "TARGET", value = "${userProfile?.targetWeightKg ?: 73.0f} kg")
                        BiometricPill(label = "HEIGHT", value = "${userProfile?.heightCm?.toInt() ?: 182} cm")
                        BiometricPill(label = "STREAK", value = "${userProfile?.streakDays ?: 14} days")
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Goals & Biometrics", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Offline Mode & Architecture Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonLime.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CloudDone, contentDescription = null, tint = NeonLimeDark, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            text = "Offline Mode Enabled",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100% full functionality without internet using Room Database local cache.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Theme Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "APPEARANCE & THEME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleDarkTheme() }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = null,
                                tint = NeonCyan
                            )
                            Column {
                                Text("Dark Mode Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(if (isDarkTheme) "High-contrast athletic dark palette" else "Clean modern light palette", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                }
            }
        }

        // 4. Push Notifications Motivation Studio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PUSH NOTIFICATIONS & MOTIVATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Test instant motivational triggers and scheduled reminders:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotificationTriggerButton(
                            title = "Daily Motivation Quote 🔥",
                            subtitle = "Inspiring athlete quotes to maintain streak",
                            onClick = {
                                viewModel.triggerTestNotification("motivation")
                                notificationSentMessage = "Sent Daily Motivation Notification!"
                            }
                        )
                        NotificationTriggerButton(
                            title = "Scheduled Workout Reminder 🏋️",
                            subtitle = "Alert for today's routine session",
                            onClick = {
                                viewModel.triggerTestNotification("workout")
                                notificationSentMessage = "Sent Workout Reminder!"
                            }
                        )
                        NotificationTriggerButton(
                            title = "Hydration Check Reminder 💧",
                            subtitle = "Stay hydrated and hit 3,000ml goal",
                            onClick = {
                                viewModel.triggerTestNotification("hydration")
                                notificationSentMessage = "Sent Hydration Reminder!"
                            }
                        )
                        NotificationTriggerButton(
                            title = "Milestone & PR Celebration 🏆",
                            subtitle = "Celebrate new trophies and streak records",
                            onClick = {
                                viewModel.triggerTestNotification("achievement")
                                notificationSentMessage = "Sent Milestone Celebration Notification!"
                            }
                        )
                    }

                    notificationSentMessage?.let { msg ->
                        Text(
                            text = "✓ $msg",
                            color = NeonLimeDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        userProfile?.let { prof ->
            EditProfileGoalsDialog(
                current = prof,
                onDismiss = { showEditProfileDialog = false },
                onSave = { updated ->
                    viewModel.updateProfileInfo(
                        height = updated.heightCm,
                        weight = updated.weightKg,
                        targetWeight = updated.targetWeightKg,
                        dailyCals = updated.dailyCalorieGoal,
                        stepGoal = updated.dailyStepGoal,
                        waterGoal = updated.dailyWaterGoalMl
                    )
                    showEditProfileDialog = false
                }
            )
        }
    }
}

@Composable
fun BiometricPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun NotificationTriggerButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = "Test Notification",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EditProfileGoalsDialog(
    current: com.example.data.model.UserProfileEntity,
    onDismiss: () -> Unit,
    onSave: (com.example.data.model.UserProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(current.name) }
    var weight by remember { mutableStateOf(current.weightKg.toString()) }
    var targetWeight by remember { mutableStateOf(current.targetWeightKg.toString()) }
    var height by remember { mutableStateOf(current.heightCm.toString()) }
    var calGoal by remember { mutableStateOf(current.dailyCalorieGoal.toString()) }
    var stepGoal by remember { mutableStateOf(current.dailyStepGoal.toString()) }
    var waterGoal by remember { mutableStateOf(current.dailyWaterGoalMl.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Goals & Biometrics", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Target (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stepGoal,
                        onValueChange = { stepGoal = it },
                        label = { Text("Steps Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calGoal,
                        onValueChange = { calGoal = it },
                        label = { Text("Calories (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = waterGoal,
                        onValueChange = { waterGoal = it },
                        label = { Text("Water (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        current.copy(
                            name = name.trim(),
                            weightKg = weight.toFloatOrNull() ?: current.weightKg,
                            targetWeightKg = targetWeight.toFloatOrNull() ?: current.targetWeightKg,
                            heightCm = height.toFloatOrNull() ?: current.heightCm,
                            dailyCalorieGoal = calGoal.toIntOrNull() ?: current.dailyCalorieGoal,
                            dailyStepGoal = stepGoal.toIntOrNull() ?: current.dailyStepGoal,
                            dailyWaterGoalMl = waterGoal.toIntOrNull() ?: current.dailyWaterGoalMl
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
