# NZeyMod Manager

> Manager untuk ROM NZeyMod (Virtual Master Android 11, identitas Pixel 5).
> Menggabungkan part ROM, memverifikasi SHA-256, mengacak identitas,
> mengatur profil Magisk, dan mengganti keybox — tanpa Virtual Master premium.

**Status: Dalam Tahap Pengembangan — Beta Tester**

Proyek ini masih dalam tahap pengembangan. Fitur, tampilan, dan performa
dapat berubah. Laporan bug dan hasil pengujian sangat membantu pengembangan
selanjutnya.

## Tentang Proyek

NZeyMod Manager adalah aplikasi pendamping untuk ROM NZeyMod dari
repositori `KiZakiXD/nzeymod-rom` (repositori privat).

Aplikasi ini **bukan** pengganti Virtual Master dan **bukan** emulator
Android di dalam HP. Menjalankan Android 11 di dalam HP membutuhkan
hypervisor / emulator, dan itu tidak dapat dibangun menjadi satu APK
dalam sekali jalan. ROM tetap dijalankan di virtual machine.

Yang dilakukan Manager:

1. **Gabung ROM** — menggabungkan 5 part ROM (masing-masing ≤ 90 MB,
   total ± 405 MB) menjadi satu file utuh.
2. **Verifikasi SHA-256** — memeriksa hasil gabungan terhadap hash resmi
   `1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c`.
3. **Acak identitas** — membuat identitas perangkat baru (Pixel 5 /
   redfin) dan menyimpan / memulihkan profil.
4. **Profil Magisk** — mengatur modul Magisk ON / OFF per profil
   (Zygisk Next, Play Integrity Fork, Shamiko, LSPosed, Tricky Store).
5. **Ganti keybox** — mengganti keybox dan memeriksa masa berlakunya.
6. **Buka NZey Master** — menjalankan ROM di NZey Master, yaitu
   Virtual Master 3.2.68 yang sudah di-patch (VIP aktif) dan di-rebrand,
   yang sudah ada di folder `vmaster-remake` pada repositori ROM.
   File APK-nya (± 145 MB) di-host di luar GitHub.

## Catatan Penting

- Keybox yang ada di repositori saat ini hanya ECDSA dan **kedaluwarsa
  28 November 2026**. Keybox pengganti perlu disiapkan sebelum tanggal
  tersebut, jika tidak Play Integrity akan bermasalah.
- Play Integrity STRONG tidak dapat dijamin di virtual machine mana pun.
  Hal ini sudah ditulis di README repositori ROM.
- Modul Magisk dan keybox **tidak ada** di dalam repositori, hanya
  daftar dan hash-nya di `docs/modules-report.md`. Manager meminta file
  tersebut diimpor, bukan menganggap file tersebut sudah ada.
- Dua ketidakcocokan di repositori sudah diperbaiki di Manager ini,
  lihat `docs/fixes.md`.

## APK yang sudah dibangun

- `NZeyMod-Manager-debug.apk` — 42 KB, paket `com.kizakixd.nzeymodmanager`, versi 1.0-beta (versionCode 1), minSdk 26, targetSdk 34.
- SHA-256: `d6e6f82587eab4920fb1c245bdc1cc3a0ae110c5dd566aaf8925d6009e5acec4`
- Terverifikasi dengan `apksigner verify`: skema v2 dan v3, 1 penanda tangan. `aapt2 dump badging` mengonfirmasi paket, versi, dan SDK di atas.
- APK ini **belum diuji di perangkat fisik**. Pengujian yang dilakukan baru berupa verifikasi statis dan pengujian logika identitas (1.000 IMEI acak: semuanya 15 digit, TAC valid, dan lolos Luhn; vektor Luhn yang dikenal `490154203237518` terverifikasi).

## Mencoba v0.1

1. Salin APK ke HP.
2. Di HP, izinkan instalasi dari sumber tidak dikenal.
3. Buka APK, izinkan akses penyimpanan saat diminta.
4. Pilih folder berisi 5 part ROM, tekan **Gabung ROM**, tunggu sampai
   verifikasi SHA-256 selesai.

Build memakai pipeline manual yang sama dengan Voice-Changer-Beta-Android
(`android/build-manual.sh`), karena Gradle daemon tidak berjalan di VM ini.
