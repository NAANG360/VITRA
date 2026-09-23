#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK="$HOME/android-sdk"
PLATFORM="$SDK/platforms/android-33/android.jar"
D8="$SDK/build-tools/35.0.0/d8"
AAPT2="$SDK/build-tools/34.0.4/aapt2"
ZIPALIGN="$SDK/build-tools/34.0.4/zipalign"
APKSIGNER="$SDK/build-tools/34.0.4/apksigner"
KEY="$HOME/vitra/vitra-release.jks"
ALIAS="vitra"
OUT="$ROOT/out"
for f in "$PLATFORM" "$D8" "$AAPT2" "$ZIPALIGN" "$APKSIGNER" "$KEY"; do
  [ -e "$f" ] || { echo "Missing required file: $f" >&2; exit 2; }
done
command -v javac >/dev/null || { echo 'Missing javac (Java JDK required)' >&2; exit 2; }
command -v clang >/dev/null || { echo 'Missing clang (pkg install clang)' >&2; exit 2; }
command -v zip >/dev/null || { echo 'Missing zip (pkg install zip)' >&2; exit 2; }
command -v jar >/dev/null || { echo 'Missing jar (Java JDK required)' >&2; exit 2; }
rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex" "$OUT/lib/arm64-v8a" "$OUT/linked"
find "$ROOT/app/src/main/java" -name '*.java' -print0 | xargs -0 javac -source 8 -target 8 -encoding UTF-8 -classpath "$PLATFORM" -d "$OUT/classes"
jar cf "$OUT/classes.jar" -C "$OUT/classes" .
"$D8" --lib "$PLATFORM" --output "$OUT/dex" "$OUT/classes.jar"
clang --target=aarch64-linux-android24 -fPIC -shared -Wl,-soname,libvitra.so "$ROOT/app/src/main/asm/vitra.S" -o "$OUT/lib/arm64-v8a/libvitra.so"
"$AAPT2" link -o "$OUT/base-res.apk" -I "$PLATFORM" --manifest "$ROOT/app/src/main/AndroidManifest.xml" --min-sdk-version 23 --target-sdk-version 33
cp "$OUT/base-res.apk" "$OUT/unsigned.apk"
(cd "$OUT/dex" && zip -q -u "$OUT/unsigned.apk" classes.dex)
cd "$OUT"
zip -q -u unsigned.apk lib/arm64-v8a/libvitra.so
"$ZIPALIGN" -f 4 unsigned.apk aligned.apk
"$APKSIGNER" sign --ks "$KEY" --ks-key-alias "$ALIAS" --out VITRA-overhaul.apk aligned.apk
"$APKSIGNER" verify --verbose VITRA-overhaul.apk
mkdir -p "$HOME/storage/shared/Download/VITRA-Overhaul"
cp VITRA-overhaul.apk "$HOME/storage/shared/Download/VITRA-Overhaul/VITRA-overhaul.apk"
echo "Build complete: $HOME/storage/shared/Download/VITRA-Overhaul/VITRA-overhaul.apk"
echo "Signature verification is not an install/runtime test."