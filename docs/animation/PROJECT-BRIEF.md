# Project: Nish's ANC Engine — Custom Android Lottie Overlay

You are an expert Android engineer, Kotlin developer, motion graphics designer, and automation engineer.

Your task is to **design, generate, build, install, and test** a fully functioning Android application that displays a premium, animated ANC status indicator as a transparent system overlay.

**Do not stop at providing instructions or source code.** You have access to my Windows PC, development environment, and connected Android device through ADB. Perform the implementation and deployment yourself wherever possible.

Research the relevant official documentation, identify compatibility requirements, implement the solution, and validate the result on the physical device.

## 1. Environment

**Target device:**
- Phone: OnePlus 15
- OS: OxygenOS / Android
- Connected to Windows PC through ADB
- Wireless debugging also available
- Shizuku installed
- Tasker installed
- Existing custom volume-button event catcher already functioning

**Development environment:**
- Windows 11 PC
- Use Android Studio, Gradle, Kotlin, Android SDK, Python or any other appropriate tools.
- Check which dependencies are installed before installing anything.
- You may download relevant libraries, documentation and open-source tooling from reputable sources.
- Use current stable, mutually compatible dependencies.

The existing button event catcher is outside the scope of this project. Do not replace or modify it.

Your application must expose a straightforward mechanism to receive external triggers from my existing automation infrastructure.

## 2. Primary objective

Create an Android application named:

**Nish's ANC Engine**

Package name:

`com.nish.ancengine`

When externally triggered, display an animated, transparent Lottie overlay above whichever application is currently running, including the OxygenOS home screen.

The animation must:

- Run for exactly 1.5 seconds of animation timeline duration.
- Be authored at 120 FPS.
- Display two lines of fancy, centred typography.
- Feature a fluid, elegant swoosh-in and swoosh-out effect.
- Contain no opaque rectangular background.
- Be positioned horizontally centred and one-third down from the top of the screen.
- Remain within a compact local region rather than entering from the screen edges.
- Automatically disappear after playback.
- Not obstruct or intercept touchscreen interactions.
- Avoid changing the user's current application or bringing up a visible Activity.

**Initially implement only the HIGH mode.**

Design the implementation so it can later support all six ANC modes with minimal changes.

## 3. Exact visual design

### Text

The animation must display:

```text
Nish's ANC Engine:
       High
```

The spaces above are illustrative. Both lines must be mathematically centred independently.

