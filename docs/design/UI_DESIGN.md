# Spectra Logger UI Design Specification

> Centralized design document to ensure consistent UI across iOS (SwiftUI) and Android (Compose).

---

## Navigation Structure

The logger UI uses a **3-tab layout** at the bottom (via `NavigationSuiteScaffold`):

| Tab | Icon | Description |
|-----|------|-------------|
| **Logs** | `list.bullet.rectangle` | Application logs with filtering |
| **Network** | `network` | Network request/response logs |
| **Settings** | `gearshape` | Configuration and storage management |

On Medium/Expanded devices (IPad, desktop), `NavigationSuiteScaffold` automatically converts the bottom bar into a **side navigation rail**.

---

## Adaptive UI Architecture

Spectra uses a centralized **`ScreenConfig`/`AdaptiveNavigator`** architecture to deliver different layouts based on window width.

### ScreenConfig

`ScreenConfig` is resolved via `rememberScreenConfig()` and reports:
- `isCompact: Boolean` — window width < 600dp (phone)
- `isDualPane: Boolean` — window width ≥ 600dp (tablet, desktop)

### AdaptiveNavigator Routing

```
                          ┌─────────────────────────────┐
                          │       AdaptiveNavigator      │
                          └─────────────┬───────────────┘
                 ┌────────────────────── │ ──────────────────┐
      isDualPane == false (Compact)      │      isDualPane == true (Medium/Expanded)
                 │                       │                   │
    ┌────────────────────────┐           │    ┌─────────────────────────────────────┐
    │  Animated Nav Stack    │           │    │  40% List │ Divider │ 60% Detail    │
    │  (slide left/right)    │           │    │           │         │               │
    │                        │           │    │  - No X   │         │ - No ← Back   │
    │  Root: 'X' Close btn   │           │    │  - No ←   │         │ - Empty state │
    │  Detail: '← Back' btn  │           │    │             placeholder             │
    └────────────────────────┘           │    └─────────────────────────────────────┘
```

### Compact Navigation Flow (< 600dp)

1. Root screen shows full-screen List with **'X' (Close)** button in TopAppBar nav slot.
2. Tapping a row **pushes** Detail full-screen with slide-in animation (300ms).
3. Detail screen shows **'← Back'** navigation arrow.
4. Tapping Back **pops** the detail with reverse slide animation.

### Dual-Pane Navigation Flow (≥ 600dp)

1. List pane (40% width) and Detail pane (60% width) are shown **simultaneously**.
2. Tapping a row **instantly** updates the Detail pane — no push animation.
3. Detail pane shows **no Back arrow** (navigation is contextual, not modal).
4. Detail pane shows "Select an item to view details" when nothing is selected.
5. The 'X' Close button is **hidden** in dual-pane mode as well (no overlay paradigm).

---

## Design Tokens (Hardcoded)

All tokens are defined in `SpectraDesignTokens.kt` and must be used instead of ad-hoc color literals.

### Log Level Colors

| Level | Token | Hex | Usage |
|-------|-------|-----|-------|
| Verbose | `VerboseGray` | `#8E8E93` | Low priority, noise |
| Debug | `DebugBlue` | `#007AFF` | Development info |
| Info | `InfoGreen` | `#34C759` | Normal operations |
| Warning | `WarningOrange` | `#FF9500` | Potential issues |
| Error | `ErrorRed` | `#FF3B30` | Errors requiring attention |
| Fatal | `FatalPurple` | `#AF52DE` | Critical failures |

### HTTP Status Code Colors

| Range | Token | Hex | Meaning |
|-------|-------|-----|---------|
| 2xx | `InfoGreen` | `#34C759` | Success |
| 3xx | `DebugBlue` | `#007AFF` | Redirect |
| 4xx | `WarningOrange` | `#FF9500` | Client error |
| 5xx | `ErrorRed` | `#FF3B30` | Server error |

