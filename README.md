# Plus Key ANC

- [Complete setup and reinstall guide](RECOVERY.md)
- [Full uninstall, permission removal and debugging reset](UNINSTALL.md)
- [Physical validation and known limits](VALIDATION.md)

Native Android controller for the OnePlus 15 (CPH2747) and OnePlus Buds Pro 3. Built and installed on Android 16, CPH2747_16.0.9.400(EX01). Physical single/double/hold transitions and locked/screen-off operation were tested with the owner; see PROGRESS.md and evidence/integrated-device.txt.

## Use

Wear the earbuds and connect them to the phone. Open **Plus Key ANC**, tap **ENABLE KEY** to authorize Shizuku, then **START**. A persistent notification indicates the controller service is running; the status panel records actual observations and errors.

- Single: High → Medium → Low → High; Off, Transparency, Auto or Unknown → High.
- Double: Auto.
- Hold: ANC/Auto/Unknown → Transparency; Transparency → Off; Off → Transparency.
- STOP releases capture and stops the app service. Disconnecting the verified earbuds releases capture automatically.
- TIMING changes double-press and hold thresholds. Defaults: 220ms after release for double recognition, 600ms hold, 25ms noise filter. Increase the double window if your taps are slower. A single must wait for that window to avoid executing the first tap of a double.

No PC or Internet connection is used during normal operation. Tasker is not required.

The app restarts its service after reboot when previously started. The installed Shizuku manager also has an Android 13+ wireless ADB boot path, configured and physically tested. It requires Shizuku pairing, WRITE_SECURE_SETTINGS, Wi-Fi and debugging; it enables USB/wireless debugging and disables authorization expiry. **Reboot recovery passed:** the owner tested all gestures with USB unplugged, without opening either app, then reconnected USB. Both services and one key helper were running automatically. If needed, start Shizuku through its on-phone Wireless debugging Start, then START this app. Force-stopping the app requires opening it again. See RECOVERY.md for complete reinstall instructions and conditions.

## Build and install

Prerequisites: Windows PowerShell, JDK 17, Android SDK platform 36 and build-tools 36.1.0. The four pinned Shizuku API 13.1.5 jars are bundled in libs; MIT license and SHA-256 hashes are included. The app itself is Java and has no Gradle or AndroidX UI dependency.

```powershell
.\test.ps1 -Jdk 'C:\path\to\jdk-17'
.\build.ps1 -Jdk 'C:\path\to\jdk-17' -Sdk "$env:LOCALAPPDATA\Android\Sdk"
adb install -r .\BudsAncLab.apk
adb shell am start -n dev.local.anc/.MainActivity
```

The APK keeps its original diagnostic filename; its installed label is Plus Key ANC, version 1.2. The build script uses a local development signing key in the project .local/local.p12. Preserve that key for compatible updates. A fresh key requires uninstall/reinstall and renewed grants. Never share that development key as a release credential.

Grant Nearby devices, notifications and Shizuku authorization when prompted. On this phone the user approved Developer Options → **Disable system optimisation** ON (formerly Disable permission monitoring); verified property persist.sys.permission.enable=false. This is an OEM-wide change whose own warning lists power/security/system/display effects. It is required here for successful Shizuku authorization and service binding. To revert it, turn that switch OFF. Key capture may stop working afterward. No root or bootloader change was made.

The controller was added to the Android battery-optimization exemption list. To undo that particular exemption:
```powershell
adb shell cmd deviceidle whitelist -dev.local.anc
```

## Architecture

- `Anc.java`: exact effective-mode transition table.
- `Gestures.java`: deterministic edge recognizer with cancellable timers and duplicate filtering.
- `KeyCapture.java`: Shizuku shell-UID service verifies the dedicated event0 capability, applies EVIOCGRAB, and blocks on input/wakeup pipe. Only code 735 is read. Descriptor closure releases capture; no key injection or native settings replacement.
- `InputBridge.java` / `InputUserService.java`: Shizuku Binder transport with app UID and shell UID checks, death handling and bounded bind recovery.
- `ControllerService.java`: foreground connectedDevice service, serialized Bluetooth operations, active paired A2DP identity check, public RFCOMM vendor UUID, reader/framer, sequence-correlated current-state queries, mode control and confirmation.
- `Protocol.java`: framing, selected-mode decoding and wire constants. Auto's adaptive strength is not treated as selected mode.
- `MainActivity.java`: controls and status only.

Single and hold use a fresh device query, never the UI's last-observed mode. Double always sets Auto and therefore skips the unnecessary pre-query. Every set is followed by a current-state query. Strength-only commands were experimentally verified to activate ANC from Off and Transparency. Unknown state uses the requested fallback. Failures are reported; no socket-write-only success is displayed.

## Diagnostics and limitations

Use targeted logs; avoid exporting broad Bluetooth dumps or phone logs:
```powershell
adb logcat -d -s BudsAncLab:I
adb shell am broadcast -n dev.local.anc/.DiagnosticReceiver --es command QUERY
```
The diagnostic receiver requires Android's DUMP permission, available to shell but not ordinary apps. Its commands are limited to controller diagnostics; it is not a shell interface.

If state changes fail with 0x0E, verify the earbuds are worn. This rejection occurred before the owner wore them; commands succeeded afterward.

OxygenOS Earbud functions/HeyMelody can compete for the vendor RFCOMM channel. Opening its page closed the controller's socket during investigation; stopping com.oplus.melody allowed reconnection. The final controller does not repeatedly force-stop the native app. If another app holds the channel, close that app and retry START. This coexistence limitation needs to remain explicit; current-state query support does not guarantee concurrent vendor sessions.

Bluetooth reconnect uses at most two connection attempts. No audio profile is disconnected to obtain the control channel. Input capture, reads, and state changes run on the phone. The app requests no Internet, location, READ_LOGS, overlay, or accessibility permission. A short bounded gesture wakelock is used; long-term battery drain has not been benchmarked.

The input mapping is deliberately guarded for this CPH2747 build family. An OxygenOS change to the node layout causes capture to refuse activation, rather than grabbing another input device.

Consult PROGRESS.md and VALIDATION.md for performed versus outstanding tests. Source references and caveats are in RESEARCH.md. Project skills capture verified findings; they are local to this project, not installed globally.
