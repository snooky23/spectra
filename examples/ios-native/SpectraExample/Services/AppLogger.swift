import Foundation
import SpectraLoggerUI

/// A protocol that abstracts the logging capability so it can be mocked in SwiftUI Previews.
public protocol AppLogger {
    func v(tag: String, message: String, metadata: [String: String])
    func d(tag: String, message: String, metadata: [String: String])
    func i(tag: String, message: String, metadata: [String: String])
    func w(tag: String, message: String, metadata: [String: String])
    func e(tag: String, message: String, metadata: [String: String])
    func f(tag: String, message: String, metadata: [String: String])
    func logNetworkRequest(_ entry: NetworkLogEntry)
}

/// The production logger that wraps the real KMP SpectraLogger
public struct LiveAppLogger: AppLogger {
    public init() {}
    
    public func v(tag: String, message: String, metadata: [String: String] = [:]) {
        SpectraLogger.shared.v(tag: tag, message: message, throwable: nil, metadata: metadata)
    }
    
    public func d(tag: String, message: String, metadata: [String: String] = [:]) {
        SpectraLogger.shared.d(tag: tag, message: message, throwable: nil, metadata: metadata)
    }
    
    public func i(tag: String, message: String, metadata: [String: String] = [:]) {
        SpectraLogger.shared.i(tag: tag, message: message, throwable: nil, metadata: metadata)
    }
    
    public func w(tag: String, message: String, metadata: [String: String] = [:]) {
        SpectraLogger.shared.w(tag: tag, message: message, throwable: nil, metadata: metadata)
    }
    
    public func e(tag: String, message: String, metadata: [String: String] = [:]) {
        SpectraLogger.shared.e(tag: tag, message: message, throwable: nil, metadata: metadata)
    }
    
    public func f(tag: String, message: String, metadata: [String: String] = [:]) {
        SpectraLogger.shared.f(tag: tag, message: message, throwable: nil, metadata: metadata)
    }
    
    public func logNetworkRequest(_ entry: NetworkLogEntry) {
        Task { @MainActor in
            try? await SpectraLogger.shared.networkStorage.add(entry: entry)
        }
    }
}

/// A mock logger that safely prints to console, heavily used for SwiftUI Previews to avoid crashing the Canvas
public struct MockAppLogger: AppLogger {
    public init() {}
    
    public func v(tag: String, message: String, metadata: [String: String] = [:]) { print("[VERBOSE] [\(tag)] \(message)") }
    public func d(tag: String, message: String, metadata: [String: String] = [:]) { print("[DEBUG] [\(tag)] \(message)") }
    public func i(tag: String, message: String, metadata: [String: String] = [:]) { print("[INFO] [\(tag)] \(message)") }
    public func w(tag: String, message: String, metadata: [String: String] = [:]) { print("[WARNING] [\(tag)] \(message)") }
    public func e(tag: String, message: String, metadata: [String: String] = [:]) { print("[ERROR] [\(tag)] \(message)") }
    public func f(tag: String, message: String, metadata: [String: String] = [:]) { print("[FATAL] [\(tag)] \(message)") }
    public func logNetworkRequest(_ entry: NetworkLogEntry) { print("[NETWORK] \(entry.method) \(entry.url) - \(entry.responseCode)") }
}
