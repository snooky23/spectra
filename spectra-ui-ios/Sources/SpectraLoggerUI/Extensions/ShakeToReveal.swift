import SwiftUI
import SpectraLogger

// MARK: - Shake Gesture Detection

/// Override UIWindow class to intercept the physical shake gesture globally
extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        super.motionEnded(motion, with: event)
        if motion == .motionShake {
            NotificationCenter.default.post(name: .deviceDidShakeNotification, object: nil)
        }
    }
}

extension Notification.Name {
    public static let deviceDidShakeNotification = Notification.Name("deviceDidShakeNotification")
}

// MARK: - View Modifier

/// A ViewModifier that enables opening the Spectra Logger when the device is shaken.
struct ShakeToRevealModifier: ViewModifier {
    let enabled: Bool
    @State private var isPresented = false

    func body(content: Content) -> some View {
        content
            .onReceive(NotificationCenter.default.publisher(for: .deviceDidShakeNotification)) { _ in
                if enabled {
                    isPresented = true
                }
            }
            .sheet(isPresented: $isPresented) {
                SpectraLogger.shared.view()
            }
    }
}

public extension View {
    /// Enables the "Shake to Open Spectra Logger" feature for this view hierarchy.
    ///
    /// - Parameter enabled: Whether the feature should be active. Tie this to DEBUG builds for safety.
    /// - Returns: The modified view
    func onShakeToRevealSpectraLogger(enabled: Bool = true) -> some View {
        modifier(ShakeToRevealModifier(enabled: enabled))
    }
}
