# Design Specification: Spectra Adaptive UI

## 1. Overview
Design an adaptive layout that responds to window size changes in real-time, providing optimized experiences for both mobile handsets and large-screen devices (Tablets, Foldables, Desktop Mode/Stage Manager).

## 2. Adaptive Logic & Window Size Classes
We categorize windows into three primary `WindowSizeClass` categories:

| Size Class | Range (Width) | Layout Pattern |
| :--- | :--- | :--- |
| **Compact** | < 600dp | Hierarchical Drill-down (Bottom Nav) |
| **Medium** | 600dp - 839dp | List-Detail Pane (Navigation Rail) |
| **Expanded** | >= 840dp | Persistent Multi-Pane (Side Drawer) |

### 2.1 Resizable Window Metrics
- Support fluid transitions between size classes without state loss.
- Observe `currentWindowAdaptiveInfo()` to handle Split-View and Stage Manager window adjustments dynamically.

## 3. Platform-Specific Handset Experiences

### 3.1 Android 17 (Baklava): M3 Expressive & Predictive Back
- **Expressive Motion:** Use `Emphasized` easing curves for all transitions.
- **Predictive Back:** Implement `PredictiveBackHandler` in `commonMain` to support the progress-based back gesture. The detail pane will scale and fade according to the gesture's displacement.
- **Edge-to-Edge:** Ensure the UI renders behind system bars with appropriate insets.

### 3.2 iPadOS: "Liquid Glass" Principles
- **Visual Foundation:** Semi-transparent "Glass" backgrounds for floating elements.
- **Floating Interactive Elements:** Use elevated surfaces with blurred backgrounds for filter chips and action buttons.
- **Haptic Transitions:** Provide soft haptic feedback on log selection and filter toggling.
- **Fluid Pop Gesture:** Seamlessly integrate Compose navigation with the iOS native pop gesture (swipe-from-left).

## 4. Navigation State: `ScreenConfig`
Maintain a centralized `ScreenConfig` in the Common module to ensure navigation parity.

```kotlin
data class ScreenConfig(
    val windowSizeClass: WindowWidthSizeClass,
    val isDualPane: Boolean,
    val selectedItem: LogEntry? = null,
    val backProgress: Float = 0f // For Predictive Back
)
```

## 5. Components & Interactions
- **`ListDetailPaneScaffold`**: Replaces manual conditional rendering.
- **Adaptive Navigator**: Manages the pane transition logic automatically.
- **M3 Expressive Motion**: Applied via a custom `AnimationSpec` defined in `SpectraTheme`.
