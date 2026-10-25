#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."
exec ./scripts/gradle.sh clean build printBuildConfig --warning-mode all "$@"
