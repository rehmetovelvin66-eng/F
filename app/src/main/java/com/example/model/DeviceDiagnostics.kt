package com.example.model

data class DeviceDiagnostics(
    val manufacturer: String,
    val model: String,
    val deviceCodeName: String,
    val androidVersion: String,
    val sdkInt: Int,
    val hyperOsVersion: String,
    val crossWindowBlurSupported: Boolean,
    val crossWindowBlurEnabled: Boolean,
    val surfaceFlingerBlurProp: String,
    val totalRamGb: String,
    val availableRamGb: String,
    val isXiaomiFamily: Boolean
)
