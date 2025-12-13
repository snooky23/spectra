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

/// Reusable branding card component
struct BrandingCard: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 80))
                .foregroundColor(.blue)

            Text(title)
                .font(.largeTitle)
                .fontWeight(.bold)

            Text(subtitle)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
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

/// Simulates a network request and logs it to the network logs section
func simulateNetworkRequest(method: String, url: String, statusCode: Int, duration: Double) {
    Task.detached(priority: .userInitiated) {
        // Simulate network delay on background thread
        try? await Task.sleep(nanoseconds: UInt64(duration * 1_000_000_000))

        let durationMs = Int64(duration * 1000)

        // Generate realistic request body based on HTTP method
        let requestBody: String? = {
            switch method {
            case "POST":
                return "{\"username\": \"testuser\", \"email\": \"test@example.com\", \"timestamp\": \"\(Date().timeIntervalSince1970)\"}"
            case "PUT":
                return "{\"id\": \"123\", \"status\": \"updated\", \"timestamp\": \"\(Date().timeIntervalSince1970)\"}"
            case "DELETE":
                return "{\"id\": \"123\", \"confirm\": true}"
            case "PATCH":
                return "{\"field\": \"value\", \"operation\": \"update\"}"
            default:
                return nil
            }
        }()

        // Generate realistic response headers based on status code
        var responseHeaders: [String: String] = [
            "Content-Type": "application/json",
            "Server": "Example/1.0",
            "X-Response-Time": "\(durationMs)ms"
        ]

        // Add status-specific headers
        if statusCode >= 200 && statusCode < 300 {
            responseHeaders["Cache-Control"] = "max-age=3600"
            responseHeaders["ETag"] = "\"abc123\""
        } else if statusCode >= 400 {
            responseHeaders["Cache-Control"] = "no-cache, no-store"
            responseHeaders["Retry-After"] = statusCode == 429 ? "60" : nil
        }

        // Generate response body based on status code
        let responseBody: String = {
            switch statusCode {
            case 200:
                return "{\"success\": true, \"data\": {\"id\": \"123\", \"message\": \"Request completed successfully\"}}"
            case 201:
                return "{\"success\": true, \"id\": \"newly-created-id\", \"message\": \"Resource created\"}"
            case 400:
                return "{\"error\": \"Bad Request\", \"details\": \"Invalid request parameters\", \"code\": \"INVALID_PARAMS\"}"
            case 404:
                return "{\"error\": \"Not Found\", \"details\": \"The requested resource does not exist\", \"code\": \"RESOURCE_NOT_FOUND\"}"
            case 500:
                return "{\"error\": \"Internal Server Error\", \"details\": \"An unexpected error occurred\", \"code\": \"INTERNAL_ERROR\", \"requestId\": \"req-\(UUID().uuidString)\"}"
            default:
                return "{\"error\": \"HTTP \(statusCode)\", \"message\": \"Request failed with status code \(statusCode)\"}"
            }
        }()

        // Create a network log entry with proper KMP type conversions
        let networkLogEntry = NetworkLogEntry(
            id: UUID().uuidString,
            timestamp: Kotlinx_datetimeInstant.companion.fromEpochMilliseconds(
                epochMilliseconds: Int64(Date().timeIntervalSince1970 * 1000)
            ),
            url: url,
            method: method,
            requestHeaders: [
                "Content-Type": "application/json",
                "User-Agent": "SpectraExample/1.0",
                "Accept": "application/json",
                "Authorization": "Bearer token_example_12345"
            ],
            requestBody: requestBody,
            responseCode: KotlinInt(int: Int32(statusCode)),
            responseHeaders: responseHeaders,
            responseBody: responseBody,
            duration: durationMs,
            error: statusCode >= 400 ? "HTTP \(statusCode): Request failed" : nil
        )

        // Add to network logs storage - KMP suspend functions require main thread
        Task { @MainActor in
            try? await SpectraLogger.shared.networkStorage.add(entry: networkLogEntry)
        }
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

// MARK: - Tab: Example Actions

/// Tab showing basic logging examples
struct ExampleActionsTab: View {
    @State private var counter = 0
    @State private var showSpectraLogger = false
    @State private var isBackgroundLogging = false
    @State private var backgroundLogCount = 0

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    Spacer()
                        .frame(height: 20)

                    // App branding
                    BrandingCard(
                        icon: "app.badge.checkmark",
                        title: "Example App",
                        subtitle: "with Spectra Logger Integration"
                    )

                    Spacer()
                        .frame(height: 20)

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

                    // MARK: Batch Logging Section
                    Spacer().frame(height: 16)
                    SectionHeader(title: "Batch Logging")

                    LogButton(
                        label: "Generate 10 Logs",
                        icon: "list.bullet",
                        backgroundColor: .purple,
                        action: {
                            let tags = ["Auth", "Network", "UI", "Database"]
                            for i in 0..<10 {
                                let tag = tags[i % tags.count]
                                switch i % 4 {
                                case 0: SpectraLogger.shared.d(tag: tag, message: "Debug log entry #\(i + 1)", throwable: nil, metadata: [:])
                                case 1: SpectraLogger.shared.i(tag: tag, message: "Info log entry #\(i + 1)", throwable: nil, metadata: [:])
                                case 2: SpectraLogger.shared.w(tag: tag, message: "Warning log entry #\(i + 1)", throwable: nil, metadata: [:])
                                default: SpectraLogger.shared.e(tag: tag, message: "Error log entry #\(i + 1)", throwable: nil, metadata: [:])
                                }
                            }
                        }
                    )

                    LogButton(
                        label: "Generate 100 Logs",
                        icon: "list.bullet.rectangle",
                        backgroundColor: .purple,
                        action: {
                            let tags = ["Auth", "Network", "UI", "Database", "Cache", "API"]
                            for i in 0..<100 {
                                let tag = tags[i % tags.count]
                                SpectraLogger.shared.i(tag: tag, message: "Batch log entry #\(i + 1) - stress test", throwable: nil, metadata: [:])
                            }
                        }
                    )

                    // MARK: Real-Time Demo Section
                    Spacer().frame(height: 16)
                    SectionHeader(title: "Real-Time Demo")

                    LogButton(
                        label: isBackgroundLogging ? "⏹ Stop Background Logging" : "▶ Start Background Logging",
                        icon: isBackgroundLogging ? "stop.circle" : "play.circle",
                        backgroundColor: isBackgroundLogging ? .red : .green,
                        action: {
                            isBackgroundLogging.toggle()
                            if isBackgroundLogging {
                                startBackgroundLogging()
                            }
                        }
                    )

                    Text("Logs every 2 seconds while running")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    // MARK: Filtering Demo Section
                    Spacer().frame(height: 16)
                    SectionHeader(title: "Filtering Demos")

                    LogButton(
                        label: "Generate All Log Levels",
                        icon: "slider.horizontal.3",
                        backgroundColor: Color(red: 0.38, green: 0.49, blue: 0.55),
                        action: {
                            SpectraLogger.shared.v(tag: "LevelDemo", message: "This is a VERBOSE message", throwable: nil, metadata: [:])
                            SpectraLogger.shared.d(tag: "LevelDemo", message: "This is a DEBUG message", throwable: nil, metadata: [:])
                            SpectraLogger.shared.i(tag: "LevelDemo", message: "This is an INFO message", throwable: nil, metadata: [:])
                            SpectraLogger.shared.w(tag: "LevelDemo", message: "This is a WARNING message", throwable: nil, metadata: [:])
                            SpectraLogger.shared.e(tag: "LevelDemo", message: "This is an ERROR message", throwable: nil, metadata: [:])
                            SpectraLogger.shared.f(tag: "LevelDemo", message: "This is a FATAL message", throwable: nil, metadata: [:])
                        }
                    )

                    LogButton(
                        label: "Generate Searchable Logs",
                        icon: "magnifyingglass",
                        backgroundColor: Color(red: 0.38, green: 0.49, blue: 0.55),
                        action: {
                            SpectraLogger.shared.i(tag: "SearchDemo", message: "Order #12345 placed successfully", throwable: nil, metadata: [:])
                            SpectraLogger.shared.i(tag: "SearchDemo", message: "User john@example.com logged in", throwable: nil, metadata: [:])
                            SpectraLogger.shared.w(tag: "SearchDemo", message: "Payment processing delayed for order #12345", throwable: nil, metadata: [:])
                            SpectraLogger.shared.e(tag: "SearchDemo", message: "Failed to send email to john@example.com", throwable: nil, metadata: [:])
                        }
                    )

                    Spacer()
                        .frame(height: 10)
                }
                .padding(.horizontal)
            }

            Divider()

            // Open Spectra Logger button - fixed at bottom
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
            .padding()
        }
        .sheet(isPresented: $showSpectraLogger) {
            SpectraLoggerView()
        }
        .onChange(of: isBackgroundLogging) { oldValue, newValue in
            if !newValue {
                backgroundLogCount = 0
            }
        }
    }

    private func startBackgroundLogging() {
        Task {
            while isBackgroundLogging {
                backgroundLogCount += 1
                SpectraLogger.shared.i(tag: "BackgroundTask", message: "Real-time log #\(backgroundLogCount) at \(Date())", throwable: nil, metadata: [:])
                try? await Task.sleep(nanoseconds: 2_000_000_000)
            }
        }
    }
}

