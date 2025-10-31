import SwiftUI
import SpectraLogger
import Combine

/// ViewModel for the Logs screen, bridging SwiftUI to KMP storage
@MainActor
class LogsViewModel: ObservableObject {
    @Published var logs: [LogEntry] = []
    @Published var filteredLogs: [LogEntry] = []
    @Published var isLoading = true
    @Published var searchText = ""
    @Published var selectedLevels: Set<LogLevel> = []

    private let storage: LogStorage
    private var cancellables = Set<AnyCancellable>()

    init(storage: LogStorage = SpectraLogger.shared.logStorage) {
        self.storage = storage
        setupObservers()
        loadLogs()
    }

    private func setupObservers() {
        // Observe search text changes
        $searchText
            .combineLatest($selectedLevels, $logs)
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] searchText, levels, logs in
                self?.applyFilters(searchText: searchText, levels: levels, logs: logs)
            }
            .store(in: &cancellables)
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
                    self.applyFilters(searchText: self.searchText, levels: self.selectedLevels, logs: result)
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

    private func applyFilters(searchText: String, levels: Set<LogLevel>, logs: [LogEntry]) {
        var filtered = logs

        // Filter by log level
        if !levels.isEmpty {
            filtered = filtered.filter { levels.contains($0.level) }
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

    func clearLogs() {
        Task {
            try await storage.clear()
            logs = []
            filteredLogs = []
        }
    }
}
