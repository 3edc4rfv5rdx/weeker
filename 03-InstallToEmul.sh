#!/bin/sh
set -e

apk="app/build/outputs/apk/release/app-x86_64-release.apk"
serial="${1:-emulator-5554}"

if [ ! -f "$apk" ]; then
  echo "Release APK not found. Build first: ./00-MakeRelease.sh"
  exit 1
fi

echo ">>> Installing $(basename "$apk") to $serial"
adb -s "$serial" install -r "$apk"
