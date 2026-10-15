---
name: device-validation
description: Validate this project's physical OnePlus ANC controller without confusing host tests with device evidence.
---

Read ../../PROGRESS.md first. `adb devices -l` initially returned empty until user enabled debugging. On this OxygenOS build, shell `pm grant` failed with GRANT_RUNTIME_PERMISSIONS denied; user runtime dialogs succeeded. The user later explicitly approved the OEM permission-monitoring change required for Shizuku. The switch is named Disable system optimisation on this build. It was changed OFF→ON through UI, verified persist.sys.permission.enable=false. Do not infer permission for unrelated global changes.

Use `adb logcat -d -s BudsAncLab:I` for the diagnostic app. The `-t` option counts the global log tail before filtering; it previously hid relevant app records. Avoid broad Bluetooth dumps and log exports; select only relevant protocol/target state.

`adb shell getevent -lp /dev/input/event0` shows gpio-keys/BTN_TRIGGER_HAPPY32 and was physically correlated to Plus Key presses, Linux code 735. EVIOCGRAB was accepted from shell without root. Final KeyCapture verifies the capability list and uses this node only. Read no touches or typing. KEYLOG messages were duplicated with identical text, whereas evdev had true separate edges. Use Binder through Shizuku; the abstract Unix-socket experiment failed and was removed.

After changing the OEM switch, restart Shizuku to refresh its process permissions. An already-running server initially allowed authorization but its user-service starter timed out; restarting through the installed libshizuku.so fixed it. The installed 13.6 manager contains a wireless ADB boot path with existing WRITE_SECURE_SETTINGS and its own pairing key. See ../../RECOVERY.md for conditions and security effects, and ../../VALIDATION.md for the actual reboot result. Do not infer boot success from source code alone.

Run ../../test.ps1 for host assertions and ../../build.ps1 for the APK. T01–T20 require physical evidence; compilation/unit tests are not substitutes. See ../../VALIDATION.md for the eight-gesture physical sequence and successful locked/screen-off feedback. Reboot requires user coordination before interrupting phone availability.

References: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#onKeyEvent(android.view.KeyEvent) and https://shizuku.rikka.app/guide/setup/ .