// MARK: - Tab: Network Requests

/// Tab showing network request simulation examples
struct NetworkRequestsTab: View {
    @State private var showSpectraLogger = false

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    Spacer()
                        .frame(height: 20)

                    // App branding
                    BrandingCard(
                        icon: "network",
                        title: "Network Testing",
                        subtitle: "Simulate HTTP requests and responses"
                    )

                    Spacer()
                        .frame(height: 20)

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

                    // MARK: More HTTP Methods
                    Spacer().frame(height: 16)
                    SectionHeader(title: "More HTTP Methods")

                    LogButton(
                        label: "PUT Request (200 OK)",
                        icon: "arrow.up.circle",
                        backgroundColor: .green,
                        action: {
                            simulateNetworkRequest(
                                method: "PUT",
                                url: "https://api.example.com/users/123",
                                statusCode: 200,
                                duration: 0.6
                            )
                        }
                    )

                    LogButton(
                        label: "DELETE Request (204 No Content)",
                        icon: "trash.circle",
                        backgroundColor: .green,
                        action: {
                            simulateNetworkRequest(
                                method: "DELETE",
                                url: "https://api.example.com/users/456",
                                statusCode: 204,
                                duration: 0.4
                            )
                        }
                    )

                    LogButton(
                        label: "Rate Limited (429)",
                        icon: "clock.badge.exclamationmark",
                        backgroundColor: .orange,
                        action: {
                            simulateNetworkRequest(
                                method: "POST",
                                url: "https://api.example.com/bulk-upload",
                                statusCode: 429,
                                duration: 0.1
                            )
                        }
                    )

