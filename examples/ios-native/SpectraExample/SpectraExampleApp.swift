import SwiftUI
import SpectraLogger
import SpectraLoggerUI

@main
struct SpectraExampleApp: App {
    init() {
        // Initialize app
        print("Spectra Logger iOS Example Started")
        print("Version: 1.0.0")

        // Initialize SpectraLogger with default configuration
        // SpectraLogger initializes automatically, no explicit init needed
        print("SpectraLogger version: \(SpectraLogger.shared.getVersion())")

        seedSampleData()
    }

    /// Seeds sample logs, events, and network requests so the KMP UI is populated on launch
    private func seedSampleData() {
        let logger = LiveAppLogger()
        let mockGen = MockDataGenerator(logger: logger)

        // Seed initial logs
        logger.i(tag: "App", message: "Spectra Logger iOS Example Started", metadata: ["version": SpectraLogger.shared.getVersion()])
        logger.d(tag: "UI", message: "iOS Liquid Glass TabView initialized", metadata: ["style": "liquid_glass", "ios_version": "26.0+"])
        logger.i(tag: "Navigation", message: "KMP app bottom navigation active with native Liquid Glass", metadata: [:])
        logger.w(tag: "Performance", message: "Telemetry buffer capacity: 5,000 events", metadata: [:])
        logger.d(tag: "Auth", message: "User session initialized", metadata: ["user_id": "84920"])
        logger.e(tag: "Sync", message: "Network sync delayed: retrying in 5s", metadata: ["retry_count": "1"])

        // Seed initial events
        SpectraLogger.shared.event(
            name: "app_launch",
            parameters: ["platform": "iOS", "version": SpectraLogger.shared.getVersion()],
            eventType: EventType.lifecycle,
            durationMs: nil
        )
        SpectraLogger.shared.event(
            name: "navigation_opened",
            parameters: ["screen": "LogsScreen", "navigation_type": "liquid_glass"],
            eventType: EventType.screenView,
            durationMs: KotlinLong(value: 450)
        )
        SpectraLogger.shared.event(
            name: "glass_navigation_activated",
            parameters: ["behavior": "automatic_minimize", "tint": "purple"],
            eventType: EventType.custom,
            durationMs: nil
        )

        // Seed initial network requests
        mockGen.simulateNetworkRequest(method: "GET", url: "https://api.spectra.dev/v1/config", statusCode: 200, duration: 0.12)
        mockGen.simulateNetworkRequest(method: "POST", url: "https://api.spectra.dev/v1/telemetry", statusCode: 201, duration: 0.25)
        mockGen.simulateNetworkRequest(method: "GET", url: "https://api.spectra.dev/v1/user/profile", statusCode: 200, duration: 0.18)
    }

    var body: some Scene {
        WindowGroup {
            MainAppView()
                .onOpenURL { url in
                    handleURL(url)
                }
        }
    }

    /// Handle incoming URL scheme
    private func handleURL(_ url: URL) {
        print("App opened with URL: \(url.absoluteString)")

        guard url.scheme == "spectralogger" else {
            print("Unknown URL scheme: \(url.scheme ?? "nil")")
            return
        }

        // Handle different paths
        switch url.host {
        case "logs":
            print("Opening logs screen via deep link")

        case "network":
            print("Opening network logs screen via deep link")

        case "clear":
            print("Clearing logs via deep link")

        default:
            print("Unknown URL path: \(url.host ?? "nil")")
        }
    }
}
