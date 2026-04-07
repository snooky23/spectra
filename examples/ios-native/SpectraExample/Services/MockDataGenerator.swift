import Foundation
import SpectraLogger

/// A service to generate mock network requests and fake stack traces for the example app.
public struct MockDataGenerator {
    
    let logger: AppLogger
    
    public init(logger: AppLogger) {
        self.logger = logger
    }
    
    /// Generates a mock stack trace for demonstrating error logging
    public static func generateStackTrace() -> String {
        return """
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
        """
    }

    /// Simulates a network request and logs it to the network logs section via the AppLogger
    public func simulateNetworkRequest(method: String, url: String, statusCode: Int?, duration: Double, errorMessage: String? = nil) {
        Task.detached(priority: .userInitiated) {
            // Simulate network delay on background thread
            try? await Task.sleep(nanoseconds: UInt64(duration * 1_000_000_000))
            let durationMs = Int64(duration * 1000)

            // Generate realistic request body based on HTTP method
            let requestBody: String? = {
                switch method {
                case "POST": return "{\"username\": \"testuser\", \"email\": \"test@example.com\"}"
                case "PUT": return "{\"id\": \"123\", \"status\": \"updated\"}"
                case "DELETE": return "{\"id\": \"123\", \"confirm\": true}"
                case "PATCH": return "{\"field\": \"value\", \"operation\": \"update\"}"
                default: return nil
                }
            }()

            // Generate realistic response headers based on status code
            var responseHeaders: [String: String] = [:]
            
            if let code = statusCode {
                responseHeaders = [
                    "Content-Type": "application/json",
                    "Server": "Example/1.0",
                    "X-Response-Time": "\(durationMs)ms"
                ]

                if code >= 200 && code < 300 {
                    responseHeaders["Cache-Control"] = "max-age=3600"
                    responseHeaders["ETag"] = "\"abc123\""
                } else if code >= 400 {
                    responseHeaders["Cache-Control"] = "no-cache, no-store"
                }
            }

            // Generate response body based on status code
            let responseBody: String? = {
                guard let code = statusCode else { return nil }
                switch code {
                case 200: return "{\"success\": true, \"data\": {\"id\": \"123\"}}"
                case 201: return "{\"success\": true, \"id\": \"newly-created-id\"}"
                case 400: return "{\"error\": \"Bad Request\", \"code\": \"INVALID_PARAMS\"}"
                case 404: return "{\"error\": \"Not Found\", \"code\": \"RESOURCE_NOT_FOUND\"}"
                case 500: return "{\"error\": \"Internal Server Error\", \"code\": \"INTERNAL_ERROR\"}"
                default: return "{\"error\": \"HTTP \(code)\"}"
                }
            }()

            let errorText = errorMessage ?? (statusCode != nil && statusCode! >= 400 ? "HTTP \(statusCode!): Request failed" : nil)
            let ktStatusCode = statusCode != nil ? KotlinInt(int: Int32(statusCode!)) : nil

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
                    "User-Agent": "SpectraExample/1.0"
                ],
                requestBody: requestBody,
                responseCode: ktStatusCode,
                responseHeaders: responseHeaders,
                responseBody: responseBody,
                duration: durationMs,
                error: errorText,
                source: "SpectraExample",
                sourceType: SourceType.app
            )

            // Add to network logs storage using the abstracted logger
            logger.logNetworkRequest(networkLogEntry)
        }
    }
}
