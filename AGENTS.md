# OnePlus ANC controller

Read RECOVERY.md, VALIDATION.md, PROGRESS.md and RESEARCH.md before changing device configuration. User authorizes building, installing and testing a local Android controller. No root, bootloader changes, system partition writes, earbud reset/unpair, unrelated data collection, or screenshot deletion.

Keep proof stages separate: verify Android RFCOMM control and actual-state reads before hardware integration. Socket write is not confirmation. Record every real-device observation separately from host tests. Never pass an unperformed acceptance test.

Use only targeted app/protocol logs. Preserve current native Plus Key settings. Passive log observation is not interception. Do not use the upstream remapper's silence-derived release heuristic as a verified physical UP event.

Build/test scripts are portable: pass -Jdk and -Sdk as needed. Intermediates go in .build/; private signing key in .local/. Never commit keys or device identifiers. Historical investigation checkouts are outside the repository. Deliver source and APKs inside this directory. Diagnostic components must not expose unrestricted background Bluetooth commands to other apps.
