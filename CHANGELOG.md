# Changelog

> N=new feature, E=error fix, F=fine-tune, R=refactor, I=infrastructure, T=tag

## 0.6.20260316+161
- F Center event row menu horizontally

## 0.6.20260315+159
- F Add explicit menu button on event rows for discoverability

## 0.6.20260315+158
- F Unify button layout: cancel/save in one row, cancel always left, action always right
- F Restore and About dialogs use WeekerButton for consistency

## 0.6.20260315+156
- E Backup/restore includes all data: events, week notes, templates, settings
- N Confirm dialog before restore with data loss warning
- F Settings screen: language and theme as rows with selection dialogs

## 0.6.20260315+147
- F Auto-focus text field in note add/edit dialogs (WeekNotes, AllNotes)

## 0.6.20260315+145
- N Templates screen: add/edit/delete templates, alphabetical sort
- N Templates entry in Settings screen
- N Template autocomplete in event title and comment fields (popup suggestions)
- N "From template" button on event add/edit screen fills active field
- N Confirm delete dialog for events in Day and Week screens
- N Warning toast (orange) when trying to delete past events
- F Settings: bold primary-colored section headers
- F ConfirmDeleteNoteDialog supports custom title key
- I i18n keys: delete event, cannot delete past events

## 0.6.20260314+127
- N Week notes: add/edit/delete notes per week from Week and Day screens
- N Notes button (N/N!) on Week screen header with indicator for existing notes
- N All notes screen with search, grouped by week (menu entry)
- N Calendar highlights weeks with notes using colored row background
- N Confirm delete dialog for notes
- I DB migration v1 to v2: week_notes and event_templates tables
- I WeekNoteDao, WeekNoteRepository, EventTemplateDao, EventTemplateRepository
- I Routes for WEEK_NOTES, ALL_NOTES, TEMPLATES
- F Compact navigation buttons on Week screen (reduced padding/gap)
- F Disable add event button for past days on Day screen

## 0.5.20260311+116
- R Reformat i18n.json to multi-line style for readability
- F Update app icon to new design

## 0.5.20260309+112
- I Add LICENSE and icon source file
- I Auto-detect APK names in install scripts
- I Remove redundant 03-InstallToEmul script
- T Tag v0.5.20260309+112

## 0.5.20260309+111
- I ABI splits: universal + arm64-v8a/armeabi-v7a/x86_64 APKs
- I Enable minify, shrink resources, compress native .so libs
- I Auto-increment build number, release APK renaming
- F Update launcher icons and manifest references

## 0.5.20260309+108
- N Add "Exit" menu item to app menu on all screens

## 0.5.20260309+107
- E Show event notes in compact mode (day and week views)
- F Set compact font size to 18sp for title and note

## 0.5.20260309+103
- F Reduce and align title font sizes across today/week/calendar screens
- F Calendar picker title shows "Day" or "Week" based on context

## 0.5.20260309+101
- F Reduce week screen title font size

## 0.5.20260309+100
- E Fix dark theme: adaptive event row colors, menu colors, about dialog

## 0.5.20260309+98
- F Shorten edit screen header to just "Edit"

## 0.5.20260309+97
- N Edit event from long-press context menu (all screens)

## 0.5.20260309+96
- F Fix multi-position drag in week view (preserve composition identity)

## 0.5.20260309+95
- F Prevent long press on drag handle from opening context menu

## 0.5.20260309+94
- N Drag handle for reordering events within day in week view

## 0.5.20260309+93
- F Close (x) button instead of back arrow on today screen

## 0.5.20260309+92
- N Calendar picker context-aware: opens week from week screen, day from day screen

## 0.5.20260309+91
- F Move "today" button to the left in week screen

## 0.5.20260309+89
- N Calendar picker opens day view instead of week
- F Add "today" button in day view, shorten "add event" to "add"
- F Slightly increase compact row height and font size

## 0.5.20260309+87
- N Navigate to day view by tapping day label in week screen

## 0.5.20260309+86
- F Compact event rows in today and week views (fit >=9 events)
- F Replace up/down arrows with drag handle for reordering
- F Auto-focus title field when adding event
- R Remove unused moveUndoneToNextWeek

## 0.5.20260309+85
- F Shorten week labels and reduce chip/card paddings
- F Shorten UA/RU labels to reduce line wrapping
- F Refine calendar picker and week UI interactions
- E Fix navigation, backup restore, and launcher assets
- R Simplify to single base theme with light/dark mode and status colors
- F Capitalize screen headers and weekday titles
- F Replace week add button with circular plus
- N Add double-back exit on today screen
- F Remove done label from event rows
- N Disallow creating events for past dates
- N Backup/restore (SQLite .db + CSV)
- N Internationalization: en, uk, ru
- E Fix onboarding default language and English key fallback
- E Fix debug build issues for theme resources
- I Add release signing config
- I Add helper scripts for debug/release build
- I Initialize app with Gradle wrapper and base implementation
