# Weeker

Weekly planner for Android. Simple, lightweight, offline-first.

## Features

 - **Day & Week views** with quick navigation between them
 - **Event management**: add, edit, delete, reorder (drag handle), copy/move to another day
 - **Event templates**: save titles as templates, autocomplete suggestions while typing, fill from template list
 - **Week notes**: add, edit, delete notes per week; notes indicator on week header
 - **All notes**: browse all week notes with search, grouped by week
 - **Calendar picker** with context-aware mode (day or week selection), weeks with notes highlighted
 - **Backup/Restore** to SQLite `.db` and CSV files, restore from backup list or file picker
 - **Dark/Light theme** with adaptive colors
 - **3 languages**: English, Ukrainian, Russian
 - **Settings**: edit past days toggle, language/theme selection, templates management
 - **Colored in-app toasts** for success, error, warning, and info messages
 - **Double-back exit** from main screen
 - **Onboarding** with language and theme selection

## Notes

- On API < 29 backup files are written to `Documents/Weeker`, so the install needs the legacy storage permission to use those exports.
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
