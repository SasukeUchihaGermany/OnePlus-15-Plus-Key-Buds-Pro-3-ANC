# Validation record

## Native Live Alerts integration (v1.3)

The controller now posts a native speaker/mode capsule only from set() after a desired/actual query match, dispatched on the main thread so notification failures cannot fail the Bluetooth command. Root gesture and protocol host tests still pass73assertions. Rebuilt, aligned, signed, verified and installed versionCode4/version1.3 with the original private signing key.

Real diagnostic mode commands confirmed HIGH, MEDIUM, LOW and AUTO. Before enabling the correct controller's Live Alerts permission, ANC still succeeded and the visual path reported unavailable. Enabling **Show Live Updates on Live Alerts** for **Plus Key ANC** then produced LIVE ALERT LOW and LIVE ALERT AUTO. An integrated-device screenshot confirmed the native speaker/**Auto** capsule after the real AUTO query confirmation at12:25:57.445, notification dispatch at12:25:57.451. The earlier feasibility probe separately verified automatic timeout/removal. The integration requests1500ms but exact visual presentation is system-controlled.

Physical input logs on the final installed build recorded LONG: TRANSPARENCY→OFF at12:27:31.222, followed by LIVE ALERT OFF at12:27:31.230; SINGLE: OFF→HIGH at12:27:34.614, followed by LIVE ALERT HIGH at12:27:34.621. Thus the previously dedicated OFF+single trial is now device-observed too. The owner subsequently confirmed the integrated feature works well. Physical SINGLE/LONG are independently evidenced above; no separate timestamped integrated DOUBLE screenshot is claimed. No claim yet of a repeated reboot test for v1.3 or external-squeeze visual integration (external-only changes intentionally do not trigger visuals). Existing Shizuku startup and key detection logic are unchanged.

Host tests: 73 assertions passed. APK compiled, aligned, signed, signature-verified and installed. These results alone are not physical acceptance.

## Bluetooth proof on the phone

All six modes confirmed by current-state replies while worn, 10:58–10:59 device log time. Exact initial OFF reply: `aa0c00000c810005000001010800`. HIGH, MEDIUM, LOW, AUTO, TRANSPARENCY masks subsequently matched the source map. Before wearing, mode commands returned status 0x0E and current state remained OFF; these failed trials were not counted as passes.

Strength-only HIGH was confirmed to activate ANC from OFF (11:18:29) and TRANSPARENCY (11:18:31). Public UUID connection resolved to channel 15; no reflection used for Bluetooth transport. Incrementing query sequences were echoed by replies (e.g. request/reply sequence 01 at 11:14:33).

## Physical sequence

Owner reported “buttons tests work” following the eight-gesture test. Targeted logs independently recorded:

| Device time | Physical gesture | Queried before | Confirmed after |
|---|---|---|---|
| 11:16:49.925 | Single | HIGH | MEDIUM |
| 11:16:51.726 | Single | MEDIUM | LOW |
| 11:16:56.573 | Single | LOW | HIGH |
| 11:17:02.329 | Double | HIGH | AUTO |
| 11:17:10.010 | Hold | AUTO | TRANSPARENCY |
| 11:17:14.765 | Hold | TRANSPARENCY | OFF |
| 11:17:19.467 | Hold | OFF | TRANSPARENCY |
| 11:17:24.092 | Single | TRANSPARENCY | HIGH |

Owner subsequently confirmed the faster build works locked/screen-off and feels faster. ADB observed `mWakefulness=Dozing` and keyguard `showing=true`. Device logs recorded additional physical single/hold/double transitions at 11:21 while in this test period. Later single at 11:25:25.251 reached confirmed HIGH at 11:25:25.473 (222ms after recognition, excluding tap duration and the 220ms recognition window).

## Acceptance tracking

| Test | Status |
|---|---|
| T01 HIGH + single → MEDIUM | Passed physical + query |
| T02 MEDIUM + single → LOW | Passed physical + query |
| T03 LOW + single → HIGH | Passed physical + query |
| T04 OFF + single → HIGH | Passed physical capture + query on v1.3 |
| T05 TRANSPARENCY + single → HIGH | Passed physical + query |
| T06 AUTO + single → HIGH | Host logic passed; dedicated physical trial outstanding |
| T07 double → AUTO | Passed physical from HIGH; all initial states host-tested |
| T08 HIGH + hold → TRANSPARENCY | Host logic passed; dedicated HIGH trial outstanding; physical MEDIUM hold passed |
| T09 AUTO + hold → TRANSPARENCY | Passed physical + query |
| T10 TRANSPARENCY + hold → OFF | Passed physical + query |
| T11 OFF + hold → TRANSPARENCY | Passed physical + query |
| T12 Disconnected native key | Pending; user relaxed original requirement during work |
| T13 Reconnect restores control | Pending complete disconnect/reconnect physical trial |
| T14 Locked gestures | Passed owner report and device observations |
| T15 Screen-off gestures | Passed owner report and device observations |
| T16 External changes used by next press | Passed owner report: external squeeze then single/double used actual state |
| T17 Rapid presses, no duplicates | Double recognized without extra single; host duplicate/debounce tests passed; broader stress trial outstanding |
| T18 Communication failure recovery | Initial failures reported honestly; successful later reconnection; full fault-injection trial outstanding |
| T19 Reboot | Passed: owner confirmed gestures after reboot with USB unplugged; automatically running Shizuku/controller/helper independently observed |
| T20 Music unaffected | Passed owner report: music continued through squeeze and key changes |

## Power and persistence

App and Shizuku battery exemptions applied. No sustained wakelock: gesture acquisition expires after six seconds; input and Bluetooth readers block. One CPU snapshot showed 0.1% for an earlier app process; this is not a battery benchmark. The user-approved OEM optimisation switch has wider power/system effects. One real unplugged reboot recovery passed on this configuration. Shizuku, foreground controller and one helper were running without any PC start command. adb_wifi_enabled=1 and adb_allowed_connection_time=0 matched the manager boot path. This requires configured Shizuku, Wi-Fi and debugging; boot Wi-Fi timing and future firmware remain limitations.
