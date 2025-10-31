import SwiftUI
import SpectraLogger

/// Pure SwiftUI Logs screen with native iOS design
struct LogsView: View {
    @StateObject private var viewModel = LogsViewModel()
    @State private var selectedLog: LogEntry?

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search bar
                SearchBar(text: $viewModel.searchText)
                    .padding(.horizontal)
                    .padding(.top, 8)

                // Filter chips
                if !LogLevel.entries.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(LogLevel.entries, id: \.self) { level in
                                FilterChip(
                                    title: level.name,
                                    isSelected: viewModel.selectedLevels.contains(level),
                                    color: ColorUtilities.colorForLogLevel(level)
                                ) {
                                    viewModel.toggleLevel(level)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                    .frame(height: 44)
                }

                Divider()

                // Content
                if viewModel.isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if viewModel.filteredLogs.isEmpty {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: viewModel.logs.isEmpty ? "tray" : "magnifyingglass")
                            .font(.system(size: 48))
                            .foregroundColor(.secondary)
                        Text(viewModel.logs.isEmpty ? "No logs to display" : "No matching logs")
                            .font(.headline)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    List {
                        ForEach(viewModel.filteredLogs, id: \.id) { log in
                            LogRow(log: log)
                                .onTapGesture {
                                    selectedLog = log
                                }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Logs")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(role: .destructive, action: viewModel.clearLogs) {
                            Label("Clear All Logs", systemImage: "trash")
                        }
                        Button(action: viewModel.loadLogs) {
                            Label("Refresh", systemImage: "arrow.clockwise")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            .sheet(item: $selectedLog) { log in
                LogDetailView(log: log)
            }
        }
    }

}

/// Single log entry row
struct LogRow: View {
    let log: LogEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                // Log level badge
                Text(log.level.name)
                    .font(.caption2)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(ColorUtilities.colorForLogLevel(log.level).opacity(0.2))
                    .foregroundColor(ColorUtilities.colorForLogLevel(log.level))
                    .cornerRadius(4)

                // Tag
                Text(log.tag)
                    .font(.caption)
                    .foregroundColor(.secondary)

                Spacer()

                // Timestamp
                Text(DateFormattingUtilities.formatShortTime(log.timestamp))
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            // Message
            Text(log.message)
                .font(.body)
                .lineLimit(2)

            // Error indicator
            if log.throwable != nil {
                HStack(spacing: 4) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.caption2)
                    Text("Has Error")
                        .font(.caption2)
                }
                .foregroundColor(.orange)
            }
        }
        .padding(.vertical, 4)
    }


}

/// Log detail sheet
struct LogDetailView: View {
    let log: LogEntry
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Level and timestamp
                    HStack {
                        Text(log.level.name)
                            .font(.headline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(ColorUtilities.colorForLogLevel(log.level).opacity(0.2))
                            .foregroundColor(ColorUtilities.colorForLogLevel(log.level))
                            .cornerRadius(6)

                        Spacer()

                        Text(DateFormattingUtilities.formatFullTimestamp(log.timestamp))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    // Tag
                    DetailSection(title: "Tag") {
                        Text(log.tag)
                    }

                    // Message
                    DetailSection(title: "Message") {
                        Text(log.message)
                    }

                    // Error
                    if let error = log.throwable {
                        DetailSection(title: "Error") {
                            Text(error)
                                .font(.system(.body, design: .monospaced))
                                .foregroundColor(.red)
                        }
                    }

                    // Metadata
                    if !log.metadata.isEmpty {
                        DetailSection(title: "Metadata") {
                            ForEach(Array(log.metadata.keys.sorted()), id: \.self) { key in
                                HStack(alignment: .top) {
                                    Text(key)
                                        .fontWeight(.medium)
                                    Spacer()
                                    Text(log.metadata[key] ?? "")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Log Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }


}

/// Reusable detail section
struct DetailSection<Content: View>: View {
    let title: String
    let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundColor(.secondary)
                .textCase(.uppercase)

            content()
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.systemGray6))
                .cornerRadius(8)
        }
    }
}

/// Native iOS search bar
struct SearchBar: View {
    @Binding var text: String

    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.secondary)

            TextField("Search logs (min 2 chars)...", text: $text)
                .textFieldStyle(.plain)

            if !text.isEmpty {
                Button(action: { text = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(8)
        .background(Color(.systemGray6))
        .cornerRadius(10)
    }
}

/// Filter chip component
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? color.opacity(0.2) : Color(.systemGray5))
                .foregroundColor(isSelected ? color : .primary)
                .cornerRadius(16)
        }
    }
}

// Extension to make LogEntry identifiable for sheet
extension LogEntry: Identifiable {}

// MARK: - Previews

#Preview("LogsView - Light") {
    LogsView()
        .preferredColorScheme(.light)
}

#Preview("LogsView - Dark") {
    LogsView()
        .preferredColorScheme(.dark)
}
