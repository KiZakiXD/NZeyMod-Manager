# NZeyMod Launcher — Build Manual

Aplikasi Android native (Java saja, tanpa Gradle, tanpa dependensi eksternal).
Paket: `com.kizakixd.nzeymodmanager`, label NZeyMod Launcher, minSdk 26,
targetSdk 34, versionCode 2, versionName 2.0-beta.

## Prasyarat

- JDK 17 di `$HOME/opt/jdk-17*` (atau set `JAVA_HOME`)
- Android SDK di `$HOME/Android/sdk` (atau set `ANDROID_HOME`)
  - build-tools 34.0.0 (`aapt2`, `d8`, `zipalign`, `apksigner`)
  - platform `android-34` (`android.jar`)

## Build

```bash
cd ~/workspace/nzeymod-manager/android
chmod +x build-manual.sh
./build-manual.sh
```

Hasil: `app/build/manual/app-debug.apk`

## Alur pipeline

1. `aapt2 compile` — kompilasi resource (`res/`)
2. `aapt2 link` — gabung APK + hasilkan `R.java`
3. `javac` — kompilasi `MainActivity.java` + `R.java` terhadap `android.jar`
4. `d8` — ubah `.class` menjadi `classes.dex`
5. Tambah `classes.dex` ke APK, lalu `zipalign`
6. `apksigner` — tanda tangani dengan keystore debug, lalu verifikasi

## Catatan

- Tidak ada Gradle dan tidak ada Kotlin; seluruh sumber hanya memakai
  API `android.*` dan `java.*` yang tersedia di API 34.
- Karena tidak ada dependensi androidx, hasil Activity memakai
  `onActivityResult` / `startActivityForResult` (bukan
  `registerForActivityResult`, yang berasal dari androidx).
- Identitas dan profil disimpan di direktori file eksternal aplikasi,
  `Android/data/com.kizakixd.nzeymodmanager/files/NZeyMod/`.
- Aplikasi ini BUKAN mesin virtual; aplikasi ini hanya menyiapkan dan
  mengimpor ROM, lalu meluncurkan NZey Master. ROM berjalan di dalam
  NZey Master.
