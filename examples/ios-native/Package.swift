// swift-tools-version: 5.9
import PackageDescription

// MARK: - Dependency Configuration
// This example app can be used in two ways:
//
// 1. LOCAL DEVELOPMENT (default)
//    Uses local SpectraLoggerUI package for testing
//    Relative path: ../../SpectraLoggerUI
//    When: Testing the integration during development
//
// 2. RELEASE MODE
//    Uses published SpectraLoggerUI from Swift Package Registry or GitHub
//    When: Testing against released versions

let package = Package(
    name: "SpectraExample",
    platforms: [
        .iOS(.v15),
    ],
    dependencies: [
        // LOCAL DEVELOPMENT MODE (default)
        // Uses local SpectraLoggerUI package from workspace
        .package(path: "../../SpectraLoggerUI"),

        // RELEASE MODE (uncomment after packages are published)
        // GitHub release:
        // .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0"),
        //
        // Swift Package Index (future):
        // .package(url: "https://github.com/snooky23/spectra.git", .upToNextMajor(from: "1.0.0")),
    ],
    targets: [
        .target(
            name: "SpectraExample",
            dependencies: [
                .product(name: "SpectraLoggerUI", package: "SpectraLoggerUI")
            ],
            path: "SpectraExample"
        ),
    ]
)
