import SwiftUI
import SpectraLogger

@main
struct SpectraExampleApp: App {
    init() {
        // Initialize app
        print("Spectra Logger iOS Example Started")
        print("Version: 1.0.0")

        // Initialize SpectraLogger with default configuration
        // SpectraLogger initializes automatically, no explicit init needed
        print("SpectraLogger version: \(SpectraLogger.shared.getVersion())")
    }

    var body: some Scene {
        WindowGroup {
            MainAppView()
                .statusBar(hidden: true)
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
