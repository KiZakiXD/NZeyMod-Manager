# Batasan — NZeyMod Launcher v2.1-beta

Dokumen ini menjelaskan batasan secara jujur, agar tidak ada klaim yang melebihi kemampuan aplikasi dan ROM.

## 1. Launcher bukan mesin virtual

NZeyMod Launcher tidak menjalankan ROM Android 11. Launcher hanya menyiapkan berkas: memilih ROM utuh dari penyimpanan, memverifikasi SHA-256, menyalinnya ke folder `NZeyMod/`, menggabungkan part ROM, membuat identitas, menyimpan profil, menyiapkan keybox, dan menampilkan daftar modul — lalu meluncurkan NZey Master. ROM hanya berjalan di dalam NZey Master (atau Virtual Master).

## 2. Play Integrity STRONG tidak dijamin

Play Integrity STRONG tidak dapat dijamin di VM mana pun. VM tidak memiliki TEE (Trusted Execution Environment) atau bootloader terkunci sendiri. Yang dapat diupayakan adalah hasil BASIC dan DEVICE, dengan keybox dan konfigurasi Play Integrity Fork yang benar.

## 3. Keybox kedaluwarsa 28 November 2026

Keybox yang digunakan saat ini hanya ECDSA dan kedaluwarsa pada 28 November 2026. Setelah tanggal tersebut, keybox harus diganti dengan keybox baru yang masih berlaku. Manager menampilkan peringatan ini pada fitur Ganti Keybox.

## 4. IMEI bersifat best-effort

Di Android 11, IMEI dibaca dari layanan telepon virtual. Penggantian properti IMEI bersifat best-effort dan tidak selalu terbaca sama oleh semua aplikasi.

## 5. Shopee melarang emulator dan VM

Shopee secara resmi melarang emulator atau VM di ketentuan layanannya. Modifikasi ini hanya menurunkan sinyal deteksi lokal. Hal ini tidak menjamin akun aman dari pembatasan atau penindakan oleh pihak aplikasi.

## 6. Modul Magisk tidak termasuk

File ZIP dan APK modul Magisk tidak ada di repositori `nzeymod-rom` dan tidak disertakan di dalam APK Manager. Daftar modul di Manager hanya berfungsi sebagai panduan versi dan sumber. Modul harus diperoleh dari sumber resmi masing-masing.

Daftar modul yang dirujuk:

- Zygisk Next v1.2.8
- Play Integrity Fork v18.4.1
- Shamiko v1.2.1
- LSPosed v1.10.1-7244
- Tricky Store v1.3.0
- Hide My Applist v3.7.0
- Clone App Pro v1.0.0 (APK milik pengguna)

## 7. Pemeriksaan di host bukan pemeriksaan di VM

Fitur Cek Kebocoran di Manager memeriksa perangkat host. Hasilnya tidak membuktikan kondisi di dalam VM. Pemeriksaan yang berlaku adalah pemeriksaan di dalam VM, melalui aplikasi NZeyMod Identity dan log `nzeymod.log`.

## 8. APK belum diuji di perangkat fisik

APK versi 2.1-beta dibangun dan diverifikasi secara statis di lingkungan pengembangan (tanda tangan, manifest, dan SHA-256). APK belum dipasang dan diuji di perangkat fisik. Pengujian di perangkat POCO X7 Pro masih diperlukan sebelum digunakan secara rutin, terutama alur impor ROM utuh ke NZey Master.

## 9. NZey Master hasil repackage belum teruji — rename package berisiko

`NZeyMaster-nzey.apk` adalah Virtual Master 3.2.68 hasil repackage: paket diubah dari `com.clone.android.dual.space` menjadi `com.kizakixd.nzeymaster` agar dapat terpasang berdampingan dengan Virtual Master lama tanpa uninstall, dan lib `armeabi-v7a` dibuang (hanya arm64 yang dipertahankan, sesuai POCO X7 Pro). String path paket lama di native lib `libkr*.so` (mis. `/data/data/com.clone.android.dual.space/vm/vm%d/fs`) di-patch ke paket baru dengan panjang berkas yang dipertahankan; tidak ada smali yang diubah, jadi patch VIP dipertahankan.

Risiko yang tersisa dan wajib dites di HP:

- Login server / cloud sync Virtual Master dapat menolak paket baru, karena server dapat memeriksa nama paket atau tanda tangan aplikasi.
- Provider / FileProvider dan Firebase / Mobile Ads memakai authority berbasis nama paket — sudah diubah di manifest, tetapi perilaku runtime-nya belum teruji.
- VM dapat gagal booting bila ada path lain yang masih mengarah ke paket lama di dalam native lib atau di dalam ROM yang diimpor.
- APK hasil repackage hanya untuk perangkat arm64 (POCO X7 Pro). Perangkat ARM 32-bit tidak dapat memakai APK ini karena lib `armeabi-v7a` sudah dibuang.
- Bila NZey Master hasil repackage gagal jalan, fallback-nya adalah APK paket lama — tetapi APK lama tetap bentrok dengan Virtual Master yang masih terpasang dan memerlukan uninstall terlebih dahulu.

Detail lengkap ada di `nzeymaster/REPACK.md` dan `nzeymaster/REMOVED.md`.

## 10. Keamanan berkas

- `identity.json` berisi identitas perangkat. Jangan dibagikan ke pihak yang tidak dipercaya.
- `keybox.xml` adalah kredensial sensitif. Jangan dibagikan dan jangan diunggah ke repositori publik.
- Simpan cadangan profil di tempat yang aman, dan hapus berkas yang sudah tidak diperlukan.
