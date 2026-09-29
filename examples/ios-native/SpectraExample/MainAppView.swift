import SwiftUI
import Spectra

// MARK: - Main App View

/// Main app screen providing the SwiftUI testing host application,
/// mirroring Android's MainAppScreen with tabs for Actions, Network, and Events,
/// screen dwell duration tracking, and integrated Spectra Logger presentation.
public struct MainAppView: View {
    private let logger: AppLogger
    private let initialSpectraTab: Int
    @State private var selectedTab: Int = 0
    @State private var showSpectraLogger: Bool = false
    @State private var currentTrackedScreen: String = "ActionsScreen"

    public init(logger: AppLogger = LiveAppLogger(), initialTab: Int = 0, initialShowSpectra: Bool = false, initialSpectraTab: Int = 0) {
        self.logger = logger
        self.initialSpectraTab = initialSpectraTab
        _selectedTab = State(initialValue: initialTab)
        _showSpectraLogger = State(initialValue: initialShowSpectra)
    }

    public var body: some View {
        ZStack(alignment: .bottomTrailing) {
            TabView(selection: $selectedTab) {
                ExampleActionsView(logger: logger, onOpenSpectra: { showSpectraLogger = true })
                    .tabItem {
                        Label("Actions", systemImage: "sparkles")
                    }
                    .tag(0)

                NetworkRequestsView(logger: logger, onOpenSpectra: { showSpectraLogger = true })
                    .tabItem {
                        Label("Network", systemImage: "network")
                    }
                    .tag(1)

                EventsView(logger: logger, onOpenSpectra: { showSpectraLogger = true })
                    .tabItem {
                        Label("Events", systemImage: "waveform.path.ecg")
                    }
                    .tag(2)
            }
            .onChange(of: selectedTab) { _, newTab in
                let nextScreen = screenName(for: newTab)
                logger.screenEnd(screenName: currentTrackedScreen, additionalParameters: ["navigated_to": nextScreen])
                logger.screenStart(screenName: nextScreen, parameters: ["tab_index": "\(newTab)"])
                currentTrackedScreen = nextScreen
            }
            .onAppear {
                let initialScreen = screenName(for: selectedTab)
                logger.screenStart(screenName: initialScreen, parameters: ["tab_index": "\(selectedTab)"])
                currentTrackedScreen = initialScreen
            }
            .fullScreenCover(isPresented: $showSpectraLogger) {
                SpectraLoggerView(initialTab: initialSpectraTab, onDismiss: {
                    showSpectraLogger = false
                })
            }

            // Floating Action Button (FAB) overlay for quick Spectra Logger access from any tab
            // Mirrors Android's SpectraLoggerFabOverlay
            Button(action: { showSpectraLogger = true }) {
                Image(systemName: "doc.text.magnifyingglass")
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 56, height: 56)
                    .background(Color.purple)
                    .clipShape(Circle())
                    .shadow(color: Color.black.opacity(0.3), radius: 6, x: 0, y: 3)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 64)
        }
    }

    private func screenName(for tab: Int) -> String {
        switch tab {
        case 0: return "ActionsScreen"
        case 1: return "NetworkScreen"
        case 2: return "EventsScreen"
        default: return "UnknownScreen"
        }
    }
}

// MARK: - Previews (Enabled and Working!)

#Preview("Main App View (MVVM)") {
    MainAppView(logger: MockAppLogger())
}
