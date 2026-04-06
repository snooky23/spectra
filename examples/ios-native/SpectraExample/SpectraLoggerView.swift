import SwiftUI
import UIKit
import SpectraLogger // Business logic and UIManager
import SpectraLoggerUI // The new KMP UI module framework

/**
 * A SwiftUI wrapper for the Compose Multiplatform Spectra Logger UI.
 */
struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Initialize the UI module if needed (registers the provider)
        SpectraUI.shared.init()
        
        // Create the controller from the KMP UI module
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: {
            SpectraUIManager.shared.dismissScreen()
        })
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No update needed
    }
}
