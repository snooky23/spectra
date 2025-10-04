import SwiftUI
import shared

@main
struct SpectraExampleApp: App {

    init() {
        // Configure Spectra Logger
        SpectraLogger.shared.configure { config in
            config.minLogLevel = LogLevel.verbose
            config.logStorage { storage in
                storage.maxCapacity = 20000
            }
            config.networkStorage { storage in
                storage.maxCapacity = 2000
            }
        }

        // Generate sample logs
        generateSampleLogs()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func generateSampleLogs() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            SpectraLogger.shared.i(tag: "App", message: "Spectra Logger iOS Example Started")
            SpectraLogger.shared.i(tag: "App", message: "Version: \(SpectraLogger.shared.getVersion())")

            // Verbose logs
            SpectraLogger.shared.v(tag: "UI", message: "App UI initialized")
            SpectraLogger.shared.v(tag: "Lifecycle", message: "App launched")

            // Debug logs
            SpectraLogger.shared.d(tag: "Init", message: "Initializing example app")
            SpectraLogger.shared.d(tag: "Config", message: "Logger configured")

            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                // Info logs with metadata
                let metadata = ["user_id": "12345", "platform": "iOS"]
                SpectraLogger.shared.i(tag: "User", message: "User opened the app", metadata: metadata)
                SpectraLogger.shared.i(tag: "Navigation", message: "Showing logs screen")

                // Warning logs
                SpectraLogger.shared.w(tag: "Performance", message: "Large dataset detected")
                SpectraLogger.shared.w(tag: "Memory", message: "Memory usage: 45MB / 128MB")

                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    // Error log
                    SpectraLogger.shared.e(tag: "Error", message: "Sample error for demonstration")

                    // Fatal log
                    let criticalMetadata = ["severity": "high", "impact": "user_experience"]
                    SpectraLogger.shared.f(tag: "Critical", message: "Critical error example", metadata: criticalMetadata)

                    // Generate additional sample logs
                    for i in 0..<20 {
                        DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.1) {
                            switch i % 5 {
                            case 0:
                                SpectraLogger.shared.v(tag: "Sample", message: "Verbose log #\(i)")
                            case 1:
                                SpectraLogger.shared.d(tag: "Sample", message: "Debug log #\(i)")
                            case 2:
                                SpectraLogger.shared.i(tag: "Sample", message: "Info log #\(i)")
                            case 3:
                                SpectraLogger.shared.w(tag: "Sample", message: "Warning log #\(i)")
                            case 4:
                                SpectraLogger.shared.e(tag: "Sample", message: "Error log #\(i)")
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
