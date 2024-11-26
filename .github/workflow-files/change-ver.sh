#!/bin/bash

test -f ./build.gradle.kts.bak && rm ./build.gradle.kts
test -f ./build.gradle.kts.bak && mv ./build.gradle.kts.bak ./build.gradle.kts

echo "Target version: $1"
cp build.gradle.kts build.gradle.kts.bak
version_name=$(find ~/.m2/repository/org/spigotmc/spigot-api -type d -maxdepth 1 -name "$1*" -print -quit 2> /dev/null)
api_ver_name="${version_name/$HOME\/.m2\/repository\/org\/spigotmc\/spigot-api\//}"
api_ver=$(echo "$api_ver_name" | cut -f1,2 -d'.')

echo "Target Spigot-API version: $api_ver_name, api-version: $api_ver"
sed -i s/1.16.5-R0.1-SNAPSHOT/$api_ver_name/g ./build.gradle.kts
sed -i s/1.16/$api_ver/g ./src/main/resources/plugin.yml

echo "Delta Patching source code to match with SDK changes on Spigot-API: $api_ver_name"
./__legacy__/process.sh $1

echo "Done."
