package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeviceControlManager
import com.example.data.GlassPreferencesRepository
import com.example.model.ControlSystemState
import com.example.model.DeviceDiagnostics
import com.example.model.EdgeTriggerPosition
import com.example.model.GlassSettings
import com.example.model.GlassTintPreset
import com.example.service.GlassOverlayBridge
import com.example.service.GlobalSystemAction
import com.example.ui.overlay.GlassOverlayActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val settings: GlassSettings = GlassSettings(),
    val systemState: ControlSystemState = ControlSystemState(),
    val diagnostics: DeviceDiagnostics? = null,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = GlassPreferencesRepository(application)
    private val controlManager = DeviceControlManager(application)

    private val _systemState = MutableStateFlow(controlManager.readSystemState())
    private val _diagnostics = MutableStateFlow(controlManager.getDeviceDiagnostics())
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        prefsRepository.settingsFlow,
        _systemState,
        _diagnostics,
        _snackbarMessage,
        controlManager.torchEnabled
    ) { settings, sys, diag, msg, torchOn ->
        MainUiState(
            settings = settings,
            systemState = sys.copy(flashlightEnabled = torchOn),
            diagnostics = diag,
            snackbarMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            systemState = _systemState.value,
            diagnostics = _diagnostics.value
        )
    )

    fun refreshSystemState() {
        _systemState.value = controlManager.readSystemState()
        _diagnostics.value = controlManager.getDeviceDiagnostics()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun launchGlassOverlay() {
        if (!GlassOverlayBridge.requestShowOverlay()) {
            val app = getApplication<Application>()
            val intent = Intent(app, GlassOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(intent)
        }
    }

    // Hardware & System Actions
    fun onWifiClick() = controlManager.openWifiPanel()
    fun onMobileDataClick() = controlManager.openInternetPanel()
    fun onBluetoothClick() = controlManager.openBluetoothSettings()

    fun onFlashlightClick() {
        if (!controlManager.toggleFlashlight()) {
            _snackbarMessage.value = "Bu cihazda kamera flaşı bulunamadı."
        }
        refreshSystemState()
    }

    fun onBrightnessChange(percent: Int) {
        if (!controlManager.setBrightnessPercent(percent)) {
            _snackbarMessage.value = "Parlaklık ayarı için Sistem Ayarları izni vermelisiniz."
        } else {
            refreshSystemState()
        }
    }

    fun onVolumeChange(percent: Int) {
        controlManager.setMediaVolumePercent(percent)
        refreshSystemState()
    }

    fun onAutoRotateClick() {
        if (!controlManager.toggleAutoRotate()) {
            _snackbarMessage.value = "Otomatik döndürme için Sistem Ayarları izni gerekli."
        } else {
            refreshSystemState()
        }
    }

    fun onRingerModeClick() {
        controlManager.cycleRingerMode()
        refreshSystemState()
    }

    fun onCameraClick() = controlManager.openCamera()
    fun onSystemSettingsClick() = controlManager.openSystemSettings()

    fun onMediaKey(keyCode: Int) {
        controlManager.dispatchMediaKey(keyCode)
    }

    fun onGlobalAction(action: GlobalSystemAction) {
        if (!GlassOverlayBridge.requestGlobalAction(action)) {
            _snackbarMessage.value = "Ekran kilidi ve ekran görüntüsü için Erişilebilirlik Servisini açın."
        }
    }

    // Permission Intents
    fun openAccessibilitySettings() = controlManager.openAccessibilitySettings()
    fun openOverlaySettings() = controlManager.openOverlaySettings()
    fun openWriteSettings() = controlManager.openWriteSettings()

    // Style Preferences
    fun updatePanelAlpha(value: Float) = viewModelScope.launch { prefsRepository.updatePanelAlpha(value) }
    fun updateTileAlpha(value: Float) = viewModelScope.launch { prefsRepository.updateTileAlpha(value) }
    fun updateBlurRadius(value: Int) = viewModelScope.launch { prefsRepository.updateBlurRadius(value) }
    fun updateCornerRadius(value: Int) = viewModelScope.launch { prefsRepository.updateCornerRadius(value) }
    fun updateTintPreset(preset: GlassTintPreset) = viewModelScope.launch { prefsRepository.updateTintPreset(preset) }
    fun updateEdgeTriggerEnabled(enabled: Boolean) = viewModelScope.launch { prefsRepository.updateEdgeTriggerEnabled(enabled) }
    fun updateEdgePosition(pos: EdgeTriggerPosition) = viewModelScope.launch { prefsRepository.updateEdgePosition(pos) }
    fun updateEdgeWidth(widthDp: Int) = viewModelScope.launch { prefsRepository.updateEdgeWidth(widthDp) }
    fun updateEdgeIndicatorVisible(visible: Boolean) = viewModelScope.launch { prefsRepository.updateEdgeIndicatorVisible(visible) }
    fun updateShowMediaCard(show: Boolean) = viewModelScope.launch { prefsRepository.updateShowMediaCard(show) }
    fun updateShowTelemetryPill(show: Boolean) = viewModelScope.launch { prefsRepository.updateShowTelemetryPill(show) }
    fun updateShowSystemActions(show: Boolean) = viewModelScope.launch { prefsRepository.updateShowSystemActions(show) }
    fun resetDefaults() = viewModelScope.launch {
        prefsRepository.resetDefaults()
        _snackbarMessage.value = "Cam görünüm ayarları varsayılana sıfırlandı."
    }

    override fun onCleared() {
        super.onCleared()
        controlManager.release()
    }
}
