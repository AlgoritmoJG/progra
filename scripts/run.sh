#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/db-env.sh pos_dev
exec scripts/mvn.sh compile exec:java -Dpos.mainClass=com.progra.pos.view.ConsolaApp "$@"
