import Foundation
import Combine
import Spectra

@MainActor
public class EventsTestingViewModel: ObservableObject {
    let logger: AppLogger

    @Published public var isTrackingProductDetails = false

    public init(logger: AppLogger) {
        self.logger = logger
    }

    public func toggleProductDetailsScreenView() {
        if !isTrackingProductDetails {
            isTrackingProductDetails = true
            logger.screenStart(
                screenName: "ProductDetailsScreen",
                parameters: [
                    "product_id": "PROD-9021",
                    "category": "Electronics",
                    "source": "HomeCarousel"
                ]
            )
        } else {
            isTrackingProductDetails = false
            logger.screenEnd(
                screenName: "ProductDetailsScreen",
                additionalParameters: [
                    "scroll_depth": "92%",
                    "action_taken": "added_to_wishlist"
                ]
            )
        }
    }

    public func simulateQuickScreenView() {
        Task {
            logger.screenStart(
                screenName: "CartCheckoutScreen",
                parameters: ["cart_items_count": "3"]
            )
            try? await Task.sleep(nanoseconds: 1_500_000_000)
            logger.screenEnd(
                screenName: "CartCheckoutScreen",
                additionalParameters: ["checkout_step": "shipping_selection"]
            )
        }
    }

    public func logAddToCart() {
        logger.event(
            name: "add_to_cart",
            parameters: [
                "item_id": "SKU-4928",
                "item_name": "Noise-Cancelling Headphones",
                "price": "249.99",
                "currency": "USD"
            ],
            eventType: EventType.userAction,
            durationMs: nil
        )
    }

    public func logLikePost() {
        logger.event(
            name: "like_post",
            parameters: [
                "post_id": "post_84920",
                "author": "Avi Levin",
                "reaction": "heart"
            ],
            eventType: EventType.userAction,
            durationMs: nil
        )
    }

    public func logSearchSubmitted() {
        logger.event(
            name: "search_submitted",
            parameters: [
                "query": "kotlin multiplatform",
                "filter_count": "2",
                "results_count": "42"
            ],
            eventType: EventType.userAction,
            durationMs: nil
        )
    }

    public func logAppBackground() {
        logger.event(
            name: "app_backgrounded",
            parameters: [
                "active_session_sec": "320",
                "last_visible_screen": "EventsScreen"
            ],
            eventType: EventType.lifecycle,
            durationMs: nil
        )
    }

    public func logAppForeground() {
        logger.event(
            name: "app_foregrounded",
            parameters: [
                "cold_start": "false",
                "time_in_background_sec": "14"
            ],
            eventType: EventType.lifecycle,
            durationMs: nil
        )
    }

    public func logLowMemoryWarning() {
        logger.event(
            name: "low_memory_warning",
            parameters: [
                "available_ram_mb": "78",
                "total_ram_mb": "4096",
                "level": "TRIM_MEMORY_RUNNING_CRITICAL"
            ],
            eventType: EventType.lifecycle,
            durationMs: nil
        )
    }

    public func logPurchaseCompleted() {
        logger.event(
            name: "purchase_completed",
            parameters: [
                "order_id": "ORD-2026-8819",
                "amount": "249.99",
                "payment_method": "ApplePay",
                "items_count": "1",
                "shipping_country": "US"
            ],
            eventType: EventType.custom,
            durationMs: nil
        )
    }

    public func logFeatureFlagEvaluated() {
        logger.event(
            name: "feature_flag_evaluated",
            parameters: [
                "flag_key": "events_ui_redesign",
                "variant": "treatment_v2",
                "evaluation_reason": "TARGETING_RULE_MATCH"
            ],
            eventType: EventType.custom,
            durationMs: nil
        )
    }

    public func generate20MixedEvents() {
        Task {
            let eventTemplates: [(name: String, params: [String: String], type: EventType)] = [
                ("user_tab_clicked", ["tab": "feed"], EventType.userAction),
                ("button_pressed", ["id": "refresh_btn"], EventType.userAction),
                ("ProfileScreen", ["user_id": "usr_991"], EventType.screenView),
                ("SettingsScreen", ["section": "notifications"], EventType.screenView),
                ("network_connectivity_changed", ["status": "connected", "type": "wifi"], EventType.lifecycle),
                ("battery_level_changed", ["level": "82%", "charging": "true"], EventType.lifecycle),
                ("experiment_enrolled", ["exp": "checkout_revamp", "bucket": "variant_a"], EventType.custom),
                ("notification_received", ["campaign": "flash_sale_sep"], EventType.custom)
            ]

            for i in 1...20 {
                let template = eventTemplates[i % eventTemplates.count]
                var params = template.params
                params["batch_seq"] = "\(i)"
                let duration: KotlinLong? = template.type == EventType.screenView ? KotlinLong(value: Int64.random(in: 500...5000)) : nil
                logger.event(
                    name: "\(template.name)_\(i)",
                    parameters: params,
                    eventType: template.type,
                    durationMs: duration
                )
                try? await Task.sleep(nanoseconds: 50_000_000)
            }
        }
    }
}