### Background & Structure Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `SystemGray6` | `#F2F2F7` | Card/section backgrounds |
| `FilterChipAlpha` | `0.2f` | Filter chip background opacity |
| `SearchBarShape` | `RoundedCornerShape(10.dp)` | Search bar corners |
| `DetailCardShape` | `RoundedCornerShape(8.dp)` | Log/Network detail cards |
| `ScreenHorizontalPaddingCompact` | `16.dp` | Horizontal padding on phone |
| `ScreenHorizontalPaddingExpanded` | `24.dp` | Horizontal padding on tablet/desktop |
| `ListToDetailGap` | `1.dp` | Divider between list and detail panes |

---

## Screens

### 1. Logs Screen

#### Layout
```
┌─────────────────────────────────────┐
│  Logs                [⌘] [↑] [...]  │   <- Navigation bar
├─────────────────────────────────────┤
│  🔍 Search logs (min 2 chars)...    │   <- Search bar
├─────────────────────────────────────┤
│  Active: [INFO ✕] [Tag:Auth ✕]      │   <- Filter badges
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐    │
│  │ [DEBUG]  Auth    12:34:56   │    │   <- Log row
│  │ User login attempt started  │    │
│  │ ⚠️ Has Error                │    │   <- Error indicator
│  └─────────────────────────────┘    │
│                                     │
│  ... (scrollable list)              │
│                                     │
└─────────────────────────────────────┘
```

> **Design Decision**: All filter controls (log levels, tags, time, metadata) are in the Filter modal. Main screen stays clean with only search and active filter badges.

#### Toolbar Actions
- **Filter button** (⌘): Opens Filter Screen (see below)
  - Badge shows active filter count (red number)
- **Share button** (↑): Opens share action sheet with options:
  - **"Share Filtered Logs (X items)"** - Exports only currently filtered logs
  - **"Share All Logs (Y items)"** - Exports all logs regardless of filters
  - **Cancel**
  > **UX Rationale**: Single button triggers action sheet for clarity. Count indicators help users understand exactly what they're sharing. This follows iOS/Android design patterns used in Safari, Mail, and Photos apps.
- **Menu** (...):
  - Refresh
  - Clear All Logs (destructive)

#### Active Filter Indicators (How Users Know Filters Are Active)
1. **Filter Button Badge** (red circle): Shows count of active advanced filters (tags, time, metadata, errors)
2. **Selected Level Chips**: Highlighted level chips show which log levels are included
3. **Active Filter Badges Row**: Removable pills below level chips showing each filter:
   - `Tag: Auth ✕` | `Time Range ✕` | `Errors Only ✕`
   - Tap ✕ to remove individual filter
4. **Different Counts in Share Sheet**: When sharing, user sees:
   - "Share Filtered Logs **(47 items)**"
   - "Share All Logs **(1,234 items)**"
   
   > If these numbers differ, users immediately know filters are reducing results.
5. **Empty State Message**: If no matches, shows "No matching logs" (not "No logs")

#### Log Row Components
- **Level badge**: Colored background (0.2 opacity), colored text, rounded corners
- **Tag**: Secondary text color
- **Timestamp**: Caption size, secondary color, format: `HH:mm:ss`
- **Message**: Body font, max 2 lines
- **Error indicator**: Orange warning icon + "Has Error" (if throwable present)

#### Log Detail Sheet
Opens as modal sheet when tapping a log row:
- Level badge + full timestamp
- **Tag section**: Gray background card
- **Message section**: Gray background card  
- **Error/Stack Trace section** (if present):
  - Expandable/collapsible (default: expanded)
  - Header shows line count
  - Copy button to clipboard
  - Monospaced font with line numbers
  - Horizontal scroll for long lines
- **Metadata section**: Key-value pairs (excludes stack_trace if shown above)

---

### 1b. Logs Filter Screen (Modal)

Opens as a full-screen modal from the Filter button.

