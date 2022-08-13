#!/bin/bash

PATCH_DIR="./__legacy__"

echo "Patching Codebase for version: $1"
target_version=$1

# Copy/Pasted from: https://stackoverflow.com/a/4025065
vercomp () {
    if [[ $1 == $2 ]]
    then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    # fill empty fields in ver1 with zeros
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
    do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++))
    do
        if [[ -z ${ver2[i]} ]]
        then
            # fill empty fields in ver2 with zeros
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]}))
        then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]}))
        then
            return 2
        fi
    done
    return 0
}

patch_versions=()
bak=$(pwd)

cd $PATCH_DIR

for patch_dir in ./*/; do
  if [ -d "$patch_dir" ]; then
    version="${patch_dir/.\//}"
    version="${version/\//}"

    vercomp $target_version $version
    comp_res=$?

    if [[ $comp_res -eq 2 ]]; then
      patch_versions+=(
        $version
      )
    fi

    if [[ $comp_res -eq 0 ]]; then
      patch_versions+=(
        $version
      )
    fi
  fi
done

cd $bak

# Performing Bubble sort
patch_ver_len=${#patch_versions[@]}
for (( i=0; i<$patch_ver_len; i++ )); do
  for ((j=0; j<$patch_ver_len-i-1; j++)); do
    vercomp ${patch_versions[j]} ${patch_versions[$((j+1))]}
    res=$?

    if [[ $res -eq 2 ]]; then
      # swap
      temp=${patch_versions[j]}
      patch_versions[$j]=${patch_versions[$((j+1))]}  
      patch_versions[$((j+1))]=$temp
    fi
  done
done

echo "Patches to install: ${patch_versions[@]}"

for patch in ${patch_versions[@]}; do
  echo "Installing patch for version ${patch}"
  cp -rfv $PATCH_DIR/$patch/* ./
done

echo "Patch Instllation Complete."
