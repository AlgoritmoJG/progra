#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
[[ -n "${DISPLAY:-}" ]] || { echo 'Se requiere una pantalla gráfica o Xvfb; configure DISPLAY.' >&2; exit 1; }
source scripts/db-env.sh pos_test
export POS_RUN_DB_TESTS=true POS_RUN_SWING_TESTS=true
exec scripts/mvn.sh test "$@"
