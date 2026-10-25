#!/bin/bash
# Shared installed-JDK selection for Gradle and the disposable Paper runner.
set -euo pipefail

one_mb_resolve_jdk() {
    local major="$1" override_name="$2" selected="${!2:-}" details actual_major vendor runtime compiler
    if [[ -z "$selected" ]]; then
        if [[ ! -x /usr/libexec/java_home ]] || ! selected="$(/usr/libexec/java_home -F -v "$major" 2>/dev/null)"; then
            echo "Required installed Oracle JDK $major unavailable. Set $override_name to a valid installed Oracle JDK $major home. No JDK will be downloaded." >&2
            return 1
        fi
    fi
    if [[ ! -x "$selected/bin/java" || ! -x "$selected/bin/javac" ]]; then
        echo "Invalid $override_name: $selected must contain executable bin/java and bin/javac for Oracle JDK $major." >&2
        return 1
    fi
    selected="$(cd "$selected" && pwd -P)"
    details="$("$selected/bin/java" -XshowSettings:properties -version 2>&1)" || return 1
    actual_major="$(awk -F ' = ' '/^[[:space:]]*java.specification.version = / {print $2}' <<< "$details")"
    vendor="$(awk -F ' = ' '/^[[:space:]]*java.vendor = / {print $2}' <<< "$details")"
    runtime="$(awk -F ' = ' '/^[[:space:]]*java.runtime.version = / {print $2}' <<< "$details")"
    compiler="$("$selected/bin/javac" -version 2>&1)" || return 1
    if [[ "$actual_major" != "$major" || "$vendor" != 'Oracle Corporation' || ! "$compiler" =~ (^|$'\n')javac\ $major([.+-]|$) ]]; then
        echo "Invalid $override_name: expected Oracle JDK $major; found Java $actual_major, vendor '$vendor', $compiler at $selected. Select a valid installed Oracle JDK; no fallback is allowed." >&2
        return 1
    fi
    echo "Oracle JDK $major: $selected ($runtime; $vendor; $compiler)" >&2
    printf '%s\n' "$selected"
}

ONE_MB_BUILD_JAVA_HOME="$(one_mb_resolve_jdk 25 ONE_MB_BUILD_JAVA_HOME)"
ONE_MB_TEST_JAVA_HOME="$(one_mb_resolve_jdk 27 ONE_MB_TEST_JAVA_HOME)"
export ONE_MB_BUILD_JAVA_HOME ONE_MB_TEST_JAVA_HOME
export JAVA_HOME="$ONE_MB_BUILD_JAVA_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
    if [[ "${1:-}" == --test-home && $# == 1 ]]; then
        printf '%s\n' "$ONE_MB_TEST_JAVA_HOME"
    else
        echo 'Source scripts/java-env.sh, or run it with --test-home.' >&2
        exit 1
    fi
fi
