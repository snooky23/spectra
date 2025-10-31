import SwiftUI
import SpectraLoggerUI
import SpectraLogger

// MARK: - Reusable Components

/// Reusable log-generating button component
struct LogButton: View {
    let label: String
    let icon: String
    let backgroundColor: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Label(label, systemImage: icon)
                .frame(maxWidth: .infinity)
                .padding()
                .background(backgroundColor.opacity(0.1))
                .cornerRadius(10)
        }
        .foregroundColor(.primary)
    }
}

/// Info card showing app branding
struct AppBrandingCard: View {
    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: "app.badge.checkmark")
                .font(.system(size: 80))
                .foregroundColor(.blue)

            Text("Example App")
                .font(.largeTitle)
                .fontWeight(.bold)

            Text("with Spectra Logger Integration")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .padding(.top, 50)
    }
}

/// Section header for grouping UI elements
struct SectionHeader: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.headline)
    }
}

// MARK: - Main App View

/// Main app screen with example content and button to open Spectra Logger
struct MainAppView: View {
    @State private var showSpectraLogger = false
    @State private var counter = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 30) {
                Spacer()
                    .frame(height: 20)

                // App branding
                AppBrandingCard()

                Spacer()

                // Example actions that generate logs
                VStack(spacing: 20) {
                    SectionHeader(title: "Example Actions")

                    LogButton(
                        label: "Tap Me (Generates Log)",
                        icon: "hand.tap",
                        backgroundColor: .blue,
                        action: {
                            counter += 1
                            SpectraLogger.shared.i(
                                tag: "Example",
                                message: "Button tapped \(counter) times",
                                throwable: nil,
                                metadata: [:]
                            )
                        }
                    )

                    LogButton(
                        label: "Generate Warning",
                        icon: "exclamationmark.triangle",
                        backgroundColor: .orange,
                        action: {
                            SpectraLogger.shared.w(
                                tag: "Example",
                                message: "Warning log generated",
                                throwable: nil,
                                metadata: [:]
                            )
                        }
                    )

                    LogButton(
                        label: "Generate Error",
                        icon: "xmark.circle",
                        backgroundColor: .red,
                        action: {
                            SpectraLogger.shared.e(
                                tag: "Example",
                                message: "Error log generated",
                                throwable: nil,
                                metadata: [:]
                            )
                        }
                    )
                }
                .padding(.horizontal)

                Spacer()

                // Open Spectra Logger button
                Button(action: {
                    showSpectraLogger = true
                }) {
                    HStack {
                        Image(systemName: "doc.text.magnifyingglass")
                        Text("Open Spectra Logger")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.purple)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .padding(.horizontal)
                .padding(.bottom, 30)
            }
            .navigationBarTitleDisplayMode(.inline)
            .sheet(isPresented: $showSpectraLogger) {
                SpectraLoggerView()
            }
        }
    }
}

// Full app previews require a running simulator due to KMP framework dependencies.
// To test the full app:
// 1. Start a simulator: xcrun simctl boot "iPhone 16 Pro"
// 2. Run the app: xcodebuild install -scheme SpectraExample -configuration Debug -sdk iphonesimulator -arch arm64
// For quick UI previews, see Component Previews section below

// MARK: - Component Previews

#Preview("LogButton - Info") {
    LogButton(
        label: "Tap Me (Generates Log)",
        icon: "hand.tap",
        backgroundColor: .blue,
        action: {}
    )
    .padding()
}

#Preview("LogButton - Warning") {
    LogButton(
        label: "Generate Warning",
        icon: "exclamationmark.triangle",
        backgroundColor: .orange,
        action: {}
    )
    .padding()
}

#Preview("LogButton - Error") {
    LogButton(
        label: "Generate Error",
        icon: "xmark.circle",
        backgroundColor: .red,
        action: {}
    )
    .padding()
}

#Preview("AppBrandingCard") {
    AppBrandingCard()
}

#Preview("SectionHeader") {
    SectionHeader(title: "Example Actions")
        .padding()
}
