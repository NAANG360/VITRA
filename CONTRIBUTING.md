# Contributing to VITRA

Thanks for your interest in VITRA. Contributions should keep the project useful, transparent, and evidence-first.

## Before opening a change

- Search existing issues and pull requests for related work.
- Describe the behavior you intend to change and the Android versions/devices affected.
- Keep changes focused; avoid bundling unrelated refactors.

## Engineering principles

1. **Evidence over inference.** Distinguish observed values from guesses; show confidence and source when adding inference.
2. **No fabricated telemetry.** Empty scans and unsupported capabilities are valid states.
3. **Android-aware behavior.** Respect runtime permissions, API-level differences, and OEM restrictions.
4. **Privacy by default.** Avoid transmitting, persisting, or logging device identifiers unless necessary and clearly disclosed.
5. **Honest status.** Do not describe a feature as tested unless it was actually exercised on-device.

## Testing notes

For Android or BLE changes, include:

- Android version and device model
- Permissions granted and Bluetooth state
- Reproduction/test steps
- Relevant build output and observed behavior

The current project uses a manual Termux build pipeline. See `BUILD-IN-TERMUX.txt` and `docs/BUILDING.md`.

## Pull requests

Include a concise summary, rationale, limitations, and any screenshots from a real build. Never include signing keys, passwords, private tokens, personal scan dumps, or unrelated device data.
