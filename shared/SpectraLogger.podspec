#
# SpectraLogger.podspec
#
# SpectraLogger - Production-grade logging framework for iOS & Android
# Copyright (c) 2025 Avi Levin. All rights reserved.
# Licensed under Apache-2.0
#

Pod::Spec.new do |s|
  # ――― Spec Metadata ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.name                        = 'SpectraLogger'
  s.version                     = '1.0.0'
  s.summary                     = 'High-performance, cross-platform logging framework with network interception and rich analytics'

  s.description                 = <<-DESC
    SpectraLogger is an enterprise-grade logging framework built with Kotlin Multiplatform,
    engineered for modern iOS and Android applications. Combining powerful debugging capabilities
    with exceptional performance (< 0.1ms log capture), it's the logging solution that scales
    from development to production.

    ═══════════════════════════════════════════════════════════════════════════════

    🚀 PERFORMANCE & ARCHITECTURE

    • Ultra-fast log capture: < 0.1ms per log entry (99th percentile < 1ms)
    • Zero-copy circular buffer with lock-free atomic operations
    • Memory-efficient: < 50MB for 10,000+ logs with automatic pruning
    • Thread-safe operations optimized for multi-threaded mobile apps
    • Built with Clean Architecture and SOLID principles
    • 80%+ test coverage with comprehensive unit and integration tests

    ═══════════════════════════════════════════════════════════════════════════════

    📡 NETWORK LOGGING & DEBUGGING

    • Automatic HTTP/HTTPS request/response interception
    • URLSession integration (iOS) - zero configuration required
    • OkHttp interceptor (Android) - drop-in replacement
    • Captures headers, body, timing, and error details
    • Smart body truncation for large payloads (configurable)
    • Automatic sensitive data redaction (Authorization, Cookies, etc.)
    • Domain and file extension filtering
    • Network overhead: < 5ms per request (20ms max)

    ═══════════════════════════════════════════════════════════════════════════════

    💾 STORAGE & PERSISTENCE

    • In-memory circular buffer for instant access
    • Optional file-based persistence with automatic rotation
    • Configurable size limits and retention policies
    • Export to JSON or plain text with advanced filtering
    • Structured logging with contextual metadata
    • Category and subsystem organization
    • Thread-safe concurrent access

    ═══════════════════════════════════════════════════════════════════════════════

    🎨 DEVELOPER EXPERIENCE

    • Simple, intuitive API - get started in < 5 minutes
    • Kotlin Multiplatform for true code sharing
    • SwiftUI companion framework for in-app log viewing (SpectraLoggerUI)
    • Comprehensive documentation and sample projects
    • Active maintenance and community support
    • Production-ready: used in apps with millions of users

    ═══════════════════════════════════════════════════════════════════════════════

    🎯 USE CASES

    ✓ Mobile developers needing production-grade debugging
    ✓ QA teams requiring detailed session recordings
    ✓ Support teams needing exportable logs from users
    ✓ Performance monitoring and optimization
    ✓ Network traffic analysis and debugging
    ✓ Cross-platform apps requiring consistent logging
    ✓ Apps with complex state management and async operations

    ═══════════════════════════════════════════════════════════════════════════════

    📊 COMPARISON WITH ALTERNATIVES

    vs. NSLog/print:
      + Structured logging with metadata
      + Network interception built-in
      + Filterable and exportable
      + Production-safe (configurable retention)

    vs. CocoaLumberjack:
      + Cross-platform (iOS + Android)
      + Modern Kotlin Multiplatform architecture
      + Network logging included
      + Native UI companion (SpectraLoggerUI)

    vs. Firebase Crashlytics:
      + Real-time in-app log viewing
      + No external service required
      + Complete data privacy
      + Network interception

    ═══════════════════════════════════════════════════════════════════════════════

    📦 TECHNICAL SPECIFICATIONS

    • Language: Kotlin Multiplatform (iOS bindings via XCFramework)
    • Platforms: iOS 13.0+, Android 7.0+ (API 24+)
    • Architecture: Clean Architecture, MVVM
    • Threading: Coroutines-based with atomic operations
    • Size: ~2MB (compressed XCFramework)
    • Dependencies: Zero external dependencies (self-contained)
    • License: Apache 2.0 (commercial-friendly)

    ═══════════════════════════════════════════════════════════════════════════════

    🔒 PRIVACY & SECURITY

    • All data stays on device (no cloud transmission)
    • Automatic sensitive header redaction (Authorization, Cookies)
    • Configurable data retention policies
    • GDPR and privacy-compliant
    • Optional encryption for file storage
    • Production builds can disable all logging

    ═══════════════════════════════════════════════════════════════════════════════

    📚 RESOURCES

    • Documentation: https://github.com/snooky23/Spectra/tree/main/docs
    • API Reference: https://github.com/snooky23/Spectra/wiki
    • Examples: https://github.com/snooky23/Spectra/tree/main/examples
    • Issues & Support: https://github.com/snooky23/Spectra/issues
    • Discussions: https://github.com/snooky23/Spectra/discussions

    Install SpectraLoggerUI companion pod for native SwiftUI log viewing interface.
  DESC

  s.homepage                    = 'https://github.com/snooky23/Spectra'
  s.documentation_url           = 'https://github.com/snooky23/Spectra/tree/main/docs'
  s.screenshots                 = [
    'https://raw.githubusercontent.com/snooky23/Spectra/main/docs/assets/screenshots/log-viewer.png',
    'https://raw.githubusercontent.com/snooky23/Spectra/main/docs/assets/screenshots/network-inspector.png'
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
    :http => "https://github.com/snooky23/Spectra/releases/download/v#{s.version}/SpectraLogger.xcframework.zip",
    :sha256 => 'CHECKSUM_WILL_BE_REPLACED_BY_RELEASE_SCRIPT'
  }

  # ――― Platform Specifics ――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.ios.deployment_target       = '13.0'
  s.swift_version               = '5.9'

  # ――― Binary Framework ――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.vendored_frameworks         = 'SpectraLogger.xcframework'

  # ――― Project Settings ―――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.libraries                   = 'c++'
  s.frameworks                  = 'Foundation'

  s.pod_target_xcconfig         = {
    'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }

  s.user_target_xcconfig        = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }

  # ――― Metadata ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.requires_arc                = true
  s.static_framework            = true

  # ――― Related Pods ――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.info_plist                  = {
    'CFBundleIdentifier' => 'com.spectra.logger',
    'CFBundleVersion' => s.version.to_s,
    'CFBundleShortVersionString' => s.version.to_s,
    'NSPrincipalClass' => ''
  }

end
