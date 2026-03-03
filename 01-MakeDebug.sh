#!/bin/sh
set -e

./gradlew :app:assembleDebug

echo "Debug APK: app/build/outputs/apk/debug/app-debug.apk"
