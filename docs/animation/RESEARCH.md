# Animation extension: research and decisions

Research date: 15 October 2026. **No implementation authorized yet.** This report supersedes provisional technical points in PLAN.md; user decisions below remain pending. No app scaffolding, animation generation, dependencies installed, APK build/deployment or phone settings changed. `adb devices` returned no connected device this turn. Current refresh modes, density/insets and OEM behavior cannot be measured until connected again; these are implementation validation tasks, not facts to invent from model specifications.

## Recommended architecture

Keep the animation in a separate Kotlin/View app `com.nish.ancengine`, with its own future Gradle project under animation-engine/. Preserve the root Java controller/build and its key capture. Use a user-enabled `specialUse` foreground service as the overlay host, with an honest manifest subtype explanation and quiet ongoing notification/Stop action. Cache one local Lottie composition, keep no overlay view attached while idle, and forward validated external broadcasts to the running host through an in-process interface. A visible setup Activity handles initial overlay approval and host enablement. No hidden Activity launch per event.

This is an engineering recommendation, not a tested app or a guarantee of Play Store approval. It fits the requested sideloaded custom indicator more closely than connectedDevice, mediaPlayback or an indefinitely extended shortService. Official specialUse requirements are `FOREGROUND_SERVICE_SPECIAL_USE` plus the service subtype property; its justification is reviewed for Play submissions. [Android foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types).

