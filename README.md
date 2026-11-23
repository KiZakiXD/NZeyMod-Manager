# NZeyMod Launcher

> Launcher untuk ROM NZeyMod (Virtual Master Android 11, identitas Pixel 5).
> Memilih ROM utuh dari penyimpanan, memverifikasi SHA-256, menyiapkan
> impor ke NZey Master, lalu meluncurkan NZey Master — tanpa Virtual Master
> premium. Menggabungkan part ROM, mengacak identitas, mengatur profil
> Magisk, dan mengganti keybox tetap tersedia.

**Status: Dalam Tahap Pengembangan — Beta Tester**

Proyek ini masih dalam tahap pengembangan. Fitur, tampilan, dan performa
dapat berubah. Laporan bug dan hasil pengujian sangat membantu pengembangan
selanjutnya.

## Penting: Launcher ini BUKAN mesin virtual

NZeyMod Launcher **bukan** pengganti Virtual Master dan **bukan** emulator
Android di dalam HP. Menjalankan Android 11 di dalam HP membutuhkan
hypervisor / emulator, dan itu tidak dapat dibangun menjadi satu APK
dalam sekali jalan. Mesin yang menjalankan ROM adalah **NZey Master**
(Virtual Master 3.2.68 remake, VIP sudah di-patch).

Yang dilakukan Launcher:

1. **Jalankan ROM (ROM utuh) — fitur utama v2** — memilih berkas ROM utuh
   (sekitar 405 MB) dari **folder mana pun di penyimpanan** melalui
   file picker. Nama berkas dan folder sumber bebas — verifikasi
   berdasarkan isi berkas (SHA-256), bukan nama berkas.
   SHA-256 dan membandingkannya dengan hash resmi
   `1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c`.
   Bila hash tidak cocok, impor ditolak: kemungkinan berkas corrupt,
   bukan ROM NZeyMod utuh, atau masih berupa bagian terpisah.
   Bila lolos, Launcher menyalin berkas ke folder `NZeyMod/` dan
   menampilkan langkah impor, lalu meluncurkan NZey Master.
2. **Impor di NZey Master** — buka NZey Master, tap tombol **+**, tap
   titik tiga, pilih **Import Local ROM**, pilih berkas ROM dari folder
   `NZeyMod/`, tunggu instalasi selesai, lalu jalankan VM.
3. **Gabung ROM (opsional)** — menggabungkan 5 part ROM (masing-masing
   ≤ 90 MB, total ± 405 MB) menjadi satu file utuh. Hanya diperlukan bila
   yang dimiliki masih berupa bagian terpisah, bukan ROM utuh.
4. **Acak identitas** — membuat identitas perangkat baru (Pixel 5 /
   redfin) dan menyimpan / memulihkan profil.
5. **Profil Magisk** — mengatur modul Magisk ON / OFF per profil
   (Zygisk Next, Play Integrity Fork, Shamiko, LSPosed, Tricky Store).
6. **Ganti keybox** — mengganti keybox dan memeriksa masa berlakunya.
7. **Buka NZey Master** — meluncurkan paket `com.clone.android.dual.space`.
   Bila belum terpasang, Launcher menampilkan tautan unduh APK NZey Master
   (± 145 MB, SHA-256
   `2d70516e1f7bd85ca105341b8bd13760fe7484d6b1983e0446a436253d90c602`)
   dan peringatan untuk menghapus Virtual Master lama terlebih dahulu,
   karena tanda tangannya berbeda.

NZeyMod Launcher adalah aplikasi pendamping untuk ROM NZeyMod dari
repositori `KiZakiXD/nzeymod-rom` (repositori privat). ROM ini hanya
dapat dijalankan oleh mesin Virtual Master / NZey Master, bukan oleh
QEMU, Vectras, DroidVM, atau VineOS — format ROM-nya khusus Virtual
Master.

## Catatan Penting

- Keybox yang ada di repositori saat ini hanya ECDSA dan **kedaluwarsa
  28 November 2026**. Keybox pengganti perlu disiapkan sebelum tanggal
  tersebut, jika tidak Play Integrity akan bermasalah.
- Play Integrity STRONG tidak dapat dijamin di virtual machine mana pun.
  Hal ini sudah ditulis di README repositori ROM.
- Modul Magisk dan keybox **tidak ada** di dalam repositori, hanya
  daftar dan hash-nya di `docs/modules-report.md`. Launcher meminta file
  tersebut diimpor, bukan menganggap file tersebut sudah ada.
- Dua ketidakcocokan di repositori sudah diperbaiki di Launcher ini,
  lihat `docs/fixes.md`.

## APK yang sudah dibangun

### v2.0-beta (terbaru)

- `NZeyMod-Manager-v2.0-beta.apk` — 46.375 byte (46 KB), paket
  `com.kizakixd.nzeymodmanager`, versi 2.0-beta (versionCode 2), minSdk 26,
  targetSdk 34, label NZeyMod Launcher.
- SHA-256: `4ace99669148f2c608322c44d636963026c8193b33ce507ce8cda5072c62360e`
- Terverifikasi dengan `apksigner verify`: skema v2 dan v3, 1 penanda tangan.
  `aapt2 dump badging` mengonfirmasi paket, versi, dan SDK di atas.
- APK ini **belum diuji di perangkat fisik**. Pengujian yang dilakukan baru
  berupa verifikasi statis dan pengujian logika identitas (1.000 IMEI acak:
  semuanya 15 digit, TAC valid, dan lolos Luhn; vektor Luhn yang dikenal
  `490154203237518` terverifikasi).

### v1.0-beta

- `NZeyMod-Manager-debug.apk` — 42 KB, paket `com.kizakixd.nzeymodmanager`,
  versi 1.0-beta (versionCode 1), minSdk 26, targetSdk 34.
- SHA-256: `d6e6f82587eab4920fb1c245bdc1cc3a0ae110c5dd566aaf8925d6009e5acec4`
- Terverifikasi dengan `apksigner verify`: skema v2 dan v3, 1 penanda tangan.

## Mencoba v2.0-beta

1. Salin APK ke HP (POCO X7 Pro).
2. Di HP, izinkan instalasi dari sumber tidak dikenal.
3. Buka APK, izinkan akses penyimpanan saat diminta.
4. Di bagian Jalankan ROM, pilih ROM utuh dari folder mana pun di
   penyimpanan (nama berkas bebas, yang penting isinya), tekan
   **Verifikasi ROM Utuh**, tunggu sampai hasil LOLOS.
5. Tekan **Siapkan untuk Impor**, lalu tekan **Buka NZey Master**.
6. Di NZey Master: tap **+**, tap titik tiga, pilih **Import Local ROM**,
   pilih berkas ROM dari folder `NZeyMod/`, tunggu instalasi selesai,
   lalu jalankan VM.

Prototipe web dengan fitur yang sama ada di `prototype/index.html`.

Build memakai pipeline manual yang sama dengan Voice-Changer-Beta-Android
(`android/build-manual.sh`), karena Gradle daemon tidak berjalan di VM ini.
