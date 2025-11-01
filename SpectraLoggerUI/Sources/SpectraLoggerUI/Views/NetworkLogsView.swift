import SwiftUI
import SpectraLogger

/// Pure SwiftUI Network Logs screen with native iOS design
struct NetworkLogsView: View {
    @State private var viewModel = NetworkLogsViewModel()
    @State private var selectedLog: NetworkLogEntry?
    @State private var shareItems: [Any] = []

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search bar
                SearchBar(text: $viewModel.searchText)
                    .padding(.horizontal)
                    .padding(.top, 8)

                // Method filters
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(viewModel.availableMethods, id: \.self) { method in
                            FilterChip(
                                title: method,
                                isSelected: viewModel.selectedMethods.contains(method),
                                color: .blue
                            ) {
                                viewModel.toggleMethod(method)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .frame(height: 44)

                // Status range filters
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(viewModel.availableStatusRanges, id: \.self) { range in
                            FilterChip(
                                title: range,
                                isSelected: viewModel.selectedStatusRanges.contains(range),
                                color: ColorUtilities.colorForStatusRange(range)
                            ) {
                                viewModel.toggleStatusRange(range)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .frame(height: 44)

                Divider()

                // Content
                if viewModel.isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if viewModel.filteredLogs.isEmpty {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: viewModel.logs.isEmpty ? "network.slash" : "magnifyingglass")
                            .font(.system(size: 48))
                            .foregroundColor(.secondary)
                        Text(viewModel.logs.isEmpty ? "No network logs to display" : "No matching logs")
                            .font(.headline)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    List {
                        ForEach(viewModel.filteredLogs, id: \.id) { log in
                            NetworkLogRow(log: log)
                                .onTapGesture {
                                    selectedLog = log
                                }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Network")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
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
                NetworkLogDetailView(log: log)
            }
        }
    }

    private func shareAllLogs() {
        let logsText = viewModel.logs.map { log in
            let method = log.method
            let status = log.responseCode?.int32Value ?? 0
            let url = log.url
            let timestamp = DateFormattingUtilities.formatFullTimestamp(log.timestamp)
            return "[\(timestamp)] \(method) \(status) - \(url)"
        }.joined(separator: "\n")

        let textToShare = logsText.isEmpty ? "No network logs to share" : logsText
        shareItems = [textToShare]
    }

}

/// Single network log entry row
struct NetworkLogRow: View {
    let log: NetworkLogEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                // HTTP method badge
                Text(log.method)
                    .font(.caption)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(Color.blue.opacity(0.2))
                    .foregroundColor(.blue)
                    .cornerRadius(4)

                // Status code badge
                if let status = log.responseCode?.int32Value {
                    Text("\(status)")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(ColorUtilities.colorForStatusCode(status).opacity(0.2))
                        .foregroundColor(ColorUtilities.colorForStatusCode(status))
                        .cornerRadius(4)
                }

                Spacer()

                // Timestamp
                Text(DateFormattingUtilities.formatShortTime(log.timestamp))
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            // URL
            Text(log.url)
                .font(.body)
                .lineLimit(2)

            // Duration if available
            // TODO: Add duration display when API is finalized
            /*if let duration = log.durationMs?.int64Value {
                HStack(spacing: 4) {
                    Image(systemName: "clock")
                        .font(.caption2)
                    Text("\(duration)ms")
                        .font(.caption2)
                }
                .foregroundColor(.secondary)
            }*/
        }
        .padding(.vertical, 4)
    }

}

/// Network log detail sheet
struct NetworkLogDetailView: View {
    let log: NetworkLogEntry
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Method and status
                    HStack {
                        Text(log.method)
                            .font(.headline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.blue.opacity(0.2))
                            .foregroundColor(.blue)
                            .cornerRadius(6)

                        if let status = log.responseCode?.int32Value {
                            Text("\(status)")
                                .font(.headline)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(ColorUtilities.colorForStatusCode(status).opacity(0.2))
                                .foregroundColor(ColorUtilities.colorForStatusCode(status))
                                .cornerRadius(6)
                        }

                        Spacer()

                        // TODO: Show duration when API is finalized
                        /*if let duration = log.durationMs?.int64Value {
                            Text("\(duration)ms")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }*/
                    }

                    // URL
                    DetailSection(title: "URL") {
                        Text(log.url)
                            .font(.system(.body, design: .monospaced))
                    }

                    // Request Headers
                    if !log.requestHeaders.isEmpty {
                        DetailSection(title: "Request Headers") {
                            ForEach(Array(log.requestHeaders.keys.sorted()), id: \.self) { key in
                                HStack(alignment: .top) {
                                    Text(key)
                                        .fontWeight(.medium)
                                        .font(.caption)
                                    Spacer()
                                    Text(log.requestHeaders[key] ?? "")
                                        .foregroundColor(.secondary)
                                        .font(.caption)
                                        .multilineTextAlignment(.trailing)
                                }
                            }
                        }
                    }

                    // Request Body
                    if let body = log.requestBody, !body.isEmpty {
                        DetailSection(title: "Request Body") {
                            Text(body)
                                .font(.system(.caption, design: .monospaced))
                        }
                    }

                    // Response Headers
                    if !log.responseHeaders.isEmpty {
                        DetailSection(title: "Response Headers") {
                            ForEach(Array(log.responseHeaders.keys.sorted()), id: \.self) { key in
                                HStack(alignment: .top) {
                                    Text(key)
                                        .fontWeight(.medium)
                                        .font(.caption)
                                    Spacer()
                                    Text(log.responseHeaders[key] ?? "")
                                        .foregroundColor(.secondary)
                                        .font(.caption)
                                        .multilineTextAlignment(.trailing)
                                }
                            }
                        }
                    }

                    // Response Body
                    if let body = log.responseBody, !body.isEmpty {
                        DetailSection(title: "Response Body") {
                            Text(body)
                                .font(.system(.caption, design: .monospaced))
                        }
                    }

                    // Error
                    if let error = log.error, !error.isEmpty {
                        DetailSection(title: "Error") {
                            Text(error)
                                .font(.system(.body, design: .monospaced))
                                .foregroundColor(.red)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Request Details")
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

    private func colorForStatus(_ status: Int32) -> Color {
        switch status {
        case 200..<300: return .green
        case 300..<400: return .blue
        case 400..<500: return .orange
        case 500..<600: return .red
        default: return .gray
        }
    }
}

// Extension to make NetworkLogEntry identifiable for sheet
extension NetworkLogEntry: Identifiable {}

// MARK: - Previews

#Preview("NetworkLogsView - Light") {
    NetworkLogsView()
        .preferredColorScheme(.light)
}

#Preview("NetworkLogsView - Dark") {
    NetworkLogsView()
        .preferredColorScheme(.dark)
}
