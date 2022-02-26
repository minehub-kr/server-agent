#!/bin/bash

test -f ./pom.xml.bak && rm ./pom.xml
test -f ./pom.xml.bak && mv ./pom.xml.bak ./pom.xml

echo "Target version: $1"
cp pom.xml pom.xml.bak
version_name=$(find ~/.m2/repository/org/spigotmc/spigot-api -type d -maxdepth 1 -name "$1*" -print -quit 2> /dev/null)
api_ver_name="${version_name/$HOME\/.m2\/repository\/org\/spigotmc\/spigot-api\//}"

echo "Target Spigot-API version: $api_ver_name"
sed -i s/1.16.5-R0.1-SNAPSHOT/$api_ver_name/g ./pom.xml

echo "Done."
