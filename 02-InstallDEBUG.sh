#!/bin/sh
set -e

apk="app/build/outputs/apk/debug/app-x86_64-debug.apk"
serial="${1:-emulator-5554}"

if [ ! -f "$apk" ]; then
  echo "Debug APK not found. Build first: ./01-MakeDebug.sh"
  exit 1
fi

echo ">>> Installing $(basename "$apk") to $serial"
adb -s "$serial" install -r "$apk"
