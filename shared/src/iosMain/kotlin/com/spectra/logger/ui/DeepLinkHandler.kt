package com.spectra.logger.ui

/**
 * iOS URL scheme handler for Spectra Logger.
 *
 * **Setup in Xcode:**
 *
 * 1. Add URL scheme to Info.plist:
 * ```xml
 * <key>CFBundleURLTypes</key>
 * <array>
 *     <dict>
 *         <key>CFBundleURLSchemes</key>
 *         <array>
 *             <string>spectra</string>
 *         </array>
 *         <key>CFBundleURLName</key>
 *         <string>com.spectra.logger</string>
 *     </dict>
 * </array>
 * ```
 *
 * 2. Handle URL in AppDelegate (UIKit):
 * ```swift
 * import UIKit
 * import SpectraLogger
 *
 * @UIApplicationMain
 * class AppDelegate: UIResponder, UIApplicationDelegate {
 *     func application(_ app: UIApplication,
 *                      open url: URL,
 *                      options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
 *         if url.scheme == "spectra" {
 *             // TODO: Present Spectra Logger UI
 *             // Example: show logs or network based on url.host
 *             return true
 *         }
 *         return false
 *     }
 * }
 * ```
 *
 * 3. Or handle URL in SwiftUI App:
 * ```swift
 * import SwiftUI
 * import SpectraLogger
 *
 * @main
 * struct MyApp: App {
 *     var body: some Scene {
 *         WindowGroup {
 *             ContentView()
 *                 .onOpenURL { url in
 *                     if url.scheme == "spectra" {
 *                         // TODO: Present Spectra Logger UI
 *                     }
 *                 }
 *         }
 *     }
 * }
 * ```
 *
 * **Usage:**
 * ```bash
 * # From terminal (simulator)
 * xcrun simctl openurl booted "spectra://open"
 * xcrun simctl openurl booted "spectra://logs"
 * xcrun simctl openurl booted "spectra://network"
 *
 * # From Safari on device/simulator
 * # Just type in URL bar: spectra://open
 * ```
 *
 * **Supported URLs:**
 * - spectra://open - Open logger (main screen)
 * - spectra://logs - Open logs tab
 * - spectra://network - Open network tab
 */
object DeepLinkHandler {
    /**
     * Parse a Spectra deep link URL.
     *
     * @param urlString The URL string (e.g., "spectra://logs")
     * @return The host part ("logs", "network", "open") or null if invalid
     */
    fun parseDeepLink(urlString: String): String? {
        if (!urlString.startsWith("spectra://")) return null

        val parts = urlString.removePrefix("spectra://").split("?")
        return parts.firstOrNull()
    }
}
