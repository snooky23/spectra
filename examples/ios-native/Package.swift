// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SpectraExample",
    platforms: [
        .iOS(.v15),
    ],
    dependencies: [
        // Add dependencies here
    ],
    targets: [
        .target(
            name: "SpectraExample",
            dependencies: [],
            path: "SpectraExample"
        ),
    ]
)
