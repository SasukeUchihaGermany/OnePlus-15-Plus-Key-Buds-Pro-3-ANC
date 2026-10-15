# Native Fluid Cloud / Live Alerts feasibility probe

**Device-tested 15 October 2026. Native capsule works on the connected OnePlus15 CPH2747 / OxygenOS CPH2747_16.0.9.400(EX01), Android16 API36, sdk_full36.0.** This is a small isolated Java test app, not the planned Kotlin/Lottie implementation and not connected to actual Bluetooth state. Values are simulated. The user explicitly authorized this native-notification experiment after the earlier implementation pause. The existing controller, helper, Shizuku and phone ANC settings were left unchanged.

## Observed results

- Installed package `com.nish.ancnativeprobe`, label **ANC Native Alert Test**, target/minAPI36. Build aligned, signed and signature verified. Permissions: POST_NOTIFICATIONS and POST_PROMOTED_NOTIFICATIONS only. No overlay, accessibility, Shizuku, network, Bluetooth or foreground-service permission. No background service or boot receiver.
- Granted this test app's notification permission through authorized ADB. Its initial notification had `eligible=true`, `canPromote=false`, `flags=0x2`, `promoted=false`.
- Opened the test app's standard notification settings. OxygenOS exposes **Show Live Updates on Live Alerts**, initially OFF. Enabled this switch for the test app only. Posting then yielded `eligible=true`, `canPromote=true`, `flags=0x40002`, `promoted=true`.
- Screenshot inspection confirmed the real black native camera-area capsule with the test icon and **High**. Medium and then Auto were also displayed; Medium→Auto updated the existing notification/capsule without creating a second active notification.
- A long critical-text test with **Nish's ANC Engine: Low** produced a wider capsule, but only part of the string was visible at the sampled frame. No guarantee of the full sentence remaining readable in the compact representation. The full **Nish's ANC Engine: Medium** title and body were independently found in the native notification shade UI. Do not describe tapping/holding as a verified expandable card interaction: that test opened the configured Activity instead.
- A notification requested with `ttlMs=1500` was active/promoted at the probe's1-second status check, absent from active notifications at a later check after3seconds, and the capsule was gone in a screenshot. This confirms automatic removal, **not** a measured exact1500ms visual lifetime; SystemUI animation/scheduling remains outside the app's control.
- Existing dev.local.anc foreground process, its single pluskey helper and shizuku_server were still running after the experiment. No real ANC mode command was sent by this probe.
- Test notification cleared after checks. Test app and its per-app Live Alerts permission remain installed/enabled for further trials. No full private phone screenshot is committed; only a tight camera-capsule crop is retained.

## Reproduce

Build on Windows using existing SDK36/build-tools36.1.0 and Android Studio JBR, or pass -Sdk/-Jdk:

```powershell
.\build.ps1
adb install -r .\AncNativeProbe.apk
adb shell am start -n com.nish.ancnativeprobe/.MainActivity
```

Use **ALLOW NOTIFICATIONS**, or grant this app through Android App info. Tap **LIVE ALERT SETTINGS** to open the standard app notification settings, then enable **Show Live Updates on Live Alerts**. Return home or to another app before triggering.

```powershell
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es mode High
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es mode Medium
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es mode Auto
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es mode Low --ez big true
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es mode High --ei ttlMs 1500
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es command STATUS
adb logcat -d -s AncNativeProbe:I
adb shell am broadcast -n com.nish.ancnativeprobe/.ProbeReceiver --es command CLEAR
```

The receiver requires DUMP, making these shell diagnostics rather than a Tasker integration interface. Normal expiry defaults to60seconds; ttlMs is clamped to1000–60000. `big=true` tries both BigTextStyle and the long critical string; ordinary mode uses standard style with a short critical value. Only the six enumerated display modes are accepted. This does not establish native behavior for every possible state or locked/reboot scenario.

## API detail and limitations

On the installed SDK36 stubs the public `setRequestPromotedOngoing` builder and EXTRA_REQUEST_PROMOTED_ONGOING constant are absent; current official documentation marks them36.1. The documented extra value `android.requestPromotedOngoing=true` was added directly to the notification bundle, alongside the public API36 short critical text. This firmware accepted it and reported promotion. No hidden OEM API, root, navigation/call/media impersonation or whitelist bypass was used.

The originally attempted literal promoted-settings intent did not resolve on this build. The probe now opens `Settings.ACTION_APP_NOTIFICATION_SETTINGS` with Settings.EXTRA_APP_PACKAGE instead; that page contains the verified OxygenOS switch. Do not copy the failed literal action as a working setup instruction.

Native UI controls the geometry, typography, animation, text fitting, coexistence and visual lifetime. ANC change alerts are not a typical ongoing journey in Android's Live Updates guidance; technical promotion here does not imply general OEM compatibility or a Play Store approval. User dismissal/promotion settings must remain respected. These results prove feasibility on this phone, not production reliability across updates.

Recommended eventual design: show **High / Medium / Low / Auto / Off / Transparent** (or shorter label when needed) in the capsule, and full **Nish's ANC Engine: STATE** in notification title. Reuse the existing controller's confirmed-mode result if later integration is requested; no need for a second overlay host solely for native notifications. Update confirmed state only, no fake requested-mode success.

## Remove just the experiment

Tap CLEAR, or use its CLEAR broadcast, then:

```powershell
adb uninstall com.nish.ancnativeprobe
```

This removes the probe's app grants/channel/data. It does not uninstall or change Plus Key ANC or Shizuku. If keeping the app but disabling its capsule, turn **Show Live Updates on Live Alerts** OFF in this test app's notification settings.

Sources: [Android Live Updates](https://developer.android.com/develop/ui/views/notifications/live-update), [promotion extra/version](https://developer.android.com/reference/android/app/Notification#EXTRA_REQUEST_PROMOTED_ONGOING), [short critical text](https://developer.android.com/reference/android/app/Notification.Builder#setShortCriticalText(java.lang.String)), [OnePlus Fluid Cloud](https://www.oneplus.com/uk/oxygenos16).
