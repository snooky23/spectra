import SwiftUI

@main
struct SpectraExampleApp: App {
    @StateObject private var settingsViewModel = SettingsViewModel()

    init() {
        // Initialize app
        print("Spectra Logger iOS Example Started")
        print("Version: 1.0.0")
    }

    var body: some Scene {
        WindowGroup {
            MainAppView()
                .preferredColorScheme(settingsViewModel.appearanceMode.colorScheme)
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
