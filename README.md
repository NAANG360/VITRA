<div align="center">

# V I T R A
### FIELD INTELLIGENCE · LOCAL SIGNAL DISCOVERY · AURORA GLASS

**A pocket-sized observation deck for the devices and services around you.**

![Android](https://img.shields.io/badge/PLATFORM-Android-8B7CFF?style=for-the-badge&logo=android&logoColor=white) ![Java + AArch64](https://img.shields.io/badge/JAVA%20%2B%20AArch64-NATIVE-54D6FF?style=for-the-badge) ![Termux](https://img.shields.io/badge/BUILD-Termux-7C5CFF?style=for-the-badge&logo=gnubash&logoColor=white) ![Experimental](https://img.shields.io/badge/STATUS-Experimental-FFB86B?style=for-the-badge)

*See what is observable. Keep evidence separate from assumptions.*

</div>

---

## ◈ The VITRA signal

VITRA is an Android-native, Aurora-styled exploration interface built around nearby-signal observations and local-service discovery. Its visual language is deep navy, luminous cyan/violet, translucent glass panels, and a restrained heads-up-display layout—not a terminal pretending to be an app.

This repository contains the Termux-built source snapshot, including the BLE scan timer/UI-refresh fix and a manual APK packaging script.

> **Reality check:** VITRA reports what Android exposes to it. A BLE advertisement is not a verified identity; an advertised service is not proof that a device is secure, reachable, or controllable. No scan result is fabricated to make Radar look busy.

## ✦ Modules

| Module | Role |
|---|---|
| **RADAR / BLE** | Android BLE observations, exposed name/address, RSSI, callback/error diagnostics, bounded scan window. |
| **Scan lifecycle** | Freezes elapsed time after scan completion; periodic UI refresh avoids stale timeout display. |
| **Aurora interface** | Custom Java/Canvas + OpenGL ES visual layer. |
| **ARM64 JNI** | Small AArch64 assembly helper used by the existing visual pulse path. |
| **Manual build** | `javac` → D8 → `clang` → AAPT2 → `zipalign` → `apksigner`; no Gradle. |

### Evidence boundaries

- BLE names may be absent or randomized; RSSI fluctuates with environment and device behavior.
- Scan availability depends on Android version, permissions, Bluetooth state, and OEM behavior.
- A Matter-related advertisement is only a clue—not proof of certification, commissioning, or control.
- VITRA does not claim cellular interception, arbitrary RF decoding, or unrestricted network visibility.

## ⬡ Build in Termux

The included script expects the project’s existing toolchain: Java JDK, `clang`, `zip`, Android SDK platform `android-33`, D8 from Build Tools `35.0.0`, and AAPT2/`zipalign`/`apksigner` from `34.0.4`.

The developer's signing key is intentionally **not included**. The script expects it at `~/vitra/vitra-release.jks` with alias `vitra`. Never commit a keystore, signing password, private token, or personal scan dump.

```bash
bash ./build-termux.sh
```

Output: `out/VITRA-overhaul.apk`, also copied to `~/storage/shared/Download/VITRA-Overhaul/VITRA-overhaul.apk`.

Signature verification is **not** an install/runtime test. If Android rejects the APK, inspect its archive structure and compiled manifest; a successful signature check alone does not prove installability.

## 🧭 Repository map

```text
.
├── README.md
├── BUILD-IN-TERMUX.txt
├── SCAN-TIMER-FIX.txt
├── build-termux.sh
└── app/src/main/
    ├── AndroidManifest.xml
    ├── asm/vitra.S
    └── java/com/vitra/glass/MainActivity.java
```

## ⚙️ Status & limitations

**Experimental / device-validation required.** BLE callbacks and timeout behavior were observed during development, but this source snapshot has not been independently built or runtime-tested in CI. The current ARM64 routine is a small animation helper—not a signal-analysis engine. BLE is the concrete live discovery path; broader network correlation, device intelligence, and Studio controls remain limited.

## 🛡️ Use responsibly

Use VITRA only to observe broadcasts and services on devices/networks you own or are authorized to assess. An observed identifier is not identity verification, consent, or permission to connect.

## 🧪 Bug reports

Include Android version/device model, source revision, permission/Bluetooth state, exact diagnostics, and reproduction steps. Share the **first failing build command** and its output; redact secrets.

<div align="center">

**VITRA** · *Observe the signal. Respect the boundary.*

<sub>Built in Termux. Rendered in Aurora. Evidence first.</sub>

</div>