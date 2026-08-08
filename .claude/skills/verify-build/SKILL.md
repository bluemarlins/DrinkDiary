---
name: verify-build
description: Run the standard DrinkDiary build-verify sequence (gradle assemble/test, install to emulator, launch, screenshot) to confirm a change actually works. Use after code changes — especially UI changes — before considering work done or before committing. Don't declare a change finished on code inspection alone.
---

# verify-build

"Looks done" is not a verification. Run this sequence and look at the actual output before saying a change works.

## 1. Build + unit test

```
cd "<repo root>" && ./gradlew :app:assembleDebug :app:testDebugUnitTest --console=plain
```

If this is the first build after adding a dependency, it can take longer — don't assume a timeout means failure without checking the tail of the output. Gradle cache writes on Windows occasionally fail transiently (`Could not write workspace metadata`) — just retry once before treating it as a real error.

## 2. Emulator

Check first: `adb devices -l`. If nothing is listed, start one:

```
"$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe" -avd Medium_Phone_API_36.1 -no-snapshot-load -no-window -gpu swiftshader_indirect
```

Run this with `run_in_background: true` on the Bash tool call itself (not a trailing `&` inside the command — that has left orphaned/duplicate emulator processes in this project before). Poll `adb shell getprop sys.boot_completed` until it returns `1` rather than sleeping a fixed guess.

**If the emulator vanishes from `adb devices` after appearing to boot**, it died on snapshot load — the log shows `Failed to load snapshot 'default_boot'` and then the process exits without an obvious error. Relaunch with `-no-snapshot` (not just `-no-snapshot-load`) to force a clean cold boot. This happened twice in one session; a plain retry with the same flags reproduces the failure.

## 3. Install + launch

```
cd "<repo root>" && ./gradlew installDebugApk --console=plain
adb shell am force-stop com.bluemarlin.drinkdiary
adb shell am start -n com.bluemarlin.drinkdiary/.MainActivity
```

## 4. Screenshot

```
adb exec-out screencap -p > "<scratchpad>/<name>.png"
```

Then use the Read tool on the PNG to actually look at it — don't infer the UI state from logs alone.

**Timing**: this app has a splash screen and a cold start after a fresh install routinely takes ~8-10s to reach the dashboard, well past the 2-4s that's enough for a warm restart. If the screenshot shows the green wine-glass splash icon, that is not "the app is broken" — wait and capture again. Budget the longer wait after any `install`; only a warm `am start` of an already-running process is quick.

**Multi-display devices (foldables)**: on a device with more than one display — the Galaxy Z Fold in this project — a bare `adb exec-out screencap -p` prints a `[Warning] Multiple displays were found...` banner to stdout *before* the PNG bytes, corrupting the file (the Read tool then reports "not a valid PNG"). Enumerate the real display IDs and target the active one explicitly:

```
adb -s <serial> shell dumpsys SurfaceFlinger --display-id
adb -s <serial> exec-out screencap -p -d <active-display-id> > "<scratchpad>/<name>.png"
```

The active display is generally not the first one listed — on the Fold, display 0 was an inactive/black panel and the real screen was HWC display 3. Determine it once per device and reuse the ID; it's stable across a session.

**With several devices attached**, always pass `-s <serial>`. A physical device reconnecting mid-session silently turns bare `adb` commands into `more than one device/emulator` errors, and it is easy to misread that as the app having crashed.

## Automating taps (for seeding sample data, navigating flows)

- **Don't compute tap coordinates from the screenshot at all.** Screenshots are read back at a *displayed* resolution different from the device's real pixels, and the Read tool states the multiplier ("displayed at 900x2000, multiply by 1.20"). Applying that multiplier by hand has gone wrong repeatedly in this project — it has opened a record detail screen instead of hitting a nearby toggle, twice in one session, costing a navigate-back and a re-dump each time. Get the bounds from the UI dump instead and tap their center; the dump is already in real device pixels, so no conversion is involved:
  ```
  MSYS_NO_PATHCONV=1 adb shell uiautomator dump /sdcard/window_dump.xml
  MSYS_NO_PATHCONV=1 adb pull /sdcard/window_dump.xml "<scratchpad>/dump.xml"
  ```
  Then grep the target's bounds by its visible text, e.g. `text="펼치기 ▼"[^/]*bounds="\[[0-9,\]\[]+\]"` with `-o`, and tap the midpoint. Use the screenshot to decide *what* to tap, never *where*.
  (The `MSYS_NO_PATHCONV=1` prefix is required in this Windows/git-bash environment — without it `adb pull /sdcard/...` fails with `failed to stat remote object 'C:/Program Files/Git/sdcard/...'` because git-bash rewrites the path before `adb` sees it.)
- **A stale dump looks exactly like a valid one.** `adb uiautomator dump` (missing `shell`) fails with `adb.exe: unknown command uiautomator`, but a subsequent `pull` still succeeds and hands back the *previous* dump left on the device — in this project that silently returned the editor screen's hierarchy while the dashboard was on screen, and the coordinates derived from it were nonsense. Always check that the `dump` step printed `UI hierchary dumped to: ...` before trusting the pulled file, and sanity-check that the XML contains text you expect on the current screen.
- Dismiss the on-screen keyboard/floating IME toolbar with `adb shell input keyevent 111` before tapping something it might be covering.
- Star-rating or stepper-style inputs are more reliable to drive via a repeated "+"/"-" button tap than by trying to land exactly on a specific star glyph.

## Ad banners in verification screenshots

If a screen has `showBannerAd = true`, the captured screenshot will include the live AdMob test banner. That's fine for a functional check, but **strip it before using a screenshot for marketing assets or design review** — crop the ad strip out and recompose the bottom nav flush against the content above it (see `app/docs/marketing/store-listing-assets.md` for the exact Pillow approach used before).
