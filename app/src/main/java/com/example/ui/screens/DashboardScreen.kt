package com.example.ui.screens

import android.os.Build
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.service.GlobalSystemAction
import com.example.ui.components.GlassControlCenterContent
import com.example.ui.theme.AuroraMint
import com.example.viewmodel.MainUiState

@Composable
fun DashboardScreen(
    uiState: MainUiState,
    onLaunchOverlay: () -> Unit,
    onNavigateToStyle: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenOverlayPermission: () -> Unit,
    onOpenWriteSettings: () -> Unit,
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
    onMediaKey: (Int) -> Unit,
    onGlobalAction: (GlobalSystemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val sys = uiState.systemState
    val diag = uiState.diagnostics
    val readyCount = listOf(
        sys.isAccessibilityEnabled,
        sys.canDrawOverlays,
        sys.canWriteSettings
    ).count { it }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cam Kontrol Merkezi",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = diag?.let {
                                    "${it.manufacturer} ${it.model} • Android ${it.androidVersion}"
                                } ?: "HyperOS Şeffaf Eklenti",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (sys.isAccessibilityEnabled) {
                                AuroraMint.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            border = BorderStroke(
                                1.dp,
                                if (sys.isAccessibilityEnabled) AuroraMint.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (sys.isAccessibilityEnabled) "Aktif ($readyCount/3)" else "Kurulum ($readyCount/3)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (sys.isAccessibilityEnabled) AuroraMint else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = if (diag?.crossWindowBlurEnabled == true) {
                            "Sistem pencere bulanıklığı destekleniyor. Gerçek zamanlı şeffaf cam katmanı hazır."
                        } else {
                            "Cihazınızda sistem bulanıklığı (Advanced Texture) kapalı. Özel çok katmanlı şeffaf cam motorumuz tüm uygulamalarda şeffaf görünüm sağlar."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onLaunchOverlay,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("launch_overlay_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Şeffaf Paneli Aç")
                        }

                        FilledTonalButton(
                            onClick = onNavigateToStyle,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("open_style_studio_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Görünüm Ayarları",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Interactive Glass Preview over Hero Artwork
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Canlı Cam Önizleme",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Şeffaflık %${((1f - uiState.settings.panelAlpha) * 100).toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    val backdropBlur = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        (uiState.settings.blurRadius * 0.35f).dp
                    } else {
                        0.dp
                    }

                    Image(
                        painter = painterResource(id = R.drawable.img_glass_hero),
                        contentDescription = "Cam kontrol merkezi arka plan önizlemesi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .then(if (backdropBlur > 0.dp) Modifier.blur(backdropBlur) else Modifier)
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )

                    GlassControlCenterContent(
                        settings = uiState.settings,
                        systemState = uiState.systemState,
                        isOverlayMode = false,
                        onWifiClick = onWifiClick,
                        onMobileDataClick = onMobileDataClick,
                        onBluetoothClick = onBluetoothClick,
                        onFlashlightClick = onFlashlightClick,
                        onBrightnessChange = onBrightnessChange,
                        onVolumeChange = onVolumeChange,
                        onAutoRotateClick = onAutoRotateClick,
                        onRingerModeClick = onRingerModeClick,
                        onCameraClick = onCameraClick,
                        onSystemSettingsClick = onSystemSettingsClick,
                        onMediaPrev = { onMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                        onMediaPlayPause = { onMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                        onMediaNext = { onMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) },
                        onGlobalAction = onGlobalAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }
        }

        // 3-Step Activation Checklist
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Sistem Entegrasyonu ve İzinler",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                PermissionChecklistCard(
                    title = "1. Erişilebilirlik Servisi (Kenar Çekme & Kısayollar)",
                    subtitle = "Sağ üst köşeden aşağı çekerek şeffaf kontrol merkezini açar, ekran kilidi ve ekran görüntüsü kısayollarını çalıştırır.",
                    granted = sys.isAccessibilityEnabled,
                    icon = Icons.Rounded.AccessibilityNew,
                    buttonLabel = if (sys.isAccessibilityEnabled) "Yönet" else "Etkinleştir",
                    testTag = "perm_accessibility_button",
                    onClick = onOpenAccessibility
                )

                PermissionChecklistCard(
                    title = "2. Diğer Uygulamaların Üzerinde Gösterme",
                    subtitle = "Diğer uygulamalar açıkken kesintisiz üst katman görünürlüğü sağlar.",
                    granted = sys.canDrawOverlays,
                    icon = Icons.Rounded.Layers,
                    buttonLabel = if (sys.canDrawOverlays) "İzin Verildi" else "İzin Ver",
                    testTag = "perm_overlay_button",
                    onClick = onOpenOverlayPermission
                )

                PermissionChecklistCard(
                    title = "3. Sistem Ayarlarını Değiştirme",
                    subtitle = "Cam paneldeki ekran parlaklığı kaydırıcısı ve otomatik döndürme anahtarı için gereklidir.",
                    granted = sys.canWriteSettings,
                    icon = Icons.Rounded.Tune,
                    buttonLabel = if (sys.canWriteSettings) "İzin Verildi" else "İzin Ver",
                    testTag = "perm_write_settings_button",
                    onClick = onOpenWriteSettings
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hızlı Ayarlar Karesi (Quick Tile)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "HyperOS Kontrol Merkezini düzenleyip \"Şeffaf Kontrol\" düğmesini ekleyerek tek dokunuşla da açabilirsiniz.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionChecklistCard(
    title: String,
    subtitle: String,
    granted: Boolean,
    icon: ImageVector,
    buttonLabel: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (granted) AuroraMint.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = if (granted) "İzin verildi" else "İzin bekleniyor",
                tint = if (granted) AuroraMint else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = onClick,
                modifier = Modifier
                    .height(48.dp)
                    .testTag(testTag),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(buttonLabel, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
