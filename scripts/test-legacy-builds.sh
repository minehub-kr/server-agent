#!/usr/bin/env bash
set -euo pipefail

project_dir=$(cd "$(dirname "$0")/.." && pwd)

docker run --rm -i \
  -e GRADLE_USER_HOME=/workspace/build/compatibility/gradle-cache \
  -v "$project_dir":/workspace -w /workspace eclipse-temurin:17-jdk \
  bash -s <<'BUILD_LEGACY'
set -euo pipefail

mkdir -p /workspace/build/compatibility
task_dir=$(mktemp -d /workspace/build/compatibility/legacy-build.XXXXXX)
trap 'rm -rf "$task_dir"' EXIT

for task_version in 1.8.8 1.12.2; do
  task_version_dir="$task_dir/$task_version"
  mkdir -p "$task_version_dir"
  cp -R src __legacy__ gradle "$task_version_dir/"
  cp build.gradle.kts settings.gradle.kts gradle.properties gradlew "$task_version_dir/"

  (
    cd "$task_version_dir"
    sed -i "s/1.16.5-R0.1-SNAPSHOT/$task_version-R0.1-SNAPSHOT/g" build.gradle.kts
    sed -i "s/api-version: 1.16/api-version: ${task_version%.*}/" src/main/resources/plugin.yml
    bash __legacy__/process.sh "$task_version"
    bash ./gradlew --no-daemon shadowJar
    cp build/libs/agent-0.0.1-ALPHA-all.jar "/workspace/build/compatibility/legacy-$task_version.jar"
  )
done
BUILD_LEGACY
