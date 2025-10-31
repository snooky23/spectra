import SwiftUI
import SpectraLogger

/// The main entry point for Spectra Logger UI.
///
/// This view provides a complete logging interface with:
/// - Application logs with filtering and search
/// - Network request/response logs
/// - Settings including dark mode support
///
/// ## Usage
///
/// ### Present as Modal Sheet
/// ```swift
/// import SpectraLoggerUI
///
/// struct ContentView: View {
///     @State private var showLogger = false
///
///     var body: some View {
///         Button("Show Logger") {
///             showLogger = true
///         }
///         .sheet(isPresented: $showLogger) {
///             SpectraLoggerView()
///         }
///     }
/// }
/// ```
///
/// ### Embed in Navigation
/// ```swift
/// NavigationLink("Logs") {
///     SpectraLoggerView()
/// }
/// ```
///
/// ### Full Screen Cover
/// ```swift
/// .fullScreenCover(isPresented: $showLogger) {
///     SpectraLoggerView()
/// }
/// ```
///
/// ## Features
///
/// - **Logs Tab**: View all application logs with real-time updates
///   - Filter by log level (Verbose, Debug, Info, Warning, Error, Fatal)
///   - Search by message, tag, or metadata
///   - Tap to see full log details including stack traces
///
/// - **Network Tab**: Monitor network requests and responses
///   - Filter by HTTP method (GET, POST, PUT, DELETE, PATCH)
///   - Filter by status code range (2xx, 3xx, 4xx, 5xx)
///   - View request/response headers and bodies
///   - See request duration
///
/// - **Settings Tab**: Configure logger behavior
///   - Choose appearance: Light, Dark, or System
///   - View storage statistics
///   - Clear logs
///   - Export logs (coming soon)
///
public struct SpectraLoggerView: View {
    @State private var selectedTab = 0
    @StateObject private var settingsViewModel = SettingsViewModel()

    public init() {}

    public var body: some View {
        TabView(selection: $selectedTab) {
            // Logs Tab
            LogsView()
                .tabItem {
                    Label("Logs", systemImage: "list.bullet.rectangle")
                }
                .tag(0)

            // Network Tab
            NetworkLogsView()
                .tabItem {
                    Label("Network", systemImage: "network")
                }
                .tag(1)

            // Settings Tab
            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                }
                .tag(2)
        }
        .preferredColorScheme(settingsViewModel.appearanceMode.colorScheme)
    }
}

// MARK: - Previews

#Preview("SpectraLoggerView - Light") {
    SpectraLoggerView()
        .preferredColorScheme(.light)
}

#Preview("SpectraLoggerView - Dark") {
    SpectraLoggerView()
        .preferredColorScheme(.dark)
}
