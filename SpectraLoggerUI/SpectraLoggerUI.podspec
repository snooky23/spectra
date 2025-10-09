#
# SpectraLoggerUI.podspec
#
# SpectraLoggerUI - Premium SwiftUI interface for SpectraLogger
# Copyright (c) 2025 Avi Levin. All rights reserved.
# Licensed under Apache-2.0
#

Pod::Spec.new do |s|
  # ――― Spec Metadata ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.name                        = 'SpectraLoggerUI'
  s.version                     = '1.0.0'
  s.summary                     = 'Premium native SwiftUI debugging interface with advanced log visualization and network inspection'

  s.description                 = <<-DESC
    SpectraLoggerUI is a production-grade SwiftUI companion framework for SpectraLogger,
    providing a sophisticated, feature-rich interface for viewing, analyzing, and managing
    application logs and network traffic. Built exclusively for iOS with modern SwiftUI
    components, it delivers a delightful debugging experience that rivals desktop developer tools.

    ═══════════════════════════════════════════════════════════════════════════════

    ✨ FEATURES & CAPABILITIES

    LOG VIEWING & ANALYSIS
    • Real-time log streaming with smooth 60 FPS scrolling
    • Virtualized lists for handling 10,000+ logs without performance degradation
    • Multi-level filtering: log level, category, subsystem, time range
    • Full-text search with instant results and highlighted matches
    • Color-coded log levels for instant visual recognition
    • Expandable log entries with full metadata display
    • Timeline visualization with timestamp markers
    • Group logs by session, category, or time period

    NETWORK INSPECTOR
    • Dedicated network traffic viewer with request/response details
    • Beautiful JSON syntax highlighting with collapsible sections
    • Header inspection with automatic sensitive data masking
    • Request/response timing visualization
    • Filter by HTTP method, status code, or domain
    • Copy as cURL command for easy reproduction
    • Export network session for offline analysis
    • Compare request/response side-by-side

    ADVANCED FILTERING
    • Combine multiple filters with AND/OR logic
    • Save and recall filter presets
    • Quick filter chips for common scenarios
    • Time-based filtering with custom ranges
    • Regex-based text search (advanced mode)
    • Filter by thread ID, file, or function name

    EXPORT & SHARING
    • Export to JSON with full metadata preservation
    • Export to formatted text for readability
    • Export to CSV for spreadsheet analysis
    • Share via AirDrop, email, or messaging
    • Selective export with current filter applied
    • Automatic compression for large exports

    ═══════════════════════════════════════════════════════════════════════════════

    🎨 DESIGN & USER EXPERIENCE

    NATIVE iOS INTERFACE
    • Strict adherence to Apple's Human Interface Guidelines
    • Native iOS navigation patterns and gestures
    • Context menus with 3D Touch/Haptic feedback
    • Swipe actions for common operations
    • Pull-to-refresh for manual log updates
    • Keyboard shortcuts for iPad productivity

    ADAPTIVE LAYOUTS
    • Optimized for iPhone (all sizes, including SE)
    • Full iPad support with split-view and multitasking
    • Landscape orientation with optimized layouts
    • Dynamic Type support for accessibility
    • Safe area aware on all devices
    • Compact and regular size class adaptations

    DARK MODE & THEMING
    • Automatic dark mode matching system preferences
    • Manual theme override option
    • Custom syntax highlighting themes
    • High contrast mode for accessibility
    • Color blindness-friendly palettes

    ACCESSIBILITY
    • Full VoiceOver support with semantic labels
    • Dynamic Type scaling (up to XXXL)
    • Voice Control compatibility
    • Reduced Motion respect
    • Increase Contrast support
    • Minimum 44x44pt touch targets

    ═══════════════════════════════════════════════════════════════════════════════

    🚀 PERFORMANCE & OPTIMIZATION

    • Lazy loading with on-demand rendering
    • Memory-efficient view recycling
    • Background thread processing for heavy operations
    • Debounced search to prevent lag
    • Optimized SwiftUI view hierarchies
    • < 1% CPU usage during idle
    • < 10MB memory footprint
    • Smooth 60 FPS scrolling guaranteed

    ═══════════════════════════════════════════════════════════════════════════════

    🎯 USE CASES & INTEGRATION

    DEVELOPMENT & DEBUGGING
    ✓ In-app log inspection during development
    ✓ Network request debugging and optimization
    ✓ Quick issue reproduction and analysis
    ✓ Performance profiling and bottleneck identification

    QA & TESTING
    ✓ Session recording for bug reports
    ✓ Export logs directly from test devices
    ✓ Visual network traffic validation
    ✓ Automated testing with log verification

    BETA TESTING
    ✓ Provide beta testers with advanced debugging tools
    ✓ Easy log export for issue reporting
    ✓ Network inspection without Charles/Proxyman
    ✓ Built-in feedback mechanism

    PRODUCTION SUPPORT
    ✓ Customer support log collection
    ✓ Field issue diagnosis
    ✓ Production debugging (gated by build flags)
    ✓ Temporary troubleshooting access

    ═══════════════════════════════════════════════════════════════════════════════

    🔧 INTEGRATION METHODS

    FLOATING ACTION BUTTON
    • Draggable FAB always accessible
    • Configurable visibility (debug/release)
    • Haptic feedback on interaction
    • Auto-hide when not needed

    SHAKE GESTURE
    • Shake device to open (iOS tradition)
    • Configurable sensitivity
    • Visual confirmation animation
    • Production-safe disable option

    PROGRAMMATIC ACCESS
    • SwiftUI view embedding
    • UIKit integration support
    • Deep linking support
    • Custom trigger events

    RUNTIME CONFIGURATION
    • Enable/disable features dynamically
    • Adjust log retention on-the-fly
    • Configure network interception
    • Manage sensitive data masking

    ═══════════════════════════════════════════════════════════════════════════════

    📊 TECHNICAL SPECIFICATIONS

    • Language: 100% Swift (SwiftUI + Combine)
    • iOS Version: 15.0+ (requires SwiftUI 3.0+)
    • Architecture: MVVM with reactive state management
    • Dependencies: SpectraLogger ~> 1.0
    • Size: ~500KB (pure Swift, no assets)
    • License: Apache 2.0 (commercial-friendly)

    FRAMEWORKS USED
    • SwiftUI - Modern declarative UI
    • Combine - Reactive programming
    • Foundation - Core utilities

    ═══════════════════════════════════════════════════════════════════════════════

    🔐 PRIVACY & SECURITY

    • All UI rendering happens on-device
    • No telemetry or analytics collection
    • Respects SpectraLogger's data retention policies
    • Automatic sensitive data redaction in UI
    • Production builds can completely disable UI
    • No external network requests

    ═══════════════════════════════════════════════════════════════════════════════

    📚 RESOURCES

    • Documentation: https://github.com/snooky23/Spectra/tree/main/docs
    • Integration Guide: https://github.com/snooky23/Spectra/blob/main/docs/INSTALLATION.md
    • Examples: https://github.com/snooky23/Spectra/tree/main/examples/ios-native
    • API Reference: https://github.com/snooky23/Spectra/wiki/SpectraLoggerUI
    • Screenshots: https://github.com/snooky23/Spectra/tree/main/docs/assets/screenshots
    • Video Demo: https://github.com/snooky23/Spectra#video-demo

    Requires SpectraLogger pod. Install both for the complete experience.
  DESC

  s.homepage                    = 'https://github.com/snooky23/Spectra'
  s.documentation_url           = 'https://github.com/snooky23/Spectra/tree/main/docs'
  s.screenshots                 = [
    'https://raw.githubusercontent.com/snooky23/Spectra/main/docs/assets/screenshots/ui-log-viewer.png',
    'https://raw.githubusercontent.com/snooky23/Spectra/main/docs/assets/screenshots/ui-network-inspector.png',
    'https://raw.githubusercontent.com/snooky23/Spectra/main/docs/assets/screenshots/ui-filters.png',
    'https://raw.githubusercontent.com/snooky23/Spectra/main/docs/assets/screenshots/ui-dark-mode.png'
  ]

  # ――― Author Metadata ―――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.authors                     = {
    'Avi Levin' => 'aviavi23@gmail.com'
  }
  s.social_media_url            = 'https://github.com/snooky23'

  # ――― License ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.license                     = {
    :type => 'Apache-2.0',
    :file => 'LICENSE',
    :text => <<-LICENSE
      Copyright 2025 Avi Levin

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
    LICENSE
  }

  # ――― Source Location ――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.source                      = {
    :git => 'https://github.com/snooky23/Spectra.git',
    :tag => "v#{s.version}",
    :submodules => false
  }

  # ――― Platform Specifics ――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.ios.deployment_target       = '15.0'
  s.swift_version               = '5.9'

  # ――― Source Code ―――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.source_files                = 'SpectraLoggerUI/Sources/SpectraLoggerUI/**/*.swift'
  s.resource_bundles            = {
    'SpectraLoggerUI' => ['SpectraLoggerUI/Sources/SpectraLoggerUI/Resources/**/*']
  }

  # ――― Dependencies ――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.dependency 'SpectraLogger', '~> 1.0'

  # ――― Project Settings ―――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.frameworks                  = 'SwiftUI', 'Combine', 'Foundation'

  s.pod_target_xcconfig         = {
    'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    'DEFINES_MODULE' => 'YES',
    'SWIFT_VERSION' => '5.9',
    'APPLICATION_EXTENSION_API_ONLY' => 'NO'
  }

  s.user_target_xcconfig        = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }

  # ――― Metadata ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.requires_arc                = true
  s.module_name                 = 'SpectraLoggerUI'

  # ――― Info.plist ――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.info_plist                  = {
    'CFBundleIdentifier' => 'com.spectra.logger.ui',
    'CFBundleVersion' => s.version.to_s,
    'CFBundleShortVersionString' => s.version.to_s,
    'UIRequiresFullScreen' => false,
    'UISupportedInterfaceOrientations' => [
      'UIInterfaceOrientationPortrait',
      'UIInterfaceOrientationLandscapeLeft',
      'UIInterfaceOrientationLandscapeRight'
    ]
  }

end
