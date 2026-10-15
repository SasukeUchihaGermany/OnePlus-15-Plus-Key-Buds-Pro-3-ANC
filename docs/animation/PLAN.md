# Animation extension — analysis and handoff

Status: **preparation only; implementation is on hold until the user explicitly resumes it**. Prepared 15 October 2026 on branch `Animation-implementation` from main commit `4c9ce82`. The requested human-readable branch name was “Animation implementation”; Git does not allow spaces. No new app, animation asset, dependency installation, APK build, device deployment or permission change has been performed for this extension. The existing controller remains unchanged.

Follow-up research is in [RESEARCH.md](RESEARCH.md): service/overlay rules, source-level timing findings, version/license candidates, trigger integration and five user decisions. It supersedes the provisional technical choices below. Research and questions are authorized; implementation stays on hold even after answers until the user separately resumes it.

The user subsequently authorized a connected-phone **native Fluid Cloud feasibility test** as an alternative. It passed: see [isolated native-alert experiment](../../experiments/native-alert/README.md). That authorization covers the probe; no Lottie implementation or automatic controller integration has started. Native capsule shows short mode labels, while the full title fits the notification card; native timing/layout remains system-controlled.

The user then explicitly requested tying the native short text/speaker capsule to the ANC key. Version1.3 integrates native notifications directly into the existing controller's confirmed set-result path, with the per-app Live Alerts switch. See root SETUP.md and VALIDATION.md. No Lottie or overlay implementation has started; the separate native probe is no longer needed for normal operation. This supersedes the earlier integration hold for this specific native-feedback scope.

## Scope and source of truth

[PROJECT-BRIEF.md](PROJECT-BRIEF.md) preserves the supplied specification. Its embedded instruction to implement immediately is superseded by the user's latest instruction to analyze/set up only and wait for token refresh. Do not automatically resume based on elapsed time or a guessed reset time.

Build a separate Kotlin app, **Nish's ANC Engine**, package `com.nish.ancengine`. Keep the existing `dev.local.anc` controller, Java build and Plus Key/Shizuku input pipeline intact. Proposed future project location: `animation-engine/` with its own Gradle wrapper/build and app module, leaving root build.ps1/test.ps1 for the working controller. This directory has not been scaffolded yet.

Implement only HIGH first. Preserve exact strings `Nish's ANC Engine:` and `High`, independent horizontal centering, white title and red #FF2020 mode. Genuine self-contained Lottie vector shapes, outlined text, independently animated red/white swooshes, local motion, transparent background. Composition 1200×400, fr=120, ip=0, op=180, 1.5-second timeline, one playback. Phase boundaries 0/36/72/126/180. Future modes share the generator/template, but do not generate their final assets yet.

## Existing environment and integration findings

- Previously device-tested phone: CPH2747 / Android 16 API36, OxygenOS CPH2747_16.0.9.400(EX01). This turn did not inspect or alter the connected phone; recheck current firmware, display modes, density and insets when implementation resumes.
- Local SDK platforms 29/35/36 and build-tools 30.0.2/34.0.0/36.1.0 exist. Android Studio JBR java exists, and a private JDK17 toolchain was used for the controller. Global Gradle/kotlinc commands were not found; a project Gradle wrapper is the likely future route. A python executable resolves, but its usable runtime/packages were not tested this turn.
- Existing controller confirms changes through fresh Bluetooth queries in ControllerService.set(); it does **not** currently emit the overlay's external intent. The brief's “existing volume-button/Shizuku/Tasker catcher” wording differs from this repository's actual Plus Key Java controller. Do not assume a mode trigger is already wired or alter the catcher to make the initial demo work.
- First prove the separate overlay with manual ADB and Tasker triggers. Any later automatic connection to confirmed controller state needs an explicitly scoped integration decision. A button press alone is not proof that ANC changed, and a desired HIGH command must not produce a false success overlay after rejection.
- Proposed external contract: explicit broadcast component `com.nish.ancengine/.AncTriggerReceiver`, action `com.nish.ancengine.SHOW`, extra `mode=HIGH`. The command in the brief is a target contract, **not a verified command**. Receiver should reject other actions, invalid/missing modes and unsupported modes, and expose only local visual playback. Future TRANSPARENT display/API mode needs mapping from controller enum TRANSPARENCY.

## Platform decisions to resolve before coding

