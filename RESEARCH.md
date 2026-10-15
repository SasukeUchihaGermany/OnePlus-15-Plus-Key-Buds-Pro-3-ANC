# Research and evidence

Device-tested findings are in PROGRESS.md. Findings here are from source review unless explicitly stated otherwise.

## Primary protocol sources

- [Python source](https://github.com/nic0manz/oneplus_buds3_pro_python/blob/faec7e5fe66acba7b89a907610ba1d0c86bdbba9/op_buds3_pro.py), read in full. Windows AF_BLUETOOTH transport cannot be ported directly to Android. It sends HELLO and REGISTER, supports a persistent socket, and scans received chunks for status markers. Its fallback `Done: label` does not prove a mode change. Level commands and listening-mode commands are separate packets. Its receiver is not a complete stream parser and it provides no ANC getter.
- [C# source](https://github.com/nic0manz/oneplus_buds3_pro_dotnet/blob/ecb5ab8ae502f8993c3378cc81dd817791dc9b18/OnePlusBudsPro3/BudsConnection.cs) adds an always-reading thread, AA/length stream reassembly, mode notifications, and `RequestAncNow`. Its `MainWindow.xaml.cs` disables ANC controls when wear detection says neither bud is in an ear. Some comments are stale: a comment says no usable getter, but code implements function 0x010C. Treat source comments as hypotheses until verified.

The queried current-mode frame was tested on this phone and decoded OFF. Query: `AA 09 00 00 0C 01 00 02 00 01 01`. Reply: `AA 0C 00 00 0C 81 00 05 00 00 01 01 08 00`.

Selected-mode masks: OFF=0x0008; TRANSPARENCY=0x0100; HIGH=0x0010; MEDIUM=0x0020; LOW=0x0040; AUTO=0x0080. Source notifications use cmd 0x0204 and payload `03 01 01 <mask LE>`. Field 04 describes Auto's instantaneous strength; it must never replace the selected AUTO mode. A current-mode reply has status byte 00 and field 01. Unknown/malformed frames are not current-state confirmations.

Reference handshake bytes:
```
HELLO    AA 07 00 00 00 01 23 00 00 12
REGISTER AA 0C 00 00 00 85 41 05 00 00 B5 50 A0 69
```
HELLO has an unusual extra trailing byte relative to the length field. Preserve reference wire bytes during the proof; do not apply a generic builder silently. Normal received frames have total length = length byte + 2, 9-byte header and little-endian payload length.

Mode sets: `AA 0A 00 00 04 04 42 03 00 01 01 <mode>` where OFF=01, ANC=02, transparency=04. Strength sets: `AA 0B 00 00 04 04 42 04 00 01 01 <mask low byte> 00`. Strength alone was subsequently verified to activate ANC from OFF and Transparency; see VALIDATION.md. Do not send speculative commands outside ANC functionality.

## Android transport reference

[OppoPods socket factory](https://github.com/Leaf-lsgtky/OppoPods/blob/main/app/src/main/java/moe/chenxy/oppopods/pods/OppoRfcommSocketFactory.kt) implements public UUID-based RFCOMM, tries 00001107-D102-11E1-9B23-00025B00A5A5 and 0000079A-D102-11E1-9B23-00025B00A5A5, and provides reflection-based channel 15 as an alternative. Its `Packets.kt` implements framing and subscription 0x0205. Source reviewed, APK not installed; its broader Xposed/HyperOS design is unnecessary here.

Our device advertises 079A, and the Android diagnostic app successfully connected through that public UUID, resolving to channel 15. No Windows Bluetooth socket is used and no earbuds were re-paired.

## Plus Key remapper review

[Source](https://github.com/SelenStudio/plus-key-remapper/tree/1842a11ba57d7c20c366c2ceffda57ef61ad636e), particularly `LogcatWatcher.java`, `DetectorService.java`, `PlusKeyService.java`, manifest, boot receiver and accessibility configuration. README advertises three gestures, but the active watcher synthesizes release after silence (50ms single-only / 600ms dual), with an 850ms long-press timing cap. This is not explicit physical UP. The internal key code 9999 is synthetic. The active watcher reads broad logcat and also tracks foreground apps. DetectorService acquires a partial wakelock without a timeout. Permission scope includes logs, overlays, camera, package visibility, accessibility window contents, usage stats, DND and battery exemption. This exceeds the scope needed for an ANC-only helper.

Its boot receiver checks BOOT_COMPLETED and READ_LOGS; a manifest claim of direct boot does not prove locked-boot behavior. The code inspected does not establish conditional native-action interception. It is unsuitable for blind reuse as the final solution. No upstream binaries installed or executed.

## Tasker discussions

- [OnePlus Logcat discussion](https://www.reddit.com/r/oneplus/comments/1p6h5qh/) reports KEYLOG_OplusKeyEventUtil twice per press and unsuccessful accessibility detection. This is a report about other builds, not evidence of edge fidelity on this phone. Alternating a variable per record would fail if records are duplicated/missed.
- [Earlier remapping discussion](https://www.reddit.com/r/oneplus/comments/1oxafoh/) links a screenshot-triggered Tasker workaround that dismisses UI and deletes screenshots. This changes native behavior and is unsuitable for the required conditional preservation. No screenshots deleted and no Taskernet project imported.
- [Tasker follow-up](https://www.reddit.com/r/tasker/comments/1saejll/) links [XDA](https://xdaforums.com/t/how-to-customize-plus-key-using-tasker-logcat.4769540/). XDA access returned an error; it has not been read successfully. Forum timing suggestions do not establish reliable gestures.

## Official documentation reviewed

- [BluetoothDevice](https://developer.android.com/reference/android/bluetooth/BluetoothDevice): public RFCOMM API uses service UUID and SDP. Keep blocking connection/read off the UI thread; close sockets to cancel operations.
- [Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions): Android 12+ runtime BLUETOOTH_CONNECT applies to paired-device communication. Scanning is unnecessary for our verified paired active target.
- [Background foreground-service starts](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start) and [connectedDevice service type](https://developer.android.com/develop/background-work/services/fgs/service-types): correct service type and permission do not grant unrestricted background starts. Initial POC starts from visible activity; no boot guarantee made.
- [Accessibility key filtering](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#onKeyEvent(android.view.KeyEvent)): consumption requires events to be delivered to the service. Down/up handling must remain consistent. OEM system interception may precede accessible dispatch; test physical key behavior.
- [Shizuku setup](https://shizuku.rikka.app/guide/setup/): wireless-debugging setup and OEM background restrictions documented. No running Shizuku server was observed in the targeted process check. Availability after reboot must be tested, never inferred from installed package.
- [Tasker Logcat event](https://tasker.joaoapps.com/userguide/en/help/eh_logcat_entry.html) and [user guide](https://tasker.joaoapps.com/userguide_summary.html): log access is permission-dependent. A log trigger observes activity; it does not suppress native input.

## Architecture decision

Final choice: native Java Android app plus Shizuku's supported Binder UserService. The shell service can read and exclusively grab the verified dedicated evdev key without root. Abstract Unix-socket IPC failed and was removed. The UserService authenticates the app UID; the app authenticates shell UID on callbacks. No external network or general command execution interface is exposed.

The owner confirmed the full eight-gesture ANC sequence and faster locked/screen-off operation. Native key priority was explicitly relaxed by the user, although conditional release on target disconnect is implemented. Event-driven poll/read threads replace logcat heuristics; Tasker and accessibility are unnecessary. Shizuku is the privilege bridge. Its installed manager's Android 13+ wireless boot path was configured and passed an unplugged physical reboot test; see RECOVERY.md for the pinned source, required permissions and debugging effects. Manual wireless start remains a fallback.

Shizuku API 13.1.5 official api/provider/shared/aidl artifacts are bundled, with MIT license. [Official source and guide](https://github.com/RikkaApps/Shizuku-API) describe UserService and non-root reboot limitations. Source demo and bind/death APIs reviewed before use.

On this OS the permission-monitoring control was renamed Disable system optimisation. The user approved the change; UI was changed and property persist.sys.permission.enable=false verified. The [official Shizuku issue](https://github.com/RikkaApps/Shizuku/issues/2149) records the rename. This broader OEM control has power/security/display effects and is documented separately from app-level battery exemption.
