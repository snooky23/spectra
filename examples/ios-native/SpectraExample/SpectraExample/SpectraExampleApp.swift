import SwiftUI
import SpectraLogger

@main
struct SpectraExampleApp: App {
    @State private var openSpectraLogger = false

    init() {
        // Initialize Spectra Logger
        SpectraLoggerKt.doInitialize(
            config: SpectraConfig(
                minLogLevel: LogLevel.verbose,
                maxInMemoryLogs: 10000,
                maxNetworkLogs: 1000,
                enableConsoleLogging: true
            )
        )

        // Generate sample logs
        generateSampleLogs()
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
        SpectraLoggerKt.i(tag: "DeepLink", message: "App opened with URL: \(url.absoluteString)")

        guard url.scheme == "spectralogger" else {
            SpectraLoggerKt.w(tag: "DeepLink", message: "Unknown URL scheme: \(url.scheme ?? "nil")")
            return
        }

        // Handle different paths
        switch url.host {
        case "logs":
            SpectraLoggerKt.i(tag: "DeepLink", message: "Opening logs screen")
            openSpectraLogger = true

        case "network":
            SpectraLoggerKt.i(tag: "DeepLink", message: "Opening network logs screen")
            openSpectraLogger = true

        case "clear":
            SpectraLoggerKt.i(tag: "DeepLink", message: "Clearing logs via deep link")
            Task {
                do {
                    try await SpectraLoggerKt.logStorage.clear()
                    SpectraLoggerKt.i(tag: "DeepLink", message: "Logs cleared successfully")
                } catch {
                    SpectraLoggerKt.e(tag: "DeepLink", message: "Failed to clear logs: \(error)")
                }
            }

        default:
            SpectraLoggerKt.w(tag: "DeepLink", message: "Unknown URL path: \(url.host ?? "nil")")
        }
    }

    private func generateSampleLogs() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            SpectraLoggerKt.i(tag: "App", message: "Spectra Logger iOS Example Started")
            SpectraLoggerKt.i(tag: "App", message: "Version: 1.0.0")

            // Verbose logs
            SpectraLoggerKt.v(tag: "UI", message: "App UI initialized")
            SpectraLoggerKt.v(tag: "Lifecycle", message: "App launched")

            // Debug logs
            SpectraLoggerKt.d(tag: "Init", message: "Initializing example app")
            SpectraLoggerKt.d(tag: "Config", message: "Logger configured with verbose level")

            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                // Info logs with metadata
                let metadata = ["user_id": "12345", "platform": "iOS", "version": "17.0"]
                SpectraLoggerKt.i(tag: "User", message: "User opened the app", metadata: metadata)
                SpectraLoggerKt.i(tag: "Navigation", message: "Showing logs screen")

                // Warning logs
                SpectraLoggerKt.w(tag: "Performance", message: "Large dataset detected: 5000 items")
                SpectraLoggerKt.w(tag: "Memory", message: "Memory usage: 45MB / 128MB")

                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    // Error log
                    SpectraLoggerKt.e(tag: "Network", message: "Failed to fetch data: timeout")

                    // Fatal log
                    let criticalMetadata = ["severity": "high", "impact": "user_experience", "retry_count": "3"]
                    SpectraLoggerKt.f(tag: "Critical", message: "Critical error: database corruption detected", metadata: criticalMetadata)

                    // Generate additional sample logs
                    for i in 0..<30 {
                        DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.1) {
                            let logMetadata = ["iteration": "\(i)", "timestamp": "\(Date().timeIntervalSince1970)"]
                            switch i % 6 {
                            case 0:
                                SpectraLoggerKt.v(tag: "Sample", message: "Verbose log #\(i)", metadata: logMetadata)
                            case 1:
                                SpectraLoggerKt.d(tag: "Sample", message: "Debug log #\(i)", metadata: logMetadata)
                            case 2:
                                SpectraLoggerKt.i(tag: "Sample", message: "Info log #\(i)", metadata: logMetadata)
                            case 3:
                                SpectraLoggerKt.w(tag: "Sample", message: "Warning log #\(i)", metadata: logMetadata)
                            case 4:
                                SpectraLoggerKt.e(tag: "Sample", message: "Error log #\(i)", metadata: logMetadata)
                            case 5:
                                SpectraLoggerKt.f(tag: "Sample", message: "Fatal log #\(i)", metadata: logMetadata)
                            default:
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}
