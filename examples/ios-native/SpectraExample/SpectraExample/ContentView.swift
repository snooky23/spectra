import SwiftUI
import SpectraLogger
import UIKit

struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            LogsView()
                .tabItem {
                    Label("Logs", systemImage: "list.bullet.rectangle")
                }
                .tag(0)

            NetworkView()
                .tabItem {
                    Label("Network", systemImage: "network")
                }
                .tag(1)

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                }
                .tag(2)
        }
    }
}

// MARK: - Logs View (Using Compose UI)
struct LogsView: View {
    var body: some View {
        NavigationView {
            ComposeViewController(
                viewController: MainViewControllerKt.LogListViewController(
                    storage: SpectraLoggerKt.logStorage
                )
            )
            .navigationTitle("Logs")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// MARK: - Network View (Using Compose UI)
struct NetworkView: View {
    var body: some View {
        NavigationView {
            ComposeViewController(
                viewController: MainViewControllerKt.NetworkLogViewController(
                    storage: SpectraLoggerKt.networkStorage
                )
            )
            .navigationTitle("Network")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// MARK: - Settings View (Native SwiftUI)
struct SettingsView: View {
    @State private var logCount: Int32 = 0
    @State private var networkCount: Int32 = 0
    @State private var showClearLogsAlert = false
    @State private var showClearNetworkAlert = false

    var body: some View {
        NavigationView {
            Form {
                Section("Storage") {
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Application Logs")
                            Text("\(logCount) logs stored")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Button("Clear") {
                            showClearLogsAlert = true
                        }
                        .foregroundColor(.red)
                        .disabled(logCount == 0)
                    }

                    HStack {
                        VStack(alignment: .leading) {
                            Text("Network Logs")
                            Text("\(networkCount) logs stored")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Button("Clear") {
                            showClearNetworkAlert = true
                        }
                        .foregroundColor(.red)
                        .disabled(networkCount == 0)
                    }
                }

                Section("Export") {
                    Button("Export All Logs") {
                        exportLogs()
                    }
                    Text("Export all logs to share with developers")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Section("About") {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(.secondary)
                    }

                    HStack {
                        Text("Framework")
                        Spacer()
                        Text("Spectra Logger")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .alert("Clear Application Logs?", isPresented: $showClearLogsAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Clear", role: .destructive) {
                    Task {
                        await clearLogs()
                    }
                }
            } message: {
                Text("This will permanently delete all \(logCount) application logs. This action cannot be undone.")
            }
            .alert("Clear Network Logs?", isPresented: $showClearNetworkAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Clear", role: .destructive) {
                    Task {
                        await clearNetworkLogs()
                    }
                }
            } message: {
                Text("This will permanently delete all \(networkCount) network logs. This action cannot be undone.")
            }
            .task {
                await updateCounts()
            }
        }
    }

    private func updateCounts() async {
        do {
            logCount = try await SpectraLoggerKt.logStorage.count()
            networkCount = try await SpectraLoggerKt.networkStorage.count()
        } catch {
            SpectraLoggerKt.e(tag: "Settings", message: "Failed to update counts: \(error.localizedDescription)")
        }
    }

    private func clearLogs() async {
        do {
            try await SpectraLoggerKt.logStorage.clear()
            await updateCounts()
            SpectraLoggerKt.i(tag: "Settings", message: "Application logs cleared")
        } catch {
            SpectraLoggerKt.e(tag: "Settings", message: "Failed to clear logs: \(error.localizedDescription)")
        }
    }

    private func clearNetworkLogs() async {
        do {
            try await SpectraLoggerKt.networkStorage.clear()
            await updateCounts()
            SpectraLoggerKt.i(tag: "Settings", message: "Network logs cleared")
        } catch {
            SpectraLoggerKt.e(tag: "Settings", message: "Failed to clear network logs: \(error.localizedDescription)")
        }
    }

    private func exportLogs() {
        SpectraLoggerKt.i(tag: "Export", message: "Export logs requested")
        // TODO: Implement export to file and share sheet
        // This would involve:
        // 1. Query all logs from storage
        // 2. Format logs as text/JSON
        // 3. Save to temporary file
        // 4. Present UIActivityViewController
    }
}

// MARK: - Compose UIViewController Wrapper
struct ComposeViewController: UIViewControllerRepresentable {
    let viewController: UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed for Compose UI
    }
}

#Preview {
    ContentView()
}
