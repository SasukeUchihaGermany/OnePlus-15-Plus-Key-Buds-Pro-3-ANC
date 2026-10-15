# Progress — 15 October 2026

## Current phase
Phase 6: integrated physical validation. All six Bluetooth modes confirmed through fresh query while earbuds worn. Shizuku Binder input capture active; physical gesture acceptance sequence requested.

## Verified device findings
- Model CPH2747, OnePlus 15, Android 16 / API 36.
- Build CPH2747_16.0.9.400(EX01); ro.build.version.oplusrom=V16.1.0.
- OTA CPH2747_11.A.43_0430_202607012229.
- Tasker 6.6.20; Shizuku 13.6.0.r1086.2650830c; com.oplus.melody reports version names 17.6.6 and 16.7.0 in package dump (multiple package records; do not assume one without resolving records).
- Earbuds identified by exact paired name and active A2DP profile; advertised vendor UUID 0000079a-d102-11e1-9b23-00025b00a5a5.
- Public secure RFCOMM UUID API connected after initial connection failure; Bluetooth manager reports channel 15. No hidden API necessary for this connection.
- Current-state query `aa0900000c010002000101` returned `aa0c00000c810005000001010800` (OFF), 10:54:32 device log time.
- Initial HIGH commands got replies `aa08000004844201000e`, then query still OFF. This is a rejection, not a successful set. Upstream C# UI documents wear detection gating. User asked to wear both earbuds; verification pending.
- Shell can read input device event0 (gpio-keys), dedicated BTN_TRIGGER_HAPPY32. Not yet physically correlated to Plus Key. No key interception or native suppression demonstrated.
- No KEYLOG marker in recent filtered buffer at first inspection; this does not establish absence during a real key press.
- OxygenOS rejected shell pm grant; user granted Bluetooth and notifications using app UI. ADB input taps now invoke app buttons. No system permissions relaxed.

## Implemented/deployed
Native Java diagnostic app dev.local.anc (Buds ANC Lab), target SDK 36. Foreground service connectedDevice, single serialized worker, one socket, bounded connect/query timeouts, stream reassembly, exact reference handshake, candidate state query, separate observed replies and requested states. Paired active A2DP identity checked before commands. No network permission.

Installed local APK; runtime permissions granted by user. Native Plus Key settings unchanged. No remapper, Tasker profiles or root modifications installed.

## Validation
- APK compiled, aligned, signed and signature verified.
- Real phone: installation and runtime grants verified; RFCOMM and OFF query verified.
- Real phone: HIGH attempt rejected (status 0x0E); no mode-control pass yet.
- T01–T20 remain unperformed as integrated acceptance tests. Do not infer passes from protocol unit tests or compilation.

## Known issues / next actions
1. Repeat mode control while worn; verify all six modes through query plus OxygenOS controls.
2. Check whether level alone activates ANC and whether queries follow external changes.
3. Physically correlate event0, compare Logcat, test feasible conditional suppression.
4. Select final input architecture from evidence; implement gestures and reconnection only after proofs.
5. Validate screen-off, disconnected default, reboot, music, failure recovery and battery behavior.

Initial permission-denied service starts crashed before Bluetooth permission was granted; diagnostic use must wait for runtime permission.

## Integration update (11:15 device time)
- HIGH, MEDIUM, LOW, AUTO, OFF, TRANSPARENCY each produced matching current-mode query replies at 10:58:43–10:59:20 after earbuds were worn. Status 0x0E occurred before wearing; upstream UI wear restriction is consistent with this experiment.
- Physical event0 edges correlate to Plus Key. Linux code 735 / BTN_TRIGGER_HAPPY32. Real down/up durations include ~144 ms tap, ~1.18s hold and double taps ~250ms apart. KEYLOG marker is present but duplicates identical lines; no explicit action field. User reports native single opens Translate.
- User explicitly relaxed native preservation priority: “feel free to ignore native function for now.” Implementation nevertheless conditionally grabs only this dedicated key while the verified target is A2DP-connected.
- Shell capability probe accepted EVIOCGRAB and released immediately by close. First abstract Unix socket bridge failed; it was removed from active architecture. Temporary shell bridge process was killed. Final transport uses supported Shizuku Binder UserService, verifying calling app UID and callback shell UID.
- Shizuku authorization initially auto-denied due OEM limited ADB permissions. User explicitly approved changing the OEM permission-monitoring control. On this build the UI label is **Disable system optimisation**. Changed OFF→ON through UI; confirmed persist.sys.permission.enable true→false. Native warning mentioned increased power, affected system features, reduced protection and display changes. No other developer setting changed. A direct setprop attempt was denied and had no effect.
- Started installed Shizuku through its own libshizuku.so starter. After the approved OEM change, restarted Shizuku and its UserService successfully bound. `CAPTURE ON (Shizuku)` recorded at 11:15:16.204.
- Fresh queries now use incrementing sequence bytes; matching sequence echoes verified on phone. This prevents delayed replies from a previous query satisfying the next one.
- Service uses START_STICKY; BOOT_COMPLETED receiver starts app service if previously enabled. This **does not start Shizuku after reboot**. On-device wireless-debugging restart of Shizuku is required after a phone reboot.
- Host tests: 65 assertions passed, including all 21 transitions, split-frame parsing, malformed input, AUTO selected vs adaptive strength, duplicate edges, single/double/long exclusion and disconnect cancellation.
- APK installed and Bluetooth/notification/Shizuku permissions granted. No Internet, accessibility, READ_LOGS or overlay permission in the final app. Limited per-gesture wakelock (6 seconds maximum), blocking input poll and Bluetooth reader; no continuous status polling.

