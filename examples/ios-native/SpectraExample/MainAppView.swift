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

// MARK: - Utility Functions

/// Simulates a network request and logs it via URLSession
func simulateNetworkRequest(method: String, url: String, statusCode: Int, duration: Double) {
    Task.detached { () -> Void in
        let startTime = Date()

        // Log the request initiation
        SpectraLogger.shared.d(
            tag: "Network",
            message: "Starting \(method) request to \(url)",
            throwable: nil,
            metadata: [
                "method": method,
                "url": url,
                "type": "request"
            ]
        )

        // Simulate network delay
        try? await Task.sleep(nanoseconds: UInt64(duration * 1_000_000_000))

        let elapsed = Date().timeIntervalSince(startTime)

        // Log the response based on status code
        let logLevel: (_ tag: String, _ message: String, _ throwable: Error?, _ metadata: [String: String]) -> Void = statusCode >= 400 ? SpectraLogger.shared.w : SpectraLogger.shared.i

        logLevel(
            "Network",
            "\(method) \(url) - Status \(statusCode)",
            nil,
            [
                "method": method,
                "url": url,
                "status_code": String(statusCode),
                "duration_ms": String(format: "%.0f", elapsed * 1000),
                "response_size": "2048",
                "type": "response"
            ]
        )
    }
}

/// Generates a mock stack trace for demonstrating error logging
func generateStackTrace() -> String {
    let stackTrace = """
    Fatal error: Attempted to divide by zero
    Stack trace:
    0 SpectraExample                    0x0000000104b8e3a0 calculateDivision(_:) + 52
    1 SpectraExample                    0x0000000104b8e2c8 processUserInput(_:) + 120
    2 SpectraExample                    0x0000000104b8e1f0 handleButtonTap() + 88
    3 UIKitCore                         0x00000001a01c9e20 -[UIApplication sendAction:to:from:forEvent:] + 96
    4 UIKitCore                         0x00000001a01c9c00 -[UIControl sendAction:withEvent:] + 128
    5 UIKitCore                         0x00000001a01ca020 -[UIControl _sendActionsForEvents:withEvent:] + 324
    6 SwiftUI                           0x00000001a8c4f188 <closure>() + 420
    7 SwiftUI                           0x00000001a8c4e5d8 closure #1 in _EnvironmentKeyWritingModifier.body + 360
    8 CoreFoundation                    0x000000019fb3e504 __CFRUNLOOP_IS_CALLING_OUT_TO_A_BLOCK__ + 24
    9 CoreFoundation                    0x000000019fb3e050 __CFRunLoopDoBlocks + 368
    10 CoreFoundation                   0x000000019fb3c9e0 __CFRunLoopRun + 828
    11 CoreFoundation                   0x000000019fb3c400 CFRunLoopRunSpecific + 600
    12 GraphicsServices                 0x00000001a4a96050 GSEventRunModal + 164
    13 UIKitCore                        0x00000001a01b1de8 -[UIApplication _run] + 888
    14 UIKitCore                        0x00000001a01b1268 UIApplicationMain + 340
    15 SpectraExample                   0x0000000104b8e000 main + 8
    16 dyld                             0x00000001a01b9e94 start + 2220
    """
    return stackTrace
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

                    LogButton(
                        label: "Error with Stack Trace",
                        icon: "exclamationmark.triangle.fill",
                        backgroundColor: .red,
                        action: {
                            let stackTrace = generateStackTrace()
                            SpectraLogger.shared.e(
                                tag: "Example",
                                message: "Fatal error: Attempted to divide by zero",
                                throwable: nil,
                                metadata: [
                                    "operation": "calculateDivision",
                                    "dividend": "10",
                                    "divisor": "0",
                                    "severity": "CRITICAL",
                                    "error_type": "ArithmeticException",
                                    "stack_trace": stackTrace
                                ]
                            )
                        }
                    )
                }
                .padding(.horizontal)

                Spacer()
                    .frame(height: 10)

                // Network request simulation section
                VStack(spacing: 20) {
                    SectionHeader(title: "Network Requests")

                    LogButton(
                        label: "GET Request (200 OK)",
                        icon: "arrow.down.circle",
                        backgroundColor: .green,
                        action: {
                            simulateNetworkRequest(
                                method: "GET",
                                url: "https://api.example.com/users",
                                statusCode: 200,
                                duration: 0.5
                            )
                        }
                    )

                    LogButton(
                        label: "POST Request (201 Created)",
                        icon: "plus.circle",
                        backgroundColor: .green,
                        action: {
                            simulateNetworkRequest(
                                method: "POST",
                                url: "https://api.example.com/users",
                                statusCode: 201,
                                duration: 1.0
                            )
                        }
                    )

                    LogButton(
                        label: "GET Request (404 Not Found)",
                        icon: "questionmark.circle",
                        backgroundColor: .orange,
                        action: {
                            simulateNetworkRequest(
                                method: "GET",
                                url: "https://api.example.com/users/9999",
                                statusCode: 404,
                                duration: 0.3
                            )
                        }
                    )

                    LogButton(
                        label: "Server Error (500)",
                        icon: "xmark.circle.fill",
                        backgroundColor: .red,
                        action: {
                            simulateNetworkRequest(
                                method: "GET",
                                url: "https://api.example.com/data",
                                statusCode: 500,
                                duration: 2.0
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
