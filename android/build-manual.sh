#!/bin/bash
# Build manual APK NZeyMod Manager tanpa Gradle (Java saja).
#
# Dipakai karena Gradle daemon di VM ini tidak bisa berkomunikasi
# (koneksi TCP loopback antar proses Java diblokir sandbox),
# sementara aapt2 / javac / d8 / apksigner berjalan lokal tanpa TCP.
# Hasilnya sama dengan ./gradlew assembleDebug: APK debug yang
# bisa diinstal. Untuk build normal di Android Studio, tetap
# pakai Gradle.
set -e
cd "$(dirname "$0")"

JAVA_HOME="${JAVA_HOME:-$(echo $HOME/opt/jdk-17*)}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

SDK="${ANDROID_HOME:-$HOME/Android/sdk}"
AAPT2="$SDK/build-tools/34.0.0/aapt2"
D8="$SDK/build-tools/34.0.0/d8"
ZIPALIGN="$SDK/build-tools/34.0.0/zipalign"
APKSIGNER="$SDK/build-tools/34.0.0/apksigner"
ANDROID_JAR="$SDK/platforms/android-34/android.jar"

OUT="app/build/manual"
rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

echo "== 1. aapt2 compile (resources) =="
"$AAPT2" compile --dir app/src/main/res -o "$OUT/compiled.zip"

echo "== 2. aapt2 link (APK + R.java) =="
"$AAPT2" link -o "$OUT/app-unsigned.apk" \
  --manifest app/src/main/AndroidManifest.xml \
  -I "$ANDROID_JAR" \
  --java "$OUT/gen" \
  --min-sdk-version 26 --target-sdk-version 34 \
  --version-code 1 --version-name "1.0-beta" \
  --auto-add-overlay \
  "$OUT/compiled.zip"

echo "== 3. javac (Java -> class) =="
"$JAVA_HOME/bin/javac" -J-Xmx768m \
  -encoding UTF-8 \
  -source 17 -target 17 \
  -cp "$ANDROID_JAR" \
  -d "$OUT/classes" \
  app/src/main/java/com/kizakixd/nzeymodmanager/*.java \
  $(find "$OUT/gen" -name "*.java")

echo "== 4. d8 (class -> dex) =="
(cd "$OUT/classes" && "$JAVA_HOME/bin/jar" cf "../classes.jar" .)
"$D8" --lib "$ANDROID_JAR" --output "$OUT/dex" \
  "$OUT/classes.jar"

echo "== 5. tambah classes.dex ke APK =="
python3 - "$OUT/app-unsigned.apk" "$OUT/dex/classes.dex" << 'PY'
import sys, zipfile
apk, dex = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(apk, "a", zipfile.ZIP_DEFLATED) as z:
    z.write(dex, "classes.dex")
print("dex ditambahkan")
PY

echo "== 6. zipalign =="
"$ZIPALIGN" -f 4 "$OUT/app-unsigned.apk" "$OUT/app-aligned.apk"

echo "== 7. keystore debug (jika belum ada) =="
if [ ! -f "$OUT/debug.keystore" ]; then
  keytool -genkeypair -keystore "$OUT/debug.keystore" \
    -alias androiddebugkey -storepass android -keypass android \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" -noprompt
fi

echo "== 8. apksigner =="
"$APKSIGNER" sign --ks "$OUT/debug.keystore" \
  --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/app-debug.apk" "$OUT/app-aligned.apk"

echo "== 9. verifikasi =="
"$APKSIGNER" verify --verbose "$OUT/app-debug.apk"
"$AAPT2" dump badging "$OUT/app-debug.apk" | head -5
ls -lh "$OUT/app-debug.apk"
