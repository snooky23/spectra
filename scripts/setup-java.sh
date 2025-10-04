#!/bin/bash

# Setup Java environment
# This script detects and configures the correct Java version

# Function to check if a Java installation is version 17
is_java_17() {
    local java_path="$1"
    if [ -x "$java_path/bin/java" ]; then
        local version=$("$java_path/bin/java" -version 2>&1 | head -1)
        if echo "$version" | grep -q "version \"17"; then
            return 0
        fi
    fi
    return 1
}

# Function to find Java 17
find_java_17() {
    # Try java_home on macOS with specific version
    if command -v /usr/libexec/java_home &> /dev/null; then
        JAVA_17=$(/usr/libexec/java_home -v 17 2>/dev/null)
        if [ -n "$JAVA_17" ] && is_java_17 "$JAVA_17"; then
            echo "$JAVA_17"
            return 0
        fi

        # Try finding any 17.x version
        JAVA_17=$(/usr/libexec/java_home -v 17.0 2>/dev/null)
        if [ -n "$JAVA_17" ] && is_java_17 "$JAVA_17"; then
            echo "$JAVA_17"
            return 0
        fi
    fi

    # Try common macOS locations
    for path in /Library/Java/JavaVirtualMachines/*/Contents/Home; do
        if is_java_17 "$path"; then
            echo "$path"
            return 0
        fi
    done

    # Try common Linux locations
    for path in /usr/lib/jvm/java-17-* /usr/lib/jvm/jdk-17*; do
        if is_java_17 "$path"; then
            echo "$path"
            return 0
        fi
    done

    return 1
}

# Check current Java version
CURRENT_JAVA_VERSION=$(java -version 2>&1 | head -1)
if echo "$CURRENT_JAVA_VERSION" | grep -q "version \"17"; then
    echo "✅ Java 17 already configured"
else
    echo "⚠️  Current Java version: $CURRENT_JAVA_VERSION"
    echo "🔍 Searching for Java 17..."

    DETECTED_JAVA=$(find_java_17)
    if [ -n "$DETECTED_JAVA" ]; then
        export JAVA_HOME="$DETECTED_JAVA"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "✅ Using Java 17 from: $JAVA_HOME"

        # Verify it works
        NEW_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -1)
        echo "   Version: $NEW_VERSION"
    else
        echo ""
        echo "❌ Java 17 not found. Please install JDK 17."
        echo ""
        echo "Installation options:"
        echo "   macOS: brew install openjdk@17"
        echo "   Linux: sudo apt install openjdk-17-jdk"
        echo ""
        exit 1
    fi
fi
