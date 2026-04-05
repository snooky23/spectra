import SwiftUI
import SpectraUI
import SpectraLogger

// MARK: - Main App View

/// Main app screen with tab-based navigation for different example types
public struct MainAppView: View {
    private let logger: AppLogger
    
    public init(logger: AppLogger = LiveAppLogger()) {
        self.logger = logger
    }
    
    public var body: some View {
        TabView {
            ExampleActionsView(logger: logger)
                .tabItem {
                    Label("Actions", systemImage: "sparkles")
                }
            
            NetworkRequestsView(logger: logger)
                .tabItem {
                    Label("Network", systemImage: "network")
                }
        }
    }
}

// MARK: - Previews (Enabled and Working!)

#Preview("Main App View (MVVM)") {
    // Injected MockAppLogger completely bypasses the KMP singleton, making Xcode Previews work natively!
    MainAppView(logger: MockAppLogger())
}