#### Layout
```
┌─────────────────────────────────────┐
│  ✕ Filters               [Reset All]│   <- Close, Reset
├─────────────────────────────────────┤
│                                     │
│  LOG LEVELS                         │
│  [Verbose][Debug][Info][Warn][Error]│   <- Multi-select
│  [Fatal]                            │
│                                     │
│  TAGS                               │
│  ┌─────────────────────────────┐    │
│  │ [+] Add custom tag...       │    │   <- Custom tag input
│  └─────────────────────────────┘    │
│  ┌─────────────────────────────┐    │
│  │ ☑ Auth                      │    │   <- Existing tags
│  │ ☐ Network                   │    │
│  │ ☐ Database                  │    │
│  └─────────────────────────────┘    │
│                                     │
│  TIME RANGE                         │
│  [Last hour] [Today] [Last 24h]     │   <- Quick presets
│  ┌─────────────────────────────┐    │
│  │ From: [Select date/time]    │    │
│  │ To:   [Select date/time]    │    │
│  └─────────────────────────────┘    │
│                                     │
│  METADATA                           │
│  ┌─────────────────────────────┐    │
│  │ Key:   [    user_id      ]  │    │   <- Key-value filter
│  │ Value: [    12345        ]  │    │
│  └─────────────────────────────┘    │
│                                     │
│  ☐ Show only logs with errors       │   <- Error toggle
│                                     │
│  ┌─────────────────────────────┐    │
│  │        Apply Filters        │    │   <- Apply button
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

#### Filter Options

**1. Tags Section**
- **Custom tag input**: Text field to add a tag that may not exist yet in logs
  - Allows filtering for logs that will appear in the future
  - Added tags appear in the list with "(custom)" label
- **Existing tags list**: Checkboxes for all tags currently present in logs
  - Dynamically populated from log storage
  - Multi-select allowed
- **Match mode**: Toggle for "Match Any" vs "Match All" tags

**2. Time Range Section**
- **From date/time**: Start of time range filter
- **To date/time**: End of time range filter
- Quick presets: "Last hour", "Today", "Last 24h", "Last 7 days"

**3. Metadata Section** (Advanced)
- Filter by specific metadata key-value pairs
- Add multiple key-value conditions
- Useful for filtering by user_id, session_id, etc.

**4. Has Error Toggle**
- Show only logs that have an attached throwable/error

#### Behavior
- Filters persist until explicitly cleared
- "Reset All" clears all filters
- "Apply Filters" closes modal and applies filters
- Filter icon in toolbar shows badge when filters are active

---

### 2. Network Screen

#### Layout
```
┌─────────────────────────────────────┐
│  Network           [⌘] [↑] [...]   │  <- Navigation bar
├─────────────────────────────────────┤
│  🔍 Search URL or host...           │  <- Search bar
├─────────────────────────────────────┤
│  Active: [POST ✕] [4xx ✕]           │  <- Filter badges
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐    │
│  │ [POST] [201]       12:34:56 │    │  <- Network row
│  │ https://api.example.com/... │    │
│  │ ⏱️ 423ms                    │    │  <- Duration
│  └─────────────────────────────┘    │
│                                     │
│  ... (scrollable list)              │
│                                     │
└─────────────────────────────────────┘
```

> **Design Decision**: All filter controls (methods, status codes, host, time, response time) are in the Filter modal. Main screen stays clean with only search and active filter badges.

#### Toolbar Actions
- **Filter button** (⌘): Opens Network Filter Screen (see below)
  - Badge shows active filter count (red number)
- **Share button** (↑): Opens share action sheet with options:
  - **"Share Filtered Logs (X items)"** - Exports only currently filtered network logs
  - **"Share All Logs (Y items)"** - Exports all network logs regardless of filters
  - **Cancel**
- **Menu** (...):
  - Refresh
  - Clear All Network Logs (destructive)

#### Active Filter Indicators (How Users Know Filters Are Active)
1. **Filter Button Badge** (red circle): Shows count of active advanced filters
2. **Selected Method/Status Chips**: Highlighted chips show which are included
3. **Active Filter Badges Row**: Removable pills below status chips showing each filter:
   - `Host: api.* ✕` | `Time Range ✕` | `Slow Requests ✕` | `Errors Only ✕`
   - Tap ✕ to remove individual filter
4. **Different Counts in Share Sheet**: When sharing, user sees filtered vs all counts
5. **Empty State Message**: If no matches, shows "No matching logs" (not "No network logs")

#### Network Row Components
- **Method badge**: Blue background (0.2 opacity), blue text
- **Status code badge**: Color based on range (green/blue/orange/red)
- **Timestamp**: Caption size, secondary color
- **URL**: Body font, max 2 lines
- **Duration** (if available): Shows response time with clock icon

#### Network Detail Sheet
Opens as modal sheet when tapping a row:
- Method badge + Status badge + Duration
- **URL section**: Monospaced font
- **Request Headers section**: Key-value list (if not empty)
- **Request Body section**: Monospaced, scrollable (if not empty)
- **Response Headers section**: Key-value list (if not empty)
- **Response Body section**: Monospaced, scrollable (if not empty)
- **Error section**: Red text (if error present)

---

### 2b. Network Filter Screen (Modal)

Opens as a full-screen modal from the Filter button.

#### Layout
```
┌─────────────────────────────────────┐
│  ✕ Network Filters      [Reset All] │  <- Close, Reset
├─────────────────────────────────────┤
│                                     │
│  HTTP METHODS                       │
│  [GET][POST][PUT][DELETE][PATCH]    │  <- Multi-select
│  [HEAD][OPTIONS]                    │
│                                     │
│  STATUS CODES                       │
│  [2xx ✓][3xx][4xx][5xx]             │  <- Multi-select
│                                     │
│  HOST / DOMAIN                      │
│  ┌─────────────────────────────┐    │
│  │ Filter by host pattern...   │    │  <- Wildcards ok
│  └─────────────────────────────┘    │
│                                     │
│  TIME RANGE                         │
│  [Last hour] [Today] [Last 24h]     │  <- Quick presets
│  ┌─────────────────────────────┐    │
│  │ From: [Select date/time]    │    │
│  │ To:   [Select date/time]    │    │
│  └─────────────────────────────┘    │
│                                     │
│  RESPONSE TIME                      │
│  ☐ > 100ms   ☐ > 500ms   ☐ > 1s    │  <- Slow requests
│                                     │
│  ☐ Show only failed requests        │  <- Error toggle
│                                     │
│  ┌─────────────────────────────┐    │
│  │        Apply Filters        │    │  <- Apply button
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

