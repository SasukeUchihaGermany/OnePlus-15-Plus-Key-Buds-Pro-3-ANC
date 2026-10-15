# Fully uninstall and restore debugging settings

These are instructions to run when you choose to remove the setup. Publishing this guide does **not** uninstall or change the working phone. Menu labels below are from the tested OxygenOS 16 phone and can change with firmware.

The controller is `dev.local.anc` (**Plus Key ANC**). Its privilege bridge is `moe.shizuku.privileged.api` (**Shizuku**). No root, bootloader changes, earbud reset, Bluetooth re-pairing, Tasker profile or native Plus Key reassignment was installed by this project.

## 1. Release the key and remove controller access

1. Open Plus Key ANC and tap **STOP**. This disables its saved startup preference, releases the key grab and stops its foreground service/helper. If the app cannot open, uninstall it and reboot at the end to terminate any remaining helper.
2. In Shizuku's authorized applications list, revoke Plus Key ANC's authorization. This is separate from Android's Nearby devices permission.
3. In the controller's App info, restore normal battery optimization/background restrictions and disable any auto-launch allowance you added. Alternatively, while USB debugging still works, remove its exemption with the first command below.
4. Uninstall Plus Key ANC through App info, or use the second command. Do not use `-k`: retaining app data is unnecessary for a complete removal.

```powershell
adb shell cmd deviceidle whitelist -dev.local.anc
adb uninstall dev.local.anc
```

Uninstall removes its private preferences/data and Android permission grants, including Nearby devices, notifications and the installed boot receiver/provider. No additional accessibility, overlay, location, Internet or log-reading permission is used by the final app. The temporary accessibility probe was removed during development; preserve other apps' accessibility services rather than clearing the entire accessibility list.

## 2. Remove Shizuku's automatic startup privileges

Shizuku may be used by other apps. Full removal below disables their Shizuku access too. If keeping it for other purposes, revoke only this controller's authorization, but still remove its WRITE_SECURE_SETTINGS privilege before resetting debugging: its boot receiver would otherwise re-enable USB/wireless ADB and non-expiring authorization.

While ADB is still available, run:

```powershell
adb shell cmd deviceidle whitelist -moe.shizuku.privileged.api
adb shell pm revoke moe.shizuku.privileged.api android.permission.WRITE_SECURE_SETTINGS
```

Check that revocation succeeds. On this phone the grant already existed before this project; revoking it is part of returning debugging to defaults, rather than an exact restoration of every pre-project app grant. If OxygenOS rejects revocation, uninstalling Shizuku removes its package permissions instead. Do not proceed with a retained privileged boot receiver and assume debugging will stay off.

For full removal, stop Shizuku using its manager's Stop action if available, then uninstall its manager through App info or:

```powershell
adb uninstall moe.shizuku.privileged.api
```

Uninstalling the manager removes its private pairing key, stored authorizations/preferences and Android grants. It may not immediately terminate an already running shell server; the final reboot below does. If retaining Shizuku, revoke its WRITE_SECURE_SETTINGS as above, restore its normal battery/auto-launch settings, and clear its app storage if you also want to remove its stored pairing key and authorizations. Clearing storage resets other Shizuku clients' setup.

## 3. Restore ADB authorization expiry before disconnecting

The Shizuku boot path set `adb_allowed_connection_time=0`, meaning authorization does not expire. The value was absent before that boot setup. Restore the absent setting so Android uses its firmware default instead of hardcoding a guessed timeout:

```powershell
adb shell settings delete global adb_allowed_connection_time
adb shell settings get global adb_allowed_connection_time
```

The second command should return `null`. In Developer options, if **Disable adb authorization timeout** is shown, ensure it is **OFF**. This restores the firmware's normal expiry policy; explicitly revoking saved authorizations in the next step removes existing trust immediately.

## 4. Forget pairing and turn debugging off on the phone

Do the following on the phone after the preceding commands; revoking the PC authorization or disabling debugging ends the ADB connection.

1. Developer options > **Wireless debugging**: open each entry under **Paired devices** and choose **Forget**. This includes Shizuku and any PC pairing. Forgetting all entries intentionally removes all wireless-debugging trust, including pairings unrelated to this project, to return to an unpaired default.
2. Turn **Wireless debugging OFF**.
3. Developer options > **Revoke USB debugging authorizations**: confirm. This removes authorization for all previously trusted computers, including unrelated ones; they will need approval again if you later re-enable debugging.
4. Turn **USB debugging OFF**. If an OEM **USB debugging (Security settings)** or **Install via USB** switch is enabled and you want a default debugging setup, turn that off too. This project did not enable those extra switches.
5. Turn **Disable system optimisation OFF** (older firmware calls the related control **Disable permission monitoring**). This restores the OEM protection relaxed during setup. The final expected `persist.sys.permission.enable` value is `true`; do not use a direct `setprop` command, which was denied during setup. Restore this while Developer options is still accessible.
6. Optionally turn the Developer options master switch OFF once the individual settings above are restored. The master switch alone is not a substitute for forgetting pairing, revoking authorizations or restoring the OEM optimization switch.
7. Reboot the phone. This terminates the remaining Shizuku shell server/helper. With Shizuku removed or its secure-settings privilege revoked, its boot receiver cannot turn debugging back on through the configured path.

Wi-Fi and ordinary Bluetooth can stay enabled. Do not reset network settings or unpair/reset the earbuds. The native Plus Key assignment was left unchanged; it should resume its original action once the helper releases the key. Tasker, Nova and their permissions are unrelated and should remain untouched.

## 5. Verify completion

- Plus Key ANC is absent from installed apps; Shizuku is also absent if you chose full removal.
- No ANC controller notification remains after reboot, and the Plus Key performs its native action.
- Wireless debugging and USB debugging remain OFF after reboot; no paired wireless-debugging devices remain.
- Disable system optimisation is OFF. Any added app auto-launch allowances and battery exemptions have been removed.
- Connecting the USB cable does not provide an authorized ADB session while debugging is off.

If you keep ADB enabled temporarily to audit the cleanup before the final UI steps, inspect only these targeted values:

```powershell
adb shell settings get global adb_allowed_connection_time
adb shell getprop persist.sys.permission.enable
adb shell cmd deviceidle whitelist
adb shell ps -A
```

Check the whitelist/process lists locally only for `dev.local.anc` and `moe.shizuku.privileged.api`/`shizuku_server`; do not export unrelated app/process data. A process may remain until reboot. Do not re-enable debugging solely to verify that it was disabled.

## Optional cleanup on the PC

Delete this project's downloaded APK, clone and tool/build caches if no longer wanted. Keep a private backup of `.local/local.p12` only if you want to sign compatible future updates; otherwise delete that project-specific signing key too. Never delete the PC's global Android SDK, Tasker configuration, SSH credentials or `%USERPROFILE%\.android\adbkey*` as routine project cleanup: those can be shared with unrelated work. Revoking trust on the phone already prevents the old PC key from accessing it without fresh approval. `adb disconnect` and `adb kill-server` can stop local ADB connections/server; neither revokes phone-side trust or removes pairing records by itself.
