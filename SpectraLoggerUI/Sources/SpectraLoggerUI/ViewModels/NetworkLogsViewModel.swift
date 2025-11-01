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

    let availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH"]
    let availableStatusRanges = ["2xx", "3xx", "4xx", "5xx"]

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
                applyFilters(
                    searchText: searchText,
                    methods: selectedMethods,
                    statusRanges: selectedStatusRanges,
                    logs: logs
                )
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

    private func applyFilters(
        searchText: String,
        methods: Set<String>,
        statusRanges: Set<String>,
        logs: [NetworkLogEntry]
    ) {
        var filtered = logs

        // Filter by HTTP method
        if !methods.isEmpty {
            filtered = filtered.filter { methods.contains($0.method) }
        }

        // Filter by status range
        if !statusRanges.isEmpty {
            filtered = filtered.filter { log in
                guard let status = log.responseCode?.int32Value else { return false }
                return statusRanges.contains { range in
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

        filteredLogs = filtered
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
}
