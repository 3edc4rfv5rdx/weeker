# Changelog

> N=new feature, E=error fix, F=fine-tune, R=refactor, I=infrastructure

## 0.5.20260309+96
- F Fix multi-position drag in week view (preserve composition identity)

## 0.5.20260309+95
- F Prevent long press on drag handle from opening context menu

## 0.5.20260309+94
- N Drag handle for reordering events within day in week view

## 0.5.20260309+100
- E Fix dark theme: adaptive event row colors, menu colors, about dialog

## 0.5.20260309+98
- F Shorten edit screen header to just "Edit"

## 0.5.20260309+97
- N Edit event from long-press context menu (all screens)

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