**Line 1:**
- Text: `Nish's ANC Engine:`
- Colour: pure white (#FFFFFF).
- Elegant, premium, futuristic typography.
- Slightly italic or modern display-font styling.
- Crisp, easily readable.
- Smaller than the second line.

**Line 2:**
- Text: `High`
- Colour: vivid red (#FF2020), with subtle brighter red accents.
- Larger than the first line.
- Slightly bolder visual treatment.
- Horizontally centred beneath line 1.
- Visually emphasised through animation.

Select an appropriate high-quality font with a licence permitting use in the application.

Prefer a clean, luxurious, high-tech appearance rather than a generic Android notification aesthetic.

Convert text to vector paths inside the Lottie animation to eliminate Android font-dependency problems.

Use a reproducible vector conversion pipeline, preserving Bézier curves and character spacing.

### Visual inspiration

Create an original design combining:
- Premium futuristic typography.
- Sweeping, curved red and white light trails.
- Thin orbital energy arcs surrounding the text.
- Subtle sparkle highlights.
- Smooth fluid motion.
- Slight depth from layered transparent shapes.
- Tasteful neon-like emphasis created through vector layering.

The result should look like a polished custom OxygenOS visual effect, not a conventional notification.

**Do not simply animate a static PNG.** Generate a genuine Lottie vector animation with independently animated shapes.

If an existing reference PNG is available, use it as visual inspiration only. Otherwise implement the design directly from this specification.

### Swoosh design

Create multiple coordinated swoosh elements:

1. A primary red curved light trail.
2. A thinner white/silver accompanying trail.
3. A secondary red orbital arc.
4. A small number of subtle highlight particles.
5. Optional short-lived accent strokes around `High`.

These must animate independently using Lottie-supported shape and transform operations.

Use cubic Bézier paths, animated trim paths, stroke width, opacity, scale, position and rotation.

Avoid unsupported After Effects effects, Gaussian blur, raster-heavy glows and expensive masking.

All decorative elements must remain inside the local composition.

## 4. Screen placement

Position the complete animation at:

- Horizontal anchor: **50% of screen width**.
- Vertical anchor: **33.33% of usable screen height measured from the top**.

This is approximately two-thirds of the way up the display.

**Critical positioning requirements:**

- Position the visual centre of the animation at the anchor.
- The animation must NOT fly in from the left/right edge of the screen.
- The animation must NOT originate from the top or bottom screen edge.
- The swoosh should form locally around the text.
- The exit animation should dissipate locally around the same position.
- The overlay window should cover only the required composition area, not the entire screen.
- Respect appropriate display insets.
- Support portrait mode first.
- Handle landscape orientation gracefully.
- Make X/Y offsets and scaling configurable in Kotlin.

Suggested Lottie artboard:

`1200 × 400` logical pixels.

Scale it responsively to the device, maintaining aspect ratio.

Target a visually compact presentation: large enough to read comfortably, but not so large that it resembles a banner covering the display.

Do not assume a fixed physical pixel density.

## 5. Animation duration and frame rate

Create the Lottie composition with:

- Frame rate: **120 FPS**
- In-point: 0
- Out-point: 180
- Duration: **1.5 seconds**
- Width: 1200
- Height: 400
- Looping: disabled
- Background: fully transparent

### Exact timeline

| Frames | Time | Animation |
|---|---|---|
| 0–36 | 0.00–0.30s | Swoosh-in and text arrival |
| 36–72 | 0.30–0.60s | Swoosh completes, text settles |
| 72–126 | 0.60–1.05s | Readable hold, subtle energy effects |
| 126–180 | 1.05–1.50s | Swoosh-out, fade and disappearance |

### Entrance: frames 0–36

- Begin with full transparency.
- Red and white energy trails curve into existence around the central anchor.
- Animate their path revelation using trim paths.
- The two text lines appear with a subtle horizontal displacement of approximately 20–35 logical pixels.
- Text opacity transitions from 0% to 100%.
- Text scale transitions from approximately 96% to 100%.
- Use smooth ease-out cubic Bézier interpolation.
- Keep all motion local.

### Settle: frames 36–72

- Complete the orbital swoosh.
- Settle the text into its final centred position.
- Add a restrained red emphasis pulse to `High`.
- Let small highlight trails dissipate.
- Avoid continuous excessive movement that compromises legibility.

### Hold: frames 72–126

- Both text lines remain centred.
- Maintain full readability.
- Red `High` remains prominent.
- Optionally animate subtle energy movement in the decorative arcs.
- Do not animate the text itself excessively during this interval.

### Exit: frames 126–180

- Initiate a controlled swoosh-out.
- Contract or sweep the orbital arcs away locally.
- Fade the text smoothly.
- Apply a very small horizontal or vertical drift if it improves the exit.
- Use ease-in interpolation.
- By frame 180, all visible content must be fully transparent.

The animation must not leave visible fragments or light trails.

### Physical refresh-rate considerations

A 120 FPS Lottie composition does not guarantee 120 FPS physical display output.

Check the OnePlus 15's current display settings and supported refresh modes.

Where supported, request a 120 Hz display refresh rate using official Android APIs without modifying global user preferences.

Use appropriate runtime instrumentation to measure actual playback frame delivery and dropped frames.

Prioritise smooth playback and consistent 1.5-second duration over trying to force unsupported refresh rates.

## 6. Lottie asset generation

Generate the actual Lottie JSON file programmatically or through an appropriate open-source animation pipeline.

Required output:

`anc_high.json`

Store it under:

`app/src/main/assets/`

The asset must be valid, fully self-contained and playable using Lottie for Android.

### Requirements

- Genuine Lottie vector shape layers.
- Text converted to vector outlines.
- Transparent background.
- No external image references.
- No external runtime font dependencies.
- Smooth Bézier easing.
- Multiple independent animated swoosh layers.
- Optimised path and layer counts.
- 120 FPS composition metadata.
- Exactly 180 frames of animation.
- No looping.

Use official Lottie specifications to ensure correct JSON structure.

Validate the generated JSON using a Lottie-compatible renderer, rather than merely checking JSON syntax.

If possible, generate a visual preview as a GIF, MP4, HTML preview or frame sequence for quality inspection.

Do not substitute an animated PNG or WebP for the requested Lottie file.

### Visual quality

The animation must be aesthetically finished, not a basic placeholder.

Implement a deliberate motion-design composition with coordinated swoosh movement, high-quality typography and refined easing.

If initial rendering is visually poor, iterate on the animation until it meets the design specification.

## 7. Android application architecture

Build a minimal, clean Kotlin application.

Suggested architecture:

```text
com.nish.ancengine/
├── MainActivity.kt
├── AncMode.kt
├── AncTriggerReceiver.kt
├── AncOverlayService.kt
├── AncOverlayManager.kt
└── AnimationConfig.kt
```

Choose the appropriate Android component architecture after checking current background execution restrictions.

Use a minimal launcher Activity for:
- Initial setup.
- Overlay permission requests.
- Testing the animation manually.
- Displaying permission status.
- Configuring animation size/position if straightforward.

The launcher UI should be minimal and functional.

### Overlay implementation

Use Android's `WindowManager`.

Use `TYPE_APPLICATION_OVERLAY` for supported modern Android versions.

Request `SYSTEM_ALERT_WINDOW` through the normal Android user permission flow.

Use appropriate window flags:

- `FLAG_NOT_FOCUSABLE`
- `FLAG_NOT_TOUCHABLE`
- `FLAG_NOT_TOUCH_MODAL` where appropriate
- Any additional flags that are necessary and compatible with the current Android version.

**Important:** Investigate Android 12+ restrictions on untrusted touch events through overlay windows.

Ensure underlying applications continue receiving touches wherever supported. Keep the overlay window bounds tight, rather than creating an unnecessarily large full-screen transparent window.

Do not bypass Android's touch security protections.

The root layout and Lottie view must have transparent backgrounds.

### Playback

Use `LottieAnimationView` or a suitable current Lottie Android API.

- Load `anc_high.json`.
- Disable looping.
- Start from frame 0.
- Play once.
- Remove the overlay view on completion.
- Also implement a safe fallback timeout in case the completion callback fails.
- Clean up listeners and references.
- Avoid Activity leaks and orphaned views.

### Trigger responsiveness

The animation should appear quickly after receiving the external event.

Minimise startup latency.

Cache or preload the Lottie composition where appropriate.

Avoid unnecessarily loading and parsing JSON on each trigger.

Ensure repeated triggers do not accumulate overlay views.

If a new trigger arrives while the animation is active, **restart the animation from frame 0** rather than displaying a second instance.

Make this behaviour configurable for future development.

### Background execution

This application must remain usable when another app is in the foreground.

Investigate Android background service restrictions for the device's actual API level.

Do not assume that an exported BroadcastReceiver can always launch a background service.

Select a compliant architecture that delivers reliable event-to-overlay behaviour, potentially using a user-enabled persistent service with a notification if genuinely required.

Avoid unnecessary battery drain.

Document any OxygenOS-specific background execution or battery optimisation settings needed for reliable operation.

## 8. External trigger integration

My existing Shizuku/Tasker automation already detects button events.

Your task is to expose a simple, reliable trigger for the overlay.

Use the following intent contract:

**Action:**

`com.nish.ancengine.SHOW`

**Extra key:**

`mode`

**Initial value:**

`HIGH`

Use an explicit broadcast receiver or a more reliable compatible interface if required by modern Android restrictions.

For example, aim to support a command equivalent to:

```bash
adb shell am broadcast \
  -n com.nish.ancengine/.AncTriggerReceiver \
  -a com.nish.ancengine.SHOW \
  --es mode HIGH
```

Verify this command against the actual manifest and implementation.

Do not assume this command is functional until tested.

For Tasker, document:
- Intent action.
- Package/component.
- Extras.
- Whether the intent is a broadcast or service request.
- Any permissions needed.

Do not modify my existing button catcher.

### Security

Keep the external API minimal.

Validate accepted mode values.

Avoid exposing privileged commands, private data or unnecessary components.

If an exported receiver is required for Tasker integration, restrict its functionality to triggering the benign local animation.

## 9. Future mode support

Only the HIGH animation needs to be fully implemented now.

However, structure the application for six modes.

| Mode | Display text | Status colour |
|---|---|---|
| HIGH | High | Red `#FF2020` |
| MEDIUM | Medium | Orange `#FF9800` |
| LOW | Low | Yellow `#FFEB3B` |
| OFF | Off | White `#FFFFFF` |
| AUTO | Auto | Black `#000000` |
| TRANSPARENT | Transparent | Blue `#2196F3` |

All modes share the first line:

`Nish's ANC Engine:`

Each mode should eventually have the same:
- Font.
- Animation timing.
- Swoosh structure.
- Overlay position.
- Layout logic.
- 120 FPS composition.
- 1.5-second duration.

Only the status text and colour should change, together with appropriate swoosh accent colours.

Design the asset generation process so that all six animations can be produced from one reusable Python script or animation template.

For the eventual AUTO mode, black text may be difficult to read on dark backgrounds. Preserve the requested black text but allow a subtle light outline or equivalent contrast treatment.

For the longer word `Transparent`, ensure the second line remains centred and fits within the composition without clipping.

**Do not generate all six final animations yet. Perfect HIGH first.**

## 10. Documentation and research

Before implementation, consult current official sources and relevant technical examples.

Prioritise:

- Android Developers: overlay windows and permissions.
- Android Developers: foreground/background service restrictions.
- Android Developers: explicit broadcast receivers.
- Android Developers: display refresh-rate handling.
- Airbnb Lottie Android GitHub repository.
- Official Lottie JSON specification.
- Lottie supported-feature documentation.
- Existing open-source Lottie animation generation libraries, where useful.

Useful starting points:

https://developer.android.com/develop/ui/views/animations/adaptive-refresh-rate

https://developer.android.com/develop/background-work/background-tasks/broadcasts

https://developer.android.com/about/versions/12/behavior-changes-all

https://github.com/airbnb/lottie-android

https://lottie.github.io/lottie-spec/latest/specs/composition/

If a third-party library or tool is useful, verify its maintenance status, licence and compatibility before integrating it.

Use modern Android best practices rather than blindly copying outdated examples.

## 11. Build and deployment

You have access to my Windows PC and connected OnePlus 15.

Perform the following tasks:

1. Check `adb devices` and identify the connected phone.
2. Determine the device's Android API level.
3. Determine supported screen resolution, density and refresh rates.
4. Check the available Java, Android SDK, Gradle and Kotlin toolchain.
5. Create the Android project.
6. Generate the real Lottie JSON asset.
7. Validate and preview the asset.
8. Implement the overlay application.
9. Compile a debug APK.
10. Resolve build errors.
11. Install the APK using ADB.
12. Launch the configuration Activity.
13. Guide me through any overlay permission confirmation requiring interaction on the phone.
14. Trigger the HIGH animation using ADB.
15. Inspect logcat for errors.
16. Confirm that the overlay is removed after playback.

Use existing installed tools where possible.

Do not change unrelated device settings, root the phone, or disable Android security protections.

For any settings requiring user confirmation, stop only at that specific point and explain precisely what action I need to perform.

### Tests

Perform tests covering:

- Animation over the home screen.
- Animation over another ordinary application.
- Transparent background.
- Correct text and colours.
- Correct two-line centring.
- Correct horizontal and vertical positioning.
- Actual playback duration.
- Repeated triggers.
- Triggers while an animation is still running.
- Touch interaction with the underlying app.
- Portrait/landscape transitions.
- Overlay permission removal.
- Process restart or app backgrounding.

Use ADB screenshots, screen recordings, logcat and performance diagnostics where appropriate.

Note that high-refresh-rate screen recording may require alternative measurement techniques. Do not infer actual 120 FPS playback solely from the Lottie metadata.

## 12. Deliverables

At completion, provide:

**A. Working Android project**
- Complete Kotlin source code.
- Gradle configuration.
- AndroidManifest.xml.
- Required layouts/resources.
- Lottie animation assets.

**B. Lottie asset**
- `anc_high.json`
- Self-contained and validated.
- Reusable generation script.
- Optional rendered preview.

**C. Installable APK**
- Debug APK installed on the OnePlus 15.
- Local APK path on the PC.

**D. External trigger commands**
- Verified ADB command.
- Tasker intent configuration.
- Instructions for integrating my existing Shizuku button event catcher.

**E. Test results**
- Android version and device information.
- Animation duration measured.
- Frame-rendering performance where measurable.
- Successful/failed test cases.
- Any remaining platform limitations.

**F. Modification guide**
Explain how to:
- Adjust animation position.
- Adjust animation size.
- Change colours.
- Change text.
- Modify swoosh timing.
- Generate the remaining five modes.

## 13. Acceptance criteria

The project is complete when:

- [ ] The application builds successfully.
- [ ] The APK installs on my OnePlus 15.
- [ ] Overlay permission can be granted normally.
- [ ] ADB can trigger the overlay.
- [ ] Tasker-compatible external triggering is implemented.
- [ ] The overlay works over the home screen and normal applications.
- [ ] The first line reads exactly `Nish's ANC Engine:`.
- [ ] The second line reads exactly `High`.
- [ ] Both lines are independently centred.
- [ ] The first line is white.
- [ ] The second line is red.
- [ ] The animation uses premium, fancy typography.
- [ ] The swooshes animate in and out smoothly.
- [ ] The swooshes remain local, not screen-edge transitions.
- [ ] The complete composition is horizontally centred.
- [ ] The composition's visual centre is at 33.33% of usable screen height.
- [ ] The background is completely transparent.
- [ ] The Lottie asset is authored at 120 FPS.
- [ ] The animation timeline lasts 1.5 seconds.
- [ ] Actual display rendering performance is measured where possible.
- [ ] The overlay disappears automatically.
- [ ] No orphaned overlay views remain.
- [ ] The application does not unnecessarily block touch input.
- [ ] The animation can be retriggered reliably.
- [ ] Future ANC modes can reuse the same animation generator.

## 14. Execution instructions

**Important: This is an implementation task, not a research report or coding tutorial.**

Proceed autonomously.

1. Inspect the connected device and local environment.
2. Research technical requirements.
3. Decide on the implementation architecture.
4. Generate the animation.
5. Create the Android application.
6. Build and deploy it.
7. Run real-world tests.
8. Fix any issues discovered.
9. Report final results.

Use reasonable engineering judgement when a detail is unspecified.

Do not repeatedly ask for approval on routine implementation decisions.

Do not stop after creating a placeholder animation.

Do not claim functionality has been verified unless you have actually performed the relevant test.

If an API or platform limitation prevents some requirement, explain it clearly and implement the closest compliant solution without silently changing the specification.

**Final objective:** When my existing automation detects an ANC mode change, my OnePlus 15 should display a beautiful, transparent, 120-FPS-authored, 1.5-second Lottie animation reading:

`Nish's ANC Engine:`

`High`

Both lines centred, floating approximately one-third down from the top of the screen, with elegant red-and-white swoosh effects that materialise and disappear locally.

Build it, install it, and test it on the connected device.