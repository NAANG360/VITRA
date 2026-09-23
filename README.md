<div align="center">

# V I T R A
### A U R O R A   /   L O C A L   S I G N A L   O B S E R V A T I O N

**A pocket-sized observation deck for the devices and services around you.**

![Android](https://img.shields.io/badge/PLATFORM-ANDROID-8B7CFF?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/CORE-JAVA-54D6FF?style=for-the-badge&logo=openjdk&logoColor=white)
![ARM64](https://img.shields.io/badge/NATIVE-AARCH64-7C5CFF?style=for-the-badge)
![Build](https://img.shields.io/badge/BUILD-TERMUX-10182D?style=for-the-badge&logo=gnubash&logoColor=white)
![Status](https://img.shields.io/badge/STATUS-EXPERIMENTAL-FFB86B?style=for-the-badge)

*Observe the signal. Respect the boundary.*

</div>

---

<div align="center">

`01 / RADAR`　·　`02 / AURORA`　·　`03 / CORE`　·　`04 / EVIDENCE`

</div>

## ◈ The VITRA signal

VITRA is an experimental Android-native exploration interface built with a custom Java/Canvas overlay and an OpenGL ES Aurora background. Its visual language is deep navy, luminous cyan and violet, translucent panels, and restrained instrumentation—not a terminal cosplaying as an app.

The current snapshot centers on **BLE observation**, a bounded scan lifecycle, on-device diagnostics, and basic local-interface awareness. It is intentionally evidence-first: a radio observation is not a verified identity, and a pretty interface is not permission to overclaim.

## ✦ Inside the glass

| Module | What it currently does |
|:--|:--|
| **RADAR / BLE** | Displays Android BLE scan observations, exposed name/address, RSSI, and callback/error diagnostics. |
| **Scan lifecycle** | Uses a 15-second scan window; freezes elapsed time at stop and refreshes the overlay periodically. |
| **AURORA** | OpenGL ES animated background behind a custom Canvas UI. |
| **STUDIO** | Basic Aurora, deep-mode, and motion toggles. |
| **CORE** | Reports scan state and visible local interface addresses. |
| **ARM64 JNI** | A small AArch64 animation helper, loaded through JNI. It is not a signal-processing engine. |

## ⬡ Build it on-device

VITRA's current build path is a manual Termux pipeline—**no Gradle wrapper**. The included script expects the toolchain paths documented in `BUILD-IN-TERMUX.txt` and your existing local signing key.

```bash
bash ./build-termux.sh
```

The script compiles Java, runs D8, assembles the ARM64 JNI library, links the Android manifest, packages, aligns, signs, and verifies the APK signature. Output is written under `out/` and copied to your shared Downloads folder.

> **Build ≠ validation.** A valid signature does not prove that Android can install or run the APK. Device/OEM behavior and runtime testing still matter. Do not treat this repository as a verified release build.

### Toolchain snapshot

- Android SDK platform: `android-33`
- D8: Build Tools `35.0.0`
- AAPT2, zipalign, apksigner: Build Tools `34.0.4`
- Java JDK + `clang` + `zip`
- ARM64 native target: `aarch64-linux-android24`

The signing keystore is deliberately **not tracked**. The build script expects the developer's existing key at `~/vitra/vitra-release.jks` (alias `vitra`). Keep keystores, passwords, tokens, scan dumps, and private device data out of Git.

## ◉ Evidence boundaries

- BLE names can be absent; addresses may be randomized; RSSI is approximate and environment-dependent.
- Scan behavior depends on Android version, runtime permissions, Bluetooth state, and OEM implementation.
- The current Matter-related byte-pattern heuristic is only an **unverified clue**. It is not proof of Matter support, certification, commissioning, or control.
- Local interface enumeration is not a LAN host scan.
- VITRA does not provide cellular interception, arbitrary RF decoding, or unrestricted network visibility.

## ⌘ Repository map

```text
VITRA/
├── README.md
├── BUILD-IN-TERMUX.txt
├── SCAN-TIMER-FIX.txt
├── build-termux.sh
├── .gitignore
├── CONTRIBUTING.md
├── SECURITY.md
├── docs/
│   ├── ARCHITECTURE.md
│   ├── BUILDING.md
│   └── PERMISSIONS.md
└── app/src/main/
    ├── AndroidManifest.xml
    ├── asm/vitra.S
    └── java/com/vitra/glass/MainActivity.java
```

## 🧭 Roadmap — not a feature claim

- [x] BLE scan observations and bounded scan timeout
- [x] Aurora UI and basic visual toggles
- [x] Local interface summary
- [ ] Reliable mDNS/DNS-SD discovery and endpoint resolution
- [ ] Evidence-backed device/service correlation
- [ ] Persistent, user-controlled observation history
- [ ] Expanded Studio renderer controls
- [ ] Reproducible build + install/runtime validation on multiple Android versions

## 🛡️ Responsible use

Use VITRA only with devices and networks you own or are authorized to assess. Observed broadcasts do not establish identity, consent, reachability, or authorization to connect.

## 🧪 Reporting an issue

Include Android version/device model, app revision, permission and Bluetooth state, exact reproduction steps, and the first failing build command/output. Redact personal identifiers and secrets.

---

<div align="center">

### V I T R A

`BUILT IN TERMUX`　/　`RENDERED IN AURORA`　/　`EVIDENCE FIRST`

<sub>Experimental software. Capabilities are limited by Android APIs, permissions, hardware, and OEM behavior.</sub>

</div>
