# Spectra Logger — Desktop Browser Companion 🌐

A zero-dependency Node.js HTTP server and WebSocket bridge that provides a real-time big-screen dashboard for mobile telemetry over local Wi-Fi.

## Features

- ⚡ **Zero External Dependencies**: Built using standard Node.js `http`, `crypto`, and `fs` modules.
- 📱 **QR Code Pairing**: Generates a high-contrast pairing QR code on launch for instant mobile connection.
- 🛡️ **Authorization Gatekeeper**: Requires browser confirmation before any mobile telemetry stream is accepted.
- 📡 **Dual-Pane Live Inspection**:
  - **Logs**: Real-time log entries with level filtering (DEBUG, INFO, WARN, ERROR).
  - **Network**: HTTP requests, headers, query parameters, payloads, and response status codes.
  - **Events**: Analytics events with rich parameter key-value inspection.
  - **WebView**: Dedicated console logs intercepted from mobile embedded WebViews.
- 📋 **JSON Export**: One-click raw JSON copy for any selected inspection item.

## Getting Started

### Prerequisites

- Node.js 18 or newer installed on your machine.

### Running the Companion Website

You can start the companion server using either npm or Gradle:

```bash
# Option 1: Via npm / Node
cd tools/desktop-companion
npm start

# Option 2: Via Gradle from repository root
./gradlew runDesktopCompanion
```

Once started, the server outputs:
```
======================================================
🚀 Spectra Logger Desktop Browser Companion is ACTIVE
======================================================
🌐 Dashboard URL:    http://localhost:9292
📡 Local Wi-Fi URL:  http://192.168.x.x:9292
📱 Mobile WS URL:    ws://192.168.x.x:9292/spectra-ws?token=<token>
======================================================
```

### Pairing with Mobile App

1. Open `http://localhost:9292` in your browser.
2. Click **Pair Device** to display the QR Code.
3. In your Spectra mobile app, navigate to **Settings** > **Remote Stream** and scan the QR code.
4. On the browser dashboard, click **Allow Device to Stream** when the authorization prompt appears.
5. Telemetry streams live to your browser dashboard with sub-10ms latency!
