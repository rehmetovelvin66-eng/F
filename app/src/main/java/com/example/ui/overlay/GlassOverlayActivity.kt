package com.example.ui.overlay

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.data.DeviceControlManager
import com.example.data.GlassPreferencesRepository
import com.example.model.GlassSettings
import com.example.service.GlassOverlayBridge
import com.example.ui.components.GlassControlCenterContent
import com.example.ui.theme.SeffafKontrolTheme

class GlassOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DISMISS_IMMEDIATELY = "extra_dismiss_immediately"
    }

    private lateinit var prefsRepository: GlassPreferencesRepository
    private lateinit var controlManager: DeviceControlManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.getBooleanExtra(EXTRA_DISMISS_IMMEDIATELY, false) == true) {
            finish()
            return
        }

        enableEdgeToEdge()
        prefsRepository = GlassPreferencesRepository(applicationContext)
        controlManager = DeviceControlManager(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }

        setContent {
            val settings by prefsRepository.settingsFlow.collectAsState(initial = GlassSettings())
            val torchOn by controlManager.torchEnabled.collectAsState()
            var systemState by remember { mutableStateOf(controlManager.readSystemState()) }

            LaunchedEffect(settings.blurRadius) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    runCatching {
                        window.setBackgroundBlurRadius((settings.blurRadius * 2).coerceIn(0, 120))
                    }
                }
            }

            LaunchedEffect(torchOn) {
                systemState = controlManager.readSystemState()
            }

            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                systemState = controlManager.readSystemState()
            }

            SeffafKontrolTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            finish()
                        }
                        .pointerInput(Unit) {
                            var dragTotal = 0f
                            detectVerticalDragGestures(
                                onDragStart = { dragTotal = 0f },
                                onVerticalDrag = { _, dragAmount ->
                                    dragTotal += dragAmount
                                    if (dragTotal < -90f) {
                                        finish()
                                    }
                                }
                            )
                        }
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .verticalScroll(rememberScrollState())
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
                        GlassControlCenterContent(
                            settings = settings,
                            systemState = systemState,
                            isOverlayMode = true,
                            onWifiClick = {
                                controlManager.openWifiPanel()
                                finish()
                            },
                            onMobileDataClick = {
                                controlManager.openInternetPanel()
                                finish()
                            },
                            onBluetoothClick = {
                                controlManager.openBluetoothSettings()
                                finish()
                            },
                            onFlashlightClick = {
                                controlManager.toggleFlashlight()
                                systemState = controlManager.readSystemState()
                            },
                            onBrightnessChange = { pct ->
                                val ok = controlManager.setBrightnessPercent(pct)
                                if (!ok) {
                                    Toast.makeText(
                                        this@GlassOverlayActivity,
                                        "Parlaklık için Sistem Ayarları izni gerekli",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    controlManager.openWriteSettings()
                                    finish()
                                } else {
                                    systemState = controlManager.readSystemState()
                                }
                            },
                            onVolumeChange = { pct ->
                                controlManager.setMediaVolumePercent(pct)
                                systemState = controlManager.readSystemState()
                            },
                            onAutoRotateClick = {
                                val ok = controlManager.toggleAutoRotate()
                                if (!ok) {
                                    controlManager.openWriteSettings()
                                    finish()
                                } else {
                                    systemState = controlManager.readSystemState()
                                }
                            },
                            onRingerModeClick = {
                                controlManager.cycleRingerMode()
                                systemState = controlManager.readSystemState()
                            },
                            onCameraClick = {
                                controlManager.openCamera()
                                finish()
                            },
                            onSystemSettingsClick = {
                                controlManager.openSystemSettings()
                                finish()
                            },
                            onMediaPrev = { controlManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                            onMediaPlayPause = { controlManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                            onMediaNext = { controlManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) },
                            onGlobalAction = { action ->
                                val ok = GlassOverlayBridge.requestGlobalAction(action)
                                if (!ok) {
                                    Toast.makeText(
                                        this@GlassOverlayActivity,
                                        "Bu işlem için Erişilebilirlik Servisini etkinleştirin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    controlManager.openAccessibilitySettings()
                                }
                                finish()
                            },
                            onClose = { finish() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::controlManager.isInitialized) {
            controlManager.release()
        }
    }
}
