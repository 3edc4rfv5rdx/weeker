# Tomorrow Notes (2026-03-04)

1. Verify week header layout on device:
- Centered block with two lines (`Week` + date range).
- Date range has extra horizontal padding and should not clip on small screens.

2. Verify calendar picker UX in copy/move dialogs:
- Month/year dropdown buttons show larger text.
- Year dropdown is around selected year (`-10..+10`), not far historical values first.
- Month/year buttons have no `v` suffix text.

3. Week list readability check:
- Day name and date are rendered as separate chips.
- Ensure chips stay readable with long localized day names.

4. Clean-up task:
- `WeekScreen` still has `onMoveUndone` parameter unused (compiler warning).
- Decide: either restore button usage or remove parameter/call chain.

5. Regression smoke test before next commit:
- Today screen: long-press event menu contains `Delete / Move to / Copy to`.
- Week screen: no standalone `Move undone` button.
- `./gradlew :app:assembleDebug` remains green.

6. Logic follow-up from review:
- Secondary screens use exit-on-double-back wiring in several routes; keep back arrow and system back behavior aligned with actual stack navigation.
- `Today` date/event source is derived from `remember { LocalDate.now() }`; verify rollover after midnight without recreating the process.
- Menu action `Settings` can push duplicate `settings` destinations; keep it single-top or ignore when already open.
- Rule `cannot add events in past` is enforced only on create screen; align move/copy dialogs and repository validation with the same rule.
- Theme persistence model is incomplete: `themeFlow` exists but app theme selection still always uses `defaultThemeId`.
