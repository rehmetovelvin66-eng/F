package com.example.data

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.example.model.ControlSystemState
import com.example.model.DeviceDiagnostics
import com.example.service.GlassControlAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.roundToInt

class DeviceControlManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private var torchCameraId: String? = null
    private var torchCallbackRegistered = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                _torchEnabled.value = enabled
            }
        }
    }

    init {
        findTorchCameraId()
        registerTorchCallback()
    }

    private fun findTorchCameraId() {
        runCatching {
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    torchCameraId = id
                    return
                }
            }
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                if (chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                    torchCameraId = id
                    return
                }
            }
        }
    }

    private fun registerTorchCallback() {
        if (!torchCallbackRegistered && torchCameraId != null) {
            runCatching {
                cameraManager.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
                torchCallbackRegistered = true
            }
        }
    }

    fun release() {
        if (torchCallbackRegistered) {
            runCatching { cameraManager.unregisterTorchCallback(torchCallback) }
            torchCallbackRegistered = false
        }
    }

    fun readSystemState(): ControlSystemState {
        val brightnessRaw = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 150)
        }.getOrDefault(150)
        val brightnessPct = ((brightnessRaw / 255f) * 100f).roundToInt().coerceIn(5, 100)

        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volPct = ((curVol.toFloat() / maxVol.toFloat()) * 100f).roundToInt().coerceIn(0, 100)

        val autoRotate = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        }.getOrDefault(false)

        val wifiOn = isWifiActive()
        val btPermGranted = hasBluetoothPermission()
        val btOn = if (btPermGranted) isBluetoothEnabledSafe() else false

        val (batteryPct, charging) = readBatteryInfo()
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val availGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
        val ramText = String.format(Locale.US, "%.1f GB boş", availGb)

        return ControlSystemState(
            wifiEnabled = wifiOn,
            bluetoothEnabled = btOn,
            flashlightEnabled = _torchEnabled.value,
            autoRotateEnabled = autoRotate,
            ringerMode = audioManager.ringerMode,
            brightnessPercent = brightnessPct,
            mediaVolumePercent = volPct,
            batteryPercent = batteryPct,
            isCharging = charging,
            availableRamText = ramText,
            canWriteSettings = Settings.System.canWrite(context),
            canDrawOverlays = Settings.canDrawOverlays(context),
            isAccessibilityEnabled = isAccessibilityServiceEnabled(),
            isBluetoothPermissionGranted = btPermGranted
        )
    }

    private fun isWifiActive(): Boolean {
        val network = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(network)
        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        }
        return runCatching { wifiManager.isWifiEnabled }.getOrDefault(false)
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun isBluetoothEnabledSafe(): Boolean {
        return runCatching {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter?.isEnabled == true
        }.getOrDefault(false)
    }

    private fun readBatteryInfo(): Pair<Int, Boolean> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else 80
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        return pct to charging
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val expectedComponent = ComponentName(context, GlassControlAccessibilityService::class.java)
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { info ->
            val serviceInfo = info.resolveInfo?.serviceInfo
            serviceInfo != null &&
                serviceInfo.packageName == expectedComponent.packageName &&
                serviceInfo.name == expectedComponent.className
        }
    }

    fun setBrightnessPercent(percent: Int): Boolean {
        if (!Settings.System.canWrite(context)) return false
        val clamped = percent.coerceIn(2, 100)
        val value = ((clamped / 100f) * 255f).roundToInt().coerceIn(5, 255)
        return runCatching {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
            true
        }.getOrDefault(false)
    }

    fun setMediaVolumePercent(percent: Int) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val target = ((percent.coerceIn(0, 100) / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }

    fun toggleFlashlight(): Boolean {
        val id = torchCameraId ?: run {
            findTorchCameraId()
            torchCameraId
        } ?: return false
        val next = !_torchEnabled.value
        return runCatching {
            cameraManager.setTorchMode(id, next)
            _torchEnabled.value = next
            true
        }.getOrDefault(false)
    }

    fun toggleAutoRotate(): Boolean {
        if (!Settings.System.canWrite(context)) return false
        val current = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        }.getOrDefault(false)
        return runCatching {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (current) 0 else 1
            )
            true
        }.getOrDefault(false)
    }

    fun cycleRingerMode() {
        val next = when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        runCatching { audioManager.ringerMode = next }
    }

    fun dispatchMediaKey(keyCode: Int) {
        runCatching {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        }
    }

    fun openWifiPanel() {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Panel.ACTION_WIFI
        } else {
            Settings.ACTION_WIFI_SETTINGS
        }
        launchExternalIntent(Intent(action))
    }

    fun openInternetPanel() {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Panel.ACTION_INTERNET_CONNECTIVITY
        } else {
            Settings.ACTION_WIRELESS_SETTINGS
        }
        launchExternalIntent(Intent(action))
    }

    fun openBluetoothSettings() {
        launchExternalIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun openSystemSettings() {
        launchExternalIntent(Intent(Settings.ACTION_SETTINGS))
    }

    fun openCamera() {
        launchExternalIntent(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
    }

    fun openAccessibilitySettings() {
        launchExternalIntent(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
        launchExternalIntent(intent)
    }

    fun openWriteSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            android.net.Uri.parse("package:${context.packageName}")
        )
        launchExternalIntent(intent)
    }

    private fun launchExternalIntent(intent: Intent) {
        runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun getDeviceDiagnostics(): DeviceDiagnostics {
        val hyperOsRaw = getSystemProperty("ro.miui.ui.version.name")
        val blurProp = getSystemProperty("ro.surface_flinger.supports_background_blur")
        val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val blurEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { windowManager.isCrossWindowBlurEnabled }.getOrDefault(false)
        } else {
            false
        }

        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val totalGb = String.format(Locale.US, "%.1f GB", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0))
        val availGb = String.format(Locale.US, "%.1f GB", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))

        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val isXiaomi = manufacturer.contains("xiaomi", ignoreCase = true) ||
            manufacturer.contains("redmi", ignoreCase = true) ||
            manufacturer.contains("poco", ignoreCase = true) ||
            hyperOsRaw.isNotBlank()

        return DeviceDiagnostics(
            manufacturer = manufacturer,
            model = Build.MODEL,
            deviceCodeName = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE ?: "14",
            sdkInt = Build.VERSION.SDK_INT,
            hyperOsVersion = if (hyperOsRaw.isNotBlank()) hyperOsRaw else "Standart Android / MIUI Yok",
            crossWindowBlurSupported = blurSupported,
            crossWindowBlurEnabled = blurEnabled,
            surfaceFlingerBlurProp = if (blurProp.isNotBlank()) blurProp else "Tanımlı değil / Kapalı",
            totalRamGb = totalGb,
            availableRamGb = availGb,
            isXiaomiFamily = isXiaomi
        )
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String): String {
        return runCatching {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            (method.invoke(null, key, "") as? String).orEmpty()
        }.getOrDefault("")
    }
}
