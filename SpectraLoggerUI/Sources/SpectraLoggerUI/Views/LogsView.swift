import SwiftUI
import SpectraLogger

/// Pure SwiftUI Logs screen with native iOS design
struct LogsView: View {
    @State private var viewModel = LogsViewModel()
    @State private var selectedLog: LogEntry?
    @State private var shareItems: [Any] = []
    @State private var showFilterScreen = false

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search bar
                SearchBar(text: $viewModel.searchText)
                    .padding(.horizontal)
                    .padding(.top, 8)

                // Filter chips - Log Levels
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

                // Active filter badges (from Filter Screen)
                if viewModel.advancedFilter.hasActiveFilters {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            // Tag filters
                            ForEach(Array(viewModel.advancedFilter.allSelectedTags).sorted(), id: \.self) { tag in
                                ActiveFilterBadge(label: "Tag: \(tag)") {
                                    viewModel.removeTagFilter(tag)
                                }
                            }
                            
                            // Time range filter
                            if viewModel.advancedFilter.fromTimestamp != nil || viewModel.advancedFilter.toTimestamp != nil {
                                ActiveFilterBadge(label: "Time Range") {
                                    viewModel.clearTimeRangeFilter()
                                }
                            }
                            
                            // Metadata filter
                            if !viewModel.advancedFilter.metadataKey.isEmpty && !viewModel.advancedFilter.metadataValue.isEmpty {
                                ActiveFilterBadge(label: "\(viewModel.advancedFilter.metadataKey)=\(viewModel.advancedFilter.metadataValue)") {
                                    viewModel.clearMetadataFilter()
                                }
                            }
                            
                            // Has error filter
                            if viewModel.advancedFilter.hasErrorOnly {
                                ActiveFilterBadge(label: "Errors Only") {
                                    viewModel.clearHasErrorFilter()
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
                    Button(action: { showFilterScreen = true }) {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: "line.3.horizontal.decrease.circle")
                            
                            // Badge for active filter count
                            if viewModel.advancedFilter.activeFilterCount > 0 {
                                Text("\(viewModel.advancedFilter.activeFilterCount)")
                                    .font(.caption2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                    .padding(4)
                                    .background(Color.red)
                                    .clipShape(Circle())
                                    .offset(x: 8, y: -8)
                            }
                        }
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: shareAllLogs) {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: viewModel.loadLogs) {
                            Label("Refresh", systemImage: "arrow.clockwise")
                        }
                        Button(role: .destructive, action: viewModel.clearLogs) {
                            Label("Clear All Logs", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            .shareSheet(items: $shareItems)
            .sheet(item: $selectedLog) { log in
                LogDetailView(log: log)
            }
            .sheet(isPresented: $showFilterScreen) {
                LogsFilterView(
                    filter: $viewModel.advancedFilter,
                    availableTags: viewModel.availableTags,
                    onApply: {}
                )
            }
        }
    }

    private func shareAllLogs() {
        let logsText = viewModel.logs.map { log in
            "[\(log.level.name.uppercased())] \(DateFormattingUtilities.formatFullTimestamp(log.timestamp)) - \(log.tag): \(log.message)"
        }.joined(separator: "\n")

        let textToShare = logsText.isEmpty ? "No logs to share" : logsText
        shareItems = [textToShare]
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

                    // Error / Stack Trace (from throwable or metadata)
                    if let error = log.throwable {
                        ExpandableErrorSection(error: error)
                    } else if let stackTrace = log.metadata["stack_trace"] {
                        ExpandableErrorSection(error: stackTrace)
                    }

                    // Metadata (excluding stack_trace if already displayed)
                    if !log.metadata.isEmpty {
                        let filteredMetadata = log.metadata.filter { $0.key != "stack_trace" }
                        if !filteredMetadata.isEmpty {
                            DetailSection(title: "Metadata") {
                                ForEach(Array(filteredMetadata.keys.sorted()), id: \.self) { key in
                                    HStack(alignment: .top) {
                                        Text(key)
                                            .fontWeight(.medium)
                                        Spacer()
                                        Text(filteredMetadata[key] ?? "")
                                            .foregroundColor(.secondary)
                                    }
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

/// Expandable error section showing stack trace with line count and copyable content
struct ExpandableErrorSection: View {
    let error: String
    @State private var isExpanded = true

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Header with expand/collapse toggle
            HStack {
                Button(action: { isExpanded.toggle() }) {
                    HStack(spacing: 8) {
                        Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                            .font(.caption)
                        Text("Error / Stack Trace")
                            .font(.caption)
                            .fontWeight(.semibold)
                        Spacer()
                        Text("\(error.split(separator: "\n").count) lines")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                    .foregroundColor(.red)
                }

                // Copy button
                Button(action: {
                    UIPasteboard.general.string = error
                }) {
                    Image(systemName: "doc.on.doc")
                        .font(.caption)
                        .foregroundColor(.blue)
                }
            }
            .padding(.vertical, 8)

            // Expandable content
            if isExpanded {
                ScrollView(.horizontal, showsIndicators: true) {
                    VStack(alignment: .leading, spacing: 0) {
                        ForEach(error.split(separator: "\n", omittingEmptySubsequences: false).indices, id: \.self) { index in
                            HStack(alignment: .top, spacing: 8) {
                                // Line number
                                Text(String(format: "%3d", index + 1))
                                    .font(.system(.caption, design: .monospaced))
                                    .foregroundColor(.secondary)
                                    .frame(width: 30, alignment: .trailing)

                                // Line content
                                Text(String(error.split(separator: "\n")[index]))
                                    .font(.system(.caption, design: .monospaced))
                                    .foregroundColor(.primary)
                                    .lineLimit(nil)

                                Spacer()
                            }
                            .padding(.vertical, 2)

                            if index < error.split(separator: "\n").count - 1 {
                                Divider()
                                    .opacity(0.3)
                            }
                        }
                    }
                    .padding(12)
                }
                .background(Color(.systemGray6))
                .cornerRadius(8)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.red.opacity(0.05))
        .cornerRadius(8)
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
