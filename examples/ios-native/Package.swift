// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SpectraExample",
    platforms: [
        .iOS(.v15),
    ],
    dependencies: [
        // Spectra Logger UI (includes core framework)
        .package(path: "../../SpectraLoggerUI")
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
