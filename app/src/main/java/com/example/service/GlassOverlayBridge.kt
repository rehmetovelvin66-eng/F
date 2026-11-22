package com.example.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GlobalSystemAction {
    LOCK_SCREEN,
    TAKE_SCREENSHOT,
    NOTIFICATIONS,
    POWER_DIALOG
}

sealed interface OverlayCommand {
    data object ShowControlCenter : OverlayCommand
    data object HideControlCenter : OverlayCommand
    data class PerformGlobalAction(val action: GlobalSystemAction) : OverlayCommand
}

object GlassOverlayBridge {
    private val _serviceConnected = MutableStateFlow(false)
    val serviceConnected: StateFlow<Boolean> = _serviceConnected.asStateFlow()

    private val _commands = MutableSharedFlow<OverlayCommand>(extraBufferCapacity = 8)
    val commands: SharedFlow<OverlayCommand> = _commands.asSharedFlow()

    fun setServiceConnected(connected: Boolean) {
        _serviceConnected.value = connected
    }

    fun requestShowOverlay(): Boolean {
        if (!_serviceConnected.value) return false
        return _commands.tryEmit(OverlayCommand.ShowControlCenter)
    }

    fun requestHideOverlay() {
        _commands.tryEmit(OverlayCommand.HideControlCenter)
    }

    fun requestGlobalAction(action: GlobalSystemAction): Boolean {
        if (!_serviceConnected.value) return false
        return _commands.tryEmit(OverlayCommand.PerformGlobalAction(action))
    }
}
