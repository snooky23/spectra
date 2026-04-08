import Foundation
import Combine
import SpectraLoggerUI

@MainActor
public class ActionsViewModel: ObservableObject {
    let logger: AppLogger
    
    @Published var counter = 0
    @Published var isBackgroundLogging = false
    @Published var backgroundLogCount = 0
    
    private var backgroundTask: Task<Void, Never>?
    
    public init(logger: AppLogger) {
        self.logger = logger
    }
    
    public func logButtonTapped() {
        counter += 1
        logger.i(tag: "Example", message: "Button tapped \(counter) times", metadata: [:])
    }
    
    public func generateWarning() {
        logger.w(tag: "Example", message: "Warning log generated", metadata: [:])
    }
    
    public func generateError() {
        logger.e(tag: "Example", message: "Error log generated", metadata: [:])
    }
    
    public func generateErrorWithStackTrace() {
        let stackTrace = MockDataGenerator.generateStackTrace()
        logger.e(
            tag: "Example",
            message: "Fatal error: Attempted to divide by zero",
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
    
    public func generate10Logs() {
        let tags = ["Auth", "Network", "UI", "Database"]
        for i in 0..<10 {
            let tag = tags[i % tags.count]
            switch i % 4 {
            case 0: logger.d(tag: tag, message: "Debug log entry #\(i + 1)", metadata: [:])
            case 1: logger.i(tag: tag, message: "Info log entry #\(i + 1)", metadata: [:])
            case 2: logger.w(tag: tag, message: "Warning log entry #\(i + 1)", metadata: [:])
            default: logger.e(tag: tag, message: "Error log entry #\(i + 1)", metadata: [:])
            }
        }
    }
    
    public func generate100Logs() {
        let tags = ["Auth", "Network", "UI", "Database", "Cache", "API"]
        for i in 0..<100 {
            let tag = tags[i % tags.count]
            logger.i(tag: tag, message: "Batch log entry #\(i + 1) - stress test", metadata: [:])
        }
    }
    
    public func toggleBackgroundLogging() {
        isBackgroundLogging.toggle()
        if isBackgroundLogging {
            startBackgroundLogging()
        } else {
            backgroundTask?.cancel()
            backgroundLogCount = 0
        }
    }
    
    private func startBackgroundLogging() {
        backgroundTask = Task {
            while isBackgroundLogging && !Task.isCancelled {
                backgroundLogCount += 1
                logger.i(tag: "BackgroundTask", message: "Real-time log #\(backgroundLogCount) at \(Date())", metadata: [:])
                try? await Task.sleep(nanoseconds: 2_000_000_000)
            }
        }
    }
    
    public func generateAllLogLevels() {
        logger.v(tag: "LevelDemo", message: "This is a VERBOSE message", metadata: [:])
        logger.d(tag: "LevelDemo", message: "This is a DEBUG message", metadata: [:])
        logger.i(tag: "LevelDemo", message: "This is an INFO message", metadata: [:])
        logger.w(tag: "LevelDemo", message: "This is a WARNING message", metadata: [:])
        logger.e(tag: "LevelDemo", message: "This is an ERROR message", metadata: [:])
        logger.f(tag: "LevelDemo", message: "This is a FATAL message", metadata: [:])
    }
    
    public func generateSearchableLogs() {
        logger.i(tag: "SearchDemo", message: "Order #12345 placed successfully", metadata: [:])
        logger.i(tag: "SearchDemo", message: "User john@example.com logged in", metadata: [:])
        logger.w(tag: "SearchDemo", message: "Payment processing delayed for order #12345", metadata: [:])
        logger.e(tag: "SearchDemo", message: "Failed to send email to john@example.com", metadata: [:])
    }
}
