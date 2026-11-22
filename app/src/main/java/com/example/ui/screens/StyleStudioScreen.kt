package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.EdgeTriggerPosition
import com.example.model.GlassSettings
import com.example.model.GlassTintPreset
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleStudioScreen(
    settings: GlassSettings,
    onPanelAlphaChange: (Float) -> Unit,
    onTileAlphaChange: (Float) -> Unit,
    onBlurRadiusChange: (Int) -> Unit,
    onCornerRadiusChange: (Int) -> Unit,
    onTintSelected: (GlassTintPreset) -> Unit,
    onEdgeTriggerEnabledChange: (Boolean) -> Unit,
    onEdgePositionChange: (EdgeTriggerPosition) -> Unit,
    onEdgeWidthChange: (Int) -> Unit,
    onEdgeIndicatorVisibleChange: (Boolean) -> Unit,
    onShowMediaCardChange: (Boolean) -> Unit,
    onShowTelemetryPillChange: (Boolean) -> Unit,
    onShowSystemActionsChange: (Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Cam & Şeffaflık Ayarları",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    StyleSliderItem(
                        label = "Arka Plan Opaklığı",
                        valueLabel = "%${(settings.panelAlpha * 100).roundToInt()}",
                        value = settings.panelAlpha,
                        range = 0.12f..0.90f,
                        testTag = "studio_panel_alpha_slider",
                        onValueChange = onPanelAlphaChange
                    )

                    StyleSliderItem(
                        label = "Kart ve Düğme Opaklığı",
                        valueLabel = "%${(settings.tileAlpha * 100).roundToInt()}",
                        value = settings.tileAlpha,
                        range = 0.15f..0.90f,
                        testTag = "studio_tile_alpha_slider",
                        onValueChange = onTileAlphaChange
                    )

                    StyleSliderItem(
                        label = "Buzlu Cam Bulanıklığı (Blur)",
                        valueLabel = "${settings.blurRadius} dp",
                        value = settings.blurRadius.toFloat(),
                        range = 0f..60f,
                        testTag = "studio_blur_slider",
                        onValueChange = { onBlurRadiusChange(it.roundToInt()) }
                    )

                    StyleSliderItem(
                        label = "Köşe Yuvarlaklığı",
                        valueLabel = "${settings.cornerRadiusDp} dp",
                        value = settings.cornerRadiusDp.toFloat(),
                        range = 12f..34f,
                        testTag = "studio_corner_slider",
                        onValueChange = { onCornerRadiusChange(it.roundToInt()) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cam Renk Tonu",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassTintPreset.entries.forEach { preset ->
                            val selected = preset == settings.tintPreset
                            Row(
                                modifier = Modifier
                                    .testTag("tint_preset_${preset.id}")
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) preset.accentColor.copy(alpha = 0.16f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    )
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) preset.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onTintSelected(preset) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(preset.baseColor)
                                        .border(2.dp, preset.accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Kenar Çekme Tetikleyicisi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    StyleSwitchRow(
                        title = "Üst Kenar Çekme Aktif",
                        subtitle = "Erişilebilirlik servisi açıkken üst köşeden çekerek açılır",
                        checked = settings.edgeTriggerEnabled,
                        testTag = "switch_edge_trigger",
                        onCheckedChange = onEdgeTriggerEnabledChange
                    )

                    if (settings.edgeTriggerEnabled) {
                        Text(
                            text = "Tetikleme Konumu",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EdgeTriggerPosition.entries.forEach { pos ->
                                FilterChip(
                                    selected = settings.edgeTriggerPosition == pos,
                                    onClick = { onEdgePositionChange(pos) },
                                    label = { Text(pos.label) },
                                    modifier = Modifier.testTag("edge_pos_${pos.id}")
                                )
                            }
                        }

                        StyleSliderItem(
                            label = "Tetikleme Bölgesi Genişliği",
                            valueLabel = "${settings.edgeTriggerWidthDp} dp",
                            value = settings.edgeTriggerWidthDp.toFloat(),
                            range = 48f..160f,
                            testTag = "studio_edge_width_slider",
                            onValueChange = { onEdgeWidthChange(it.roundToInt()) }
                        )

                        StyleSwitchRow(
                            title = "İnce Kenar Çizgisi Göster",
                            subtitle = "Tetikleme alanını gösteren yarı saydam beyaz çizgi",
                            checked = settings.edgeIndicatorVisible,
                            testTag = "switch_edge_indicator",
                            onCheckedChange = onEdgeIndicatorVisibleChange
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Panel Modülleri",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    StyleSwitchRow(
                        title = "Medya Kontrol Kartı",
                        subtitle = "Oynat/Duraklat ve parça değiştirme kontrolleri",
                        checked = settings.showMediaCard,
                        testTag = "switch_media_card",
                        onCheckedChange = onShowMediaCardChange
                    )

                    StyleSwitchRow(
                        title = "Pil ve RAM Bilgi Rozeti",
                        subtitle = "Üst kısımda anlık pil yüzdesi ve boş bellek",
                        checked = settings.showTelemetryPill,
                        testTag = "switch_telemetry_pill",
                        onCheckedChange = onShowTelemetryPillChange
                    )

                    StyleSwitchRow(
                        title = "Sistem Kısayolları Satırı",
                        subtitle = "Ekran Kilitle, Ekran Görüntüsü, Bildirim ve Güç Menüsü",
                        checked = settings.showSystemActions,
                        testTag = "switch_system_actions",
                        onCheckedChange = onShowSystemActionsChange
                    )

                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_defaults_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Varsayılan Ayarlara Sıfırla")
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleSliderItem(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

@Composable
private fun StyleSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
