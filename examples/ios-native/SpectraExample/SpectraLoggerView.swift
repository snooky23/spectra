import SwiftUI
import UIKit
import Spectra

/**
 * A SwiftUI wrapper for the Compose Multiplatform Spectra Logger UI.
 * Adopts iOS 26 Liquid Glass navigation with floating, translucent TabView,
 * falling back to the unified Compose navigation for older iOS versions.
 */
public struct SpectraLoggerView: View {
    private var initialTab: Int
    private var onDismiss: () -> Void

    public init(initialTab: Int = 0, onDismiss: @escaping () -> Void = {
        SpectraUIManager.shared.dismissScreen()
    }) {
        self.initialTab = initialTab
        self.onDismiss = onDismiss
    }

    public var body: some View {
        if #available(iOS 26.0, *) {
            SpectraLiquidGlassTabView(initialTab: initialTab, onDismiss: onDismiss)
        } else {
            LegacySpectraLoggerRepresentable(onDismiss: onDismiss)
                .ignoresSafeArea(.all)
        }
    }
}

/**
 * Native SwiftUI TabView adopting iOS 26 Liquid Glass styling.
 * The system applies glass-like translucency, depth, and floating tab behavior
 * via .tabBarMinimizeBehavior(.automatic).
 */
@available(iOS 26.0, *)
public struct SpectraLiquidGlassTabView: View {
    @State private var selectedTab: Int
    var onDismiss: () -> Void

    public init(initialTab: Int = 0, onDismiss: @escaping () -> Void = {}) {
        _selectedTab = State(initialValue: initialTab)
        self.onDismiss = onDismiss
    }

    public var body: some View {
        TabView(selection: $selectedTab) {
            Tab(String(localized: "Logs"), systemImage: "list.bullet", value: 0) {
                NavigationStack {
                    SpectraTabComposeView(tabIndex: 0, onDismiss: onDismiss)
                        .ignoresSafeArea(.all)
                        .navigationTitle(String(localized: "Logs"))
                        .navigationBarHidden(true)
                }
            }
            Tab(String(localized: "Network"), systemImage: "network", value: 1) {
                NavigationStack {
                    SpectraTabComposeView(tabIndex: 1, onDismiss: onDismiss)
                        .ignoresSafeArea(.all)
                        .navigationTitle(String(localized: "Network"))
                        .navigationBarHidden(true)
                }
            }
            Tab(String(localized: "Events"), systemImage: "waveform.path.ecg", value: 2) {
                NavigationStack {
                    SpectraTabComposeView(tabIndex: 2, onDismiss: onDismiss)
                        .ignoresSafeArea(.all)
                        .navigationTitle(String(localized: "Events"))
                        .navigationBarHidden(true)
                }
            }
            Tab(String(localized: "Settings"), systemImage: "gearshape", value: 3) {
                NavigationStack {
                    SpectraTabComposeView(tabIndex: 3, onDismiss: onDismiss)
                        .ignoresSafeArea(.all)
                        .navigationTitle(String(localized: "Settings"))
                        .navigationBarHidden(true)
                }
            }
        }
        .tabBarMinimizeBehavior(.automatic)
        .tint(Color.purple)
    }
}

/**
 * Embeds an individual Compose tab root UIViewController inside SwiftUI.
 */
public struct SpectraTabComposeView: UIViewControllerRepresentable {
    let tabIndex: Int
    var onDismiss: () -> Void

    public init(tabIndex: Int, onDismiss: @escaping () -> Void = {}) {
        self.tabIndex = tabIndex
        self.onDismiss = onDismiss
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        return SpectraLoggerViewControllerKt.SpectraTabViewController(
            tabIndex: Int32(tabIndex),
            onDismiss: onDismiss
        )
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No-op
    }
}

/**
 * Pre-iOS 26 fallback: full Compose navigation shell.
 */
public struct LegacySpectraLoggerRepresentable: UIViewControllerRepresentable {
    var onDismiss: () -> Void

    public init(onDismiss: @escaping () -> Void = {}) {
        self.onDismiss = onDismiss
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: onDismiss)
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No-op
    }
}
