import Foundation
import SpectraLogger

/// Utility functions for date and timestamp formatting
enum DateFormattingUtilities {
    // MARK: - Formatters

    private static let shortTimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        return formatter
    }()

    private static let fullTimestampFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, yyyy 'at' HH:mm:ss"
        return formatter
    }()

    // MARK: - Formatting Functions

    /// Formats a timestamp to short time format (HH:mm:ss)
    /// - Parameter timestamp: Kotlinx_datetimeInstant from KMP
    /// - Returns: Formatted string like "14:30:45"
    static func formatShortTime(_ timestamp: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp.epochSeconds))
        return shortTimeFormatter.string(from: date)
    }

    /// Formats a timestamp to full date and time format
    /// - Parameter timestamp: Kotlinx_datetimeInstant from KMP
    /// - Returns: Formatted string like "Oct 31, 2025 at 14:30:45"
    static func formatFullTimestamp(_ timestamp: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp.epochSeconds))
        return fullTimestampFormatter.string(from: date)
    }
}
