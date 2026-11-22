package com.example.model

import android.media.AudioManager

data class ControlSystemState(
    val wifiEnabled: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val flashlightEnabled: Boolean = false,
    val autoRotateEnabled: Boolean = false,
    val ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    val brightnessPercent: Int = 65,
    val mediaVolumePercent: Int = 55,
    val batteryPercent: Int = 80,
    val isCharging: Boolean = false,
    val availableRamText: String = "-",
    val canWriteSettings: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isBluetoothPermissionGranted: Boolean = false
)
