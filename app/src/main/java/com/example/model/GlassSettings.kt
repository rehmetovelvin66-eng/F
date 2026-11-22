package com.example.model

import androidx.compose.ui.graphics.Color

enum class GlassTintPreset(
    val id: String,
    val label: String,
    val baseColor: Color,
    val accentColor: Color,
    val borderGlow: Color
) {
    ICE_OBSIDIAN(
        id = "ice_obsidian",
        label = "Buzul Koyu",
        baseColor = Color(0xFF0C1424),
        accentColor = Color(0xFF38BDF8),
        borderGlow = Color(0xFF7DD3FC)
    ),
    HYPER_BLUE(
        id = "hyper_blue",
        label = "HyperOS Mavi",
        baseColor = Color(0xFF0A1D3B),
        accentColor = Color(0xFF60A5FA),
        borderGlow = Color(0xFF93C5FD)
    ),
    PURE_CRYSTAL(
        id = "pure_crystal",
        label = "Saf Kristal",
        baseColor = Color(0xFF1E293B),
        accentColor = Color(0xFFE2E8F0),
        borderGlow = Color(0xFFFFFFFF)
    ),
    AURORA_VIOLET(
        id = "aurora_violet",
        label = "Mor Aurora",
        baseColor = Color(0xFF1B1236),
        accentColor = Color(0xFFA78BFA),
        borderGlow = Color(0xFFC4B5FD)
    ),
    WARM_AMBER(
        id = "warm_amber",
        label = "Kehribar Cam",
        baseColor = Color(0xFF23180D),
        accentColor = Color(0xFFFBBF24),
        borderGlow = Color(0xFFFDE68A)
    );

    companion object {
        fun fromId(id: String?): GlassTintPreset =
            entries.firstOrNull { it.id == id } ?: ICE_OBSIDIAN
    }
}

enum class EdgeTriggerPosition(val id: String, val label: String) {
    TOP_RIGHT("top_right", "Sağ Üst"),
    TOP_LEFT("top_left", "Sol Üst"),
    BOTH("both", "Her İki Köşe");

    companion object {
        fun fromId(id: String?): EdgeTriggerPosition =
            entries.firstOrNull { it.id == id } ?: TOP_RIGHT
    }
}

data class GlassSettings(
    val panelAlpha: Float = 0.44f,
    val tileAlpha: Float = 0.52f,
    val blurRadius: Int = 34,
    val cornerRadiusDp: Int = 24,
    val tintPreset: GlassTintPreset = GlassTintPreset.ICE_OBSIDIAN,
    val edgeTriggerEnabled: Boolean = true,
    val edgeTriggerPosition: EdgeTriggerPosition = EdgeTriggerPosition.TOP_RIGHT,
    val edgeTriggerWidthDp: Int = 96,
    val edgeIndicatorVisible: Boolean = true,
    val showMediaCard: Boolean = true,
    val showTelemetryPill: Boolean = true,
    val showSystemActions: Boolean = true
)
