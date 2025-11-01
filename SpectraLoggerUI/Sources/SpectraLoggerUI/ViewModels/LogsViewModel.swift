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
    @Published var selectedTags: Set<String> = []
    @Published var availableTags: [String] = []

    private let storage: LogStorage
    private var cancellables = Set<AnyCancellable>()

    init(storage: LogStorage = SpectraLogger.shared.logStorage) {
        self.storage = storage
        setupObservers()
        loadLogs()
    }

    private func setupObservers() {
        // Observe search text, levels, and tags changes
        Publishers.CombineLatest3($searchText, $selectedLevels, $selectedTags)
            .combineLatest($logs)
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] combined, logs in
                let (searchText, levels, tags) = combined
                self?.applyFilters(searchText: searchText, levels: levels, tags: tags, logs: logs)
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
