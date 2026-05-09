# Navigation Flow Use Cases

## Purpose

This document defines the expected navigation behavior for DrinkDiary and lists the test cases that should protect it.

## Scope

- Bottom navigation between Dashboard, Collection, and Search.
- FAB entry into the record editor.
- Save and back behavior from the record editor.
- Detail-to-edit behavior for existing records.
- Android system Back Key behavior.

## Use Cases

### NAV-01. Open Dashboard From Bottom Navigation

Flow:
1. User taps the bottom `Home` item.
2. App navigates to `Dashboard`.
3. Dashboard becomes the selected top-level destination.

Expected result:
- Dashboard content is visible.
- Back Key exits the app because Dashboard is a top-level destination.

### NAV-02. Open Collection From Bottom Navigation

Flow:
1. User taps the bottom `Collection` item.
2. App navigates to `Collection`.
3. Collection becomes the selected top-level destination.

Expected result:
- Collection list content is visible.
- Back Key exits the app because Collection is a top-level destination.

### NAV-03. Open Search From Bottom Navigation

Flow:
1. User taps the bottom `Search` item.
2. App navigates to `Search`.
3. Search becomes the selected top-level destination.

Expected result:
- Search content is visible.
- Back Key exits the app because Search is a top-level destination.

### NAV-04. Add Record And Save

Flow:
1. User taps FAB from Dashboard, Collection, or Search.
2. App navigates to `RecordEditor(new)`.
3. User enters valid record data.
4. User taps Save.
5. App saves the record.
6. App navigates to `Collection`.

Expected result:
- The editor is removed from the visible flow.
- Collection list is visible after save.
- The new record is included in the list.

### NAV-05. Add Record And Press Back With Unsaved Input

Flow:
1. User taps FAB from Dashboard, Collection, or Search.
2. App navigates to `RecordEditor(new)`.
3. User changes at least one input value.
4. User presses the Android Back Key.
5. App shows a confirmation dialog: `입력한 내용이 초기화됩니다. 나가시겠어요?`
6. If user selects `예`, app returns to the screen where FAB was tapped.
7. If user selects `아니오`, app stays on `RecordEditor(new)`.

Expected result:
- Unsaved input is not discarded without confirmation.
- Confirming discard clears the in-progress editor state by leaving the editor destination.
- Canceling discard keeps the entered values visible.

### NAV-06. Add Record And Press Back Without Unsaved Input

Flow:
1. User taps FAB from Dashboard, Collection, or Search.
2. App navigates to `RecordEditor(new)`.
3. User does not change any input value.
4. User presses the Android Back Key.

Expected result:
- App returns immediately to the screen where FAB was tapped.
- No discard confirmation is shown.

### NAV-07. Open Existing Record, Edit, And Save

Flow:
1. User selects a record from Dashboard, Collection, or Search.
2. App navigates to `RecordDetail(recordId)`.
3. User taps Edit.
4. App navigates to `RecordEditor(recordId)`.
5. User changes data and taps Save.
6. App saves the record.
7. App navigates to `Collection`.

Expected result:
- App does not push another detail screen after save.
- Back Key from Collection exits the app because Collection is a top-level destination.

### NAV-08. Open Existing Record, Edit, And Press Back With Unsaved Input

Flow:
1. User opens `RecordEditor(recordId)` from `RecordDetail(recordId)`.
2. User changes at least one input value.
3. User presses the Android Back Key.
4. App shows the discard confirmation dialog.
5. If user selects `예`, app returns to `RecordDetail(recordId)`.
6. If user selects `아니오`, app stays on `RecordEditor(recordId)`.

Expected result:
- Existing record edits follow the same unsaved-change protection as new records.

## Test Cases

| ID | Scenario | Expected |
| --- | --- | --- |
| TC-NAV-01 | Tap Home bottom item | Dashboard is visible and selected |
| TC-NAV-02 | Tap Collection bottom item | Collection is visible and selected |
| TC-NAV-03 | Tap Search bottom item | Search is visible and selected |
| TC-NAV-04 | Press Back Key on Dashboard | App exits |
| TC-NAV-05 | Press Back Key on Collection | App exits |
| TC-NAV-06 | Press Back Key on Search | App exits |
| TC-NAV-07 | Tap FAB, enter valid data, save | Collection is visible |
| TC-NAV-08 | Tap FAB, change input, press Back, choose `예` | Previous screen is visible |
| TC-NAV-09 | Tap FAB, change input, press Back, choose `아니오` | Editor remains visible with input preserved |
| TC-NAV-10 | Tap FAB, press Back without changes | Previous screen is visible without dialog |
| TC-NAV-11 | Detail -> Edit -> Save | Collection is visible, no duplicate detail screen |
| TC-NAV-12 | Detail -> Edit -> change input -> Back -> `예` | Detail screen is visible |
| TC-NAV-13 | Detail -> Edit -> change input -> Back -> `아니오` | Editor remains visible with input preserved |

## Current Implementation Check

- Bottom top-level navigation uses `navigateTopLevel`, so Dashboard, Collection, and Search are treated as top-level destinations.
- FAB opens `RecordEditor(new)` from the current screen.
- New and edited records both navigate to `Collection` after save.
- Editor Back Key is intercepted when unsaved changes exist.
- The discard dialog keeps the editor on `아니오` and pops the editor on `예`.
