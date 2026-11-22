package com.example.ui.components

import android.media.AudioManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlSystemState
import com.example.model.GlassSettings
import com.example.service.GlobalSystemAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GlassControlCenterContent(
    settings: GlassSettings,
    systemState: ControlSystemState,
    isOverlayMode: Boolean,
    onWifiClick: () -> Unit,
    onMobileDataClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onFlashlightClick: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onAutoRotateClick: () -> Unit,
    onRingerModeClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSystemSettingsClick: () -> Unit,
    onMediaPrev: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onGlobalAction: (GlobalSystemAction) -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tint = settings.tintPreset
    val cornerRadius = settings.cornerRadiusDp.dp
    val innerTileRadius = (settings.cornerRadiusDp * 0.72f).coerceAtLeast(10f).dp

    val panelBrush = Brush.verticalGradient(
        colors = listOf(
            tint.baseColor.copy(alpha = (settings.panelAlpha + 0.08f).coerceAtMost(0.95f)),
            tint.baseColor.copy(alpha = settings.panelAlpha)
        )
    )
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            tint.borderGlow.copy(alpha = 0.42f),
            Color.White.copy(alpha = 0.12f),
            tint.accentColor.copy(alpha = 0.24f)
        )
    )

    val timeStr = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val dateStr = remember {
        SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR")).format(Date())
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(panelBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header: Clock + Telemetry Pill + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settings.showTelemetryPill) {
                        GlassTelemetryPill(
                            batteryPercent = systemState.batteryPercent,
                            isCharging = systemState.isCharging,
                            ramText = systemState.availableRamText,
                            tileAlpha = settings.tileAlpha,
                            baseColor = tint.baseColor,
                            accentColor = tint.accentColor
                        )
                    }

                    if (isOverlayMode && onClose != null) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .testTag("overlay_close_button")
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Kontrol merkezini kapat",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 2x2 Connectivity & Torch Bento Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassFeatureTile(
                    title = "Wi-Fi",
                    subtitle = if (systemState.wifiEnabled) "Bağlı" else "Kapalı",
                    icon = if (systemState.wifiEnabled) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                    active = systemState.wifiEnabled,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    cornerRadius = innerTileRadius,
                    testTag = "tile_wifi",
                    onClick = onWifiClick,
                    modifier = Modifier.weight(1f)
                )
                GlassFeatureTile(
                    title = "İnternet",
                    subtitle = "Hızlı Panel",
                    icon = Icons.Rounded.CellTower,
                    active = false,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    cornerRadius = innerTileRadius,
                    testTag = "tile_mobile_data",
                    onClick = onMobileDataClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassFeatureTile(
                    title = "Bluetooth",
                    subtitle = if (systemState.bluetoothEnabled) "Açık" else "Ayarlar",
                    icon = if (systemState.bluetoothEnabled) Icons.Rounded.Bluetooth else Icons.Rounded.BluetoothDisabled,
                    active = systemState.bluetoothEnabled,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    cornerRadius = innerTileRadius,
                    testTag = "tile_bluetooth",
                    onClick = onBluetoothClick,
                    modifier = Modifier.weight(1f)
                )
                GlassFeatureTile(
                    title = "El Feneri",
                    subtitle = if (systemState.flashlightEnabled) "Açık" else "Kapalı",
                    icon = if (systemState.flashlightEnabled) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                    active = systemState.flashlightEnabled,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    cornerRadius = innerTileRadius,
                    testTag = "tile_flashlight",
                    onClick = onFlashlightClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Brightness & Volume Glass Sliders
            GlassControlSliderRow(
                label = "Ekran Parlaklığı",
                valuePercent = systemState.brightnessPercent,
                icon = Icons.Rounded.BrightnessHigh,
                tileAlpha = settings.tileAlpha,
                baseColor = tint.baseColor,
                accentColor = tint.accentColor,
                cornerRadius = innerTileRadius,
                helperBadge = if (!systemState.canWriteSettings) "İzin gerekli" else null,
                testTag = "slider_brightness",
                onValueChange = onBrightnessChange
            )

            GlassControlSliderRow(
                label = "Medya Sesi",
                valuePercent = systemState.mediaVolumePercent,
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                tileAlpha = settings.tileAlpha,
                baseColor = tint.baseColor,
                accentColor = tint.accentColor,
                cornerRadius = innerTileRadius,
                helperBadge = null,
                testTag = "slider_volume",
                onValueChange = onVolumeChange
            )

            // Media Playback Glass Card
            if (settings.showMediaCard) {
                GlassMediaPlayerCard(
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    cornerRadius = innerTileRadius,
                    onPrev = onMediaPrev,
                    onPlayPause = onMediaPlayPause,
                    onNext = onMediaNext
                )
            }

            // Quick Utility Circle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCircleShortcut(
                    label = "Döndür",
                    icon = if (systemState.autoRotateEnabled) Icons.Rounded.ScreenRotation else Icons.Rounded.ScreenLockRotation,
                    active = systemState.autoRotateEnabled,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    testTag = "shortcut_autorotate",
                    onClick = onAutoRotateClick,
                    modifier = Modifier.weight(1f)
                )
                GlassCircleShortcut(
                    label = if (systemState.ringerMode == AudioManager.RINGER_MODE_VIBRATE) "Titreşim" else "Sesli",
                    icon = if (systemState.ringerMode == AudioManager.RINGER_MODE_VIBRATE) Icons.Rounded.Vibration else Icons.Rounded.NotificationsActive,
                    active = systemState.ringerMode == AudioManager.RINGER_MODE_VIBRATE,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    testTag = "shortcut_ringer",
                    onClick = onRingerModeClick,
                    modifier = Modifier.weight(1f)
                )
                GlassCircleShortcut(
                    label = "Kamera",
                    icon = Icons.Rounded.PhotoCamera,
                    active = false,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    testTag = "shortcut_camera",
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f)
                )
                GlassCircleShortcut(
                    label = "Ayarlar",
                    icon = Icons.Rounded.Settings,
                    active = false,
                    tileAlpha = settings.tileAlpha,
                    baseColor = tint.baseColor,
                    accentColor = tint.accentColor,
                    testTag = "shortcut_settings",
                    onClick = onSystemSettingsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // System Global Actions Row
            if (settings.showSystemActions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCircleShortcut(
                        label = "Kilitle",
                        icon = Icons.Rounded.Lock,
                        active = false,
                        tileAlpha = settings.tileAlpha,
                        baseColor = tint.baseColor,
                        accentColor = tint.accentColor,
                        testTag = "action_lock_screen",
                        onClick = { onGlobalAction(GlobalSystemAction.LOCK_SCREEN) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassCircleShortcut(
                        label = "Ekran G.",
                        icon = Icons.Rounded.Screenshot,
                        active = false,
                        tileAlpha = settings.tileAlpha,
                        baseColor = tint.baseColor,
                        accentColor = tint.accentColor,
                        testTag = "action_screenshot",
                        onClick = { onGlobalAction(GlobalSystemAction.TAKE_SCREENSHOT) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassCircleShortcut(
                        label = "Bildirim",
                        icon = Icons.Rounded.Notifications,
                        active = false,
                        tileAlpha = settings.tileAlpha,
                        baseColor = tint.baseColor,
                        accentColor = tint.accentColor,
                        testTag = "action_notifications",
                        onClick = { onGlobalAction(GlobalSystemAction.NOTIFICATIONS) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassCircleShortcut(
                        label = "Güç",
                        icon = Icons.Rounded.PowerSettingsNew,
                        active = false,
                        tileAlpha = settings.tileAlpha,
                        baseColor = tint.baseColor,
                        accentColor = tint.accentColor,
                        testTag = "action_power",
                        onClick = { onGlobalAction(GlobalSystemAction.POWER_DIALOG) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassTelemetryPill(
    batteryPercent: Int,
    isCharging: Boolean,
    ramText: String,
    tileAlpha: Float,
    baseColor: Color,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = baseColor.copy(alpha = (tileAlpha + 0.12f).coerceAtMost(0.85f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                    contentDescription = "Pil seviyesi",
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "%$batteryPercent",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Memory,
                    contentDescription = "Boş bellek",
                    tint = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = ramText,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun GlassFeatureTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    active: Boolean,
    tileAlpha: Float,
    baseColor: Color,
    accentColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) {
            accentColor.copy(alpha = (tileAlpha + 0.26f).coerceAtMost(0.92f))
        } else {
            baseColor.copy(alpha = tileAlpha)
        },
        animationSpec = spring(),
        label = "tileBg"
    )

    val borderColor = if (active) {
        Color.White.copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.14f)
    }

    Row(
        modifier = modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (active) Color.White.copy(alpha = 0.25f)
                    else Color.White.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GlassControlSliderRow(
    label: String,
    valuePercent: Int,
    icon: ImageVector,
    tileAlpha: Float,
    baseColor: Color,
    accentColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    helperBadge: String?,
    testTag: String,
    onValueChange: (Int) -> Unit
) {
    var sliderPos by remember(valuePercent) { mutableFloatStateOf(valuePercent.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(baseColor.copy(alpha = tileAlpha))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(cornerRadius))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (helperBadge != null) {
                    Text(
                        text = helperBadge,
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = "%${sliderPos.roundToInt()}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Slider(
            value = sliderPos,
            onValueChange = {
                sliderPos = it
                onValueChange(it.roundToInt())
            },
            valueRange = 0f..100f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.20f)
            )
        )
    }
}

@Composable
private fun GlassMediaPlayerCard(
    tileAlpha: Float,
    baseColor: Color,
    accentColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(baseColor.copy(alpha = tileAlpha))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(cornerRadius))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "Medya oynatıcı",
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "Medya Kontrolü",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Sistem oynatıcı kısayolları",
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onPrev,
                modifier = Modifier
                    .testTag("media_prev_button")
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Önceki parça",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .testTag("media_play_pause_button")
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.30f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Oynat veya duraklat",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .testTag("media_next_button")
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Sonraki parça",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun GlassCircleShortcut(
    label: String,
    icon: ImageVector,
    active: Boolean,
    tileAlpha: Float,
    baseColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (active) accentColor.copy(alpha = (tileAlpha + 0.25f).coerceAtMost(0.92f))
                    else baseColor.copy(alpha = tileAlpha)
                )
                .border(
                    width = 1.dp,
                    color = if (active) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.14f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
