package com.spectra.logger.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.events.model.EventType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {

    var isTrackingProductDetails by mutableStateOf(false)
        private set

    fun toggleProductDetailsScreenView() {
        if (!isTrackingProductDetails) {
            isTrackingProductDetails = true
            SpectraLogger.screenStart(
                screenName = "ProductDetailsScreen",
                parameters = mapOf(
                    "product_id" to "PROD-9021",
                    "category" to "Electronics",
                    "source" to "HomeCarousel"
                )
            )
        } else {
            isTrackingProductDetails = false
            SpectraLogger.screenEnd(
                screenName = "ProductDetailsScreen",
                additionalParameters = mapOf(
                    "scroll_depth" to "92%",
                    "action_taken" to "added_to_wishlist"
                )
            )
        }
    }

    fun simulateQuickScreenView() {
        viewModelScope.launch {
            SpectraLogger.screenStart(
                screenName = "CartCheckoutScreen",
                parameters = mapOf("cart_items_count" to "3")
            )
            delay(1500)
            SpectraLogger.screenEnd(
                screenName = "CartCheckoutScreen",
                additionalParameters = mapOf("checkout_step" to "shipping_selection")
            )
        }
    }

    fun logAddToCart() {
        SpectraLogger.event(
            name = "add_to_cart",
            parameters = mapOf(
                "item_id" to "SKU-4928",
                "item_name" to "Noise-Cancelling Headphones",
                "price" to "249.99",
                "currency" to "USD"
            ),
            eventType = EventType.USER_ACTION
        )
    }

    fun logLikePost() {
        SpectraLogger.event(
            name = "like_post",
            parameters = mapOf(
                "post_id" to "post_84920",
                "author" to "Avi Levin",
                "reaction" to "heart"
            ),
            eventType = EventType.USER_ACTION
        )
    }

    fun logSearchSubmitted() {
        SpectraLogger.event(
            name = "search_submitted",
            parameters = mapOf(
                "query" to "kotlin multiplatform",
                "filter_count" to "2",
                "results_count" to "42"
            ),
            eventType = EventType.USER_ACTION
        )
    }

    fun logAppBackground() {
        SpectraLogger.event(
            name = "app_backgrounded",
            parameters = mapOf(
                "active_session_sec" to "320",
                "last_visible_screen" to "EventsScreen"
            ),
            eventType = EventType.LIFECYCLE
        )
    }

    fun logAppForeground() {
        SpectraLogger.event(
            name = "app_foregrounded",
            parameters = mapOf(
                "cold_start" to "false",
                "time_in_background_sec" to "14"
            ),
            eventType = EventType.LIFECYCLE
        )
    }

    fun logLowMemoryWarning() {
        SpectraLogger.event(
            name = "low_memory_warning",
            parameters = mapOf(
                "available_ram_mb" to "78",
                "total_ram_mb" to "4096",
                "level" to "TRIM_MEMORY_RUNNING_CRITICAL"
            ),
            eventType = EventType.LIFECYCLE
        )
    }

    fun logPurchaseCompleted() {
        SpectraLogger.event(
            name = "purchase_completed",
            parameters = mapOf(
                "order_id" to "ORD-2026-8819",
                "amount" to "249.99",
                "payment_method" to "GooglePay",
                "items_count" to "1",
                "shipping_country" to "US"
            ),
            eventType = EventType.CUSTOM
        )
    }

    fun logFeatureFlagEvaluated() {
        SpectraLogger.event(
            name = "feature_flag_evaluated",
            parameters = mapOf(
                "flag_key" to "events_ui_redesign",
                "variant" to "treatment_v2",
                "evaluation_reason" to "TARGETING_RULE_MATCH"
            ),
            eventType = EventType.CUSTOM
        )
    }

    fun generate20MixedEvents() {
        viewModelScope.launch {
            val eventTemplates = listOf(
                Triple("user_tab_clicked", mapOf("tab" to "feed"), EventType.USER_ACTION),
                Triple("button_pressed", mapOf("id" to "refresh_btn"), EventType.USER_ACTION),
                Triple("ProfileScreen", mapOf("user_id" to "usr_991"), EventType.SCREEN_VIEW),
                Triple("SettingsScreen", mapOf("section" to "notifications"), EventType.SCREEN_VIEW),
                Triple("network_connectivity_changed", mapOf("status" to "connected", "type" to "wifi"), EventType.LIFECYCLE),
                Triple("battery_level_changed", mapOf("level" to "82%", "charging" to "true"), EventType.LIFECYCLE),
                Triple("experiment_enrolled", mapOf("exp" to "checkout_revamp", "bucket" to "variant_a"), EventType.CUSTOM),
                Triple("notification_received", mapOf("campaign" to "flash_sale_sep"), EventType.CUSTOM)
            )

            for (i in 1..20) {
                val template = eventTemplates[i % eventTemplates.size]
                SpectraLogger.event(
                    name = "${template.first}_$i",
                    parameters = template.second + mapOf("batch_seq" to "$i"),
                    eventType = template.third,
                    durationMs = if (template.third == EventType.SCREEN_VIEW) (500L..5000L).random() else null
                )
                delay(50)
            }
        }
    }
}
