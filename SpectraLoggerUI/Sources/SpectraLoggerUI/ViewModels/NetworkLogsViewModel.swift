import SwiftUI
import SpectraLogger
import Combine

/// ViewModel for the Network Logs screen, bridging SwiftUI to KMP storage
@MainActor
class NetworkLogsViewModel: ObservableObject {
    @Published var logs: [NetworkLogEntry] = []
    @Published var filteredLogs: [NetworkLogEntry] = []
    @Published var isLoading = true
    @Published var searchText = ""
    @Published var selectedMethods: Set<String> = []
    @Published var selectedStatusRanges: Set<String> = []

    private let storage: NetworkLogStorage
    private var cancellables = Set<AnyCancellable>()

    let availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH"]
    let availableStatusRanges = ["2xx", "3xx", "4xx", "5xx"]

    init(storage: NetworkLogStorage = SpectraLogger.shared.networkStorage) {
        self.storage = storage
        setupObservers()
        loadLogs()
    }

    private func setupObservers() {
        // Observe filter changes
        $searchText
            .combineLatest($selectedMethods, $selectedStatusRanges, $logs)
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] searchText, methods, statusRanges, logs in
                self?.applyFilters(
                    searchText: searchText,
                    methods: methods,
                    statusRanges: statusRanges,
                    logs: logs
                )
            }
            .store(in: &cancellables)
    }

    func loadLogs() {
        Task {
            isLoading = true
            do {
                // Query logs from KMP storage
                let result = try await storage.query(filter: NetworkLogFilter.none, limit: nil)
                logs = result
                applyFilters(
                    searchText: searchText,
                    methods: selectedMethods,
                    statusRanges: selectedStatusRanges,
                    logs: result
                )

                // Observe new logs
                observeNewLogs()
            } catch {
                print("Error loading network logs: \(error)")
            }
            isLoading = false
        }
    }

    private func observeNewLogs() {
        Task {
            for await newLog in storage.observe(filter: NetworkLogFilter.none) {
                // Prepend new log to the list
                logs.insert(newLog, at: 0)
                applyFilters(
                    searchText: searchText,
                    methods: selectedMethods,
                    statusRanges: selectedStatusRanges,
                    logs: logs
                )
            }
        }
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
