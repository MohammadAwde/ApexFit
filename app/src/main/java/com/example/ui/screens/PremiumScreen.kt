package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun PremiumScreen(
    viewModel: FitnessViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currentTier = userProfile?.subscriptionTier ?: "PRO"

    var selectedPlan by remember { mutableStateOf("PRO_ANNUAL") }
    var showSuccessBanner by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .testTag("premium_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    text = "APEXFIT PREMIUM",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = GoldStar,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Hero Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(GoldStar, ElectricOrange))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Unlock Your Ultimate Potential",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = "AI Periodization, Real-Time Wearable Telemetry & Unlimited Pro Workouts",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            }
        }

        if (showSuccessBanner) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonLime.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, NeonLime)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NeonLime)
                        Text(
                            text = "🎉 Subscription activated! Welcome to ApexFit Elite.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Feature Comparison Matrix
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EVERYTHING INCLUDED IN PRO & ELITE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )

                    PremiumFeatureRow(
                        title = "AI Smart Coach Generator",
                        desc = "Custom periodized routines built for your target muscles & equipment"
                    )
                    PremiumFeatureRow(
                        title = "Full Wearable Biometrics & ECG",
                        desc = "Live pulse telemetry, HRV recovery scores & sleep architecture"
                    )
                    PremiumFeatureRow(
                        title = "Offline Muscle Database & Macro Tracker",
                        desc = "100% offline logging with local Room DB encryption"
                    )
                    PremiumFeatureRow(
                        title = "Personal Records & Volume Analytics",
                        desc = "In-depth fatigue tracking and 1RM predictive strength curves"
                    )
                    PremiumFeatureRow(
                        title = "Social Milestone Sharing & Leaderboards",
                        desc = "Compete on global weekly calorie and volume podiums"
                    )
                }
            }
        }

        // Plan Selection Cards
        item {
            Text(
                text = "CHOOSE YOUR PLAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = DarkTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanChoiceCard(
                    id = "PRO_ANNUAL",
                    title = "Elite Annual (Best Value)",
                    price = "$4.99 / month",
                    subtitle = "$59.88 billed annually • Save 58%",
                    isPopular = true,
                    isSelected = selectedPlan == "PRO_ANNUAL",
                    onClick = { selectedPlan = "PRO_ANNUAL" }
                )
                PlanChoiceCard(
                    id = "PRO_MONTHLY",
                    title = "Pro Monthly",
                    price = "$11.99 / month",
                    subtitle = "Billed monthly • Cancel anytime",
                    isPopular = false,
                    isSelected = selectedPlan == "PRO_MONTHLY",
                    onClick = { selectedPlan = "PRO_MONTHLY" }
                )
            }
        }

        // Action CTA Button
        item {
            Button(
                onClick = {
                    val newTier = if (selectedPlan == "PRO_ANNUAL") "ELITE" else "PRO"
                    viewModel.updateSubscriptionTier(newTier)
                    showSuccessBanner = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("subscribe_cta_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldStar),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = null, tint = DarkBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upgrade to ${if (selectedPlan == "PRO_ANNUAL") "Elite" else "Pro"}",
                    color = DarkBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Current Status: Active $currentTier Member • Cancel anytime in Google Play Store",
                fontSize = 11.sp,
                color = DarkTextTertiary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumFeatureRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = NeonLime,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(desc, fontSize = 11.sp, color = DarkTextSecondary)
        }
    }
}

@Composable
fun PlanChoiceCard(
    id: String,
    title: String,
    price: String,
    subtitle: String,
    isPopular: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("plan_card_$id"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RoyalPurple.copy(alpha = 0.2f) else DarkSurface
        ),
        border = if (isSelected) BorderStroke(2.dp, GoldStar) else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onClick() },
                    colors = RadioButtonDefaults.colors(selectedColor = GoldStar)
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        if (isPopular) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldStar
                            ) {
                                Text(
                                    text = "SAVE 58%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DarkBackground,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(subtitle, fontSize = 11.sp, color = DarkTextSecondary)
                }
            }

            Text(
                text = price,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = if (isSelected) GoldStar else Color.White
            )
        }
    }
}
