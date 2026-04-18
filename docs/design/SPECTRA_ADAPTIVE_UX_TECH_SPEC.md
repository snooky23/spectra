# Specification: SpectraLoggerUI Adaptive Layout & Experience

**Version:** 1.0  
**Status:** Draft  
**Target Platforms:** Android 17 (Baklava), iPadOS 26, iOS 19+  

---

## 1. Design Objective
To provide a premium, unified logging interface that feels platform-native on both Android and iOS while autonomously adapting to any screen size—from compact handsets to expanded desktop-class environments like Stage Manager.

---

## 2. Experience Principles

### 2.1 Mobile Handset Focus (Compact Width)
On handsets, the focus is on depth, fluidity, and tactile feedback.

*   **Android 17 (Baklava): M3 Expressive & Predictive Back**
    *   **Predictive Back Progress:** Utilize the `BackHandler` and `PredictiveBackHandler` APIs. As the user swipes to go back from a detail view, the current pane should scale and fade, revealing the list beneath.
    *   **Expressive Motion:** All transitions must use Material 3 Expressive easing curves (Emphasized: `CubicBezier(0.2, 0.0, 0.0, 1.0)`).
*   **iOS: Liquid Glass & Haptics**
    *   **Liquid Glass Aesthetics:** Interactive elements (Filter chips, Detail cards) should use semi-transparent background materials with `Modifier.blur` to simulate glass.
    *   **Haptic-Enriched Transitions:** 
        *   *Selection:* `selectionChanged` feedback.
        *   *Error Highlighting:* `impact` (Heavy) feedback.
        *   *Action Success:* `notification` (Success) feedback.
    *   **Floating Elements:** Floating action buttons (FABs) and interactive surfaces should have a slight "spring" response when pressed.

### 2.2 Adaptive Logic (Large Screens)
The UI dynamically shifts its layout architecture based on `WindowSizeClass`. On tablets and desktop-class environments (Stage Manager), navigation is **Pane-Update based** rather than **Stack-based**.

| Size Class | Breakpoint | Layout Pattern | Navigation Component |
| :--- | :--- | :--- | :--- |
| **Compact** | < 600dp | Hierarchical Drill-down | Standard Bottom Bar |
| **Medium** | 600dp - 839dp | **Dual-Pane (List + Detail)** | **Floating Glass Tab Bar (Centered)** |
| **Expanded** | >= 840dp | **Persistent Multi-Pane** | Permanent Navigation Drawer |

*   **Tablet Navigation Behavior:**
    *   **Floating Navigation:** Instead of a side rail, the Medium width layout uses a centered, floating navigation bar with a "Liquid Glass" (blur + transparency) effect. This maximizes horizontal space for the two panes.
    *   **Side-by-Side Persistence:** The List Pane remains visible while the Detail Pane updates its content in-place.
    *   **Unified Main View:** Tapping "Network" or "Logs" in the floating bar swaps the entire dual-pane context (e.g., swapping both List and Detail panes at once).

---

## 3. Visual Layout Sketches

### 3.1 Compact (Mobile Handset)
*Standard 1-column stack.*

```text
┌──────────────────────────┐
│ [≡]  Spectra Logger  [S] │
├──────────────────────────┤
│  Log Entry 1             │
│  Log Entry 2             │
│  Log Entry 3 (Selected)  │
│  Log Entry 4             │
│  ...                     │
├──────────────────────────┤
│ [L]      [N]      [!]    │  <-- Standard Bottom Nav
└──────────────────────────┘
```

### 3.2 Medium (Tablet / Stage Manager / Split-View)
*2-column List-Detail with Floating Glass Tab Bar.*

```text
┌──────────────────────┬──────────────┐
│ [≡] Logs             │ Log Detail   │
│ ──────────────────── │ ──────────── │
│ Entry 1              │              │
│ Entry 2              │ [M3 Card]    │
│ Entry 3 ●            │ Message: ... │
│ Entry 4              │ StackTrace:  │
│ Entry 5              │ ...          │
│ ...                  │              │
│                      │              │
│       ┌──────────────┴─┐            │
│       │ [L]   [N]   [!]│            │ <-- Floating Glass Bar
└───────┴────────────────┴────────────┘
         ^ Centered for reach
```

