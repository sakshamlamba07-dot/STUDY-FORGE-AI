# StudyForge Android (Focus Shield)

Capacitor wrapper that loads https://studyforge-pi-inky.vercel.app and adds an
Accessibility Service that bounces out of Instagram Reels while focus mode is on.

## Build the APK (no Android Studio needed)
1. Create a new GitHub repo and push this folder as the repo root.
2. GitHub > Actions > "Build Android APK" > Run workflow.
3. Download `StudyForge-apk` from the run (or push a tag like `v1` to get a Release link).

## Install (each user)
1. Open the APK, allow "Install unknown apps" for the browser/file manager.
2. Android 13+: Settings > Apps > StudyForge > ⋮ > **Allow restricted settings**.
3. Settings > Accessibility > StudyForge Focus Shield > turn on.

## Test / fix reel detection
Instagram can rename internal view IDs. If reels stop being blocked:
  adb logcat -s FocusShield
  adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml
(run the dump while a Reel is on screen, find the `com.instagram.android:id/...`
that only appears in Reels, add it to REEL_VIEW_IDS in ReelsBlockerService.java)
