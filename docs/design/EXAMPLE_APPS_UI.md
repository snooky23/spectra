# Example Apps UI Design

Design specification for the Spectra Logger example/demo applications.

---

## Overview

The example apps demonstrate Spectra Logger integration and serve as interactive test beds for the library. Both iOS and Android apps follow the same design principles.

---

## App Structure

```
┌─────────────────────────────────────────┐
│           Example App Main              │
│  ┌───────────────────────────────────┐  │
│  │                                   │  │
│  │        [Tab Content Area]         │  │
│  │                                   │  │
│  └───────────────────────────────────┘  │
│  ┌─────────────┬─────────────────────┐  │
│  │  Actions    │      Network        │  │
│  └─────────────┴─────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## Tab 1: Actions

**Purpose:** Demonstrate application logging at various severity levels.

### Layout
```
┌─────────────────────────────────────────┐
│                  📋                     │
│         Application Logging             │
│    Generate logs at different levels    │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │ ● Tap Me (Generates Log)      →  │  │ ← Blue
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ ● Generate Warning            →  │  │ ← Orange
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ ● Generate Error              →  │  │ ← Red
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ ● Error with Stack Trace      →  │  │ ← Red
│  └───────────────────────────────────┘  │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │     🔍 Open Spectra Logger        │  │ ← Primary Color
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Button Actions

| Button | Log Level | Tag | Description |
|--------|-----------|-----|-------------|
| Tap Me | INFO | `UserInteraction` | Increment counter with timestamp |
| Generate Warning | WARNING | `Performance` | Simulated slow operation |
| Generate Error | ERROR | `Authentication` | Simulated login failure |
| Error with Stack Trace | ERROR | `CriticalError` | Full stack trace + metadata |

---

## Tab 2: Network

**Purpose:** Simulate HTTP requests to demonstrate network logging.

### Layout
```
┌─────────────────────────────────────────┐
│                  ℹ️                     │
│           Network Testing               │
│    Simulate HTTP requests/responses     │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │ ● GET Request (200 OK)        →  │  │ ← Green
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ ● POST Request (201 Created)  →  │  │ ← Green
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ ● GET Request (404 Not Found) →  │  │ ← Orange
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ ● Server Error (500)          →  │  │ ← Red
│  └───────────────────────────────────┘  │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │     🔍 Open Spectra Logger        │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Network Request Details

| Button | Method | Status | Simulated Delay |
|--------|--------|--------|-----------------|
| GET 200 | GET | 200 OK | 500ms |
| POST 201 | POST | 201 Created | 1000ms |
| GET 404 | GET | 404 Not Found | 300ms |
| Error 500 | GET | 500 Server Error | 2000ms |

All requests include:
- Request headers (Content-Type, Authorization, User-Agent)
- Response headers (Cache-Control, ETag)
- JSON response body
- Duration tracking

---

## Additional Use Cases (Recommended)

### Batch Logging Section
Demonstrate performance with bulk operations:

| Button | Action | Count |
|--------|--------|-------|
| Generate 10 Logs | Mixed levels across tags | 10 logs |
| Generate 100 Logs | Stress test logging | 100 logs |
| Generate 1000 Logs | Performance demo | 1000 logs |

### Real-Time Updates Demo
Show logs appearing live while logger is open:

```
┌───────────────────────────────────────┐
│  ⚡ Real-Time Demo                    │
│  ─────────────────────────────────────│
│  │ ● Start Background Logging     │  │  ← Starts timer
│  │ ● Stop Background Logging      │  │  ← Stops timer
│  ─────────────────────────────────────│
│  Logs every 2 seconds while running   │
└───────────────────────────────────────┘
```

### Filtering Demos
Demonstrate Spectra Logger filtering capabilities:

| Scenario | What to Generate |
|----------|------------------|
| Tag filtering | Logs with tags: `Auth`, `Network`, `UI`, `Database` |
| Level filtering | Equal mix of all 6 log levels |
| Search demo | Logs containing searchable keywords |
| Time range | Logs spread across different timestamps |

### Export/Share Scenarios

| Format | Contents |
|--------|----------|
| Filtered logs | Only currently visible logs |
| All logs | Complete log history |
| JSON export | Structured data format |
| Plain text | Human-readable format |

### Developer Triggers

| Trigger | Platform | Description |
|---------|----------|-------------|
| Shake to open | Both | Shake device to open logger |
| URL scheme | iOS | `spectra://open` deep link |
| Notification | Android | Persistent debug notification |
| Floating button | Android | Optional overlay button |

---

## Spectra Logger Modal

Opens as a full-screen modal with 3 tabs.

```
┌─────────────────────────────────────────┐
│  Logs                              ✕    │
├─────────────────────────────────────────┤
│  [Search logs...]             [Share]   │
├─────────────────────────────────────────┤
│  [DEBUG] [INFO] [WARN] [ERROR] [FATAL]  │ ← Filter chips
├─────────────────────────────────────────┤
│  │ 12:34:56  INFO  UserInteraction     │  │
│  │  User button pressed - count: 5     │  │
│  ├─────────────────────────────────────┤  │
│  │ 12:35:01  WARN  Performance         │  │
│  │  Slow operation detected...         │  │
│  ├─────────────────────────────────────┤  │
│                   ...                    │
├─────────────────────────────────────────┤
│    Logs    │   Network   │  Settings   │
└─────────────────────────────────────────┘
```

---

## Settings Tab Use Cases

| Setting | Purpose |
|---------|---------|
| Log count display | Show current log/network counts |
| Clear logs button | Demo clearing logs |
| Clear network button | Demo clearing network logs |
| Version info | Display Spectra version |
| Max capacity info | Show storage limits |

---

## Error Scenarios to Demo

| Scenario | Purpose |
|----------|---------|
| Empty state | Show UI when no logs exist |
| No search results | Show "No matches" state |
| No network logs | Empty network tab state |
| Large log message | Handle very long messages |
| Many metadata fields | Handle extensive metadata |
| Deep stack trace | Long stack trace display |

---

## Design Tokens

### Colors

| Element | Light Mode | Dark Mode |
|---------|------------|-----------|
| DEBUG | `#2196F3` Blue | Same |
| INFO | `#4CAF50` Green | Same |
| WARNING | `#FFA500` Orange | Same |
| ERROR | `#F44336` Red | Same |
| FATAL | `#9C27B0` Purple | Same |
| Success (2xx) | `#4CAF50` Green | Same |
| Client Error (4xx) | `#FFA500` Orange | Same |
| Server Error (5xx) | `#F44336` Red | Same |

### Components

- **LogButton**: Rounded card with icon + label, colored by severity
- **BrandingCard**: Centered icon, title, subtitle for section headers
- **OpenSpectraButton**: Full-width primary button, centered text

---

## Platform Specifics

### Android
- Material 3 Design
- Jetpack Compose UI
- NavigationBar for tabs
- ModalBottomSheet for Spectra Logger
- ShakeDetector for shake-to-open

### iOS
- SwiftUI native components
- TabView for navigation
- Sheet presentation for Spectra Logger
- SF Symbols for icons
- CMMotionManager for shake detection

