package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.EdgeTriggerPosition
import com.example.model.GlassTintPreset
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Şeffaf Kontrol", appName)
    }

    @Test
    fun `tint preset and edge position resolve safely`() {
        assertEquals(GlassTintPreset.HYPER_BLUE, GlassTintPreset.fromId("hyper_blue"))
        assertEquals(GlassTintPreset.ICE_OBSIDIAN, GlassTintPreset.fromId("unknown"))
        assertEquals(EdgeTriggerPosition.BOTH, EdgeTriggerPosition.fromId("both"))
        assertEquals(EdgeTriggerPosition.TOP_RIGHT, EdgeTriggerPosition.fromId(null))
    }
}
