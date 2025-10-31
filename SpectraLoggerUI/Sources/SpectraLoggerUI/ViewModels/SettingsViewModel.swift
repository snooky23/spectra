import SwiftUI
import SpectraLogger
import Combine

/// ViewModel for the Settings screen, bridging SwiftUI to KMP storage
@MainActor
class SettingsViewModel: ObservableObject {
    @Published var logCount: Int = 0
    @Published var networkLogCount: Int = 0
    @Published var showClearLogsAlert = false
    @Published var showClearNetworkAlert = false
    @Published var appearanceMode: AppearanceMode = .system

    private let logStorage: LogStorage
    private let networkStorage: NetworkLogStorage

    enum AppearanceMode: String, CaseIterable, Identifiable {
        case light = "Light"
        case dark = "Dark"
        case system = "System"

        var id: String { rawValue }

        var colorScheme: ColorScheme? {
            switch self {
            case .light: return .light
            case .dark: return .dark
            case .system: return nil
            }
        }
    }

    init(
        logStorage: LogStorage = SpectraLogger.shared.logStorage,
        networkStorage: NetworkLogStorage = SpectraLogger.shared.networkStorage
    ) {
        self.logStorage = logStorage
        self.networkStorage = networkStorage
        loadCounts()
        loadAppearanceMode()
    }

    func loadCounts() {
        Task {
            let logCountKt = try await logStorage.count()
            logCount = Int(logCountKt.int32Value)
            let networkCountKt = try await networkStorage.count()
            networkLogCount = Int(networkCountKt.int32Value)
        }
    }

    func clearLogs() {
        Task {
            try await logStorage.clear()
            logCount = 0
            showClearLogsAlert = false
        }
    }

    func clearNetworkLogs() {
        Task {
            try await networkStorage.clear()
            networkLogCount = 0
            showClearNetworkAlert = false
        }
    }

    func exportLogs() {
        // TODO: Implement export functionality
        print("Export logs functionality coming soon")
    }

    func loadAppearanceMode() {
        if let savedMode = UserDefaults.standard.string(forKey: "appearanceMode"),
           let mode = AppearanceMode(rawValue: savedMode) {
            appearanceMode = mode
        }
    }

    func saveAppearanceMode(_ mode: AppearanceMode) {
        appearanceMode = mode
        UserDefaults.standard.set(mode.rawValue, forKey: "appearanceMode")
    }
}
