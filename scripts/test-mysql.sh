#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/db-env.sh pos_test
export POS_RUN_DB_TESTS=true
exec scripts/mvn.sh test "$@"
