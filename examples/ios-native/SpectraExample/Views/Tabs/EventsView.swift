import SwiftUI
import Spectra

/// Tab showing event tracking, screen views, and telemetry simulation examples
public struct EventsView: View {
    @StateObject private var viewModel: EventsTestingViewModel
    @State private var showLocalSpectraLogger = false
    private let onOpenSpectra: (() -> Void)?

    public init(logger: AppLogger, onOpenSpectra: (() -> Void)? = nil) {
        _viewModel = StateObject(wrappedValue: EventsTestingViewModel(logger: logger))
        self.onOpenSpectra = onOpenSpectra
    }

    private func handleOpenSpectra() {
        if let onOpenSpectra = onOpenSpectra {
            onOpenSpectra()
        } else {
            showLocalSpectraLogger = true
        }
    }

    public var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    Spacer().frame(height: 20)

                    BrandingCard(
                        icon: "waveform.path.ecg",
                        title: "Events Testing",
                        subtitle: "Track screen views, user actions & telemetry"
                    )

                    Spacer().frame(height: 20)
                    SectionHeader(title: "Screen View Tracking")

                    LogButton(
                        label: viewModel.isTrackingProductDetails ? "⏹ End 'ProductDetails' Screen" : "▶ Start 'ProductDetails' Screen",
                        icon: viewModel.isTrackingProductDetails ? "stop.circle.fill" : "play.circle.fill",
                        backgroundColor: viewModel.isTrackingProductDetails ? .red : .green
                    ) {
                        viewModel.toggleProductDetailsScreenView()
                    }

                    Text(viewModel.isTrackingProductDetails ? "Dwell timer active... Tap to stop and record durationMs" : "Starts duration timer until you tap again")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    LogButton(
                        label: "Simulate 'Cart' Screen View (1.5s Dwell)",
                        icon: "eye.fill",
                        backgroundColor: .blue
                    ) {
                        viewModel.simulateQuickScreenView()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "User Actions")

                    LogButton(
                        label: "Add to Cart (with SKU & Price)",
                        icon: "cart.fill",
                        backgroundColor: .pink
                    ) {
                        viewModel.logAddToCart()
                    }

                    LogButton(
                        label: "Like Post (with Reaction param)",
                        icon: "heart.fill",
                        backgroundColor: .pink
                    ) {
                        viewModel.logLikePost()
                    }

                    LogButton(
                        label: "Submit Search Query",
                        icon: "magnifyingglass",
                        backgroundColor: .purple
                    ) {
                        viewModel.logSearchSubmitted()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Lifecycle Events")

                    LogButton(
                        label: "Simulate App Backgrounded",
                        icon: "pause.circle.fill",
                        backgroundColor: .orange
                    ) {
                        viewModel.logAppBackground()
                    }

                    LogButton(
                        label: "Simulate App Foregrounded",
                        icon: "play.circle.fill",
                        backgroundColor: .orange
                    ) {
                        viewModel.logAppForeground()
                    }

                    LogButton(
                        label: "Simulate Low Memory Warning",
                        icon: "exclamationmark.triangle.fill",
                        backgroundColor: .red
                    ) {
                        viewModel.logLowMemoryWarning()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Custom Business Events")

                    LogButton(
                        label: "Purchase Completed ($249.99)",
                        icon: "checkmark.seal.fill",
                        backgroundColor: Color(red: 0.0, green: 0.6, blue: 0.5)
                    ) {
                        viewModel.logPurchaseCompleted()
                    }

                    LogButton(
                        label: "Feature Flag Evaluated",
                        icon: "slider.horizontal.3",
                        backgroundColor: Color(red: 0.38, green: 0.49, blue: 0.55)
                    ) {
                        viewModel.logFeatureFlagEvaluated()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Batch Generation")

                    LogButton(
                        label: "Generate 20 Mixed Events",
                        icon: "list.bullet.rectangle.portrait",
                        backgroundColor: .indigo
                    ) {
                        viewModel.generate20MixedEvents()
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
        .sheet(isPresented: $showLocalSpectraLogger) {
            SpectraLoggerView()
        }
    }
}

#Preview("Events View") {
    EventsView(logger: MockAppLogger())
}
