import SwiftUI

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

// MARK: - Logs View (Placeholder)
struct LogsView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Image(systemName: "list.bullet.rectangle")
                    .font(.system(size: 60))
                    .foregroundColor(.blue)
                Text("Application Logs")
                    .font(.title)
                Text("View coming soon")
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Logs")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// MARK: - Network View (Placeholder)
struct NetworkView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Image(systemName: "network")
                    .font(.system(size: 60))
                    .foregroundColor(.orange)
                Text("Network Logs")
                    .font(.title)
                Text("View coming soon")
                    .foregroundColor(.secondary)
            }
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
        // Placeholder - would fetch from SpectraLogger storage
        logCount = 42
        networkCount = 15
    }

    private func clearLogs() async {
        // Placeholder - would clear SpectraLogger storage
        logCount = 0
    }

    private func clearNetworkLogs() async {
        // Placeholder - would clear SpectraLogger network storage
        networkCount = 0
    }

    private func exportLogs() {
        // Placeholder - would export logs
        print("Export logs requested")
    }
}

#Preview {
    ContentView()
}
