// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SpectraLogger",
    platforms: [
        .iOS(.v15),
    ],
    products: [
        .library(
            name: "SpectraLogger",
            targets: ["SpectraLogger"]
        ),
    ],
    targets: [
        // KMP Framework (binary target)
        // The XCFramework is built from the shared Kotlin Multiplatform module
        //
        // To build the XCFramework:
        // cd ../shared && ./gradlew shared:linkReleaseFrameworkIosArm64 shared:linkReleaseFrameworkIosSimulatorArm64 shared:createXCFramework
        //
        // Or for debug:
        // cd ../shared && ./gradlew shared:linkDebugFrameworkIosArm64 shared:linkDebugFrameworkIosSimulatorArm64
        // then manually create XCFramework with:
        // xcodebuild -create-xcframework \
        //   -framework shared/build/bin/iosArm64/debugFramework/SpectraLogger.framework \
        //   -framework shared/build/bin/iosSimulatorArm64/debugFramework/SpectraLogger.framework \
        //   -output SpectraLogger/build/XCFrameworks/debug/SpectraLogger.xcframework
        .binaryTarget(
            name: "SpectraLogger",
            path: "XCFrameworks/release/SpectraLogger.xcframework"
        ),
    ]
)
