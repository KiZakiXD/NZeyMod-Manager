# Panduan — NZeyMod Manager

## Perangkat yang dituju

Panduan ini ditulis untuk POCO X7 Pro sebagai perangkat host. Perangkat host adalah perangkat tempat Manager dan NZey Master dipasang. ROM Android 11 berjalan di dalam VM (NZey Master), bukan langsung di perangkat host.

## 1. Persiapan

1. Pastikan ruang kosong di perangkat host minimal 1,5 GB: sekitar 405 MB untuk 5 part ROM, 405 MB untuk file ZIP gabungan, dan 145 MB untuk APK NZey Master.
2. Unduh 5 part ROM dari repositori privat `nzeymod-rom`, folder `rom/`:
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part00` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part01` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part02` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part03` (90 MB)
   - `rom_11_0_0_1036_3281_M27_NZeyMod.zip.part04` (44,9 MB)
3. Jika Virtual Master lama masih terpasang, hapus terlebih dahulu. NZey Master menggunakan tanda tangan yang berbeda, sehingga tidak dapat dipasang menimpa Virtual Master lama.

## 2. Gabung ROM

1. Buka Manager, masuk ke bagian Gabung ROM.
2. Pilih kelima file part. Urutan file ditentukan berdasarkan nama file, dari `.part00` sampai `.part04`.
3. Pilih lokasi penyimpanan untuk file hasil gabungan, dengan nama `rom_11_0_0_1036_3281_M27_NZeyMod.zip`.
4. Tunggu proses selesai. Manager menghitung SHA-256 selama penggabungan.
5. Nilai yang benar adalah:
   `1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c`
6. Jika nilai tidak sesuai, jangan lanjutkan. Unduh ulang part yang rusak dan gabungkan kembali. Di prototipe web, cara yang sama dapat dilakukan dengan memilih file dan memeriksa hasilnya sebelum mengunduh file gabungan.

Perintah yang setara di Termux atau Linux, bila dilakukan manual, ada di `rom-fixes/join.sh`.

## 3. Buat identitas

1. Masuk ke bagian Acak Identitas.
2. Pilih mode operator: `random` atau salah satu operator (Telkomsel, Indosat Ooredoo, XL Axiata, AXIS, Tri, Smartfren).
3. Tekan tombol acak identitas. Manager membuat:
   - Android ID: 16 karakter heksadesimal acak.
   - Serial: 16 karakter heksadesimal acak.
   - IMEI: diawali TAC Pixel 5 (35254410, 35482510, atau 35737210), dilanjutkan 6 digit acak, dan diakhiri digit Luhn yang valid.
   - Operator beserta kode MCCMNC-nya.
4. Simpan hasilnya. Manager menulis `identity.json` dan `config.prop` ke folder `NZeyMod/` di penyimpanan aplikasi, dan dapat membagikannya sebagai file.
5. Untuk memakai kembali identitas yang sama di lain waktu, simpan sebagai profil dengan nama yang jelas, misalnya `shopee-bersih` atau `utama`.

## 4. Profil Magisk

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

Modul-modul tersebut **tidak termasuk** di dalam APK Manager. Unduh modul dari sumber resmi masing-masing sesuai `docs/modules-report.md` di repositori asal, lalu pastikan modul berada di dalam ROM sebelum boot pertama, atau pasang secara manual melalui aplikasi Magisk di dalam VM.

## 5. Ganti keybox

1. Siapkan file keybox XML yang baru dan masih berlaku.
2. Di Manager, pilih file XML tersebut.
3. Manager memeriksa bahwa file berekstensi `.xml` dan berisi data keybox, lalu menyalinnya sebagai `keybox.xml`.
4. Salin file tersebut ke `/sdcard/NZeyMod/keybox.xml` di dalam VM, lalu nyalakan ulang VM.

Keybox yang ada saat ini kedaluwarsa pada 28 November 2026. Setelah tanggal tersebut, keybox harus diganti, karena Play Integrity dapat gagal.

## 6. Pasang NZey Master dan impor ROM

1. Di bagian NZey Master pada Manager, periksa apakah NZey Master sudah terpasang.
2. Jika belum, unduh APK dari tautan resmi yang tercantum di Manager (145 MB). Periksa SHA-256 APK bila memungkinkan:
   `2d70516e1f7bd85ca105341b8bd13760fe7484d6b1983e0446a436253d90c602`
3. Pasang APK tersebut.
4. Impor file ZIP ROM yang sudah digabung ke NZey Master.
5. Salin `identity.json`, `config.prop`, dan `keybox.xml` hasil Manager ke folder `/sdcard/NZeyMod/` di dalam VM. Cara penyalinan berkas mengikuti fitur berbagi berkas yang disediakan NZey Master.
6. Nyalakan ulang VM. Pada boot pertama, `nzeymod.sh` memasang modul, menerapkan identitas, dan menulis log ke `/sdcard/NZeyMod/nzeymod.log` di dalam VM.

## 7. Cek kebocoran

Gunakan fitur Cek Kebocoran di dalam VM (aplikasi NZeyMod Identity) untuk pemeriksaan yang sebenarnya. Fitur Cek Kebocoran di Manager hanya memeriksa perangkat host dan digunakan sebagai gambaran awal. Nilai yang diharapkan di dalam VM antara lain `ro.debuggable=0`, `ro.build.tags=release-keys`, dan `ro.secure=1`, serta tidak ada jejak `su` di luar Magisk.

## 8. Jika terjadi masalah

- **SHA-256 ROM tidak sesuai**: unduh ulang part yang dicurigai rusak, periksa ukuran tiap part, lalu gabungkan kembali.
- **Identitas tidak berubah setelah reboot**: periksa `nzeymod.log` di dalam VM dan pastikan `identity.json` berada di `/sdcard/NZeyMod/`.
- **Modul tidak terpasang**: pastikan folder `/system/nzeymod/modules/` berisi file modul, lalu periksa log boot pertama.
- **NZey Master tidak dapat dipasang**: pastikan Virtual Master lama sudah dihapus, karena tanda tangan APK berbeda.
- **Play Integrity gagal**: periksa masa berlaku keybox dan konfigurasi Play Integrity Fork. STRONG tidak dapat dijamin di VM; lihat `docs/batasan.md`.
