#!/usr/bin/env bash
set -euo pipefail

target_version=${1:?Usage: change-ver.sh <Minecraft version>}
if [[ ! "$target_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Expected a Minecraft version such as 1.8.8 or 1.19.2." >&2
  exit 1
fi

api_ver_name="$target_version-R0.1-SNAPSHOT"
api_ver=${target_version%.*}

echo "Target Spigot-API version: $api_ver_name, api-version: $api_ver"
sed -i.bak "s/1.16.5-R0.1-SNAPSHOT/$api_ver_name/g" ./build.gradle.kts
sed -i.bak "s/^api-version: .*/api-version: $api_ver/" ./src/main/resources/plugin.yml
rm ./build.gradle.kts.bak ./src/main/resources/plugin.yml.bak

echo "Delta Patching source code to match with SDK changes on Spigot-API: $api_ver_name"
bash ./__legacy__/process.sh "$target_version"

echo "Done."
