# Building VITRA in Termux

The current project uses a manual build script rather than Gradle. Run it from the repository root or invoke it by path:

```bash
bash ./build-termux.sh
```

## Expected tools

- Java JDK (`javac`, `jar`)
- `clang` and `zip`
- Android SDK platform `android-33`
- D8 from Build Tools `35.0.0`
- AAPT2, `zipalign`, and `apksigner` from Build Tools `34.0.4`

The script expects the SDK under `~/android-sdk`. Native assembly is compiled for `aarch64-linux-android24`.

## Signing

The script expects a developer-managed keystore at `~/vitra/vitra-release.jks`, alias `vitra`. The keystore is intentionally absent from version control. If your key is stored elsewhere, edit the local script path—do not upload the key or its password.

## Output

The APK is emitted to `out/VITRA-overhaul.apk` and copied to:

`~/storage/shared/Download/VITRA-Overhaul/VITRA-overhaul.apk`

## Validation

The script's signature verification checks signing integrity only. It does not prove Android will install the APK or that the Activity/native library will run correctly. Test installation and runtime behavior on a device. If installation fails, inspect APK entries (especially root-level `classes.dex`), manifest metadata, and the package-manager error before assuming signing is the cause.