                    // MARK: Batch Network
                    Spacer().frame(height: 16)
                    SectionHeader(title: "Batch Network")

                    LogButton(
                        label: "Simulate 10 API Calls",
                        icon: "list.bullet.rectangle",
                        backgroundColor: .purple,
                        action: {
                            let endpoints = ["users", "orders", "products", "inventory", "analytics"]
                            let statuses = [200, 200, 200, 404, 500]
                            for i in 0..<10 {
                                Task {
                                    try? await Task.sleep(nanoseconds: UInt64(200_000_000 * i))
                                    simulateNetworkRequest(
                                        method: i % 3 == 0 ? "POST" : "GET",
                                        url: "https://api.example.com/\(endpoints[i % endpoints.count])",
                                        statusCode: statuses[i % statuses.count],
                                        duration: 0.2 + Double(i) * 0.1
                                    )
                                }
                            }
                        }
                    )

                    Spacer()
                        .frame(height: 10)
                }
                .padding(.horizontal)
            }

            Divider()

            // Open Spectra Logger button - fixed at bottom
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
            .padding()
        }
        .sheet(isPresented: $showSpectraLogger) {
            SpectraLoggerView()
        }
    }
}

// MARK: - Main App View

/// Main app screen with tab-based navigation for different example types
struct MainAppView: View {
    var body: some View {
        TabView {
            ExampleActionsTab()
                .tabItem {
                    Label("Actions", systemImage: "sparkles")
                }

            NetworkRequestsTab()
                .tabItem {
                    Label("Network", systemImage: "network")
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

#Preview("BrandingCard") {
    BrandingCard(
        icon: "app.badge.checkmark",
        title: "Example App",
        subtitle: "with Spectra Logger Integration"
    )
    .padding()
}

#Preview("SectionHeader") {
    SectionHeader(title: "Example Actions")
        .padding()
}
