package com.example.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.DeviceControlManager
import com.example.data.GlassPreferencesRepository
import com.example.model.EdgeTriggerPosition
import com.example.model.GlassSettings
import com.example.ui.components.GlassControlCenterContent
import com.example.ui.theme.SeffafKontrolTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GlassControlAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private lateinit var prefsRepository: GlassPreferencesRepository
    private lateinit var controlManager: DeviceControlManager

    private val settingsState = MutableStateFlow(GlassSettings())
    private var leftEdgeView: View? = null
    private var rightEdgeView: View? = null

    private var overlayContainer: FrameLayout? = null
    private var overlayLifecycleOwner: ServiceOverlayLifecycleOwner? = null
    private var collectJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsRepository = GlassPreferencesRepository(applicationContext)
        controlManager = DeviceControlManager(applicationContext)
        GlassOverlayBridge.setServiceConnected(true)

        collectJob = serviceScope.launch {
            launch {
                prefsRepository.settingsFlow.collect { newSettings ->
                    settingsState.value = newSettings
                    updateEdgeTriggers(newSettings)
                }
            }
            launch {
                GlassOverlayBridge.commands.collect { command ->
                    when (command) {
                        OverlayCommand.ShowControlCenter -> showControlCenterOverlay()
                        OverlayCommand.HideControlCenter -> hideControlCenterOverlay()
                        is OverlayCommand.PerformGlobalAction -> executeGlobalAction(command.action)
                    }
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally unused: we never read screen content or window hierarchy.
    }

    override fun onInterrupt() {
        hideControlCenterOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        GlassOverlayBridge.setServiceConnected(false)
        collectJob?.cancel()
        removeEdgeTriggers()
        hideControlCenterOverlay()
        if (::controlManager.isInitialized) {
            controlManager.release()
        }
        serviceScope.cancel()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateEdgeTriggers(settings: GlassSettings) {
        removeEdgeTriggers()
        if (!settings.edgeTriggerEnabled) return

        val density = resources.displayMetrics.density
        val widthPx = (settings.edgeTriggerWidthDp * density).toInt()
        val heightPx = (24 * density).toInt()

        if (settings.edgeTriggerPosition == EdgeTriggerPosition.TOP_RIGHT ||
            settings.edgeTriggerPosition == EdgeTriggerPosition.BOTH
        ) {
            val view = createEdgeTriggerView(settings.edgeIndicatorVisible)
            val params = createEdgeLayoutParams(
                widthPx = widthPx,
                heightPx = heightPx,
                gravity = Gravity.TOP or Gravity.END
            )
            runCatching {
                windowManager.addView(view, params)
                rightEdgeView = view
            }
        }

        if (settings.edgeTriggerPosition == EdgeTriggerPosition.TOP_LEFT ||
            settings.edgeTriggerPosition == EdgeTriggerPosition.BOTH
        ) {
            val view = createEdgeTriggerView(settings.edgeIndicatorVisible)
            val params = createEdgeLayoutParams(
                widthPx = widthPx,
                heightPx = heightPx,
                gravity = Gravity.TOP or Gravity.START
            )
            runCatching {
                windowManager.addView(view, params)
                leftEdgeView = view
            }
        }
    }

    private fun createEdgeLayoutParams(
        widthPx: Int,
        heightPx: Int,
        gravity: Int
    ): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createEdgeTriggerView(showIndicator: Boolean): View {
        val density = resources.displayMetrics.density
        val container = FrameLayout(this)
        if (showIndicator) {
            val bar = View(this).apply {
                setBackgroundColor(0x66FFFFFF)
            }
            val barParams = FrameLayout.LayoutParams(
                (44 * density).toInt(),
                (3 * density).toInt(),
                Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            ).apply {
                bottomMargin = (4 * density).toInt()
            }
            container.addView(bar, barParams)
        }

        var startY = 0f
        var triggered = false
        container.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    triggered = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!triggered && (event.rawY - startY) > 28f * density) {
                        triggered = true
                        showControlCenterOverlay()
                    }
                    true
                }
                else -> false
            }
        }
        return container
    }

    private fun removeEdgeTriggers() {
        leftEdgeView?.let { runCatching { windowManager.removeViewImmediate(it) } }
        rightEdgeView?.let { runCatching { windowManager.removeViewImmediate(it) } }
        leftEdgeView = null
        rightEdgeView = null
    }

    private fun showControlCenterOverlay() {
        if (overlayContainer != null) return

        val settings = settingsState.value
        val owner = ServiceOverlayLifecycleOwner()
        owner.onCreate()
        owner.onResume()
        overlayLifecycleOwner = owner

        val container = object : FrameLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    hideControlCenterOverlay()
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }

        container.setViewTreeLifecycleOwner(owner)
        container.setViewTreeViewModelStoreOwner(owner)
        container.setViewTreeSavedStateRegistryOwner(owner)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                val activeSettings by settingsState.collectAsState()
                val torchOn by controlManager.torchEnabled.collectAsState()
                var systemState by remember { mutableStateOf(controlManager.readSystemState()) }
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    visible = true
                }

                LaunchedEffect(torchOn) {
                    systemState = controlManager.readSystemState()
                }

                SeffafKontrolTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.28f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                hideControlCenterOverlay()
                            }
                            .pointerInput(Unit) {
                                var dragTotal = 0f
                                detectVerticalDragGestures(
                                    onDragStart = { dragTotal = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        dragTotal += dragAmount
                                        if (dragTotal < -90f) {
                                            hideControlCenterOverlay()
                                        }
                                    }
                                )
                            }
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically { -it / 3 } + fadeIn(),
                            exit = slideOutVertically { -it / 3 } + fadeOut()
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
                                    settings = activeSettings,
                                    systemState = systemState,
                                    isOverlayMode = true,
                                    onWifiClick = {
                                        hideControlCenterOverlay()
                                        controlManager.openWifiPanel()
                                    },
                                    onMobileDataClick = {
                                        hideControlCenterOverlay()
                                        controlManager.openInternetPanel()
                                    },
                                    onBluetoothClick = {
                                        hideControlCenterOverlay()
                                        controlManager.openBluetoothSettings()
                                    },
                                    onFlashlightClick = {
                                        controlManager.toggleFlashlight()
                                        systemState = controlManager.readSystemState()
                                    },
                                    onBrightnessChange = { pct ->
                                        val ok = controlManager.setBrightnessPercent(pct)
                                        if (!ok) {
                                            Toast.makeText(
                                                this@GlassControlAccessibilityService,
                                                "Parlaklık için Sistem Ayarları izni gerekli",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            hideControlCenterOverlay()
                                            controlManager.openWriteSettings()
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
                                            hideControlCenterOverlay()
                                            controlManager.openWriteSettings()
                                        } else {
                                            systemState = controlManager.readSystemState()
                                        }
                                    },
                                    onRingerModeClick = {
                                        controlManager.cycleRingerMode()
                                        systemState = controlManager.readSystemState()
                                    },
                                    onCameraClick = {
                                        hideControlCenterOverlay()
                                        controlManager.openCamera()
                                    },
                                    onSystemSettingsClick = {
                                        hideControlCenterOverlay()
                                        controlManager.openSystemSettings()
                                    },
                                    onMediaPrev = { controlManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                                    onMediaPlayPause = { controlManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                                    onMediaNext = { controlManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) },
                                    onGlobalAction = { action ->
                                        hideControlCenterOverlay()
                                        serviceScope.launch {
                                            delay(120)
                                            executeGlobalAction(action)
                                        }
                                    },
                                    onClose = { hideControlCenterOverlay() }
                                )
                            }
                        }
                    }
                }
            }
        }

        container.addView(
            composeView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_DIM_BEHIND or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            } else {
                0
            }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.25f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurBehindRadius = (settings.blurRadius * 2).coerceIn(0, 120)
            }
        }

        runCatching {
            windowManager.addView(container, params)
            overlayContainer = container
        }.onFailure {
            owner.onDestroy()
            overlayLifecycleOwner = null
        }
    }

    private fun hideControlCenterOverlay() {
        val container = overlayContainer ?: return
        overlayContainer = null
        runCatching { windowManager.removeViewImmediate(container) }
        overlayLifecycleOwner?.onDestroy()
        overlayLifecycleOwner = null
    }

    private fun executeGlobalAction(action: GlobalSystemAction) {
        when (action) {
            GlobalSystemAction.LOCK_SCREEN -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            }
            GlobalSystemAction.TAKE_SCREENSHOT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                }
            }
            GlobalSystemAction.NOTIFICATIONS -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            GlobalSystemAction.POWER_DIALOG -> performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        }
    }
}

private class ServiceOverlayLifecycleOwner :
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    fun onCreate() {
        savedStateController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }
}
