#!/bin/sh
set -e

./gradlew :app:assembleRelease

echo "Release APK: app/build/outputs/apk/release/app-release.apk"