Allow the user to disable the animation host independently of ANC control. The new app needs normal overlay approval (`SYSTEM_ALERT_WINDOW` / Settings.canDrawOverlays), foreground-service declarations, notification handling, and optionally boot receipt if automatic restart is chosen. It needs no new Bluetooth, Shizuku, accessibility, READ_LOGS, Internet, media projection, exact alarm or root access. Diagnostic screen recording/tracing can be PC-driven during testing; do not add capture permission to the product. [Android overlay permission API](https://developer.android.com/reference/android/provider/Settings#canDrawOverlays(android.content.Context)).

### Warm host, cold start and reboot

Start the host from visible setup UI. Background broadcast receipt alone is not permission to start any foreground service. For targets 35+, the SYSTEM_ALERT_WINDOW start exemption also needs a currently visible overlay; a hidden/transparent idle window is not a sound bootstrap strategy. Keep idle view detached and do not work around this using fake overlays. A stopped/force-stopped host needs a clear recovery path; do not steal focus to restart it. A user-enabled boot receiver can be evaluated if automatic boot startup is wanted, subject to service-type/OEM restrictions. Explicit force-stop still requires user reopening. [Official background FGS restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

Use START_STICKY recovery only as best-effort process recovery, not a promise against OEM kills. Start with the documented service architecture, then test actual OxygenOS behavior before deciding whether a new battery exemption is needed. This overlay's lifecycle is independent of the existing controller/Shizuku reboot mechanism. Do not change those settings during overlay setup.

## Overlay geometry, security and user input

Use TYPE_APPLICATION_OVERLAY, a transparent root and Lottie view, FLAG_NOT_FOCUSABLE and FLAG_NOT_TOUCHABLE. NOT_TOUCH_MODAL is redundant with nonfocusability for many cases but may be explicit for clarity. Window dimensions cover just the scaled artboard, not the screen.

Important design tradeoff: Android checks **window LayoutParams.alpha**, not just empty pixels in the artwork, for untrusted touch pass-through. The documented maximum obscuring opacity is 0.8; obtain it through InputManager.getMaximumObscuringOpacityForTouch rather than assuming it. Use one window at or below the returned limit, with a small rounding margin if necessary. This makes the composited artwork slightly translucent even though its source vector colors remain exact #FFFFFF/#FF2020. Other overlays and the destination app's own obscured-touch protections can impose additional restrictions. Touch delivery must be tested beneath the visible text and beneath transparent areas inside the window. [Window flags and opacity rules](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_NOT_TOUCHABLE).

Ordinary overlays sit above normal app windows but below critical system windows. Apps/system surfaces may hide overlays, including sensitive screens; do not promise a lock-screen or permission-dialog bypass. Prefer unlocked-screen-only by default, discard screen-off triggers, do not wake the screen or replay stale status after unlock. Lock-screen display is a user decision and still subject to platform limits.

Use WindowContext/WindowManager metrics appropriate to the overlay's display, then derive the usable rectangle from relevant system bars/cutout insets. For usable origin (L,T), width W, height H, visual center is (L+W/2,T+H/3). Subtract half scaled artboard width/height for window top-left, accounting for WindowManager's gravity/inset policy exactly once. Keep the title and mode geometrically centered independently using their outlined visible bounds; center the overall artwork in its artboard too. Apply no IME-driven animation jumps by default. Recalculate on rotation, density/display-size or inset changes; scale down to fit landscape rather than stretching.

Recommended initial configurable artboard width: about 65% of usable portrait width, capped around 320dp and constrained by available landscape height, with a 3:1 aspect ratio. This is a proposed visual starting point, not a spec change; device preview will settle readability. The exact anchor and local motion from the brief remain fixed.

## Trigger contract and honest state integration

The future manifest receiver can be exported for explicit external triggers, validate action and mode, then dispatch to the running host and return promptly. No custom signature-only permission on the basic interface if ordinary Tasker must call it. Exposed capability is only a local visual; accept HIGH initially, reject unsupported/unknown/malformed values, bound processing and restart one existing animation instead of allocating views or queues. An open benign receiver allows other local apps to request the same visual; keep that limitation clear. A private authenticated IPC alternative is possible later, but is unnecessary for the specified open Tasker intent. [Android broadcasts and receiver exposure](https://developer.android.com/develop/background-work/background-tasks/broadcasts).

Target ADB contract, **not implemented or verified**:

```powershell
adb shell am broadcast -n com.nish.ancengine/.AncTriggerReceiver -a com.nish.ancengine.SHOW --es mode HIGH
```

Target Tasker **Send Intent** configuration, likewise unverified until a real Tasker test:

| Field | Value |
|---|---|
| Action | com.nish.ancengine.SHOW |
| Package | com.nish.ancengine |
| Class | com.nish.ancengine.AncTriggerReceiver |
| Extra | mode:HIGH |
| Target | Broadcast Receiver |
| Category/Data/MIME | Leave unset |

Tasker's official guide documents its explicit Package/Class fields and colon-separated extras. Successful shell delivery is not evidence for app-originated cold background behavior. [Tasker intent manual](https://tasker.joaoapps.com/userguide/en/intents.html).

Local ControllerService.java was inspected: it queries actual state, records selected modes in read(), and confirms desired mode in set(), but does not emit an external mode intent. Therefore automatic physical-button integration needs one deliberate change outside the new app. Recommended later option, only if approved: add a small confirmed-state notification hook without changing the key catcher, timing, transition rules or Bluetooth protocol. Emit after successful confirmation, and never label a rejected write as HIGH. Overlay errors must never delay/break ANC control.

If external squeeze/native mode changes should also display animations, an observed-selected-mode transition hook can cover those while the existing Bluetooth session is live, with duplicate suppression and no replay of initial connection snapshots. Do not assume every external change is always observable when a competing vendor app owns the control channel. HIGH-only phase must ignore other actual modes rather than falsely showing HIGH. Keep TRANSPARENCY-to-TRANSPARENT API/display mapping explicit for the future template.

## Animation assets and font pipeline

Use standard shape layers, fills/strokes, cubic paths, trim paths and transform/opacity keyframes. No image references, text/font runtime layers, expressions, AE effects, Gaussian blur or expensive masking. Author fr=120, ip=0, op=180, w=1200, h=400 with the exact brief's phase boundaries. Use a small number of particles; emulate subtle glow by layering low-alpha broader strokes behind narrow cores. Keep final content transparent slightly before the exclusive out-point to avoid sampled end-frame fragments. [Lottie composition](https://lottie.github.io/lottie-spec/latest/specs/composition/), [shapes/trim paths](https://lottie.github.io/lottie-spec/latest/specs/shapes/), [animated properties](https://lottie.github.io/lottie-spec/latest/specs/properties/).

Recommended font: **Exo 2 Italic**, lighter title and semibold HIGH, because it has genuine italic outlines and adjustable weight without simulated raster slant. Alternative: Rajdhani for a narrower technical aesthetic. Both official Google Fonts directories contain SIL OFL1.1 licensing. No font assets downloaded/selected into the repo yet. Retain font copyright/OFL and original source/hash with the generator; outlining does not justify discarding provenance. [Exo2 metadata](https://github.com/google/fonts/blob/main/ofl/exo2/METADATA.pb), [Exo2 OFL](https://github.com/google/fonts/blob/main/ofl/exo2/OFL.txt), [Rajdhani OFL](https://github.com/google/fonts/blob/main/ofl/rajdhani/OFL.txt).

Recommended generator: pinned Python fontTools + uharfbuzz, direct deterministic Lottie JSON construction. Shape exact text with HarfBuzz, retain advances/kerning/offsets, extract glyph contours from TTFont, resolve components and instantiate the chosen variable-font weight. Preserve holes/fill rule and Y-axis orientation. Convert quadratic segments to cubic equivalents without flattening Béziers; fontTools Qu2CuPen is available, though its approximation tolerance must be deliberate. Center each completed line by actual bounds, including italic overhang. Do not generate letters by naive Unicode-width summation or trace a PNG. [fontTools glyph APIs](https://fonttools.readthedocs.io/en/latest/ttLib/ttFont.html), [Qu2CuPen source](https://github.com/fonttools/fonttools/blob/main/Lib/fontTools/pens/qu2cuPen.py), [uharfbuzz](https://github.com/harfbuzz/uharfbuzz).

Reuse one mode/palette/text template later; generate HIGH alone now. A JSON-schema/static-structure check is useful but insufficient. Validate representative frames in Lottie Android plus a pinned offline lottie-web SVG/Canvas preview (5.13.0 observed), including counters/holes, trim ordering, easing, centering, clipping and complete disappearance. Optional ThorVG is maintained but adds another rendering/toolchain surface; not needed initially. No preview or production animation has been made yet.

## Dependency and toolchain research

Observed versions are candidates, not a completed build lock or a compatibility claim. Recheck availability at implementation and record resolved checksums/dependency verification; avoid floating '+', latest or pre-release dependencies in committed builds.

| Component | Researched choice | Reason / provenance |
|---|---|---|
| Android Gradle Plugin | 9.4.1 stable artifact observed in official Google Maven metadata | 9.4 documentation specifies Gradle9.6.0, JDK17 and Build Tools36.0.0 minimum/default; compile/target36 supported |
| Gradle wrapper | 9.6.0 for that AGP family | Match official compatibility table; no global Gradle required |
| Kotlin | AGP built-in Kotlin | AGP9 enables built-in support; avoid applying a conflicting standalone kotlin-android plugin |
| SDK | compile/target36; proposed min31 | Existing SDK36 present; target actual Android16. Wider device support not promised |
| Java | JDK17 or compatible installed JBR21 | Installed Android Studio JBR reports21.0.8; private JDK17 already used by root controller |
| Lottie Android | com.airbnb.android:lottie:6.7.1 | Official latest release API reports v6.7.1, published31 October2025; Apache2.0 |
| Python fontTools | 4.67.0 observed | Official release14 October2026; main license MIT, some conversion files carry Apache2.0 notices |
| uharfbuzz | 0.56.3 observed | Official release6 October2026; binding Apache2.0, underlying HarfBuzz has its own license notices |
| lottie-web preview | 5.13.0 observed | Official source package and npm metadata agree; MIT; local pinned preview, no third-party upload required |

Sources: [AGP9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [official AGP metadata](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml), [built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin), [Lottie Android release](https://github.com/airbnb/lottie-android/releases/tag/v6.7.1), [fontTools releases](https://github.com/fonttools/fonttools/releases), [uharfbuzz releases](https://github.com/harfbuzz/uharfbuzz/releases), [lottie-web package](https://github.com/airbnb/lottie-web/blob/master/package.json). No Compose, NDK or full new Android Studio install is needed for this View-based app.

## Timing, reduced motion and actual frame delivery

Source inspection at Lottie Android v6.7.1 found that its composition parser subtracts0.01 from the JSON out-point and its duration getter truncates milliseconds. Therefore a nominal180/120 asset may report1499ms; completion sampling can add a vsync. Keep the requested JSON metadata exact, but measure first-draw/playback/removal timestamps. If needed use a deliberate1500ms linear progress driver or tested playback-speed compensation with one time source, never two competing animators. Do not claim mathematically exact physical presentation from an unmeasured callback. [Parser source](https://github.com/airbnb/lottie-android/blob/v6.7.1/lottie/src/main/java/com/airbnb/lottie/parser/LottieCompositionMoshiParser.java), [composition duration source](https://github.com/airbnb/lottie-android/blob/v6.7.1/lottie/src/main/java/com/airbnb/lottie/LottieComposition.java), [animator source](https://github.com/airbnb/lottie-android/blob/v6.7.1/lottie/src/main/java/com/airbnb/lottie/utils/LottieValueAnimator.java).

Lottie defaults to considering system/reduced-motion configuration; an explicit reduced-motion policy is needed so an entirely transparent final frame is not the only feedback when animations are disabled. Recommended: honor reduced motion with a static readable HIGH indicator for the same nominal interval, while keeping the full-motion asset unchanged. Normal test configuration should be recorded, not globally changed. [Lottie reduced-motion implementation](https://github.com/airbnb/lottie-android/blob/v6.7.1/lottie/src/main/java/com/airbnb/lottie/LottieDrawable.java).

Cache/preload the local composition when the enabled host starts, off the UI thread using Lottie's asset loader/cache; use the main thread for window/view ownership. Restart from progress0 on repeated triggers, cancel the old animator/listeners/timeout, and tag each playback generation. Old completion tasks cannot remove a newer view. Remove on completion and with a separate bounded watchdog. No persistent frame callbacks/wake locks while idle.

On API36, request120fps on the actual animated View while attached; window preferredRefreshRate=120 is a compatible preference and should not be combined with a forced resolution/mode ID unnecessarily. Restore/release preferences when removing the view. Preferences can be ignored by the OS and do not override thermal/battery/user policy. Inspect supported/active display modes rather than inferring from “OnePlus15.” [Android frame-rate APIs](https://developer.android.com/develop/ui/views/animations/adaptive-refresh-rate), [window refresh preference](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#preferredRefreshRate).

Measure trigger receipt, ready/attach, first draw, frame timestamps, last visible frame and removal separately. Choreographer callback spacing is scheduling evidence, not proof every frame reached the display. Prefer a scoped Perfetto FrameTimeline trace for app-layer expected/actual presentation/jank plus app instrumentation, with device tracing support checked first. Window.OnFrameMetricsAvailableListener requires a Window and is not automatically accessible from a service's bare WindowManager.addView overlay; do not claim Activity-only metrics prove overlay performance. A screen recording may be capped far below120fps. [Perfetto FrameTimeline](https://perfetto.dev/docs/data-sources/frametimeline), [Window frame metrics](https://developer.android.com/reference/android/view/Window.OnFrameMetricsAvailableListener).

Proposed timing acceptance: exactly1.5 seconds by authored metadata and intended progress timeline; report measured hardware duration/error with display-vsync resolution and load. A small scheduling tolerance is physically unavoidable. Do not change ip/op/fr to conceal parser/reporting differences. Preserve 120fps authoring even if this phone only presents60/90Hz under a particular policy.

## Decisions for the user

1. **Automatic integration scope:** standalone ADB/Tasker demo first, or authorize a later small confirmed-mode notification hook in the existing ANC controller while preserving its catcher? Recommended: hook after the standalone overlay passes; HIGH visual only for confirmed HIGH.
2. **Background lifecycle:** automatically enable the overlay host again after reboot (quiet persistent notification while enabled), or require opening/enabling the overlay app after reboot? Recommended: automatic recovery with an independent Stop switch; this needs a new physical boot test.
3. **Lock/screen-off behavior:** unlocked-screen-only, or attempt visible lock-screen display without waking the phone? Recommended: unlocked-only, discard screen-off triggers and never replay stale events. Secure surfaces remain restricted either way.
4. **Event scope:** show after confirmed Plus Key/app commands only, or also on observed changes made by the earbuds/native controls? Recommended: confirmed commands initially; deduplicate query/push notifications and skip initial connection snapshots. Both approaches show HIGH only in the first phase.
5. **Reduced motion:** honor system reduced-motion/disabled-animation settings with a static1.5-second indicator, or explicitly prefer the full effect for this app? Recommended: respect system settings. Do not change global animation preferences.

No need to ask the user to choose Gradle versions, service types, opacity thresholds, frame APIs or glyph conversion algorithms. Use the researched defaults and verify them during implementation. Font/size can start with Exo2Italic and the compact sizing above, then be reviewed in the actual preview. Exact strings/colors/timing/anchor and HIGH-first scope were already specified.

## Resume boundary

Research is complete enough to choose architecture and ask the above product questions. Device performance and OEM behavior remain unverified because no new app exists and no device is connected. Answers do not authorize starting implementation: the user explicitly wants to pause afterward. Save answers in this branch, then wait for a separate instruction to begin. Do not schedule an automatic continuation.
