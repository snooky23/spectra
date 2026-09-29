package com.spectra

import com.spectra.logger.feature.events.model.EventType
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpectraTest {
    @Test
    fun testSpectraFacadeAccess() {
        assertNotNull(Spectra.logger)
        assertTrue(Spectra.version.isNotEmpty())
    }

    @Test
    fun testSpectraFacadeEvent() {
        Spectra.event(
            name = "umbrella_test_event",
            parameters = mapOf("source" to "SpectraTest"),
            eventType = EventType.CUSTOM,
        )
    }
}
