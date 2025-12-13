import SwiftUI
import SpectraLogger

/// Utility functions for color mapping across the UI
enum ColorUtilities {
    // MARK: - Log Level Colors

    /// Returns the color for a given log level
    static func colorForLogLevel(_ level: LogLevel) -> Color {
        switch level {
        case .verbose:
            return .secondary
        case .debug:
            return .blue
        case .info:
            return .green
        case .warning:
            return .orange
        case .error:
            return .red
        case .fatal:
            return .purple
        default:
            return .gray
        }
    }

    // MARK: - HTTP Status Code Colors

    /// Returns the color for a given HTTP status code
    static func colorForStatusCode(_ status: Int32) -> Color {
        switch status {
        case 200..<300:
            return .green
        case 300..<400:
            return .blue
        case 400..<500:
            return .orange
        case 500..<600:
            return .red
        default:
            return .gray
        }
    }

    /// Returns the color for a status code range (e.g., "2xx", "4xx")
    static func colorForStatusRange(_ range: String) -> Color {
        switch range {
        case "2xx":
            return .green
        case "3xx":
            return .blue
        case "4xx":
            return .orange
        case "5xx":
            return .red
        default:
            return .gray
        }
    }
}
