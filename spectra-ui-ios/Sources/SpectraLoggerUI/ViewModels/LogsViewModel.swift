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

    /// Advanced filters from Filter Screen
    var advancedFilter = LogsFilter() {
        didSet { updateFilteredLogs() }
    }

    var availableTags: [String] = []
    
    /// Check if any filters are active (levels or advanced)
    var hasActiveFilters: Bool {
        !selectedLevels.isEmpty || advancedFilter.hasActiveFilters
    }
    
    /// Total count of all active filters for badge display
    var totalActiveFilterCount: Int {
        selectedLevels.count + advancedFilter.activeFilterCount
    }

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
                    advancedFilter: advancedFilter,
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
                        advancedFilter: self.advancedFilter,
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
        advancedFilter: LogsFilter,
        logs: [LogEntry]
    ) {
        var filtered = logs

        // Filter by log level
        if !levels.isEmpty {
            filtered = filtered.filter { levels.contains($0.level) }
        }

        // Filter by tags (from advanced filter)
        let allTags = advancedFilter.allSelectedTags
        if !allTags.isEmpty {
            filtered = filtered.filter { allTags.contains($0.tag) }
        }

        // Filter by time range
        if let fromTimestamp = advancedFilter.fromTimestamp {
            let fromEpoch = fromTimestamp.timeIntervalSince1970
            filtered = filtered.filter { log in
                let logEpoch = Double(log.timestamp.epochSeconds)
                return logEpoch >= fromEpoch
            }
        }
        
        if let toTimestamp = advancedFilter.toTimestamp {
            let toEpoch = toTimestamp.timeIntervalSince1970
            filtered = filtered.filter { log in
                let logEpoch = Double(log.timestamp.epochSeconds)
                return logEpoch <= toEpoch
            }
        }

        // Filter by metadata
        if !advancedFilter.metadataKey.isEmpty && !advancedFilter.metadataValue.isEmpty {
            filtered = filtered.filter { log in
                if let value = log.metadata[advancedFilter.metadataKey] {
                    return value.localizedCaseInsensitiveContains(advancedFilter.metadataValue)
                }
                return false
            }
        }

        // Filter by has error
        if advancedFilter.hasErrorOnly {
            filtered = filtered.filter { log in
                log.throwable != nil || log.metadata["stack_trace"] != nil
            }
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

    /// Remove a specific tag from the filter
    func removeTagFilter(_ tag: String) {
        advancedFilter.selectedTags.remove(tag)
        advancedFilter.customTags.remove(tag)
    }

    /// Clear time range filter
    func clearTimeRangeFilter() {
        advancedFilter.fromTimestamp = nil
        advancedFilter.toTimestamp = nil
    }

    /// Clear metadata filter
    func clearMetadataFilter() {
        advancedFilter.metadataKey = ""
        advancedFilter.metadataValue = ""
    }

    /// Clear has error filter
    func clearHasErrorFilter() {
        advancedFilter.hasErrorOnly = false
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

