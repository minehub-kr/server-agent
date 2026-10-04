#!/usr/bin/env bash
set -euo pipefail

project_dir=$(cd "$(dirname "$0")/.." && pwd)
cd "$project_dir"

service=${1:?Usage: bash scripts/test-compatibility.sh <paper service>}
compose=(docker compose -f docker-compose.compatibility.yml)

# Read the result directory from Compose so the runner follows version updates.
data_dir=$("${compose[@]}" config --format json | jq -er --arg service "$service" '
  .services[$service].volumes[] | select(.target == "/data") | .source
')
result_file="$data_dir/plugins/MinehubCompatibilityProbe/results.json"
log_file="$project_dir/build/compatibility/$service.log"

for plugin in build/libs/agent-0.0.1-ALPHA-all.jar build/libs/compatibility-probe.jar; do
  if [[ ! -f "$plugin" ]]; then
    echo "Missing plugin: $plugin. Run test shadowJar compatibilityProbeJar first." >&2
    exit 1
  fi
done

mkdir -p build/compatibility
# A previous successful report must not satisfy this run.
rm -f "$result_file"

cleanup() {
  local status=$?
  trap - EXIT
  set +e
  "${compose[@]}" logs --no-color "$service" > "$log_file" 2>&1
  cat "$log_file"
  "${compose[@]}" stop -t 10 "$service"
  "${compose[@]}" rm -f "$service"
  exit "$status"
}
trap cleanup EXIT

"${compose[@]}" up -d "$service"

deadline=$((SECONDS + 300))
while [[ ! -s "$result_file" ]] || ! jq empty "$result_file" 2>/dev/null; do
  if ((SECONDS >= deadline)); then
    echo "Timed out waiting for $service compatibility results." >&2
    exit 1
  fi
  sleep 2
done

jq . "$result_file"
jq -e '
  .results | type == "array" and length > 0 and all(.[]; .passed == true)
' "$result_file" > /dev/null
