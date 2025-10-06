import SwiftUI
import SpectraLogger

/// SwiftUI wrapper for the Spectra Logger UIViewController from KMP SDK
struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Create the complete Spectra Logger screen from the KMP SDK
        // This includes all tabs (Logs, Network, Settings) built-in
        return SpectraLoggerViewControllerKt.createSpectraLoggerViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed
    }
}
