# Weeker Implementation Plan

## Product rules
- Platform Android only
- Local storage only no account no sync
- Week starts on Monday
- Start screen after onboarding is Today
- Up navigation from Today opens Week view
- No repeating tasks in v1
- No reminders in v1
- No animations decorative effects
- High contrast buttons with filled background and rounded corners

## Localization rules
- Code is English only
- Localization data is in assets/i18n.json
- Keys are English phrases
- Translations are language objects without en field
- Fallback for missing translation is key itself
- Primary language uk
- Secondary en fallback by key
- Additional ru now and easy extension later

## Theme rules
- Theme data is in assets/themes.json
- On first launch user picks language and theme
- Selected language and theme are stored in DataStore

## Data rules
- Events are stored by dateEpochDay
- No separate week table
- No createdAt and updatedAt
- Manual move undone events to next week by plus 7 days

## Screens v1
- Onboarding language and theme
- Today events for current date
- Week weekly list Monday to Sunday
- Week picker calendar based week selection
- Event edit create event for a day
- Settings change language and theme

## Next runs checklist
- Build and run on device emulator
- Verify onboarding persistence
- Verify localization fallback to key
- Verify week picker opens Monday week
- Verify move undone duplicates only undone events at plus 7 days
- Add edit and delete actions for events
- Add instrumentation tests for repository and navigation