#### Filter Options

**1. Host/Domain Section**
- Text input for filtering by URL host
- Supports wildcard patterns: `api.*`, `*.example.com`
- Case insensitive matching

**2. Time Range Section**
- Same as Logs filter: From/To date pickers
- Quick presets: "Last hour", "Today", "Last 24h", "Last 7 days"

**3. Response Time Section**
- Filter slow requests by duration threshold
- Options: >100ms, >500ms, >1000ms
- Multi-select allowed (shows requests exceeding ANY selected threshold)

**4. Errors Only Toggle**
- Show only failed requests
- Matches: 4xx status, 5xx status, or error field present

#### Behavior
- Filters persist until explicitly cleared
- "Reset All" clears all advanced filters
- "Apply Filters" closes modal and applies filters
- Filter icon in toolbar shows badge when filters are active

---

### 3. Settings Screen

#### Layout
```
┌─────────────────────────────────────┐
│  Settings                      [↻]  │  <- Navigation bar with Refresh
├─────────────────────────────────────┤
│                                     │
│  APPEARANCE                         │
│  ┌─────────────────────────────┐    │
│  │ [Light] [Dark] [System]     │    │  <- Segmented picker
│  └─────────────────────────────┘    │
│  Choose how Spectra Logger appears  │
│                                     │
├─────────────────────────────────────┤
│  STORAGE                            │
│  ┌─────────────────────────────┐    │
│  │ Application Logs        [🗑]│    │
│  │ 1,234 logs stored           │    │
│  ├─────────────────────────────┤    │
│  │ Network Logs            [🗑]│    │
│  │ 567 logs stored             │    │
│  └─────────────────────────────┘    │
│  Manage stored logs to free space   │
│                                     │
├─────────────────────────────────────┤
│  EXPORT                             │
│  ┌─────────────────────────────┐    │
│  │ ↑ Export All Logs           │    │
│  └─────────────────────────────┘    │
│  Export all logs to share           │
│                                     │
├─────────────────────────────────────┤
│  ABOUT                              │
│  ┌─────────────────────────────┐    │
│  │ Version          1.0.0      │    │
│  │ Framework   Spectra Logger  │    │
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

#### Appearance Modes
- **Light**: Force light mode
- **Dark**: Force dark mode  
- **System**: Follow device setting (default)

#### Clear Logs Confirmation
Display alert/dialog before clearing:
- Title: "Clear Application Logs?" / "Clear Network Logs?"
- Message: "This will permanently delete all X logs. This action cannot be undone."
- Actions: Cancel, Clear (destructive)

---

## Reusable Components

### 1. SearchBar
- Magnifying glass icon on left
- Placeholder: "Search logs (min 2 chars)..."
- Clear button (X) when text present
- Background: `systemGray6`, rounded corners (10pt)

### 2. FilterChip
- Horizontal scrollable row of chips
- States:
  - **Selected**: Colored background (0.2 opacity), colored text
  - **Unselected**: Gray background, primary text
- Font: Caption, medium weight
- Padding: 12h × 6v
- Corner radius: 16pt (pill shape)

### 3. DetailSection
- Title: Caption, semibold, uppercase, secondary color
- Content: Wrapped in gray background card, 12pt padding, 8pt corner radius

### 4. ExpandableErrorSection
- Collapsible with chevron indicator
- Header: Red text, shows line count, copy button
- Content: 
  - Line numbers (right-aligned, 3 digits)
  - Monospaced font
  - Horizontal scroll for long lines
  - Light red background (`red.opacity(0.05)`)

### 5. Badge
- Small colored label for log levels, HTTP methods, status codes
- Structure: Colored background (0.2 opacity) + colored text
- Padding: 8h × 2v (small) or 12h × 6v (large in detail views)
- Corner radius: 4pt (small) or 6pt (large)

---

## Empty States

| Screen | Icon | Message |
|--------|------|---------|
| Logs (no data) | `tray` | "No logs to display" |
| Logs (no matches) | `magnifyingglass` | "No matching logs" |
| Network (no data) | `network.slash` | "No network logs to display" |
| Network (no matches) | `magnifyingglass` | "No matching logs" |

---

## Typography

| Element | Style |
|---------|-------|
| Navigation title | Large title, bold |
| Section headers | Caption, semibold, uppercase |
| Log message | Body |
| Timestamps | Caption2 |
| Badges | Caption or Caption2, semibold |
| Code/JSON | Monospaced (system) |

---

## Gestures & Interactions

- **Tap log/network row**: Open detail sheet
- **Pull to refresh**: Not currently implemented (use toolbar button)
- **Swipe row**: Not implemented
- **Shake device**: Can trigger logger open (configurable)

---

## Share/Export Formats

### Default Export Format: JSON

JSON is the industry standard for log exports (used by Firebase, Sentry, Datadog).

#### Application Logs Export
```json
{
  "exportDate": "2025-12-13T15:00:00Z",
  "appVersion": "1.0.0",
  "deviceInfo": "iPhone 15 Pro, iOS 18.0",
  "logsCount": 1234,
  "filters": {
    "levels": ["INFO", "WARN", "ERROR"],
    "tags": ["Auth"],
    "timeRange": { "from": "...", "to": "..." }
  },
  "logs": [
    {
      "id": "uuid",
      "timestamp": "2025-12-13T14:30:00Z",
      "level": "ERROR",
      "tag": "Auth",
      "message": "Login failed",
      "metadata": { "userId": "123" },
      "stackTrace": "..."
    }
  ]
}
```

#### Network Logs Export
```json
{
  "exportDate": "2025-12-13T15:00:00Z",
  "logsCount": 567,
  "networkLogs": [
    {
      "id": "uuid",
      "timestamp": "2025-12-13T14:30:00Z",
      "method": "POST",
      "url": "https://api.example.com/login",
      "statusCode": 401,
      "durationMs": 423,
      "requestHeaders": {},
      "requestBody": "...",
      "responseHeaders": {},
      "responseBody": "...",
      "error": null
    }
  ]
}
```

### Alternative Formats (via Settings)
| Format | Use Case | File Extension |
|--------|----------|----------------|
| **JSON** (default) | Machine-readable, import to other tools | `.json` |
| **Plain Text** | Human-readable, email sharing | `.txt` |
| **CSV** | Spreadsheet analysis | `.csv` |

---

## Accessibility

Following WCAG 2.1 AA and platform guidelines (iOS HIG, Material Design).

### Minimum Touch Targets
- **iOS**: 44×44 pt minimum
- **Android**: 48×48 dp minimum
- All buttons, chips, and interactive elements meet this requirement

### VoiceOver / TalkBack Labels
| Element | Accessibility Label |
|---------|---------------------|
| Filter button | "Filter logs, X active filters" |
| Share button | "Share logs" |
| Log row | "{Level} log from {Tag} at {Time}: {Message}" |
| Network row | "{Method} request to {Host}, status {Code}" |
| Clear badge | "Remove {Filter} filter" |

### Color Contrast
- All text on colored badges meets 4.5:1 contrast ratio
- Error states use icons in addition to color (not color alone)

### Dynamic Type Support
- **iOS**: All text respects user's Dynamic Type settings
- **Android**: All text respects user's font scale settings
- Layout adapts gracefully to larger text sizes

### Reduce Motion
- When "Reduce Motion" is enabled:
  - Sheet presentations use fade instead of slide
  - No bouncy animations

---

## Loading States

### Initial Load
```
┌─────────────────────────────────────┐
│  Logs              [⌘] [↑] [...]   │
├─────────────────────────────────────┤
│  🔍 Search logs...                  │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ████████████  ████  ████    │    │  <- Skeleton
│  │ ████████████████████████    │    │
│  └─────────────────────────────┘    │
│  ┌─────────────────────────────┐    │
│  │ ████████████  ████  ████    │    │
│  │ ████████████████████████    │    │
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

