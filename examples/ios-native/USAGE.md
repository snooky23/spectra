# Spectra Logger iOS - Usage Example

## Simple Integration (Just One Line!)

The Spectra Logger iOS SDK provides a **complete, ready-to-use UI** with all tabs included. You don't need to manage individual screens - just present `SpectraLoggerScreen` and you're done!

### ✅ What You Get Out of the Box

When you present `SpectraLoggerScreen`, you automatically get:
- **Logs Tab** - Application logs with filtering and search
- **Network Tab** - Network request logs
- **Settings Tab** - Log management and export options
- **Navigation** - Built-in tab bar and close button

### Basic Usage

```swift
import SwiftUI

struct MyApp: View {
    @State private var showLogger = false

    var body: some View {
        Button("Open Spectra Logger") {
            showLogger = true
        }
        .sheet(isPresented: $showLogger) {
            // That's it! One line to get the complete logger UI
            SpectraLoggerScreen()
        }
    }
}
```

### With Navigation

```swift
import SwiftUI

struct MyApp: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("My App")

                NavigationLink(destination: SpectraLoggerScreen()) {
                    Text("View Logs")
                }
            }
        }
    }
}
```

### Programmatic Presentation

```swift
import SwiftUI

class DebugManager {
    static func showLogger() {
        // Get the top view controller
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first,
              let rootVC = window.rootViewController else {
            return
        }

        // Present the logger
        let loggerView = SpectraLoggerScreen()
        let hostingController = UIHostingController(rootView: loggerView)
        rootVC.present(hostingController, animated: true)
    }
}

// Usage
DebugManager.showLogger()
```

## What Users DON'T Need to Do

❌ **Don't build tabs yourself:**
```swift
// ❌ WRONG - Users shouldn't have to do this
TabView {
    LogsView().tabItem { Label("Logs", systemImage: "list") }
    NetworkView().tabItem { Label("Network", systemImage: "network") }
    SettingsView().tabItem { Label("Settings", systemImage: "gear") }
}
```

✅ **Just use the complete screen:**
```swift
// ✅ CORRECT - SDK provides everything
SpectraLoggerScreen()
```

## Deep Linking Support

The example app also demonstrates URL scheme integration:

```swift
@main
struct MyApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Handle spectralogger:// URLs
                    if url.scheme == "spectralogger" {
                        // Open logger, navigate to specific tab, etc.
                    }
                }
        }
    }
}
```

Supported URLs:
- `spectralogger://logs` - Open logs tab
- `spectralogger://network` - Open network tab
- `spectralogger://clear` - Clear all logs

## SDK Design Philosophy

**Single Responsibility**: The SDK provides ONE complete screen that does everything. Users don't manage:
- ❌ Individual tab views
- ❌ Tab bar configuration
- ❌ Navigation structure
- ❌ Close/dismiss buttons

**Just Works**: Present `SpectraLoggerScreen()` and get a fully functional logger UI with all features included.

## Example App Structure

```
MainAppView (Your App)
    │
    └── Button: "Open Spectra Logger"
            │
            └── Presents: SpectraLoggerScreen()  ← Complete SDK screen
                    │
                    ├── Logs Tab (built-in)
                    ├── Network Tab (built-in)
                    ├── Settings Tab (built-in)
                    └── Done Button (built-in)
```

Users interact with **one component** from the SDK: `SpectraLoggerScreen()`

Everything else is internal implementation that users never see or manage.
