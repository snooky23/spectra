#!/bin/bash
set -euo pipefail

# Build all XCFrameworks for iOS distribution (Core + UI) using official KMP tasks

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_TYPE="${1:-Release}"

echo "🏗️  Building all XCFrameworks for iOS..."
echo "Project Root: $PROJECT_ROOT"
echo "Build Type: $BUILD_TYPE"

cd "$PROJECT_ROOT"

# Ensure JAVA_HOME is configured for Java 17+ (needed when invoked from Xcode / Android Studio)
TARGET_JAVA_VERSION="21"
if [ -f "$PROJECT_ROOT/.java-version" ]; then
    TARGET_JAVA_VERSION="$(tr -d '[:space:]' < "$PROJECT_ROOT/.java-version")"
fi

need_java_home=false
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
    need_java_home=true
else
    CURRENT_JAVA_VER="$("${JAVA_HOME}/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)"
    if [ -n "$CURRENT_JAVA_VER" ] && [ "$CURRENT_JAVA_VER" -lt 17 ] 2>/dev/null; then
        need_java_home=true
    fi
fi

if [ "$need_java_home" = true ]; then
    FOUND_JAVA=""
    # 1. SDKMAN candidate matching target version or current
    for candidate in "$HOME/.sdkman/candidates/java/${TARGET_JAVA_VERSION}"* "$HOME/.sdkman/candidates/java/current"; do
        if [ -d "$candidate" ] && [ -x "$candidate/bin/java" ]; then
            FOUND_JAVA="$candidate"
            break
        fi
    done

    # 2. Android Studio JBR
    if [ -z "$FOUND_JAVA" ]; then
        for jbr in "/Applications/Android Studio.app/Contents/jbr/Contents/Home" "$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"; do
            if [ -d "$jbr" ] && [ -x "$jbr/bin/java" ]; then
                FOUND_JAVA="$jbr"
                break
            fi
        done
    fi

    # 3. macOS java_home
    if [ -z "$FOUND_JAVA" ]; then
        if /usr/libexec/java_home -v "$TARGET_JAVA_VERSION" >/dev/null 2>&1; then
            FOUND_JAVA="$(/usr/libexec/java_home -v "$TARGET_JAVA_VERSION")"
        elif /usr/libexec/java_home -v "17+" >/dev/null 2>&1; then
            FOUND_JAVA="$(/usr/libexec/java_home -v "17+")"
        fi
    fi

    if [ -n "$FOUND_JAVA" ]; then
        export JAVA_HOME="$FOUND_JAVA"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "☕ Auto-configured JAVA_HOME=$JAVA_HOME for Gradle build"
    fi
fi

# Determine active architecture for Xcode local dev
ARCH_ARGS=""
if [[ "${SPECTRA_LOCAL_DEV:-}" == "1" && -n "${PLATFORM_NAME:-}" && -n "${ARCHS:-}" ]]; then
    if [[ "$PLATFORM_NAME" == "iphonesimulator" ]]; then
        if [[ "$ARCHS" == *"arm64"* ]]; then
            ARCH_ARGS="-Pspectra.activeArch=iosSimulatorArm64"
        else
            ARCH_ARGS="-Pspectra.activeArch=iosX64"
        fi
    elif [[ "$PLATFORM_NAME" == "iphoneos" ]]; then
        ARCH_ARGS="-Pspectra.activeArch=iosArm64"
    fi
    echo "🎯 Local Dev Mode: Optimizing build for active architecture: $ARCH_ARGS"
fi

if [ "$BUILD_TYPE" == "Release" ]; then
    ./gradlew :spectra-core:assembleSpectraLoggerReleaseXCFramework :spectra-ui:assembleSpectraLoggerUIReleaseXCFramework $ARCH_ARGS
else
    ./gradlew :spectra-core:assembleSpectraLoggerDebugXCFramework :spectra-ui:assembleSpectraLoggerUIDebugXCFramework $ARCH_ARGS
fi

# Ensure output directory exists for consumers (e.g. Package.swift)
XCFRAMEWORK_DIR="$PROJECT_ROOT/SpectraFrameworks"
mkdir -p "$XCFRAMEWORK_DIR"

# Copy the generated XCFrameworks to the centralized location
# The official KMP tasks put them in [module]/build/XCFrameworks/[type]/[name].xcframework

copy_xcframework() {
    local module=$1
    local name=$2
    local build_type_lower=$(echo "$BUILD_TYPE" | tr '[:upper:]' '[:lower:]')
    local src="$PROJECT_ROOT/$module/build/XCFrameworks/$build_type_lower/$name.xcframework"
    local dest="$XCFRAMEWORK_DIR/$name.xcframework"
    
    if [ -d "$src" ]; then
        rm -rf "$dest"
        cp -R "$src" "$dest"
        echo "✅ $name.xcframework copied to $dest"
    else
        echo "❌ Error: $name.xcframework not found at $src"
        exit 1
    fi
}

copy_xcframework "spectra-core" "SpectraLogger"
copy_xcframework "spectra-ui" "SpectraLoggerUI"

echo "✨ All XCFrameworks prepared successfully!"
