# Spectra Logger UI Design Specification

> Centralized design document to ensure consistent UI across iOS (SwiftUI) and Android (Compose).

---

## Navigation Structure

The logger UI uses a **3-tab layout** at the bottom:

| Tab | Icon | Description |
|-----|------|-------------|
| **Logs** | `list.bullet.rectangle` | Application logs with filtering |
| **Network** | `network` | Network request/response logs |
| **Settings** | `gearshape` | Configuration and storage management |

---

## Color Tokens

### Log Level Colors

| Level | Color | Usage |
|-------|-------|-------|
| Verbose | `secondary/gray` | Low priority, noise |
| Debug | `blue` | Development info |
| Info | `green` | Normal operations |
| Warning | `orange` | Potential issues |
| Error | `red` | Errors requiring attention |
| Fatal | `purple` | Critical failures |

### HTTP Status Code Colors

| Range | Color | Meaning |
|-------|-------|---------|
| 2xx | `green` | Success |
| 3xx | `blue` | Redirect |
| 4xx | `orange` | Client error |
| 5xx | `red` | Server error |

### Background Colors

- **Card/Section background**: `systemGray6` (iOS) / equivalent light gray
- **Selected filter chip**: `{color}.opacity(0.2)`
- **Unselected filter chip**: `systemGray5`
- **Error section background**: `red.opacity(0.05)`

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
│  Network           [⌘] [↑] [...] │   <- Navigation bar
├─────────────────────────────────────┤
│  🔍 Search URL or host...          │   <- Search bar
├─────────────────────────────────────┤
│  Active: [POST ✕] [4xx ✕]          │   <- Filter badges
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ [POST] [201]       12:34:56 │   │   <- Network row
│  │ https://api.example.com/... │   │
│  │ ⏱️ 423ms                    │   │   <- Duration
│  └─────────────────────────────┘   │
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
│  ✕ Network Filters       [Reset All]│   <- Close, Reset
├─────────────────────────────────────┤
│                                     │
│  HTTP METHODS                       │
│  [GET][POST][PUT][DELETE][PATCH]   │   <- Multi-select
│  [HEAD][OPTIONS]                   │
│                                     │
│  STATUS CODES                       │
│  [2xx ✓][3xx][4xx][5xx]            │   <- Multi-select
│                                     │
│  HOST / DOMAIN                      │
│  ┌─────────────────────────────┐   │
│  │ Filter by host pattern...   │   │   <- Wildcards ok
│  └─────────────────────────────┘   │
│                                     │
│  TIME RANGE                         │
│  [Last hour] [Today] [Last 24h]    │   <- Quick presets
│  ┌─────────────────────────────┐   │
│  │ From: [Select date/time]    │   │
│  │ To:   [Select date/time]    │   │
│  └─────────────────────────────┘   │
│                                     │
│  RESPONSE TIME                      │
│  ☐ > 100ms   ☐ > 500ms   ☐ > 1s   │   <- Slow requests
│                                     │
│  ☐ Show only failed requests       │   <- Error toggle
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Apply Filters        │   │   <- Apply button
│  └─────────────────────────────┘   │
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
│  Settings                      [↻] │  <- Navigation bar with Refresh
├─────────────────────────────────────┤
│                                     │
│  APPEARANCE                         │
│  ┌─────────────────────────────┐   │
│  │ [Light] [Dark] [System]     │   │  <- Segmented picker
│  └─────────────────────────────┘   │
│  Choose how Spectra Logger appears  │
│                                     │
├─────────────────────────────────────┤
│  STORAGE                            │
│  ┌─────────────────────────────┐   │
│  │ Application Logs        [🗑]│   │
│  │ 1,234 logs stored           │   │
│  ├─────────────────────────────┤   │
│  │ Network Logs            [🗑]│   │
│  │ 567 logs stored             │   │
│  └─────────────────────────────┘   │
│  Manage stored logs to free space   │
│                                     │
├─────────────────────────────────────┤
│  EXPORT                             │
│  ┌─────────────────────────────┐   │
│  │ ↑ Export All Logs           │   │
│  └─────────────────────────────┘   │
│  Export all logs to share           │
│                                     │
├─────────────────────────────────────┤
│  ABOUT                              │
│  ┌─────────────────────────────┐   │
│  │ Version          1.0.0      │   │
│  │ Framework   Spectra Logger  │   │
│  └─────────────────────────────┘   │
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

**Last Updated**: 2025-12-13
