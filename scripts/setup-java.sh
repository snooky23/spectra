#!/bin/bash

# Setup Java environment
# This script detects and configures the correct Java version

# Function to find Java 17
find_java_17() {
    # Try java_home on macOS
    if command -v /usr/libexec/java_home &> /dev/null; then
        JAVA_17=$(/usr/libexec/java_home -v 17 2>/dev/null)
        if [ -n "$JAVA_17" ]; then
            echo "$JAVA_17"
            return 0
        fi
    fi

    # Try common Linux locations
    for path in /usr/lib/jvm/java-17-* /usr/lib/jvm/jdk-17*; do
        if [ -d "$path" ]; then
            echo "$path"
            return 0
        fi
    done

    return 1
}

# Only set JAVA_HOME if not already valid
if ! java -version 2>&1 | grep -q "version \"17"; then
    DETECTED_JAVA=$(find_java_17)
    if [ -n "$DETECTED_JAVA" ]; then
        export JAVA_HOME="$DETECTED_JAVA"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "✅ Using Java 17 from: $JAVA_HOME"
    else
        echo "⚠️  Java 17 not found. Please install JDK 17."
        echo "   macOS: brew install openjdk@17"
        echo "   Linux: sudo apt install openjdk-17-jdk"
        exit 1
    fi
fi
