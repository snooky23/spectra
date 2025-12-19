import SwiftUI
import SpectraLogger

/// ViewModel for the Network Logs screen, bridging SwiftUI to KMP storage
@Observable
@MainActor
final class NetworkLogsViewModel {
    var logs: [NetworkLogEntry] = [] {
        didSet { updateFilteredLogs() }
    }

    var filteredLogs: [NetworkLogEntry] = []
    var isLoading = true

    var searchText = "" {
        didSet { updateFilteredLogs() }
    }

    var selectedMethods: Set<String> = [] {
        didSet { updateFilteredLogs() }
    }

    var selectedStatusRanges: Set<String> = [] {
        didSet { updateFilteredLogs() }
    }
    
    var advancedFilter = NetworkFilter() {
        didSet { updateFilteredLogs() }
    }

    let availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH"]
    let availableStatusRanges = ["2xx", "3xx", "4xx", "5xx"]
    
    /// Computed property for total active filter count (for badge)
    var totalActiveFilterCount: Int {
        var count = 0
        if !selectedMethods.isEmpty { count += 1 }
        if !selectedStatusRanges.isEmpty { count += 1 }
        count += advancedFilter.activeFilterCount
        return count
    }
    
    /// Check if any filters are active
    var hasActiveFilters: Bool {
        !selectedMethods.isEmpty || !selectedStatusRanges.isEmpty || advancedFilter.hasActiveFilters
    }

    private let storage: NetworkLogStorage
    private var filterTask: Task<Void, Never>?

    init(storage: NetworkLogStorage = SpectraLogger.shared.networkStorage) {
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
                applyFilters()
            }
        }
    }

    func loadLogs() {
        Task {
            isLoading = true
            do {
                // Query network logs from KMP storage with no filter and no limit
                let noFilter = NetworkLogFilter(
                    methods: nil,
                    urlPattern: nil,
                    statusCodes: nil,
                    minDuration: nil,
                    showOnlyErrors: false
                )
                let result = try await storage.query(filter: noFilter, limit: nil)
                DispatchQueue.main.async {
                    self.logs = result
                    // applyFilters will be called via didSet on logs
                    self.isLoading = false
                }
            } catch {
                print("Error loading network logs: \(error)")
                DispatchQueue.main.async {
                    self.isLoading = false
                }
            }
        }
    }

    private func observeNewLogs() {
        // TODO: Implement network log observation when KMP Flow-to-Swift interop is available
        // For now, logs are loaded on demand with loadLogs()
    }

    private func applyFilters() {
        var filtered = logs
        
        // Combine inline filters with advanced filter
        let methodsToFilter = selectedMethods.isEmpty ? advancedFilter.selectedMethods : selectedMethods
        let statusRangesToFilter = selectedStatusRanges.isEmpty ? advancedFilter.selectedStatusRanges : selectedStatusRanges

        // Filter by HTTP method
        if !methodsToFilter.isEmpty {
            filtered = filtered.filter { methodsToFilter.contains($0.method) }
        }

        // Filter by status range
        if !statusRangesToFilter.isEmpty {
            filtered = filtered.filter { log in
                guard let status = log.responseCode?.int32Value else { return false }
                return statusRangesToFilter.contains { range in
                    switch range {
                    case "2xx": return (200...299).contains(status)
                    case "3xx": return (300...399).contains(status)
                    case "4xx": return (400...499).contains(status)
                    case "5xx": return (500...599).contains(status)
                    default: return false
                    }
                }
            }
        }

        // Filter by search text (min 2 chars)
        if searchText.count >= 2 {
            filtered = filtered.filter { log in
                log.url.localizedCaseInsensitiveContains(searchText) ||
                log.method.localizedCaseInsensitiveContains(searchText)
            }
        }
        
        // Filter by host pattern
        if !advancedFilter.hostPattern.isEmpty {
            let pattern = advancedFilter.hostPattern.lowercased()
            filtered = filtered.filter { log in
                let host = extractHost(from: log.url).lowercased()
                return matchesWildcard(pattern: pattern, text: host)
            }
        }
        
        // Filter by time range
        if let from = advancedFilter.fromTimestamp {
            let fromEpoch = Int64(from.timeIntervalSince1970 * 1000)
            filtered = filtered.filter { log in
                log.timestamp.toEpochMilliseconds() >= fromEpoch
            }
        }
        if let to = advancedFilter.toTimestamp {
            let toEpoch = Int64(to.timeIntervalSince1970 * 1000)
            filtered = filtered.filter { log in
                log.timestamp.toEpochMilliseconds() <= toEpoch
            }
        }
        
        // Filter by response time threshold
        if let threshold = advancedFilter.responseTimeThreshold {
            filtered = filtered.filter { log in
                log.duration >= threshold.milliseconds
            }
        }
        
        // Filter by failed requests only
        if advancedFilter.showOnlyFailed {
            filtered = filtered.filter { log in
                if let error = log.error, !error.isEmpty { return true }
                if let status = log.responseCode?.int32Value {
                    return status >= 400
                }
                return false
            }
        }

        filteredLogs = filtered
    }
    
    private func extractHost(from url: String) -> String {
        guard let urlObj = URL(string: url) else { return url }
        return urlObj.host ?? url
    }
    
    private func matchesWildcard(pattern: String, text: String) -> Bool {
        if pattern.isEmpty { return true }
        if pattern == "*" { return true }
        
        // Simple wildcard matching
        if pattern.hasPrefix("*") && pattern.hasSuffix("*") {
            let middle = String(pattern.dropFirst().dropLast())
            return text.contains(middle)
        } else if pattern.hasPrefix("*") {
            let suffix = String(pattern.dropFirst())
            return text.hasSuffix(suffix)
        } else if pattern.hasSuffix("*") {
            let prefix = String(pattern.dropLast())
            return text.hasPrefix(prefix)
        } else if pattern.contains("*") {
            // Handle patterns like "api.*.com"
            let parts = pattern.split(separator: "*")
            var remaining = text
            for part in parts {
                if let range = remaining.range(of: String(part)) {
                    remaining = String(remaining[range.upperBound...])
                } else {
                    return false
                }
            }
            return true
        } else {
            return text.contains(pattern)
        }
    }

    func toggleMethod(_ method: String) {
        if selectedMethods.contains(method) {
            selectedMethods.remove(method)
        } else {
            selectedMethods.insert(method)
        }
    }

    func toggleStatusRange(_ range: String) {
        if selectedStatusRanges.contains(range) {
            selectedStatusRanges.remove(range)
        } else {
            selectedStatusRanges.insert(range)
        }
    }

    func clearLogs() {
        Task {
            try await storage.clear()
            logs = []
            filteredLogs = []
        }
    }
    
    func applyAdvancedFilter(_ filter: NetworkFilter) {
        advancedFilter = filter
    }
    
    func resetAllFilters() {
        selectedMethods = []
        selectedStatusRanges = []
        advancedFilter.reset()
        searchText = ""
    }
}

