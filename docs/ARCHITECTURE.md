# Architecture

## Runtime shape

VITRA is a single Android `Activity` (`com.vitra.glass.MainActivity`) with a layered view:

- `GLSurfaceView` renders an animated GLSL Aurora background using OpenGL ES 2.0.
- A custom `Canvas` `View` draws the glass-like interface and handles tab/touch interactions.
- Android's Bluetooth LE APIs provide nearby advertisement observations.
- Java enumerates visible local network interfaces for a small Core status summary.
- A JNI-exported AArch64 assembly function supplies a small animation-time adjustment.

## Data path

BLE scan callbacks update an in-memory list keyed by the exposed BLE address. The overlay reads this list to render names, RSSI, and diagnostic counters. The app does not currently persist scan history or perform authoritative device identification.

## Important constraints

- BLE names may be missing and addresses may be randomized.
- RSSI is an approximate radio measurement, not distance.
- The Matter-related byte-pattern check is heuristic and explicitly unverified.
- Local interface enumeration does not discover LAN hosts.
- The assembly helper is a tiny visual utility, not a discovery or signal-analysis engine.

## Source layout

- `app/src/main/java/com/vitra/glass/MainActivity.java` — Activity, BLE lifecycle, UI, GL renderer
- `app/src/main/AndroidManifest.xml` — app declaration and permissions
- `app/src/main/asm/vitra.S` — AArch64 JNI helper
- `build-termux.sh` — manual on-device packaging pipeline