### Loading Behavior
- Show skeleton placeholders (3-5 rows) during initial load
- Skeleton uses `systemGray5` animated with shimmer effect
- Load should complete in <500ms for typical log counts
- If >1 second, show subtle spinner in nav bar

### Refresh Loading
- Toolbar refresh button shows spinner while loading
- List remains visible during refresh (no full-screen loader)

---

## Error States

### Storage Errors
| Error | Message | Recovery |
|-------|---------|----------|
| Storage full | "Storage limit reached. Clear old logs to continue." | Show Settings → Storage |
| Read failed | "Unable to load logs. Please try again." | Retry button |
| Write failed | "Unable to save log. Check available storage." | Silent (logged internally) |

### Export Errors
| Error | Message | Recovery |
|-------|---------|----------|
| No logs | "No logs to export." | Dismiss |
| Export failed | "Export failed. Please try again." | Retry button |
| Share cancelled | (No message) | Dismiss sheet |

### Network (for Network Logs)
| Error | Message | Recovery |
|-------|---------|----------|
| Interceptor failed | "Network logging temporarily unavailable." | Auto-retry |

### Error Display
- Non-blocking errors: Toast/snackbar at bottom (auto-dismiss 3s)
- Blocking errors: Alert dialog with action buttons
- All errors logged internally for debugging

