package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.EdgeTriggerPosition
import com.example.model.GlassSettings
import com.example.model.GlassTintPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.glassDataStore: DataStore<Preferences> by preferencesDataStore(name = "glass_control_prefs")

class GlassPreferencesRepository(private val context: Context) {

    private object Keys {
        val PANEL_ALPHA = floatPreferencesKey("panel_alpha")
        val TILE_ALPHA = floatPreferencesKey("tile_alpha")
        val BLUR_RADIUS = intPreferencesKey("blur_radius")
        val CORNER_RADIUS = intPreferencesKey("corner_radius")
        val TINT_PRESET = stringPreferencesKey("tint_preset")
        val EDGE_TRIGGER_ENABLED = booleanPreferencesKey("edge_trigger_enabled")
        val EDGE_TRIGGER_POSITION = stringPreferencesKey("edge_trigger_position")
        val EDGE_TRIGGER_WIDTH = intPreferencesKey("edge_trigger_width")
        val EDGE_INDICATOR_VISIBLE = booleanPreferencesKey("edge_indicator_visible")
        val SHOW_MEDIA_CARD = booleanPreferencesKey("show_media_card")
        val SHOW_TELEMETRY_PILL = booleanPreferencesKey("show_telemetry_pill")
        val SHOW_SYSTEM_ACTIONS = booleanPreferencesKey("show_system_actions")
    }

    val settingsFlow: Flow<GlassSettings> = context.glassDataStore.data.map { prefs ->
        GlassSettings(
            panelAlpha = (prefs[Keys.PANEL_ALPHA] ?: 0.44f).coerceIn(0.12f, 0.92f),
            tileAlpha = (prefs[Keys.TILE_ALPHA] ?: 0.52f).coerceIn(0.15f, 0.92f),
            blurRadius = (prefs[Keys.BLUR_RADIUS] ?: 34).coerceIn(0, 60),
            cornerRadiusDp = (prefs[Keys.CORNER_RADIUS] ?: 24).coerceIn(12, 34),
            tintPreset = GlassTintPreset.fromId(prefs[Keys.TINT_PRESET]),
            edgeTriggerEnabled = prefs[Keys.EDGE_TRIGGER_ENABLED] ?: true,
            edgeTriggerPosition = EdgeTriggerPosition.fromId(prefs[Keys.EDGE_TRIGGER_POSITION]),
            edgeTriggerWidthDp = (prefs[Keys.EDGE_TRIGGER_WIDTH] ?: 96).coerceIn(48, 160),
            edgeIndicatorVisible = prefs[Keys.EDGE_INDICATOR_VISIBLE] ?: true,
            showMediaCard = prefs[Keys.SHOW_MEDIA_CARD] ?: true,
            showTelemetryPill = prefs[Keys.SHOW_TELEMETRY_PILL] ?: true,
            showSystemActions = prefs[Keys.SHOW_SYSTEM_ACTIONS] ?: true
        )
    }

    suspend fun updatePanelAlpha(alpha: Float) {
        context.glassDataStore.edit { it[Keys.PANEL_ALPHA] = alpha.coerceIn(0.12f, 0.92f) }
    }

    suspend fun updateTileAlpha(alpha: Float) {
        context.glassDataStore.edit { it[Keys.TILE_ALPHA] = alpha.coerceIn(0.15f, 0.92f) }
    }

    suspend fun updateBlurRadius(radius: Int) {
        context.glassDataStore.edit { it[Keys.BLUR_RADIUS] = radius.coerceIn(0, 60) }
    }

    suspend fun updateCornerRadius(radiusDp: Int) {
        context.glassDataStore.edit { it[Keys.CORNER_RADIUS] = radiusDp.coerceIn(12, 34) }
    }

    suspend fun updateTintPreset(preset: GlassTintPreset) {
        context.glassDataStore.edit { it[Keys.TINT_PRESET] = preset.id }
    }

    suspend fun updateEdgeTriggerEnabled(enabled: Boolean) {
        context.glassDataStore.edit { it[Keys.EDGE_TRIGGER_ENABLED] = enabled }
    }

    suspend fun updateEdgePosition(position: EdgeTriggerPosition) {
        context.glassDataStore.edit { it[Keys.EDGE_TRIGGER_POSITION] = position.id }
    }

    suspend fun updateEdgeWidth(widthDp: Int) {
        context.glassDataStore.edit { it[Keys.EDGE_TRIGGER_WIDTH] = widthDp.coerceIn(48, 160) }
    }

    suspend fun updateEdgeIndicatorVisible(visible: Boolean) {
        context.glassDataStore.edit { it[Keys.EDGE_INDICATOR_VISIBLE] = visible }
    }

    suspend fun updateShowMediaCard(show: Boolean) {
        context.glassDataStore.edit { it[Keys.SHOW_MEDIA_CARD] = show }
    }

    suspend fun updateShowTelemetryPill(show: Boolean) {
        context.glassDataStore.edit { it[Keys.SHOW_TELEMETRY_PILL] = show }
    }

    suspend fun updateShowSystemActions(show: Boolean) {
        context.glassDataStore.edit { it[Keys.SHOW_SYSTEM_ACTIONS] = show }
    }

    suspend fun resetDefaults() {
        context.glassDataStore.edit { it.clear() }
    }
}
