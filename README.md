# Weeker

Weekly planner for Android. Simple, lightweight, offline-first.

## Features

- **Day & Week views** with quick navigation between them
- **Event management**: add, edit, delete, reorder (drag handle), copy/move to another day
- **Calendar picker** with context-aware mode (day or week selection)
- **Backup/Restore** to SQLite `.db` and CSV files
- **Dark/Light theme** with adaptive colors
- **3 languages**: English, Ukrainian, Russian
- **Double-back exit** from main screen

## Tech Stack

- Kotlin
- Jetpack Compose + Material3
- Room (SQLite)
- DataStore Preferences
- Navigation Compose
- Manual DI (no Hilt/Dagger)
- Min SDK 26, Target SDK 34

## Build

```bash
./01-MakeDebug.sh        # build debug APK
./02-InstallDEBUG.sh     # install to emulator
```

## Co-authored with

- Claude Code (Anthropic)
- Codex (OpenAI)

## License

Private project.