### 3.3 Expanded (Large Tablet Landscape / Desktop Mode)
*3-pane Persistent Layout with Navigation Drawer.*

```text
┌──────────────┬───────────┬─────────────────────┐
│ Spectra      │ [≡] Logs  │ Log Detail          │
├──────────────┤ ───────── │ ─────────────────── │
│ [L] Logs     │ Entry 1   │                     │
│ [N] Network  │ Entry 2   │ [M3 Expressive]     │
│ [S] Settings │ Entry 3 ● │ Message: "Error..." │
│              │ Entry 4   │ Time: 12:00:01      │
│              │ Entry 5   │ Tag: [AuthService]  │
│              │ ...       │                     │
│              │           │ ┌─────────────────┐ │
│              │           │ │ [Metadata Pane] │ │
│              │           │ └─────────────────┘ │
└──────────────┴───────────┴─────────────────────┘
```

---

## 4. Technical Architecture (KMP State)

### 3.1 Common `ScreenConfig` State
To ensure parity between Android's Predictive Back and iOS's interactive pop gestures, navigation state is centralized in `commonMain`.

```kotlin
/**
 * Centralized state for navigation and layout configuration
 */
data class ScreenConfig(
    val sizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    val isDualPane: Boolean = false,
    val selectedLogId: String? = null,
    val navigationProgress: Float = 0f, // 0.0 to 1.0 representing back gesture progress
    val activePane: SpectraPane = SpectraPane.List
)

enum class SpectraPane {
    List, Detail, Filter
}
```

### 3.2 Navigation Parity
*   **Android:** The `PredictiveBackHandler` updates `navigationProgress`. The UI interpolates pane scales and alphas based on this value.
*   **iOS:** The `ComposeUIViewController` bridge will map the native interactive pop gesture progress to the `navigationProgress` state, ensuring the "Liquid Glass" panes slide and blur in sync with the finger.

---

## 4. UI Components Strategy

### 4.1 `ListDetailPaneScaffold`
The `spectra-ui` module will replace manual conditional rendering (`if (selected != null)`) with the `NavigableListDetailPaneScaffold`.

*   **List Pane:** Displays `LogsListContent`.
*   **Detail Pane:** Displays `LogDetailContent` (or `NetworkDetailContent`).
*   **Extra Pane (Expanded only):** Reserved for advanced filtering or statistics.

### 4.2 Material 3 Navigation Suite
`NavigationSuiteScaffold` will be the root container, automatically toggling between `NavigationBar`, `NavigationRail`, and `NavigationDrawer` based on `WindowSizeClass`.

---

## 5. Platform Implementation Details (expect/actual)

### 5.1 Haptics Bridge
```kotlin
// commonMain
expect fun provideHapticFeedback(type: HapticType)

// androidMain
actual fun provideHapticFeedback(type: HapticType) {
    // Vibrate using Android's VibratorManager
}

// iosMain
actual fun provideHapticFeedback(type: HapticType) {
    // Trigger UIImpactFeedbackGenerator / UISelectionFeedbackGenerator
}
```

### 5.2 Motion Profiles
*   **Android Baklava:** Maps CMP animations to the standard Android 17 system animations.
*   **iOS Liquid:** Overrides CMP default animations with Spring-based curves matching `UISpringTimingParameters`.

---

## 6. Verification Criteria
1.  **Android 17 Emulator:** Verify the detail pane "peeks" back to the list during a predictive back gesture.
2.  **iPadOS Stage Manager:** Drag window edges and verify the transition from 1-pane to 2-pane happens without UI "jumping."
3.  **iOS Handset:** Verify that tapping a log entry provides a subtle haptic "click" and the detail view slides up with a "glassy" blur.
