#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home
export JAVA26_HOME=/Library/Java/JavaVirtualMachines/jdk-26.0.2.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

exec gradle testJava26 printBuildConfig --warning-mode all "$@"
