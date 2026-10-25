#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."

# These settings belong to the validated selection, not Gradle CLI properties.
for argument in "$@" "${GRADLE_OPTS:-}" "${JAVA_OPTS:-}" "${JAVA_TOOL_OPTIONS:-}" "${JDK_JAVA_OPTIONS:-}" "${_JAVA_OPTIONS:-}"; do
    case "$argument" in
        *org.gradle.java.*|*org.gradle.jvm*)
            echo 'Java/Gradle JVM property overrides are not supported. Select installed homes with ONE_MB_BUILD_JAVA_HOME and ONE_MB_TEST_JAVA_HOME.' >&2
            exit 1
            ;;
    esac
done
if [[ -f gradle/gradle-daemon-jvm.properties ]]; then
    echo 'Remove stale gradle/gradle-daemon-jvm.properties: this project selects the Gradle JVM from the validated installed Oracle JDK 25.' >&2
    exit 1
fi
source scripts/java-env.sh

# Command-line settings supersede stale user/distribution gradle.properties.
# Set both system and project forms, and use a fresh daemon for each selection.
java_properties=(
    "org.gradle.java.home=$ONE_MB_BUILD_JAVA_HOME"
    'org.gradle.java.installations.fromEnv=ONE_MB_BUILD_JAVA_HOME,ONE_MB_TEST_JAVA_HOME'
    'org.gradle.java.installations.paths='
    'org.gradle.java.installations.auto-detect=false'
    'org.gradle.java.installations.auto-download=false'
)
gradle_arguments=()
for property in "${java_properties[@]}"; do
    gradle_arguments+=("-D$property" "-P$property")
done
exec gradle "$@" --no-daemon "${gradle_arguments[@]}"
