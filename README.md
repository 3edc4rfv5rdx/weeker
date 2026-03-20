# Weeker

Weekly planner for Android. Simple, lightweight, offline-first.

## Features

 - **Day & Week views** with quick navigation between them
 - **Event management**: add, edit, delete, reorder (drag handle), copy/move to another day
 - **Calendar picker** with context-aware mode (day or week selection)
 - **Backup/Restore** to SQLite `.db` and CSV files
  - **Dedicated CSV export** from Settings for spreadsheet-friendly copies
 - **Dark/Light theme** with adaptive colors
 - **3 languages**: English, Ukrainian, Russian
 - **Double-back exit** from main screen
- **Localized onboarding** in the primary Ukrainian language that still lets you switch to English or Russian the moment you open the app
- **Themes from assets** let you preview curated palettes without downloading updates
- **Lightweight storage strategy:** everything is offline, stored inside Room/DataStore, so syncing is never needed
- **Quick exports:** backup creates both `.db` and `.csv` copies, ready for transfer without digging through settings
- **Current notes**
  - On API < 29 backup files are written to `Documents/Weeker`, so the install needs the legacy storage permission to use those exports.
  - Theme changes persist in DataStore, and the app will read them once the saved `themeId` is passed to `ThemeManager`.
  - The onboarding flow is wired to pick the first language returned by `LocalizationManager.availableLanguages()` (currently `"en"`) unless overridden.
  - No automated tests are added yet (`app/src/test` and `app/src/androidTest` are not present).

## Tech Stack

- Kotlin
- Jetpack Compose + Material3
- Room (SQLite)
- DataStore Preferences
- Navigation Compose
- Manual DI (no Hilt/Dagger)
- Min SDK 26, Target SDK 34

## Co-authored with

- Claude Code (Anthropic)
- Codex (OpenAI)

## License

Public project.
