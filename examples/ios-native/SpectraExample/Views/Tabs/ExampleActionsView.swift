import SwiftUI
import SpectraLoggerUI

/// Tab showing basic logging examples
public struct ExampleActionsView: View {
    @StateObject private var viewModel: ActionsViewModel
    @State private var showSpectraLogger = false

    public init(logger: AppLogger) {
        _viewModel = StateObject(wrappedValue: ActionsViewModel(logger: logger))
    }

    public var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    Spacer().frame(height: 20)

                    BrandingCard(
                        icon: "app.badge.checkmark",
                        title: "Example App",
                        subtitle: "with Spectra Logger Integration"
                    )

                    Spacer().frame(height: 20)
                    SectionHeader(title: "Example Actions")

                    LogButton(label: "Tap Me (Generates Log)", icon: "hand.tap", backgroundColor: .blue) {
                        viewModel.logButtonTapped()
                    }

                    LogButton(label: "Generate Warning", icon: "exclamationmark.triangle", backgroundColor: .orange) {
                        viewModel.generateWarning()
                    }

                    LogButton(label: "Generate Error", icon: "xmark.circle", backgroundColor: .red) {
                        viewModel.generateError()
                    }

                    LogButton(label: "Error with Stack Trace", icon: "exclamationmark.triangle.fill", backgroundColor: .red) {
                        viewModel.generateErrorWithStackTrace()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Batch Logging")

                    LogButton(label: "Generate 10 Logs", icon: "list.bullet", backgroundColor: .purple) {
                        viewModel.generate10Logs()
                    }

                    LogButton(label: "Generate 100 Logs", icon: "list.bullet.rectangle", backgroundColor: .purple) {
                        viewModel.generate100Logs()
                    }

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Real-Time Demo")

                    LogButton(
                        label: viewModel.isBackgroundLogging ? "⏹ Stop Background Logging" : "▶ Start Background Logging",
                        icon: viewModel.isBackgroundLogging ? "stop.circle" : "play.circle",
                        backgroundColor: viewModel.isBackgroundLogging ? .red : .green
                    ) {
                        viewModel.toggleBackgroundLogging()
                    }

                    Text("Logs every 2 seconds while running")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Spacer().frame(height: 16)
                    SectionHeader(title: "Filtering Demos")

                    LogButton(label: "Generate All Log Levels", icon: "slider.horizontal.3", backgroundColor: Color(red: 0.38, green: 0.49, blue: 0.55)) {
                        viewModel.generateAllLogLevels()
                    }

                    LogButton(label: "Generate Searchable Logs", icon: "magnifyingglass", backgroundColor: Color(red: 0.38, green: 0.49, blue: 0.55)) {
                        viewModel.generateSearchableLogs()
                    }

                    Spacer().frame(height: 10)
                }
                .padding(.horizontal)
            }

            Divider()

            Button(action: { showSpectraLogger = true }) {
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

#Preview("Example Actions View") {
    ExampleActionsView(logger: MockAppLogger())
}
