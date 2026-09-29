import SwiftUI
import Spectra

// MARK: - Main App View

/// Main app screen using the KMP Spectra Logger navigation with iOS Liquid Glass
public struct MainAppView: View {
    private let logger: AppLogger
    
    public init(logger: AppLogger = LiveAppLogger()) {
        self.logger = logger
    }
    
    public var body: some View {
        SpectraLoggerView()
    }
}

// MARK: - Previews (Enabled and Working!)

#Preview("Main App View (MVVM)") {
    // Injected MockAppLogger completely bypasses the KMP singleton, making Xcode Previews work natively!
    MainAppView(logger: MockAppLogger())
}
