---
name: oneplus-buds-protocol
description: Continue this project's OnePlus Buds Pro 3 Android RFCOMM investigation using captured protocol evidence.
---

Read ../../PROGRESS.md and ../../RESEARCH.md. The public 079A vendor UUID connected to channel 15 on CPH2747 Android 16; fixed-channel reflection is unnecessary for that verified path. Never use Windows sockets as a substitute for on-phone validation.

Current mode query `aa0900000c010002000101` returned OFF as `aa0c00000c810005000001010800`. Verify freshness after external changes and writes. Selected AUTO and its adaptive strength are different fields. Do not let field 04 overwrite selected field 01. Preserve exact reference handshake while investigating its atypical HELLO length.

Commands were rejected with status 0x0E before the owner wore the earbuds, then all six modes succeeded while worn. Strength-only HIGH activated ANC from both OFF and TRANSPARENCY in dedicated trials (11:18:29 / 11:18:31). The final sender therefore sends just the strength packet for ANC levels. Do not reintroduce the redundant ANC-On packet without evidence.

Queries echo the request sequence byte at index 6. Match it to avoid consuming late replies. Never use the UI's last-observed mode for transitions. Single/hold query first; double can skip its pre-query because its target is always AUTO. All sets still require a matching state query. Raw registration frames can include device names; retain only ANC-specific frames.

The native com.oplus.melody Earbud functions page competed for the same control channel and closed our socket. A targeted force-stop during investigation released it; do not add automatic repeated force-stops to the controller or treat this as proven transparent coexistence.

Sources: https://github.com/nic0manz/oneplus_buds3_pro_dotnet/blob/ecb5ab8ae502f8993c3378cc81dd817791dc9b18/OnePlusBudsPro3/BudsConnection.cs and https://developer.android.com/reference/android/bluetooth/BluetoothDevice .
