package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BadgeRarity
import com.example.data.model.MilestoneBadge
import com.example.ui.theme.*

@Composable
fun BadgeCelebrationDialog(
    badge: MilestoneBadge,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {}
) {
    val rarityColor = when (badge.rarity) {
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700)
        BadgeRarity.EPIC -> RoyalPurple
        BadgeRarity.RARE -> NeonCyan
        BadgeRarity.COMMON -> ElectricGreen
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.radialGradient(listOf(rarityColor, BentoBorder))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("badge_celebration_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow Badge Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(rarityColor.copy(alpha = 0.4f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badge.iconEmoji, fontSize = 48.sp)
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = rarityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${badge.rarity.name} BADGE UNLOCKED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = rarityColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    badge.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    badge.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    badge.description,
                    fontSize = 13.sp,
                    color = BentoTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Stars, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "+${badge.xpReward} XP Earned",
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = rarityColor, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("claim_badge_btn")
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