Next: physical sequence; screen-off/locked tests; disconnect/reconnect; native UI cross-check; process recovery; independent operation and final evidence.

## Recovery update
Earlier entries are chronological and superseded where noted here. Owner confirmed locked/off gestures, external squeeze state synchronization and uninterrupted music. Defaults are now 220ms double and 600ms hold; 73 host assertions pass. Shizuku helper destruction now uses the library transaction constant; STOP was verified to exit its helper, restart creates one helper, and app process recovery returned to capture. VersionCode 2 / version 1.1 is installed.

Debugging-free alternatives tested: accessibility service did not receive Plus Key (native Translate still appeared); OEM observer API rejected missing signature-only OPLUS_COMPONENT_SAFE. No verified ordinary-app replacement for Shizuku shell key capture was found. Owner authorized the wireless boot path if needed. Installed Shizuku manager's boot receiver supports Android 13+ ADB startup, not just root: existing WRITE_SECURE_SETTINGS, ADB last launch mode and own wireless pairing are prerequisites. Own-key pairing completed, and the on-phone Wireless debugging Start succeeded with capture rebind. Manager battery exemption added. No credentials/codes are retained in deliverables.

Real reboot initiated for USB-unplugged physical verification; result pending. No manual shell starter should be invoked before gathering that result. See RECOVERY.md for reproducible setup and source reference. Build and tests now use repository-local ignored .build/ and .local/ directories, so a fresh Git checkout does not depend on this chat's folder layout. Git push remains gated on reboot resolution and owner's explicit "push to git" instruction; use personal SSH identity only.

## Reboot result — passed
Owner confirmed "yup, worked after reboot" following the requested USB-unplugged single/double/hold test with no app opening. USB reconnected afterward. No PC starter was issued after reboot. sys.boot_completed=1; Shizuku server, app foreground service and one pluskey helper were running. Package stopped=false; service createdFromFg=false, startRequested=true. Manager boot settings adb_wifi_enabled=1 and adb_allowed_connection_time=0 were observed. Targeted logcat returned no retained records at inspection, so do not claim unavailable gesture log timestamps. Physical confirmation plus process/service observations support this reboot result. Automatic recovery depends on Shizuku pairing/permissions and Wi-Fi/debugging, not a debugging-free architecture.

Final v1.2 / code 3 updates explanatory help only; no gesture or startup behavior changes. Source recovery guide and portable builds ready. Git has not been pushed; explicit owner confirmation remains required.

## Publication preparation
Owner explicitly requested GitHub upload after confirming reboot success. Personal SSH key authenticated as the repository owner. Added UNINSTALL.md for full controller/Shizuku permission removal, battery exemptions, wireless/USB trust revocation, authorization-expiry restoration, OEM protection restoration and final reboot. This guide is documentation only; the working phone has not been uninstalled or reconfigured. Existing remote initial README commit is retained as the parent. Private signing key and build caches remain ignored. Host tests re-run: 73 assertions passed.

## Shared setup documentation and repository rename
Owner requested a shareable descriptive repository name and every setup step. Renamed the GitHub repository to OnePlus-15-Plus-Key-Buds-Pro-3-ANC using the authenticated personal account and updated the local origin. Added SETUP.md covering prerequisites, Developer options, USB authorization, OEM Disable system optimisation and server restart, Shizuku notification pairing, runtime and Shizuku app grants, both battery exemptions, secure-settings/boot prerequisites, validation, fallback startup and observed failure remedies. Required setup is distinguished from temporary removed probes and settings that were already granted or not separately changed. No phone configuration or app code changed for this documentation update.

## Native capsule integration — v1.3
User selected short state text and the existing speaker icon, then explicitly authorized integrating it with ANC key triggers. Added NativeAlert directly to dev.local.anc: post only after set() query confirms desired mode, main-thread best-effort notification, separate quiet channel/id, requested1500ms expiry, no new service or overlay. Six display modes supported. Regular QUERY/CONNECT and unsolicited external changes do not post. STOP/service destruction and disconnect clear feedback. New normal POST_PROMOTED_NOTIFICATIONS declaration; correct Plus Key ANC Live Alerts switch enabled through OEM notification settings. Existing test-app permission is not reused. OEM Settings task reuse initially showed the probe's page; a fresh settings task resolved the correct package and the UI shortcut now does the same.

Built/signed/installed versionCode4/version1.3 with existing signing key.73host assertions pass. Confirmed real modes HIGH/MEDIUM before visual permission (visual skipped safely), then LOW and AUTO with visual dispatch; screenshot proves native Auto from controller. Physical LONG→OFF and SINGLE OFF→HIGH later recorded query confirmation and matching visual dispatch. Owner visual feedback requested. Controller and one input helper remain running; no gesture/protocol/boot-architecture changes. Removed no phone app or existing permissions. Documentation updated; Lottie remains unimplemented.

## Combined app accepted and publication authorized
Owner confirmed the integrated feature works well and explicitly requested GitHub push as one combined app. Root Plus Key ANC v1.3 contains both ANC key control and native capsule feedback; no external trigger/probe process is used. Uninstalled com.nish.ancnativeprobe successfully, verified it is absent and dev.local.anc v1.3/controller/helper remain present. Removed the obsolete probe APK from current branch contents, retained only historical experiment source/docs. Updated setup/removal docs and current APK checksum. Publishing to Animation-implementation; main remains unchanged.
