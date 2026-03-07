import SwiftUI
import SpectraLogger

/// ViewModel for the Settings screen, bridging SwiftUI to KMP storage
@Observable
@MainActor
final class SettingsViewModel {
    var logCount: Int = 0
    var networkLogCount: Int = 0
    var showClearLogsAlert = false
    var showClearNetworkAlert = false
    var appearanceMode: AppearanceMode = .system
    
    // Core Configurations
    var isNetworkLoggingEnabled: Bool = true {
        didSet { toggleNetworkLogging(enabled: isNetworkLoggingEnabled) }
    }
    var isFilePersistenceEnabled: Bool = false {
        didSet { toggleFilePersistence(enabled: isFilePersistenceEnabled) }
    }
    var ignoredDomainsText: String = "" {
        didSet { updateIgnoredDomains(domains: ignoredDomainsText) }
    }

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
        loadCoreConfigurations()
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

    // MARK: - Core Configuration Mutators

    private func loadCoreConfigurations() {
        let config = SpectraLogger.shared.configuration
        isNetworkLoggingEnabled = config.enabledFeatures.enableNetworkLogging
        isFilePersistenceEnabled = config.logStorageConfig.enablePersistence
        ignoredDomainsText = config.enabledFeatures.networkIgnoredDomains.joined(separator: ", ")
    }

    private func toggleNetworkLogging(enabled: Bool) {
        let currentFeatures = SpectraLogger.shared.configuration.enabledFeatures
        SpectraLogger.shared.configure { builder in 
            builder.features { features in
                features.enableNetworkLogging = enabled
                features.enableCrashReporting = currentFeatures.enableCrashReporting
                features.enablePerformanceMetrics = currentFeatures.enablePerformanceMetrics
                features.networkIgnoredDomains = currentFeatures.networkIgnoredDomains
                features.networkIgnoredExtensions = currentFeatures.networkIgnoredExtensions
            }
        }
    }

    private func toggleFilePersistence(enabled: Bool) {
        let currentStorage = SpectraLogger.shared.configuration.logStorageConfig
        SpectraLogger.shared.configure { builder in
            builder.logStorage { storage in
                storage.maxCapacity = currentStorage.maxCapacity
                storage.enablePersistence = enabled
                storage.fileLogLevel = currentStorage.fileLogLevel
            }
        }
    }

    private func updateIgnoredDomains(domains: String) {
        let list = domains.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
        let currentFeatures = SpectraLogger.shared.configuration.enabledFeatures
        SpectraLogger.shared.configure { builder in
            builder.features { features in
                features.enableNetworkLogging = currentFeatures.enableNetworkLogging
                features.enableCrashReporting = currentFeatures.enableCrashReporting
                features.enablePerformanceMetrics = currentFeatures.enablePerformanceMetrics
                features.networkIgnoredDomains = list
                features.networkIgnoredExtensions = currentFeatures.networkIgnoredExtensions
            }
        }
    }
}