---

## Animations & Transitions

### Sheet Presentations
- **iOS**: Native sheet animation (slide up from bottom)
- **Android**: Material BottomSheet animation

### List Animations
- **Insert**: Fade in + slide from top (150ms)
- **Remove**: Fade out (100ms)
- **Refresh**: No animation (instant update)

### Filter Chips
- **Select**: Scale bounce (0.95 → 1.0) + color transition (200ms)
- **Deselect**: Color transition only (150ms)

### Active Filter Badges
- **Appear**: Fade in + scale (0 → 1.0, 200ms)
- **Remove**: Shrink + fade out (150ms)

### Skeleton Loading
- Shimmer animation: Left-to-right gradient sweep (1.5s loop)

### Timing Guidelines
| Animation | Duration | Easing |
|-----------|----------|--------|
| Sheet appear | 300ms | easeOut |
| Sheet dismiss | 250ms | easeIn |
| List insert | 150ms | easeOut |
| Chip toggle | 200ms | spring |
| Badge appear | 200ms | spring |

---

## Dark Mode Support

All colors should adapt automatically:
- Use semantic colors (`primary`, `secondary`, `systemGray6`, etc.)
- Colored badges maintain their hue in both modes
- Background cards should use appropriate gray levels

---

## Platform-Specific Notes

