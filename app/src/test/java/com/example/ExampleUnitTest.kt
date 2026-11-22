package com.example

import com.example.model.EdgeTriggerPosition
import com.example.model.GlassSettings
import com.example.model.GlassTintPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun glassSettingsDefaults_areWithinValidBounds() {
        val defaults = GlassSettings()
        assertTrue(defaults.panelAlpha in 0.12f..0.92f)
        assertTrue(defaults.tileAlpha in 0.15f..0.92f)
        assertTrue(defaults.blurRadius in 0..60)
        assertTrue(defaults.cornerRadiusDp in 12..34)
        assertEquals(GlassTintPreset.ICE_OBSIDIAN, defaults.tintPreset)
        assertEquals(EdgeTriggerPosition.TOP_RIGHT, defaults.edgeTriggerPosition)
    }

    @Test
    fun tintPresetAndEdgePosition_resolveSafely() {
        assertEquals(GlassTintPreset.HYPER_BLUE, GlassTintPreset.fromId("hyper_blue"))
        assertEquals(GlassTintPreset.ICE_OBSIDIAN, GlassTintPreset.fromId("invalid_id"))
        assertEquals(EdgeTriggerPosition.BOTH, EdgeTriggerPosition.fromId("both"))
        assertEquals(EdgeTriggerPosition.TOP_RIGHT, EdgeTriggerPosition.fromId(null))
    }
}
