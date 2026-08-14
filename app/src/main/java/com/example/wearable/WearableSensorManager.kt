package com.example.wearable

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.model.WearableDeviceEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin
import kotlin.random.Random

data class LiveHealthTelemetry(
    val heartRateBpm: Int = 74,
    val heartRateZone: HeartRateZone = HeartRateZone.AEROBIC,
    val steps: Int = 7840,
    val activeCalories: Int = 520,
    val hrvMs: Int = 68,
    val isSensorLive: Boolean = true,
    val activeDeviceName: String = "Garmin Forerunner 965",
    val isConnected: Boolean = true
)

enum class HeartRateZone(val label: String, val minBpm: Int, val maxBpm: Int, val description: String) {
    RESTING("Rest / Recovery", 50, 99, "Resting & light recovery"),
    FAT_BURN("Fat Burn Zone", 100, 125, "Optimal lipid oxidation"),
    AEROBIC("Aerobic / Cardio", 126, 150, "Cardiovascular endurance"),
    ANAEROBIC("Anaerobic Threshold", 151, 175, "High-intensity stamina"),
    PEAK("Peak / Redline", 176, 210, "Maximum performance capacity")
}

data class DiscoveredBleDevice(
    val id: String,
    val name: String,
    val brand: String,
    val rssi: Int,
    val type: String
)

class WearableSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _telemetry = MutableStateFlow(LiveHealthTelemetry())
    val telemetry: StateFlow<LiveHealthTelemetry> = _telemetry.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredBleDevice>> = _discoveredDevices.asStateFlow()

    private var isWorkoutActive = false
    private var baseBpm = 74
    private var targetBpm = 74
    private var stepCountOffset = 0
    private var simulationJob: Job? = null

    init {
        registerHardwareSensors()
        startTelemetryLoop()
    }

    private fun registerHardwareSensors() {
        sensorManager?.let { sm ->
            val stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                ?: sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            stepSensor?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            val hrSensor = sm.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            hrSensor?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0].toInt()
                _telemetry.value = _telemetry.value.copy(steps = totalSteps)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                _telemetry.value = _telemetry.value.copy(steps = _telemetry.value.steps + 1)
            }
            Sensor.TYPE_HEART_RATE -> {
                val hr = event.values[0].toInt()
                if (hr > 0) {
                    val zone = calculateZone(hr)
                    _telemetry.value = _telemetry.value.copy(
                        heartRateBpm = hr,
                        heartRateZone = zone,
                        isSensorLive = true
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setWorkoutActiveState(active: Boolean, intensityMultiplier: Float = 1.0f) {
        isWorkoutActive = active
        targetBpm = if (active) {
            (140 + (intensityMultiplier * 25).toInt()).coerceIn(120, 185)
        } else {
            72 + Random.nextInt(0, 8)
        }
    }

    fun addManualSteps(stepIncrement: Int) {
        val newSteps = _telemetry.value.steps + stepIncrement
        val additionalCals = (stepIncrement * 0.04f).toInt()
        _telemetry.value = _telemetry.value.copy(
            steps = newSteps,
            activeCalories = _telemetry.value.activeCalories + additionalCals
        )
    }

    private fun startTelemetryLoop() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var tick = 0f
            while (isActive) {
                delay(1200)
                tick += 0.2f

                val current = _telemetry.value
                val diff = targetBpm - baseBpm
                if (diff != 0) {
                    baseBpm += if (diff > 0) minOf(3, diff) else maxOf(-2, diff)
                }

                // Add natural physiological micro-variation (sin wave + slight noise)
                val variation = (sin(tick.toDouble()) * 2.5 + Random.nextInt(-1, 2)).toInt()
                val liveBpm = (baseBpm + variation).coerceIn(48, 195)
                val zone = calculateZone(liveBpm)

                val stepIncr = if (isWorkoutActive) Random.nextInt(2, 6) else if (Random.nextFloat() > 0.6f) Random.nextInt(0, 3) else 0
                val calIncr = if (isWorkoutActive) (liveBpm / 80f * 0.35f).toInt() else 0

                _telemetry.value = current.copy(
                    heartRateBpm = liveBpm,
                    heartRateZone = zone,
                    steps = current.steps + stepIncr,
                    activeCalories = current.activeCalories + calIncr,
                    hrvMs = (65 + (sin(tick * 0.5) * 6).toInt()).coerceIn(45, 95)
                )
            }
        }
    }

    fun scanForDevices() {
        scope.launch {
            _isScanning.value = true
            _discoveredDevices.value = emptyList()
            delay(1500)
            _discoveredDevices.value = listOf(
                DiscoveredBleDevice("GARMIN_965_448", "Garmin Forerunner 965", "Garmin", -48, "SMARTWATCH"),
                DiscoveredBleDevice("POLAR_H10_991", "Polar H10 Heart Strap", "Polar", -55, "CHEST_STRAP"),
                DiscoveredBleDevice("WEAROS_PIXEL_3", "Pixel Watch 3 / WearOS", "WearOS", -62, "SMARTWATCH"),
                DiscoveredBleDevice("WHOOP_40_BAND", "Whoop 4.0 Bio-Strap", "Whoop", -58, "FITNESS_BAND"),
                DiscoveredBleDevice("OURA_RING_GEN4", "Oura Ring Gen 4 Horizon", "Oura", -70, "FITNESS_RING")
            )
            _isScanning.value = false
        }
    }

    fun setActiveDevice(device: WearableDeviceEntity) {
        _telemetry.value = _telemetry.value.copy(
            activeDeviceName = device.name,
            isConnected = device.isConnected,
            steps = if (device.stepsToday > 0) device.stepsToday else _telemetry.value.steps
        )
    }

    private fun calculateZone(bpm: Int): HeartRateZone {
        return when {
            bpm < 100 -> HeartRateZone.RESTING
            bpm < 126 -> HeartRateZone.FAT_BURN
            bpm < 151 -> HeartRateZone.AEROBIC
            bpm < 176 -> HeartRateZone.ANAEROBIC
            else -> HeartRateZone.PEAK
        }
    }

    fun destroy() {
        sensorManager?.unregisterListener(this)
        simulationJob?.cancel()
    }
}
