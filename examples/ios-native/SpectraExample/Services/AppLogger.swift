import Foundation
import Spectra

/// A protocol that abstracts the logging capability so it can be mocked in SwiftUI Previews.
public protocol AppLogger {
    func v(tag: String, message: String, metadata: [String: String])
    func d(tag: String, message: String, metadata: [String: String])
    func i(tag: String, message: String, metadata: [String: String])
    func w(tag: String, message: String, metadata: [String: String])
    func e(tag: String, message: String, metadata: [String: String])
    func f(tag: String, message: String, metadata: [String: String])
    func logNetworkRequest(_ entry: NetworkLogEntry)
    
    // Event & Screen tracking
    func event(name: String, parameters: [String: String], eventType: EventType, durationMs: KotlinLong?)
    func screenStart(screenName: String, parameters: [String: String])
    func screenEnd(screenName: String, additionalParameters: [String: String])
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
    
    public func event(name: String, parameters: [String: String] = [:], eventType: EventType = EventType.userAction, durationMs: KotlinLong? = nil) {
        SpectraLogger.shared.event(name: name, parameters: parameters, eventType: eventType, durationMs: durationMs)
    }
    
    public func screenStart(screenName: String, parameters: [String: String] = [:]) {
        SpectraLogger.shared.screenStart(screenName: screenName, parameters: parameters)
    }
    
    public func screenEnd(screenName: String, additionalParameters: [String: String] = [:]) {
        SpectraLogger.shared.screenEnd(screenName: screenName, additionalParameters: additionalParameters)
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
    
    public func event(name: String, parameters: [String: String] = [:], eventType: EventType = EventType.userAction, durationMs: KotlinLong? = nil) {
        print("[EVENT] [\(eventType)] \(name) - params: \(parameters) - duration: \(String(describing: durationMs))")
    }
    
    public func screenStart(screenName: String, parameters: [String: String] = [:]) {
        print("[SCREEN_START] \(screenName) - params: \(parameters)")
    }
    
    public func screenEnd(screenName: String, additionalParameters: [String: String] = [:]) {
        print("[SCREEN_END] \(screenName) - params: \(additionalParameters)")
    }
}