1. **Overlay lifetime and service startup.** Preferred candidate: user enables a persistent overlay host from a visible setup Activity, with a clear notification and Stop action; receiver forwards to the already-running host. Determine a legitimate foreground-service type and permissions on API36. Do not reuse connectedDevice merely because the separate controller has it, and do not pretend an indefinite host fits shortService. Evaluate specialUse and its required declarations if justified. The exact host architecture remains provisional pending this check and a device proof.
2. **Cold background trigger.** Explicit broadcasts do not automatically grant unrestricted service starts. Android's documented SYSTEM_ALERT_WINDOW exemption for targets 35+ also requires a currently visible overlay. A hidden idle window or invisible dummy overlay is not a valid workaround. Define and test behavior when host is stopped/killed: fail clearly or use a documented legal start path; no surprise Activity launch. ADB shell success alone does not prove Tasker success. [Official foreground-service restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).
3. **Touches.** Use TYPE_APPLICATION_OVERLAY with normal SYSTEM_ALERT_WINDOW approval, nonfocusable/nontouchable flags and tight composition bounds. Transparent pixels and FLAG_NOT_TOUCHABLE alone do not guarantee touch-through on Android12+. Query the permitted maximum obscuring opacity, keep WindowManager alpha compliant, account for combined overlays, and physically test touches beneath the actual bounds. Do not bypass security or disable global protections. Secure/system surfaces can restrict overlays; do not promise coverage there. [Window flag reference](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_NOT_TOUCHABLE), [Android12 behavior changes](https://developer.android.com/about/versions/12/behavior-changes-all).
4. **Placement.** Use a display/window context and current WindowMetrics/insets, not fixed pixels. Define usable rectangle excluding relevant system bars/cutout; visual center at x=50%, y=one third of that rectangle. Derive tightly bounded window top-left from composition size. Ensure artwork's visual bounds are centered in the artboard. Recompute on rotation/inset changes; no screen-edge entrance. Scaling and dp offsets belong in AnimationConfig.
5. **Frame rate and duration.** Authored 120fps is not measured display120Hz. Check actual display support/settings on resume, request supported rate through official window/view APIs without changing global preferences, and instrument Choreographer/FrameMetrics or appropriate renderer tracing. Record device refresh, rendered callback spacing, missed frames and animator-scale state separately. Account for renderer sampling and end-frame semantics when assessing the exact1.5-second timeline. Recording fps alone is not proof. [Official adaptive-refresh guidance](https://developer.android.com/develop/ui/views/animations/adaptive-refresh-rate).
6. **Typography and generation.** Choose a redistributable licensed font after visual evaluation, retain license and source/hash, and use a reproducible shaping/outline pipeline (e.g. HarfBuzz plus fontTools) that preserves advances/kerning and Bézier curves. Convert quadratic outlines when required by the Lottie path representation. Prefer vector-layered glow accents over blur/raster effects. No font/library choice is locked yet.
7. **Renderer compatibility.** Pin mutually compatible stable AGP/Gradle/Kotlin/Lottie versions when implementation begins; no versions were installed or guessed now. Use Android Lottie as the device truth; an optional compatible preview renderer helps visual iteration but does not replace device tests. Verify JSON shapes/trim paths/easing against the spec. [Lottie composition specification](https://lottie.github.io/lottie-spec/latest/specs/composition/), [official Android renderer](https://github.com/airbnb/lottie-android).

## Proposed responsibilities (not implemented)

- MainActivity: initial permission status/request, user-enabled host start/stop and local HIGH test.
- AncMode: six-mode vocabulary, but only HIGH asset available initially; reject unimplemented modes gracefully.
- AncTriggerReceiver: minimal explicit intent validation and forwarding; no Bluetooth/input/system-control privileges.
- AncOverlayService / AncOverlayManager: host lifecycle and main-thread WindowManager ownership; one view, cached composition, restart frame0 on retrigger, completion removal and bounded timeout. Use a generation identifier so an old completion/timeout cannot remove a newer animation. Cleanup on disable, permission loss, configuration change and destruction.
- AnimationConfig: scale/offsets, usable-screen placement, rate preference, playback/retrigger policy.
- Reusable generator: font/vector source inputs, palette/mode parameters, timeline and optimized independent swoosh/particle layers. HIGH output at app/src/main/assets/anc_high.json with reproducible preview frames and metadata validation.

## Execution sequence after explicit resume

1. Read this plan and the full brief, recheck the local/phone environment, choose compatible dependencies and settle legal service startup/type. Preserve the working controller and private signing material.
2. Create the separate project and prove overlay permission, bounded transparent window, home/ordinary-app placement, touch-through and trigger delivery on this actual firmware. No need to modify the key catcher.
3. Select/license font; generate outlined HIGH composition and inspect representative frames at entrance, settle, hold and exit with a Lottie renderer. Iterate visual quality before calling it finished. No PNG substitute.
4. Add cached playback, single-view retrigger semantics, cleanup and bounded fallback; instrument duration/frame delivery. Author at120fps regardless of measured refresh limits, without misreporting results.
5. Build/install only the new app, obtain normal overlay approval at that exact user-interaction step, verify ADB and actual Tasker delivery. Test cold/stopped host behavior independently of warm-host performance.
6. Run the acceptance matrix below, document setup/uninstall and future mode generation, then discuss the separately scoped confirmed-state integration if still needed. Never infer physical passes from code or metadata.

## Acceptance matrix — all outstanding

| Area | Evidence required |
|---|---|
| Build/install | Separate Kotlin project builds; new package installs; original controller source/APK remains intact |
| Permissions | Normal overlay grant, revoke during playback, denied start, notification/host state |
| Trigger | Explicit ADB action/component/extra; actual Tasker broadcast; invalid input; stopped host |
| Visuals | Exact outlined strings/colors, independently centered lines, local red/white strokes, transparent pixels, no clipping |
| Geometry | Visual center at one-third usable height; compact bounds; portrait and rotation/landscape |
| Lifecycle | One playback, frame0 restart on rapid triggers, one overlay only, no stale timeout removal, no orphan view after process/service exit |
| Touch | Tap/scroll underlying app inside overlay bounds and outside; no focus/app switching |
| Timing | Metadata120/0/180, measured wall duration with stated tolerance, refresh preference and observed frame delivery/drop evidence |
| Performance | Warm/cold latency, cache behavior, background idle cost, no unbounded wake lock |
| Scope | HIGH polished first; future template mapping documented; no changes to catcher/native/security settings |

## Resume instruction

The next authorized action is **wait**. When the user says to begin implementation, resume at the execution sequence above. Do not create a scheduled continuation or automatically start after a few hours. The original installed ANC controller continues working during this hold.
