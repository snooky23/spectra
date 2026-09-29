import SwiftUI
import Spectra

/// Tab showing network request simulation examples
public struct NetworkRequestsView: View {
    @StateObject private var viewModel: NetworkTestingViewModel
    @State private var showSpectraLogger = false
    private let onOpenSpectra: (() -> Void)?

    public init(logger: AppLogger, onOpenSpectra: (() -> Void)? = nil) {
        _viewModel = StateObject(wrappedValue: NetworkTestingViewModel(logger: logger))
        self.onOpenSpectra = onOpenSpectra
    }

    private func handleOpenSpectra() {
        if let onOpenSpectra = onOpenSpectra {
            onOpenSpectra()
        } else {
            showSpectraLogger = true
        }
    }

    public var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    Spacer().frame(height: 20)

                    BrandingCard(
                        icon: "network",
                        title: "Network Testing",
                        subtitle: "Simulate HTTP requests and responses"
                    )

                    Spacer().frame(height: 20)
                    SectionHeader(title: "Network Requests")

                    LogButton(label: "GET Request (200 OK)", icon: "arrow.down.circle", backgroundColor: .green) {
                        viewModel.simulateGet200()
                    }

                    LogButton(label: "POST Request (201 Created)", icon: "plus.circle", backgroundColor: .green) {
                        viewModel.simulatePost201()
                    }

                    LogButton(label: "GET Request (404 Not Found)", icon: "questionmark.circle", backgroundColor: .orange) {
                        viewModel.simulateGet404()
                    }

                    LogButton(label: "Server Error (500)", icon: "xmark.circle.fill", backgroundColor: .red) {
                        viewModel.simulateError500()
                    }

                    LogButton(label: "Network Error (Timeout)", icon: "wifi.slash", backgroundColor: .red) {
                        viewModel.simulateNetworkError()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "More HTTP Methods")

                    LogButton(label: "PUT Request (200 OK)", icon: "arrow.up.circle", backgroundColor: .green) {
                        viewModel.simulatePut200()
                    }

                    LogButton(label: "DELETE Request (204 No Content)", icon: "trash.circle", backgroundColor: .green) {
                        viewModel.simulateDelete204()
                    }

                    LogButton(label: "Rate Limited (429)", icon: "clock.badge.exclamationmark", backgroundColor: .orange) {
                        viewModel.simulateRateLimit429()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Batch Network")

                    LogButton(label: "Simulate 10 API Calls", icon: "list.bullet.rectangle", backgroundColor: .purple) {
                        viewModel.simulateBatchCalls()
                    }

                    Spacer().frame(height: 16)

                    Button(action: handleOpenSpectra) {
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

                    Spacer().frame(height: 80)
                }
                .padding(.horizontal)
            }
        }
        .fullScreenCover(isPresented: $showSpectraLogger) {
            SpectraLoggerView(onDismiss: {
                showSpectraLogger = false
            })
        }
    }
}

#Preview("Network Requests View") {
    NetworkRequestsView(logger: MockAppLogger())
}
