package com.spectra.logger.feature.events

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpectraLoggerEventTest {
    private val testScope = TestScope()

    @BeforeTest
    fun setUp() {
        SpectraLogger.setCoroutineScopeForTesting(testScope)
    }

    @AfterTest
    fun tearDown() {
        SpectraLogger.resetCoroutineScopeForTesting()
    }

    @Test
    fun testLogCustomEvent() =
        runTest {
            SpectraLogger.clearEvents()

            SpectraLogger.event(
                name = "login_button_click",
                parameters = mapOf("screen" to "AuthScreen", "method" to "google"),
                eventType = EventType.USER_ACTION,
            )

            testScope.advanceUntilIdle()

            val events = SpectraLogger.queryEvents()
            assertEquals(1, events.size)
            val event = events.first()
            assertEquals("login_button_click", event.name)
            assertEquals(EventType.USER_ACTION, event.eventType)
            assertEquals("google", event.parameters["method"])
        }

    @Test
    fun testScreenTrackingDuration() =
        runTest {
            SpectraLogger.clearEvents()

            SpectraLogger.screenStart("CheckoutScreen", mapOf("cart_items" to "3"))
            SpectraLogger.screenEnd("CheckoutScreen", mapOf("completed" to "true"))

            testScope.advanceUntilIdle()

            val events = SpectraLogger.queryEvents()
            assertEquals(1, events.size)
            val screenEvent = events.first()
            assertEquals("CheckoutScreen", screenEvent.name)
            assertEquals(EventType.SCREEN_VIEW, screenEvent.eventType)
            assertEquals("3", screenEvent.parameters["cart_items"])
            assertEquals("true", screenEvent.parameters["completed"])
            assertNotNull(screenEvent.durationMs)
            assertTrue(screenEvent.durationMs!! >= 0)
        }

    @Test
    fun testQueryEventsWithFilter() =
        runTest {
            SpectraLogger.clearEvents()

            SpectraLogger.event("cart_add", eventType = EventType.USER_ACTION)
            SpectraLogger.event("app_backgrounded", eventType = EventType.LIFECYCLE)

            testScope.advanceUntilIdle()

            val userActions = SpectraLogger.queryEvents(EventFilter(eventTypes = setOf(EventType.USER_ACTION)))
            assertEquals(1, userActions.size)
            assertEquals("cart_add", userActions.first().name)

            val lifecycleEvents = SpectraLogger.queryEvents(EventFilter(eventTypes = setOf(EventType.LIFECYCLE)))
            assertEquals(1, lifecycleEvents.size)
            assertEquals("app_backgrounded", lifecycleEvents.first().name)
        }
}
