import Foundation
import SpectraLogger

@MainActor
public class NetworkTestingViewModel: ObservableObject {
    let mockDataGenerator: MockDataGenerator
    
    public init(logger: AppLogger) {
        self.mockDataGenerator = MockDataGenerator(logger: logger)
    }
    
    public func simulateGet200() { mockDataGenerator.simulateNetworkRequest(method: "GET", url: "https://api.example.com/users", statusCode: 200, duration: 0.5) }
    public func simulatePost201() { mockDataGenerator.simulateNetworkRequest(method: "POST", url: "https://api.example.com/users", statusCode: 201, duration: 1.0) }
    public func simulateGet404() { mockDataGenerator.simulateNetworkRequest(method: "GET", url: "https://api.example.com/users/9999", statusCode: 404, duration: 0.3) }
    public func simulateError500() { mockDataGenerator.simulateNetworkRequest(method: "GET", url: "https://api.example.com/data", statusCode: 500, duration: 2.0) }
    public func simulatePut200() { mockDataGenerator.simulateNetworkRequest(method: "PUT", url: "https://api.example.com/users/123", statusCode: 200, duration: 0.6) }
    public func simulateDelete204() { mockDataGenerator.simulateNetworkRequest(method: "DELETE", url: "https://api.example.com/users/456", statusCode: 204, duration: 0.4) }
    public func simulateRateLimit429() { mockDataGenerator.simulateNetworkRequest(method: "POST", url: "https://api.example.com/bulk-upload", statusCode: 429, duration: 0.1) }
    
    public func simulateBatchCalls() {
        let endpoints = ["users", "orders", "products", "inventory", "analytics"]
        let statuses = [200, 200, 200, 404, 500]
        for i in 0..<10 {
            Task {
                try? await Task.sleep(nanoseconds: UInt64(200_000_000 * i))
                mockDataGenerator.simulateNetworkRequest(
                    method: i % 3 == 0 ? "POST" : "GET",
                    url: "https://api.example.com/\(endpoints[i % endpoints.count])",
                    statusCode: statuses[i % statuses.count],
                    duration: 0.2 + Double(i) * 0.1
                )
            }
        }
    }
}
