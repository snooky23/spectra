package com.spectra.logger.ui

/**
 * Shake detection on iOS.
 *
 * **Note**: iOS shake detection must be implemented in native Swift/Objective-C
 * by overriding `motionEnded(_:with:)` in your UIViewController.
 *
 * Example Swift implementation:
 * ```swift
 * import UIKit
 * import SpectraLogger
 *
 * class ViewController: UIViewController {
 *     override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
 *         if motion == .motionShake {
 *             // Show Spectra Logger UI
 *             // TODO: Implement iOS UI presentation
 *         }
 *     }
 * }
 * ```
 *
 * To detect shakes globally across your app, you can create a base UIViewController
 * class and have all your view controllers inherit from it.
 */
object ShakeDetector {
    /**
     * Placeholder for iOS shake detection.
     * Actual implementation must be done in Swift/Objective-C.
     */
    fun enableShakeToOpen() {
        // iOS implementation must be done in native Swift
        // by overriding motionEnded in UIViewController
    }
}
