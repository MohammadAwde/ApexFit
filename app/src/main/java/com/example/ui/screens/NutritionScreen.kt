package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.MealEntryEntity
import com.example.ui.components.MacroBreakdownCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

data class PresetFoodItem(
    val name: String,
    val mealType: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val serving: String
)

val PRESET_FOODS = listOf(
    PresetFoodItem("Grilled Chicken Breast", "Lunch", 280, 48, 0, 6, "200g cooked"),
    PresetFoodItem("Steel-Cut Oatmeal & Berries", "Breakfast", 320, 12, 54, 6, "1 large bowl"),
    PresetFoodItem("Atlantic Wild Salmon Fillet", "Dinner", 420, 38, 0, 24, "220g baked"),
    PresetFoodItem("Whey Isolate Protein Shake", "Snack", 140, 27, 2, 1, "1 scoop in water"),
    PresetFoodItem("Greek Yogurt (Non-Fat)", "Breakfast", 180, 22, 14, 0, "250g serving"),
    PresetFoodItem("Brown Rice & Black Beans", "Lunch", 350, 14, 65, 4, "1 bowl (250g)"),
    PresetFoodItem("Whole Eggs & Avocado Toast", "Breakfast", 450, 24, 32, 22, "2 eggs + 1 sourdough"),
    PresetFoodItem("Almonds & Dark Chocolate", "Snack", 210, 6, 16, 16, "35g mixed"),
    PresetFoodItem("Sweet Potato & Lean Beef", "Dinner", 520, 42, 48, 14, "350g plate")
)

@Composable
fun NutritionScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val todayMeals by viewModel.todayMeals.collectAsState()
    val totalWaterMl by viewModel.totalWaterTodayMl.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val totalCals = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val totalProtein = remember(todayMeals) { todayMeals.sumOf { it.proteinG } }
    val totalCarbs = remember(todayMeals) { todayMeals.sumOf { it.carbsG } }
    val totalFat = remember(todayMeals) { todayMeals.sumOf { it.fatG } }

    val calorieGoal = userProfile?.dailyCalorieGoal ?: 2450
    val proteinGoal = userProfile?.proteinGoalG ?: 165
    val carbsGoal = userProfile?.carbsGoalG ?: 230
    val fatGoal = userProfile?.fatGoalG ?: 65
    val waterGoal = userProfile?.dailyWaterGoalMl ?: 3200

    var showAddMealDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("nutrition_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddMealDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Meal") },
                text = { Text("Log Meal", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("log_meal_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Macro Breakdown Card
            item {
                MacroBreakdownCard(
                    caloriesCurrent = totalCals,
                    caloriesTarget = calorieGoal,
                    proteinCurrentG = totalProtein,
                    proteinTargetG = proteinGoal,
                    carbsCurrentG = totalCarbs,
                    carbsTargetG = carbsGoal,
                    fatCurrentG = totalFat,
                    fatTargetG = fatGoal
                )
            }

            // 2. Hydration & Water Intake Tracker
            item {
                HydrationTrackerCard(
                    currentMl = totalWaterMl,
                    targetMl = waterGoal,
                    onAdd250 = { viewModel.logWater(250) },
                    onAdd500 = { viewModel.logWater(500) },
                    onReset = { viewModel.resetWater() }
                )
            }

            // 3. Today's Logged Meals by Category
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S MEAL LOG (${todayMeals.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
            mealTypes.forEach { type ->
                val mealsOfType = todayMeals.filter { it.mealType.equals(type, ignoreCase = true) }
                item {
                    MealTypeSection(
                        mealType = type,
                        meals = mealsOfType,
                        onDeleteMeal = { viewModel.deleteMeal(it) }
                    )
                }
            }
        }
    }

    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onAddMeal = { name, type, cals, p, c, f, serving ->
                viewModel.addMeal(name, type, cals, p, c, f, serving)
                showAddMealDialog = false
            }
        )
    }
}

@Composable
fun HydrationTrackerCard(
    currentMl: Int,
    targetMl: Int,
    onAdd250: () -> Unit,
    onAdd500: () -> Unit,
    onReset: () -> Unit
) {
    val progress = (currentMl.toFloat() / targetMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hydration_tracker_card"),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WaterColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            tint = WaterColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Daily Hydration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currentMl / $targetMl ml (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset Water",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Hydration Bar with water droplets
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(WaterColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(WaterColor, Color(0xFF00B0FF))
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAdd250,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_water_250_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = WaterColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+250 ml", fontWeight = FontWeight.Bold, color = DarkBackground)
                }
                Button(
                    onClick = onAdd500,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_water_500_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+500 ml", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MealTypeSection(
    mealType: String,
    meals: List<MealEntryEntity>,
    onDeleteMeal: (MealEntryEntity) -> Unit
) {
    val totalCalsForType = meals.sumOf { it.calories }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealType.uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$totalCalsForType kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CaloriesColor
                )
            }

            if (meals.isEmpty()) {
                Text(
                    text = "No meals logged for $mealType yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                meals.forEach { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meal.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "P: ${meal.proteinG}g • C: ${meal.carbsG}g • F: ${meal.fatG}g (${meal.servingSize})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${meal.calories} kcal",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onDeleteMeal(meal) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (String, String, Int, Int, Int, Int, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Presets, 1 = Custom

    // Custom fields
    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Lunch") }
    var calories by remember { mutableStateOf("350") }
    var protein by remember { mutableStateOf("30") }
    var carbs by remember { mutableStateOf("25") }
    var fat by remember { mutableStateOf("10") }
    var serving by remember { mutableStateOf("1 plate (300g)") }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Food & Nutrition", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Popular Items") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Custom Food") }
                    )
                }

                if (selectedTab == 0) {
                    Text("Select a food item to log immediately:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(PRESET_FOODS) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddMeal(item.name, item.mealType, item.calories, item.protein, item.carbs, item.fat, item.serving)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("P: ${item.protein}g C: ${item.carbs}g F: ${item.fat}g • ${item.serving}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${item.calories} kcal", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CaloriesColor)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Food Name (e.g. Ribeye Steak & Sweet Potato)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Meal Type selector
                    Text("Meal Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(mealTypes) { type ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calories") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = carbs,
                            onValueChange = { carbs = it },
                            label = { Text("Carbs (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fat,
                            onValueChange = { fat = it },
                            label = { Text("Fat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = serving,
                        onValueChange = { serving = it },
                        label = { Text("Serving Size") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddMeal(
                                name.trim(),
                                mealType,
                                calories.toIntOrNull() ?: 200,
                                protein.toIntOrNull() ?: 20,
                                carbs.toIntOrNull() ?: 20,
                                fat.toIntOrNull() ?: 5,
                                serving.trim()
                            )
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Add Meal")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
