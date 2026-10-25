#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."
exec ./scripts/gradle.sh testJava27 printBuildConfig --warning-mode all "$@"
