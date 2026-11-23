# Panduan — NZeyMod Launcher v2.1-beta

## Penting: Launcher ini BUKAN mesin virtual

NZeyMod Launcher bukan mesin virtual dan bukan emulator Android di dalam
HP. Mesin yang menjalankan ROM NZeyMod adalah **NZey Master** (Virtual
Master 3.2.68 remake, VIP sudah di-patch). Launcher menyiapkan ROM utuh
dari penyimpanan, memverifikasi SHA-256, menyalinnya ke folder `NZeyMod/`,
lalu meluncurkan NZey Master untuk diimpor lewat **Import Local ROM**.
ROM ini hanya dapat dijalankan oleh mesin Virtual Master / NZey Master.

## Perangkat yang dituju

Panduan ini ditulis untuk POCO X7 Pro sebagai perangkat host. Perangkat
host adalah perangkat tempat Launcher dan NZey Master dipasang. ROM
Android 11 berjalan di dalam VM (NZey Master), bukan langsung di perangkat
host.

## 1. Persiapan

1. Pastikan ruang kosong di perangkat host minimal 1 GB: sekitar 405 MB
   untuk ROM utuh, dan 139 MB untuk APK NZey Master hasil repackage
   (bila belum terpasang). Bila masih memakai 5 part ROM, sediakan
   sekitar 1,5 GB (405 MB untuk part, 405 MB untuk file ZIP gabungan,
   dan 139 MB untuk APK NZey Master hasil repackage).
2. Siapkan ROM utuh yang sudah dimiliki:
   `rom_11_0_0_1036_3281_M27_NZeyMod.zip` (sekitar 405 MB).
   SHA-256 yang benar adalah:
   `1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c`
3. Sejak v2.1-beta, Virtual Master lama **tidak perlu dihapus**: NZey
   Master hasil repackage memakai paket `com.kizakixd.nzeymaster` dan
   dapat terpasang berdampingan dengan Virtual Master lama. Ini hanya
   berlaku untuk APK hasil repackage (`NZeyMaster-nzey.apk`, 139 MB).
   APK paket lama (145 MB) tetap bentrok dan tetap memerlukan uninstall
   Virtual Master lama terlebih dahulu.
4. Pasang NZey Master hasil repackage bila belum terpasang. APK-nya
   `NZeyMaster-nzey.apk`, berukuran 139 MB (144.895.840 byte), SHA-256
   `224083a8cd2920f67d9c8a67a9f6746afa62820a218d9cb5f7174760a46bb55c`.
   Tautan / berkas rilis tercantum di Launcher bagian NZey Master.
   Launcher tidak mengunduh APK tersebut di dalam aplikasi; unduhan
   dilakukan melalui peramban / aplikasi Unduhan, lalu dipasang dari sana.

## 2. Jalankan ROM utuh (fitur utama v2)

Ini adalah cara yang dipakai bila ROM utuh sudah dimiliki. Menggabungkan
5 bagian ROM tidak diperlukan dalam cara ini.

1. Buka Launcher, masuk ke bagian **Jalankan ROM (ROM Utuh)**.
2. Tekan **Pilih ROM Utuh**, lalu cari dan pilih berkas ROM utuh dari
   folder mana pun di penyimpanan (nama berkas bebas — yang diperiksa
   adalah isi berkas melalui SHA-256, bukan namanya).
   Launcher menampilkan nama berkas dan ukurannya. Ukuran yang diterima
   adalah 380–430 MB. Bila nama berkas berakhiran `.part00` sampai
   `.part04`, itu adalah bagian ROM terpisah — gunakan bagian Gabung ROM
   di bawah, bukan cara ini.
3. Tekan **Verifikasi ROM Utuh**. Launcher menghitung SHA-256 berkas
   (405 MB membutuhkan waktu beberapa saat) dan membandingkannya dengan
   hash resmi di atas.
4. Bila hasil **LOLOS**, tekan **Siapkan untuk Impor**. Launcher menyalin
   berkas ke folder `NZeyMod/` di penyimpanan aplikasi dan menghitung
   ulang SHA-256 salinan tersebut. Bila salinan tidak cocok, hapus salinan
   dan verifikasi ulang.
5. Bila hasil **GAGAL**, impor ditolak dan Launcher menjelaskan
   kemungkinannya: berkas corrupt saat diunduh atau disalin, berkas bukan
   ROM NZeyMod utuh, masih berupa bagian terpisah, atau ukuran jauh dari
   405 MB (berkas terpotong atau belum selesai diunduh). Jangan mengimpor
   berkas yang gagal verifikasi. Periksa sumber berkas, salin atau unduh
   ulang, lalu verifikasi lagi.
