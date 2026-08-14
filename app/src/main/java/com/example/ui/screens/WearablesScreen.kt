package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WearableDeviceEntity
import com.example.ui.components.LiveHeartRateChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel
import com.example.wearable.DiscoveredBleDevice
import com.example.wearable.HeartRateZone
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun WearablesScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val liveTelemetry by viewModel.liveTelemetry.collectAsState()
    val allWearables by viewModel.allWearables.collectAsState()
    val activeWearable by viewModel.activeWearable.collectAsState()
    val isScanning by viewModel.isBleScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    var showScanSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wearables_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Live Heart Rate ECG Monitor Card
        item {
            LiveHeartRateECGCard(
                bpm = liveTelemetry.heartRateBpm,
                zone = liveTelemetry.heartRateZone,
                hrvMs = liveTelemetry.hrvMs,
                activeDevice = activeWearable?.name ?: liveTelemetry.activeDeviceName
            )
        }

        // 2. Recovery & Sleep Biometrics Summary
        item {
            RecoveryBiometricsCard(
                recoveryScore = activeWearable?.recoveryScore ?: 94,
                restingBpm = activeWearable?.restingHeartRateBpm ?: 54,
                sleepHours = activeWearable?.sleepHours ?: 7.8f,
                sleepScore = activeWearable?.sleepScore ?: 88
            )
        }

        // 3. Paired Wearable Health Devices
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PAIRED HEALTH DEVICES (${allWearables.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = {
                        viewModel.scanForDevices()
                        showScanSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("scan_wearables_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.BluetoothSearching,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pair Device", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (allWearables.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                        Text("No Wearable Devices Paired", fontWeight = FontWeight.Bold)
                        Text("Pair your Garmin, Polar, WearOS, or Whoop strap above for real-time biometrics.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(allWearables) { device ->
                WearableDeviceItemCard(
                    device = device,
                    onConnect = {
                        viewModel.pairAndConnectDevice(
                            DiscoveredBleDevice(device.id, device.name, device.brand, -50, device.type)
                        )
                    },
                    onDisconnect = { viewModel.disconnectWearable(device.id) }
                )
            }
        }
    }

    if (showScanSheet) {
        BleDeviceScanDialog(
            isScanning = isScanning,
            devices = discoveredDevices,
            onDismiss = { showScanSheet = false },
            onSelectDevice = { device ->
                viewModel.pairAndConnectDevice(device)
                showScanSheet = false
            },
            onRescan = { viewModel.scanForDevices() }
        )
    }
}

@Composable
fun LiveHeartRateECGCard(
    bpm: Int,
    zone: HeartRateZone,
    hrvMs: Int,
    activeDevice: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("live_ecg_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PulseRed)
                    )
                    Text(
                        text = "LIVE SENSOR TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PulseRed,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant
                ) {
                    Text(
                        text = activeDevice,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // BPM readout & Zone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$bpm",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "BPM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PulseRed,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (zone) {
                            HeartRateZone.PEAK -> PulseRed.copy(alpha = 0.25f)
                            HeartRateZone.ANAEROBIC -> ElectricOrange.copy(alpha = 0.25f)
                            HeartRateZone.AEROBIC -> NeonLime.copy(alpha = 0.25f)
                            else -> NeonCyan.copy(alpha = 0.25f)
                        }
                    ) {
                        Text(
                            text = zone.label.uppercase(),
                            color = when (zone) {
                                HeartRateZone.PEAK -> PulseRed
                                HeartRateZone.ANAEROBIC -> ElectricOrange
                                HeartRateZone.AEROBIC -> NeonLime
                                else -> NeonCyan
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "HRV: $hrvMs ms",
                        fontSize = 12.sp,
                        color = DarkTextSecondary
                    )
                }
            }

            // Live ECG Waveform Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path()
                    val width = size.width
                    val height = size.height
                    val midY = height / 2

                    path.moveTo(0f, midY)
                    var x = 0f
                    val step = width / 40

                    var i = 0
                    while (x < width) {
                        val isPeak = i % 8 == 4
                        val isTrough = i % 8 == 5
                        val y = when {
                            isPeak -> midY - 26.dp.toPx()
                            isTrough -> midY + 16.dp.toPx()
                            else -> midY + (sin(i.toDouble()) * 3.dp.toPx()).toFloat()
                        }
                        path.lineTo(x, y)
                        x += step
                        i++
                    }

                    drawPath(
                        path = path,
                        color = PulseRed,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Heart Rate Zones Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ZonePill(label = "Warmup", bpmRange = "<100", color = NeonCyan)
                ZonePill(label = "Fat Burn", bpmRange = "100-125", color = NeonLime)
                ZonePill(label = "Cardio", bpmRange = "126-150", color = ElectricOrange)
                ZonePill(label = "Peak", bpmRange = "175+", color = PulseRed)
            }
        }
    }
}

@Composable
fun ZonePill(label: String, bpmRange: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 9.sp, color = DarkTextSecondary, fontWeight = FontWeight.Bold)
        Text(text = bpmRange, fontSize = 9.sp, color = DarkTextTertiary)
    }
}

@Composable
fun RecoveryBiometricsCard(
    recoveryScore: Int,
    restingBpm: Int,
    sleepHours: Float,
    sleepScore: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recovery_biometrics_card"),
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
                        text = "Readiness & Recovery Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Body primed for high-strain training session",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonLime.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$recoveryScore%",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = NeonLimeDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Grid of sub-metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BiometricStatCol(
                    icon = Icons.Filled.Bedtime,
                    iconColor = RoyalPurple,
                    label = "Sleep Duration",
                    value = "${sleepHours}h ($sleepScore/100)"
                )
                BiometricStatCol(
                    icon = Icons.Filled.FavoriteBorder,
                    iconColor = PulseRed,
                    label = "Resting HR",
                    value = "$restingBpm bpm"
                )
                BiometricStatCol(
                    icon = Icons.Filled.Speed,
                    iconColor = NeonCyan,
                    label = "Strain Capacity",
                    value = "High (18.4)"
                )
            }
        }
    }
}

@Composable
fun BiometricStatCol(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun WearableDeviceItemCard(
    device: WearableDeviceEntity,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (device.isConnected) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (device.type) {
                            "CHEST_STRAP" -> Icons.Filled.MonitorHeart
                            "FITNESS_BAND" -> Icons.Filled.FitnessCenter
                            else -> Icons.Filled.Watch
                        },
                        contentDescription = null,
                        tint = if (device.isConnected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = device.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (device.isConnected) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonLime.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonLimeDark,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Battery: ${device.batteryPercent}% • ${device.brand}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (device.isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Disconnect", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BleDeviceScanDialog(
    isScanning: Boolean,
    devices: List<DiscoveredBleDevice>,
    onDismiss: () -> Unit,
    onSelectDevice: (DiscoveredBleDevice) -> Unit,
    onRescan: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.BluetoothSearching, contentDescription = null, tint = NeonCyan)
                Text("Scan Nearby Wearables", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isScanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Searching Bluetooth LE beacons...", fontSize = 13.sp)
                    }
                } else {
                    Text("Found ${devices.size} devices nearby:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(devices) { dev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDevice(dev) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Filled.Watch, contentDescription = null, tint = NeonCyan)
                                    Column {
                                        Text(dev.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${dev.brand} • Signal ${dev.rssi} dBm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Button(
                                    onClick = { onSelectDevice(dev) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Pair", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRescan) { Text("Scan Again") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
