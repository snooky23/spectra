import SwiftUI
import shared

struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            LogsView()
                .tabItem {
                    Label("Logs", systemImage: "list.bullet")
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

struct LogsView: View {
    var body: some View {
        NavigationView {
            // Use Compose UI from shared module
            ComposeViewControllerRepresentable(
                createViewController: {
                    MainViewControllerKt.createLogListViewController(
                        storage: SpectraLogger.shared.logStorage
                    )
                }
            )
            .navigationTitle("Logs")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct NetworkView: View {
    var body: some View {
        NavigationView {
            // Use Compose UI from shared module
            ComposeViewControllerRepresentable(
                createViewController: {
                    MainViewControllerKt.createNetworkLogViewController(
                        storage: SpectraLogger.shared.networkStorage
                    )
                }
            )
            .navigationTitle("Network")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct SettingsView: View {
    @State private var selectedLogLevel: LogLevel = .verbose
    @State private var logCount: Int32 = 0
    @State private var networkCount: Int32 = 0
    @State private var showClearAlert = false
    @State private var showNetworkClearAlert = false

    var body: some View {
        NavigationView {
            Form {
                Section("Log Level") {
                    Picker("Minimum Level", selection: $selectedLogLevel) {
                        ForEach(LogLevel.allCases, id: \.self) { level in
                            Text(level.name).tag(level)
                        }
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: selectedLogLevel) { _, newValue in
                        SpectraLogger.shared.configure { config in
                            config.minLogLevel = newValue
                        }
                    }
                }

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
                            showClearAlert = true
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
                            showNetworkClearAlert = true
                        }
                        .foregroundColor(.red)
                        .disabled(networkCount == 0)
                    }
                }

                Section("Export") {
                    Button("Export All Logs") {
                        exportLogs()
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .alert("Clear Application Logs?", isPresented: $showClearAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Clear", role: .destructive) {
                    Task {
                        try? await SpectraLogger.shared.clear()
                        await updateCounts()
                    }
                }
            } message: {
                Text("This will permanently delete all \(logCount) application logs.")
            }
            .alert("Clear Network Logs?", isPresented: $showNetworkClearAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Clear", role: .destructive) {
                    Task {
                        try? await SpectraLogger.shared.clearNetwork()
                        await updateCounts()
                    }
                }
            } message: {
                Text("This will permanently delete all \(networkCount) network logs.")
            }
            .task {
                await updateCounts()
            }
        }
    }

    private func updateCounts() async {
        logCount = try! await SpectraLogger.shared.count()
        networkCount = try! await SpectraLogger.shared.networkCount()
    }

    private func exportLogs() {
        SpectraLogger.shared.i(tag: "Export", message: "Export logs requested")
        // TODO: Implement export functionality
    }
}

// Helper to wrap Compose ViewController in SwiftUI
struct ComposeViewControllerRepresentable: UIViewControllerRepresentable {
    let createViewController: () -> UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        createViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed
    }
}

extension LogLevel: CaseIterable {
    public static var allCases: [LogLevel] {
        return [.verbose, .debug, .info, .warning, .error, .fatal]
    }
}

#Preview {
    ContentView()
}
