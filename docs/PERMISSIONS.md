# Android permissions & discovery limits

VITRA's BLE scan path relies on Android Bluetooth APIs. Permission requirements vary by OS release and device policy.

| Permission | Purpose in this snapshot |
|---|---|
| `BLUETOOTH` / `BLUETOOTH_ADMIN` (API ≤ 30) | Legacy Bluetooth access |
| `ACCESS_FINE_LOCATION` (API ≤ 30) | Legacy BLE scan permission requirement |
| `BLUETOOTH_SCAN` (API 31+) | Nearby BLE scanning |
| `BLUETOOTH_CONNECT` (API 31+) | Bluetooth adapter/device access used by the app |
| `ACCESS_WIFI_STATE` | Declared network-awareness permission |
| `ACCESS_NETWORK_STATE` | Declared network-awareness permission |

The app requests Bluetooth permissions when the user starts a scan. Granting a permission does not guarantee scan results: Bluetooth must be enabled, hardware/OS behavior must permit scanning, and nearby devices must be advertising in a detectable way.

The manifest currently marks BLE scanning with `neverForLocation`. Android may filter some beacon results under this declaration. Device/OEM behavior can differ.

VITRA displays observations exposed by the platform. It does not bypass Android permission boundaries, decode arbitrary RF, or guarantee detection of every nearby device. A missing result is not proof that no device is present.
