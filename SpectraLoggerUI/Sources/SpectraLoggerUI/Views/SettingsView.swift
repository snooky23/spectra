import SwiftUI
import SpectraLogger

/// Pure SwiftUI Settings screen with native iOS design
struct SettingsView: View {
    @ObservedObject var viewModel: SettingsViewModel

    var body: some View {
        NavigationView {
            Form {
                // Appearance Section
                Section {
                    Picker("Appearance", selection: Binding(
                        get: { viewModel.appearanceMode },
                        set: { viewModel.saveAppearanceMode($0) }
                    )) {
                        ForEach(SettingsViewModel.AppearanceMode.allCases) { mode in
                            Text(mode.rawValue).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)
                } header: {
                    Text("Appearance")
                } footer: {
                    Text("Choose how Spectra Logger appears on your device")
                }

                // Storage Section
                Section {
                    // Application Logs
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Application Logs")
                                .font(.body)
                            Text("\(viewModel.logCount) logs stored")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }

                        Spacer()

                        Button(role: .destructive) {
                            viewModel.showClearLogsAlert = true
                        } label: {
                            Label("Clear", systemImage: "trash")
                                .labelStyle(.iconOnly)
                        }
                        .disabled(viewModel.logCount == 0)
                    }

                    // Network Logs
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Network Logs")
                                .font(.body)
                            Text("\(viewModel.networkLogCount) logs stored")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }

                        Spacer()

                        Button(role: .destructive) {
                            viewModel.showClearNetworkAlert = true
                        } label: {
                            Label("Clear", systemImage: "trash")
                                .labelStyle(.iconOnly)
                        }
                        .disabled(viewModel.networkLogCount == 0)
                    }
                } header: {
                    Text("Storage")
                } footer: {
                    Text("Manage stored logs to free up space")
                }

                // Export Section
                Section {
                    Button {
                        viewModel.exportLogs()
                    } label: {
                        HStack {
                            Label("Export All Logs", systemImage: "square.and.arrow.up")
                            Spacer()
                        }
                    }
                } header: {
                    Text("Export")
                } footer: {
                    Text("Export all logs to share with developers")
                }

                // About Section
                Section {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text(SpectraLogger.shared.getVersion())
                            .foregroundColor(.secondary)
                    }

                    HStack {
                        Text("Framework")
                        Spacer()
                        Text("Spectra Logger")
                            .foregroundColor(.secondary)
                    }
                } header: {
                    Text("About")
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        viewModel.loadCounts()
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .alert("Clear Application Logs?", isPresented: $viewModel.showClearLogsAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Clear", role: .destructive) {
                    viewModel.clearLogs()
                }
            } message: {
                Text("This will permanently delete all \(viewModel.logCount) application logs. This action cannot be undone.")
            }
            .alert("Clear Network Logs?", isPresented: $viewModel.showClearNetworkAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Clear", role: .destructive) {
                    viewModel.clearNetworkLogs()
                }
            } message: {
                Text("This will permanently delete all \(viewModel.networkLogCount) network logs. This action cannot be undone.")
            }
        }
    }
}

// MARK: - Previews

#Preview("SettingsView - Light") {
    SettingsView(viewModel: SettingsViewModel())
        .preferredColorScheme(.light)
}

#Preview("SettingsView - Dark") {
    SettingsView(viewModel: SettingsViewModel())
        .preferredColorScheme(.dark)
}
