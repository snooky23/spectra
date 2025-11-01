import SwiftUI
import SpectraLogger

/// ViewModel for the Logs screen, bridging SwiftUI to KMP storage
@Observable
@MainActor
final class LogsViewModel {
    var logs: [LogEntry] = [] {
        didSet { updateFilteredLogs() }
    }

    var filteredLogs: [LogEntry] = []
    var isLoading = true

    var searchText = "" {
        didSet { updateFilteredLogs() }
    }

    var selectedLevels: Set<LogLevel> = [] {
        didSet { updateFilteredLogs() }
    }

    var selectedTags: Set<String> = [] {
        didSet { updateFilteredLogs() }
    }

    var availableTags: [String] = []

    private let storage: LogStorage
    private var filterTask: Task<Void, Never>?

    init(storage: LogStorage = SpectraLogger.shared.logStorage) {
        self.storage = storage
        loadLogs()
    }

    private func updateFilteredLogs() {
        // Cancel previous task to debounce rapid changes
        filterTask?.cancel()

        // Schedule new filtering task with debounce
        filterTask = Task {
            try? await Task.sleep(nanoseconds: 300_000_000) // 300ms debounce

            if !Task.isCancelled {
                applyFilters(
                    searchText: searchText,
                    levels: selectedLevels,
                    tags: selectedTags,
                    logs: logs
                )
            }
        }
    }

    func loadLogs() {
        Task {
            isLoading = true
            do {
                // Query logs from KMP storage with no filter (all properties nil) and no limit
                let noFilter = LogFilter(levels: nil, tags: nil, searchText: nil, fromTimestamp: nil, toTimestamp: nil)
                let result = try await storage.query(filter: noFilter, limit: nil)
                DispatchQueue.main.async {
                    self.logs = result
                    // Extract unique tags from logs, sorted alphabetically
                    self.availableTags = Array(Set(result.map { $0.tag })).sorted()
                    self.applyFilters(
                        searchText: self.searchText,
                        levels: self.selectedLevels,
                        tags: self.selectedTags,
                        logs: result
                    )
                    self.isLoading = false
                }
            } catch {
                print("Error loading logs: \(error)")
                DispatchQueue.main.async {
                    self.isLoading = false
                }
            }
        }
    }

    private func observeNewLogs() {
        // TODO: Implement log observation when KMP Flow-to-Swift interop is available
        // For now, logs are loaded on demand with loadLogs()
    }

    private func applyFilters(
        searchText: String,
        levels: Set<LogLevel>,
        tags: Set<String>,
        logs: [LogEntry]
    ) {
        var filtered = logs

        // Filter by log level
        if !levels.isEmpty {
            filtered = filtered.filter { levels.contains($0.level) }
        }

        // Filter by tags
        if !tags.isEmpty {
            filtered = filtered.filter { tags.contains($0.tag) }
        }

        // Filter by search text (min 2 chars)
        if searchText.count >= 2 {
            filtered = filtered.filter { log in
                log.message.localizedCaseInsensitiveContains(searchText) ||
                log.tag.localizedCaseInsensitiveContains(searchText) ||
                log.level.name.localizedCaseInsensitiveContains(searchText)
            }
        }

        filteredLogs = filtered
    }

    func toggleLevel(_ level: LogLevel) {
        if selectedLevels.contains(level) {
            selectedLevels.remove(level)
        } else {
            selectedLevels.insert(level)
        }
    }

    func toggleTag(_ tag: String) {
        if selectedTags.contains(tag) {
            selectedTags.remove(tag)
        } else {
            selectedTags.insert(tag)
        }
    }

    func clearLogs() {
        Task {
            try await storage.clear()
            logs = []
            filteredLogs = []
            availableTags = []
        }
    }
}
