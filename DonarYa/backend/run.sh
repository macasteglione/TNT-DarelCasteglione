#!/bin/bash
# Build and run the DonarYa backend server
# Requires: JDK 21+ and Kotlin compiler

set -e

BACKEND_DIR="$(cd "$(dirname "$0")" && pwd)"
echo "=== DonarYa Backend ==="

# Check Java
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "Java version: $(java -version 2>&1 | head -1)"

# Build with Gradle (requires JDK 21-24; JDK 25 may need Gradle 9.5+)
echo ""
echo "Building..."
./gradlew compileKotlin --no-daemon 2>&1 || {
    echo ""
    echo "NOTE: If you get a JAVA_COMPILER error, you need JDK 21-24."
    echo "Install SDKMAN! and run: sdk install java 21.0.6-tem"
    echo ""
    echo "Alternatively, build manually:"
    echo "  kotlinc -cp \"\$HOME/.gradle/caches/modules-2/files-2.1/*/*/*/*/*/*.jar\" -d build/classes src/main/kotlin/**/*.kt"
    exit 1
}

echo ""
echo "=== Build successful! Run with: ==="
echo "  ./gradlew run --no-daemon"
echo ""
echo "Or directly:"
echo '  java -cp "build/classes:$(./gradlew -q dependencies --configuration runtimeClasspath 2>/dev/null | grep -oP '/\S+\.jar' | tr '\n' ':')" com.tnt.donarya.backend.ApplicationKt'