6. Tekan **Buka NZey Master**. Bila NZey Master belum terpasang, Launcher
   menampilkan status belum terpasang dan tautan unduhnya.
7. Di NZey Master, lakukan impor:
   - Tap tombol **+** (Tambah).
   - Tap titik tiga (⋮).
   - Pilih **Import Local ROM**.
   - Pilih berkas `rom_11_0_0_1036_3281_M27_NZeyMod.zip` dari folder
     `NZeyMod/`.
   - Tunggu instalasi ROM selesai, lalu jalankan VM.
8. Pada boot pertama, `nzeymod.sh` memasang modul, menerapkan identitas,
   dan menulis log ke `/sdcard/NZeyMod/nzeymod.log` di dalam VM.

Di prototipe web, cara yang sama dapat dicoba: pilih ROM utuh, verifikasi
SHA-256, lalu unduh salinan dengan nama yang benar ke folder `NZeyMod/`.
Peluncuran NZey Master dari peramban tidak dapat dilakukan; langkah impor
tetap dilakukan manual di NZey Master.

## 3. Gabung ROM (opsional, bila yang dimiliki masih 5 bagian)

Langkah ini hanya diperlukan bila yang dimiliki masih berupa 5 bagian
ROM, bukan ROM utuh.

1. Buka Launcher, masuk ke bagian Gabung ROM.
2. Pilih kelima file part. Urutan file ditentukan berdasarkan nama file, dari `.part00` sampai `.part04`:
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part00` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part01` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part02` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part03` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part04` (44,9 MB)
3. Pilih lokasi penyimpanan untuk file hasil gabungan, dengan nama `rom_11_0_0_1036_3281_M27_NZeyMod.zip`.
4. Tunggu proses selesai. Launcher menghitung SHA-256 selama penggabungan.
5. Nilai yang benar adalah:
   `1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c`
6. Jika nilai tidak sesuai, jangan lanjutkan. Unduh ulang part yang rusak dan gabungkan kembali. Setelah hasil gabungan lolos, kembali ke langkah 2 di atas dan impor berkas tersebut ke NZey Master.

Perintah yang setara di Termux atau Linux, bila dilakukan manual, ada di `rom-fixes/join.sh`.

## 4. Buat identitas

1. Masuk ke bagian Acak Identitas.
2. Pilih mode operator: `random` atau salah satu operator (Telkomsel, Indosat Ooredoo, XL Axiata, AXIS, Tri, Smartfren).
3. Tekan tombol acak identitas. Launcher membuat:
   - Android ID: 16 karakter heksadesimal acak.
   - Serial: 16 karakter heksadesimal acak.
   - IMEI: diawali TAC Pixel 5 (35254410, 35482510, atau 35737210), dilanjutkan 6 digit acak, dan diakhiri digit Luhn yang valid.
   - Operator beserta kode MCCMNC-nya.
4. Simpan hasilnya. Launcher menulis `identity.json` dan `config.prop` ke folder `NZeyMod/` di penyimpanan aplikasi, dan dapat membagikannya sebagai file.
5. Untuk memakai kembali identitas yang sama di lain waktu, simpan sebagai profil dengan nama yang jelas, misalnya `shopee-bersih` atau `utama`.

## 5. Profil Magisk

- **Magisk OFF (profil bersih)**: tanpa root dan tanpa modul. Profil ini ditujukan untuk aplikasi yang sensitif terhadap root.
- **Magisk ON (profil root)**: modul dipasang di dalam VM saat boot pertama oleh `nzeymod.sh`, dari folder `/system/nzeymod/modules/`. Daftar modul:
  - Zygisk Next v1.2.8
  - Play Integrity Fork v18.4.1
  - Shamiko v1.2.1
  - LSPosed v1.10.1-7244
  - Tricky Store v1.3.0
  - Hide My Applist v3.7.0
  - Clone App Pro v1.0.0

  DenyList yang diterapkan mencakup aplikasi Shopee (`com.shopee.id`, `com.shopee.tw`, `com.shopee.ph`, `com.shopee.vn`, `com.shopee.my`, `com.shopee.sg`, `com.shopee.br`, `com.shopee.co.id`) dan `com.clone.app.pro`.

Modul-modul tersebut **tidak termasuk** di dalam APK Launcher. Unduh modul dari sumber resmi masing-masing sesuai `docs/modules-report.md` di repositori asal, lalu pastikan modul berada di dalam ROM sebelum boot pertama, atau pasang secara manual melalui aplikasi Magisk di dalam VM.

## 6. Ganti keybox

1. Siapkan file keybox XML yang baru dan masih berlaku.
2. Di Launcher, pilih file XML tersebut.
3. Launcher memeriksa bahwa file berekstensi `.xml` dan berisi data keybox, lalu menyalinnya sebagai `keybox.xml`.
4. Salin file tersebut ke `/sdcard/NZeyMod/keybox.xml` di dalam VM, lalu nyalakan ulang VM.

Keybox yang ada saat ini kedaluwarsa pada 28 November 2026. Setelah tanggal tersebut, keybox harus diganti, karena Play Integrity dapat gagal.

## 7. Pasang NZey Master dan impor ROM

1. Di bagian NZey Master pada Launcher, periksa apakah NZey Master sudah terpasang. Launcher memeriksa paket baru `com.kizakixd.nzeymaster` terlebih dahulu, lalu paket lama `com.clone.android.dual.space` sebagai fallback.
2. Jika belum, pasang `NZeyMaster-nzey.apk` hasil repackage (139 MB). Periksa SHA-256 APK bila memungkinkan:
   `224083a8cd2920f67d9c8a67a9f6746afa62820a218d9cb5f7174760a46bb55c`
   APK ini dapat terpasang berdampingan dengan Virtual Master lama — tidak perlu uninstall. APK paket lama (145 MB, SHA-256 `2d70516e1f7bd85ca105341b8bd13760fe7484d6b1983e0446a436253d90c602`) hanya fallback dan tetap memerlukan uninstall Virtual Master lama.
3. Pasang APK tersebut.
4. Impor file ZIP ROM utuh yang sudah diverifikasi ke NZey Master dengan langkah pada bagian 2 di atas.
5. Salin `identity.json`, `config.prop`, dan `keybox.xml` hasil Launcher ke folder `/sdcard/NZeyMod/` di dalam VM. Cara penyalinan berkas mengikuti fitur berbagi berkas yang disediakan NZey Master.
6. Nyalakan ulang VM. Pada boot pertama, `nzeymod.sh` memasang modul, menerapkan identitas, dan menulis log ke `/sdcard/NZeyMod/nzeymod.log` di dalam VM.

## 8. Cek kebocoran

Gunakan fitur Cek Kebocoran di dalam VM (aplikasi NZeyMod Identity) untuk pemeriksaan yang sebenarnya. Fitur Cek Kebocoran di Launcher hanya memeriksa perangkat host dan digunakan sebagai gambaran awal. Nilai yang diharapkan di dalam VM antara lain `ro.debuggable=0`, `ro.build.tags=release-keys`, dan `ro.secure=1`, serta tidak ada jejak `su` di luar Magisk.

## 9. Jika terjadi masalah

- **SHA-256 ROM tidak sesuai**: jangan impor berkas tersebut. Periksa ukuran berkas (harus sekitar 405 MB), periksa apakah berkas masih berupa bagian `.part`, lalu salin atau unduh ulang dan verifikasi lagi.
- **NZey Master tidak muncul saat ditekan Buka**: periksa apakah NZey Master sudah terpasang (bagian NZey Master di Launcher). Bila belum, unduh dan pasang terlebih dahulu.
- **Import Local ROM tidak menemukan berkas**: pastikan berkas berada di folder `NZeyMod/` dan bernama `rom_11_0_0_1036_3281_M27_NZeyMod.zip`, lalu coba pilih melalui pengelola berkas NZey Master.
- **Identitas tidak berubah setelah reboot**: periksa `nzeymod.log` di dalam VM dan pastikan `identity.json` berada di `/sdcard/NZeyMod/`.
- **Modul tidak terpasang**: pastikan folder `/system/nzeymod/modules/` berisi file modul, lalu periksa log boot pertama.
- **NZey Master tidak dapat dipasang**: bila memasang APK paket lama (145 MB), pastikan Virtual Master lama sudah dihapus, karena tanda tangan APK berbeda. Bila memasang `NZeyMaster-nzey.apk` hasil repackage (139 MB, paket `com.kizakixd.nzeymaster`), uninstall tidak diperlukan — APK ini dibuat agar dapat berdampingan dengan Virtual Master lama.
- **Play Integrity gagal**: periksa masa berlaku keybox dan konfigurasi Play Integrity Fork. STRONG tidak dapat dijamin di VM; lihat `docs/batasan.md`.
