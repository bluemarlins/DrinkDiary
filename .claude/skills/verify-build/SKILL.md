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

Wait 2-4 seconds after launch before capturing — ads/network calls need a moment, and a too-early screenshot can miss content that loads asynchronously (has happened with the AdMob banner in this project).

Then use the Read tool on the PNG to actually look at it — don't infer the UI state from logs alone.

## Automating taps (for seeding sample data, navigating flows)

- **Coordinate scale**: screenshots are read back at a *displayed* resolution different from the device's real pixel resolution — the Read tool output states the multiplier (e.g. "displayed at 900x2000, multiply by 1.20 for original"). Tap coordinates must be in **real device pixels**, not the displayed image's pixel coordinates. Getting this wrong silently taps the wrong element (this has happened repeatedly — e.g. hitting an ad banner instead of a FAB a few pixels away).
- **Prefer exact bounds over eyeballing**: dump the UI hierarchy and compute the center of the target element's bounds rather than estimating from the screenshot:
  ```
  MSYS_NO_PATHCONV=1 adb shell uiautomator dump //sdcard/window_dump.xml
  MSYS_NO_PATHCONV=1 adb shell cat //sdcard/window_dump.xml > <scratchpad>/dump.xml
  ```
  (The `MSYS_NO_PATHCONV=1` prefix and `//` doubled leading slash are required in this Windows/git-bash environment — a bare `/sdcard/...` path gets mangled into a Windows path by git-bash before it reaches `adb`.)
- Dismiss the on-screen keyboard/floating IME toolbar with `adb shell input keyevent 111` before tapping something it might be covering.
- Star-rating or stepper-style inputs are more reliable to drive via a repeated "+"/"-" button tap than by trying to land exactly on a specific star glyph.

## Ad banners in verification screenshots

If a screen has `showBannerAd = true`, the captured screenshot will include the live AdMob test banner. That's fine for a functional check, but **strip it before using a screenshot for marketing assets or design review** — crop the ad strip out and recompose the bottom nav flush against the content above it (see `app/docs/marketing/store-listing-assets.md` for the exact Pillow approach used before).
