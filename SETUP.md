# Complete setup: OnePlus 15 Plus Key → Buds Pro 3 ANC

Follow the sections in order. This describes the working setup, including the OxygenOS restriction we encountered and the extra steps needed for reboot recovery. For removal and restoring the phone's debugging/security settings, use [UNINSTALL.md](UNINSTALL.md).

## Requirements and tested compatibility

| Item | Requirement / tested setup |
|---|---|
| Phone | OnePlus 15 **CPH2747**, physical Plus Key |
| Firmware actually tested | Android 16/API 36, OxygenOS `CPH2747_16.0.9.400(EX01)`; reported Oplus ROM `V16.1.0` |
| Earbuds | **OnePlus Buds Pro 3**, already paired normally with the phone; wear both during testing |
| Controller | [BudsAncLab.apk](https://github.com/SasukeUchihaGermany/OnePlus-15-Plus-Key-Buds-Pro-3-ANC/raw/refs/heads/main/BudsAncLab.apk), installed label **Plus Key ANC**, package `dev.local.anc`, version 1.2 |
| Privilege bridge | [Shizuku](https://github.com/RikkaApps/Shizuku/releases), package `moe.shizuku.privileged.api`; tested manager `13.6.0.r1086.2650830c` |
| Phone settings | Developer options, USB debugging, Wireless debugging, and the OxygenOS permission-monitoring change described below |
| Network | Wi-Fi for Shizuku wireless pairing/start and the tested boot recovery; no Internet needed during normal ANC operation |
| Setup computer | A PC with [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools), a USB data cable and an authorized ADB connection for installation/permission configuration. Commands here use PowerShell. |
| Source builds only | Windows PowerShell, JDK 17+, Android SDK platform 36 and build-tools 36.1.0. These are unnecessary if installing the bundled APK. |

Tasker, an accessibility service, root, bootloader unlocking and re-pairing/resetting the earbuds are unnecessary. Tasker happened to be installed on the original phone but is not part of this solution. Other phones/firmware are not verified: the helper deliberately checks the model and dedicated key node before capture, so sharing the APK does not imply universal compatibility.

**Settings impact:** this solution uses Shizuku shell privileges. It is not a debugging-free app. On the tested firmware, authorization only worked after relaxing the OEM permission-monitoring control. Automatic Shizuku boot startup also enables USB/wireless debugging and makes ADB authorization non-expiring. Read those steps before applying them; the uninstall guide explains how to undo them.

## 1. Download the two apps and prepare ADB

1. Download `BudsAncLab.apk` from this repository. Its historical filename differs from the installed app label. The expected SHA-256 is in [evidence/apk-sha256.txt](evidence/apk-sha256.txt). On Windows, check with `Get-FileHash .\BudsAncLab.apk -Algorithm SHA256`.
2. Install Shizuku from its official release link above and open it once. If installing an APK through the phone's browser/file manager, Android may require **Allow from this source** for that installer; revoke that installer allowance afterward if you no longer need it. Allow Shizuku notifications so its pairing notification is available.
3. Download/extract Android Platform Tools on the PC. Add its directory to PATH, or open PowerShell in that directory and use `./adb.exe` wherever this guide says `adb`. `adb version` should run successfully.
4. Keep the phone unlocked for initial setup. Use a USB cable capable of data, rather than a charging-only cable. If Windows does not recognize the device, resolve its Android ADB driver/cable connection first.

## 2. Enable Developer options and authorize the PC

1. Open phone **Settings → About device → Version** and repeatedly tap **Version number / Build number** (usually seven times). Enter the phone PIN if asked. Menu wording varies; use Settings search if necessary. Wait for the developer-enabled message.
2. Return to Settings and open **Developer options**, typically under **System & updates**. Enable its master switch if it is off.
3. Enable **USB debugging**, accepting the phone's prompt.
4. Connect USB to the PC. Run:

   ```powershell
   adb devices
   ```

5. On the phone, accept **Allow USB debugging?** for your trusted PC; choose **Always allow from this computer** if desired. Run `adb devices` again. The state must be `device`, not `unauthorized` or an empty list. Do not publish the serial number.
6. Keep Wi-Fi on. Open **Developer options → Wireless debugging** and enable it; accept the network prompt if shown. Tap the row to enter the page, rather than only touching its switch.

Do not confuse enabling debugging with granting the controller Bluetooth/Shizuku permissions: those are separate steps below.

## 3. Remove the OxygenOS restriction that blocked Shizuku

This was essential on the tested build. Initially Shizuku rejected controller authorization as **ADB permission limited**; shell permission grants also failed.

1. In **Developer options**, scroll to the **Apps** area near the bottom and find **Disable system optimisation**. Older OnePlus/ColorOS documentation calls the relevant control **Disable permission monitoring**. Use Settings search if the label differs.
2. Turn **Disable system optimisation ON**. This is a negatively worded switch: ON disables the OEM protection. On the tested phone its caution dialog required **Turn on anyway**. Read and accept that warning only if you want this setup: it concerns wider security, power, display and system behavior, not just this app.
3. Optionally confirm the actual tested state through ADB:

   ```powershell
   adb shell getprop persist.sys.permission.enable
   ```

   The tested phone returned `false` after the UI change. Do not try to force the property with `setprop`; that was denied and was not how we enabled access.
4. **Restart Shizuku after changing this switch.** A server that was already running retained restricted permissions: authorization appeared to work but helper startup timed out until Shizuku itself was restarted. The wireless **Start** action in the next section starts/replaces the server. Verify the running status afterward.

If the setting is unavailable or the property/API differs, do not assume another permission or accessibility service substitutes for it. See the troubleshooting section. The [official Shizuku guide](https://shizuku.rikka.app/guide/setup/) documents the ColorOS/OnePlus permission-monitoring restriction; the label above was observed on this phone.

## 4. Pair Shizuku's own wireless-debugging key

This pairing is between **Shizuku on the phone** and Android's debugging service. Authorizing a USB PC, or running `adb pair` on the PC, does not pair Shizuku's own key.

1. Open Shizuku. Under **Start via Wireless debugging**, tap **Pairing** and follow its tutorial/open Developer options.
2. In **Developer options → Wireless debugging**, tap **Pair device with pairing code**. Keep the resulting pairing-code dialog open. Do not choose QR pairing.
3. Pull down the notification shade. Find Shizuku's **Pairing service found** notification; expand it if needed, then tap **Enter pairing code**.
4. Enter the current six-digit code displayed by the phone's pairing dialog and submit using the notification's send/confirm action. No code needs to be sent to a PC, an AI agent or this repository.
5. Wait for **Pairing successful**. If it times out, return to Shizuku, start Pairing again, reopen a fresh pairing-code dialog and use the new code. During setup the short-lived pairing service expired; restarting the flow fixed it.
6. Close the dialog/shade and return to Shizuku. Under **Start via Wireless debugging**, tap **Start**. Wait for **Shizuku is running** in ADB mode. If it only says searching or reports a certificate/pairing error, repair this on-phone pairing before continuing.

If Shizuku was already running before the OEM switch changed, repeat **Start** now to replace it with a fresh server. A manager version label and server version label may differ; the running server must use ADB/shell mode for this controller.

### USB fallback used during initial investigation

Before on-phone wireless pairing was complete, we used the installed Shizuku starter through the authorized USB shell. For a repeatable fallback, open Shizuku's **Start by connecting to a computer** section and use the command it displays for your installed version. Its official guide also documents the `start.sh` method. Do not reuse a hardcoded `/data/app/...` install path from someone else's phone: it changes on install/update. A successful USB start alone does **not** complete wireless pairing or prove reboot independence. Finish this section's on-phone pairing and wireless Start for the boot setup below.

## 5. Install the ANC controller and grant its permissions

From the directory containing the downloaded APK:

```powershell
adb install -r .\BudsAncLab.apk
adb shell am start -n dev.local.anc/.MainActivity
```

1. Open **Plus Key ANC**. Tap **START** if the runtime permission dialogs have not appeared yet.
2. Allow **Nearby devices** and **Notifications**. If notifications were previously denied, enable them under App info → Notifications. The Nearby devices permission is needed to use the paired Bluetooth connection. On this firmware, `pm grant` from ADB was rejected, so we granted these in the phone UI.
3. Tap **ENABLE KEY**. When Shizuku asks, allow this app's access (choose the persistent/allow-all-the-time option where offered). This is separate from Android's Nearby devices dialog.
4. If ENABLE KEY opens Shizuku instead, start Shizuku there first, then return and tap ENABLE KEY again. If authorization is rejected or helper binding times out, repeat the OEM setting check and restart Shizuku as above.
5. Pair/connect the Buds Pro 3 normally through phone Bluetooth if not already paired. Do not unpair existing earbuds. Take both out of the case and **wear both**: initial commands were rejected with status `0x0E` before they were worn.
6. Return to Plus Key ANC and tap **START**. A persistent controller notification should appear. Status should report **CAPTURE ON (Shizuku)** and an actual queried mode. **QUERY** can request the current mode without choosing a new one.

Leave the native Plus Key assignment as it is. On the original phone it was Translate; no remapping in native Settings was necessary. The app grabs the dedicated key only while the verified paired Buds Pro 3 is A2DP-connected. **STOP** releases it and disables controller startup.

## Native camera capsule (Live Alerts)

Version1.3 adds native OxygenOS Fluid Cloud feedback directly to the existing controller. No separate overlay or animation app is required. The tested speaker icon remains. All six confirmed modes map to short labels: High, Medium, Low, Auto, Off, Transparent.

1. Update the controller with the bundled signed APK and keep normal Nearby devices/notification/Shizuku grants.
2. Open **Plus Key ANC → LIVE ALERTS**. Verify the settings page title is **Plus Key ANC**, not the earlier **ANC Native Alert Test** probe. On this firmware, a reused notification-settings screen could otherwise show the previous app; the in-app shortcut now opens a fresh settings task.
3. Enable **Show Live Updates on Live Alerts** for Plus Key ANC. Normal **Allow notifications** must also stay enabled. The manifest declares POST_PROMOTED_NOTIFICATIONS; the OEM Live Alerts switch controls its app-op. No overlay permission or extra Shizuku grant is added.
4. Return to the home screen and press the Plus Key. The capsule appears after the resulting mode is confirmed by an earbud query. It shows the speaker and short mode; the notification title contains `Nish's ANC Engine: STATE`. Requested expiry is1500ms; native SystemUI animations/scheduling control the precise visible duration.
5. Single/double/hold and the app's direct mode buttons all share the confirmed-result path. Plain QUERY/CONNECT and external squeeze changes do not independently trigger the capsule. Failed or mismatched sets do not announce the requested mode. ANC remains usable if Live Alerts are disabled; only visual feedback is skipped.

Turn this per-app switch OFF to disable the capsule without disabling ANC control. STOP/service destruction and target disconnection clear the transient feedback. Android versions below36 skip native Live Alerts. The persistent foreground-controller notification remains separate and is not promoted into an always-on capsule.

The old feasibility probe is optional and can be removed with `adb uninstall com.nish.ancnativeprobe`; it is not used by this integration. Its uninstall does not affect controller grants. Native promotion behavior was tested on this firmware, not guaranteed across all OxygenOS releases.

## 6. Exempt both apps from battery optimization

We added battery exemptions for **both** the controller and Shizuku. With USB still authorized, run:

```powershell
adb shell cmd deviceidle whitelist +dev.local.anc
adb shell cmd deviceidle whitelist +moe.shizuku.privileged.api
```

Alternatively, use each app's App info/battery settings to allow background activity and remove optimization if those options are exposed. OEM menu wording varies; a generic background switch is not proof that the Android exemption exists. You can inspect `adb shell cmd deviceidle whitelist` locally for the two package names. Do not publish the rest of that app list.

If your OxygenOS version exposes a separate app auto-launch allowance and background starts are blocked, allow these two apps there. We did not verify a separate auto-launch switch change on the original phone; do not claim it is part of the measured configuration. Do not force-stop either app for routine battery cleanup.

## 7. Configure Shizuku restart after reboot

This is distinct from the controller's own boot receiver. Its service cannot capture this key until privileged Shizuku is also running.

1. Keep the manager version requirement in mind. The tested Shizuku `13.6.0.r1086.2650830c` contains an Android 13+ ADB boot path, confirmed by inspecting the installed APK and [upstream receiver source](https://github.com/RikkaApps/Shizuku/blob/b844bc491f1790c72328e1a8e5b2349f8978f0ea/manager/src/main/java/moe/shizuku/manager/receiver/BootCompleteReceiver.kt). Older generic documentation describes manual restarts; do not assume every Shizuku version supports this path.
2. Shizuku must have **WRITE_SECURE_SETTINGS**. It was **already granted** on the original phone. For a fresh install, inspect the permission locally:

   ```powershell
   adb shell dumpsys package moe.shizuku.privileged.api | Select-String 'android.permission.WRITE_SECURE_SETTINGS: granted='
   ```

   If not granted, grant it through the authorized PC after completing the OEM restriction step:

   ```powershell
   adb shell pm grant moe.shizuku.privileged.api android.permission.WRITE_SECURE_SETTINGS
   ```

   Repeat the targeted check and confirm `granted=true`. If denied, resolve the OEM restriction and ADB authorization; do not silently continue claiming automatic startup is configured. This permission lets the manager modify protected system settings.
3. Open Shizuku's home screen after a successful wireless **Start**, so its saved last launch mode is **ADB**. Keep its own wireless pairing intact, its battery exemption enabled, and Wi-Fi/Developer options/debugging on. No separate root startup toggle is required for the inspected ADB boot path.
4. Leave Plus Key ANC enabled by tapping **START**, not STOP. Its private enabled preference and BOOT_COMPLETED receiver restart its foreground service. The manifest receiver is already enabled; no `pm enable` command is required. Attempts to change component state through shell were rejected on this firmware and were unnecessary.
5. Know the boot behavior: Shizuku sets `adb_wifi_enabled=1`, `adb_enabled=1`, and `adb_allowed_connection_time=0`. It enables both debugging transports and disables authorization expiry. These effects were approved for the original setup. You do not need to write these settings manually to reproduce the tested path; its receiver does so. If your firmware exposes **Disable adb authorization timeout**, non-expiring authorization corresponds to that behavior. [UNINSTALL.md](UNINSTALL.md) restores the absent/default timeout setting and removes trust.

The receiver's service-discovery window is about three seconds, so Wi-Fi startup timing can cause failure. One successful reboot on this phone is evidence for this configuration, not a guarantee for every network/firmware. If it fails, use Shizuku's on-phone Wireless debugging Start, then START the controller, and investigate before claiming automatic recovery.

## 8. Verify normal operation, then reboot without the PC

1. Wear both earbuds, close the native **Earbud functions/HeyMelody** page and keep them connected. That page can compete for the vendor control connection. The app does not automatically force-stop it.
2. Use **HIGH** in the controller as a known starting point. Press the physical Plus Key once at a time: **Medium → Low → High**.
3. Double-press: **Auto**. Hold for about one second and release: **Transparency**, then hold again: **Off**, then again: **Transparency**. Keep gestures roughly two seconds apart for the first test.
4. Lock the phone/turn its screen off and repeat single, double and hold. All worked on the original phone.
5. Play music, change mode using the earbuds' own squeeze control, then use the Plus Key single/double. Confirm it uses the actual externally changed mode and music continues. This passed on the original setup.
6. Reboot. **Unplug USB**, unlock once, keep Wi-Fi on, reconnect/wear both earbuds and wait about 30 seconds. Do **not** open Shizuku or Plus Key ANC or run a PC starter. Try single, double and hold. This exact owner test passed.
7. Optionally reconnect USB afterward for diagnostics. Check the controller foreground service, `shizuku_server`, and exactly one `dev.local.anc:pluskey` process. Use targeted `adb logcat -d -s BudsAncLab:I` for capture/query errors; do not export general phone logs.

Defaults are 220 ms after release for the double window, 600 ms for hold recognition and 25 ms debounce. A single must wait for the double window. Use **TIMING** if your double presses need a longer window. Full transition rules and honest outstanding acceptance tests are in [README.md](README.md) and [VALIDATION.md](VALIDATION.md).

## Troubleshooting and things we did not need

| Symptom | What fixed it / next check |
|---|---|
| `adb devices` empty / unauthorized | Enable USB debugging, unlock, accept the PC prompt; check data cable/driver. |
| Android runtime grants rejected from shell | Use controller permission dialogs/App info. |
| Shizuku says ADB permission limited | Enable Disable system optimisation as above, then restart Shizuku. |
| Shizuku approval succeeds but helper never binds | Refresh the already-running server after the OEM switch change. |
| Wireless Start reports unknown certificate | Pair **Shizuku's own key**, then wireless Start; USB trust alone is insufficient. |
| Pairing notification missing/expired | Allow Shizuku notifications/background activity; restart its Pairing flow and use a fresh code dialog. |
| Mode commands rejected with `0x0E` | Wear both earbuds and query again. Do not treat a socket write as confirmation. |
| Socket closes while native earbud settings are open | Close Earbud functions/HeyMelody, then START again. We force-stopped `com.oplus.melody` once during diagnosis; this is not a required permanent configuration change. |
| First START says A2DP proxy not ready | Wait for the profile callback/earbud connection and retry START. |
| Translate opens instead of ANC changes | Check Shizuku, controller START, permissions, CAPTURE ON and the active paired Buds connection. |
| Works until reboot | Recheck own-key pairing, manager version, secure-settings grant, saved ADB launch mode, battery exemptions and Wi-Fi boot timing. |
| Works after manual start only | That is a fallback, not a passing autonomous reboot test. |
| Signature mismatch installing a source build | Preserve the original private signing key for compatible updates; do not uninstall without deciding to lose saved app settings/grants. |

We tried a temporary accessibility key probe and the OEM key-observer API. Accessibility did not deliver this Plus Key and the OEM API required a signature-only permission. Those probes were removed. No accessibility, overlay, general log-reading or root permission needs enabling. No abandoned helper needs installing. Existing Tasker/Nova accessibility services were preserved. No screenshot deletion, audio-profile disconnection, earbud reset or Bluetooth re-pairing was used.

For source builds, implementation details and AI-agent recovery, see [RECOVERY.md](RECOVERY.md), [AGENTS.md](AGENTS.md), [RESEARCH.md](RESEARCH.md), and the project-local skills. For complete removal, follow [UNINSTALL.md](UNINSTALL.md) in order so that Shizuku cannot re-enable debugging after you turn it off.