### iOS (SwiftUI) - Implemented ✅
- Uses `NavigationView` with `TabView`
- Native form styling for Settings
- `preferredColorScheme` for appearance
- Share via `UIActivityViewController`

### Android (Compose) - Implemented ✅
- Uses `NavigationBar` with `Scaffold`
- Uses `TopAppBar` with Material3 styling
- Material3 components (FilterChip, Card, etc.)
- ModalBottomSheet for filter and detail views
- Share via `Intent.ACTION_SEND` (TODO: implement)

---

## Feature Checklist

| Feature | iOS | Android |
|---------|-----|---------|
| Logs list with filtering | ✅ | ✅ |
| Log level filter chips | ✅ | ✅ |
| Search (min 2 chars) | ✅ | ✅ |
| Log detail view | ✅ | ✅ |
| Stack trace with line numbers | ✅ | ✅ |
| Copy stack trace | ✅ | ✅ |
| **Filter Screen** | ✅ | ✅ |
| - Tag filters (existing + custom) | ✅ | ✅ |
| - Time range filter | ✅ | ✅ |
| - Metadata filter | ✅ | ✅ |
| - Has error toggle | ✅ | ✅ |
| Active filter badges | ✅ | ✅ |
| Smart share (filtered/all) | ✅ | ⬜ |
| Network logs list | ✅ | ✅ |
| Method/status filters | ✅ | ✅ |
| Network detail view | ✅ | ✅ |
| Headers display | ✅ | ✅ |
| Request/response body | ✅ | ✅ |
| **Network Filter Screen** | ⬜ | ⬜ |
| - Host/domain filter | ⬜ | ⬜ |
| - Time range filter | ⬜ | ⬜ |
| - Response time filter | ⬜ | ⬜ |
| - Errors only toggle | ⬜ | ⬜ |
| Network active filter badges | ⬜ | ⬜ |
| Network smart share | ⬜ | ⬜ |
| Appearance picker | ✅ | ✅ |
| Storage stats | ✅ | ✅ |
| Clear logs | ✅ | ✅ |
| Dark mode | ✅ | ✅ |

---

**Last Updated**: 2026-04-18  
**Covers**: Epic 1 (Compact Navigation), Epic 2 (Dual-Pane Layout), Epic 3 (Docs Sync)
