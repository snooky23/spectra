import SwiftUI
import UIKit
import SpectraLoggerUI

/**
 * A SwiftUI wrapper for the Compose Multiplatform Spectra Logger UI.
 */
struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Initialize the UI module if needed (registers the provider)
        SpectraUI.shared.doInit()
        
        // Create the controller from the KMP UI module
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: {
            SpectraUIManager.shared.dismissScreen()
        })
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No update needed
    }
}
