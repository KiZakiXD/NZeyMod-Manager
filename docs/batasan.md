# Batasan — NZeyMod Manager

Dokumen ini menjelaskan batasan secara jujur, agar tidak ada klaim yang melebihi kemampuan aplikasi dan ROM.

## 1. Manager bukan mesin virtual

NZeyMod Manager tidak menjalankan ROM Android 11. Manager hanya menyiapkan berkas: menggabungkan part ROM, membuat identitas, menyimpan profil, menyiapkan keybox, dan menampilkan daftar modul. ROM hanya berjalan di dalam NZey Master (atau Virtual Master).

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

APK versi 1.0-beta dibangun dan diverifikasi secara statis di lingkungan pengembangan (tanda tangan, manifest, dan SHA-256). APK belum dipasang dan diuji di perangkat fisik. Pengujian di perangkat POCO X7 Pro masih diperlukan sebelum digunakan secara rutin.

## 9. Keamanan berkas

- `identity.json` berisi identitas perangkat. Jangan dibagikan ke pihak yang tidak dipercaya.
- `keybox.xml` adalah kredensial sensitif. Jangan dibagikan dan jangan diunggah ke repositori publik.
- Simpan cadangan profil di tempat yang aman, dan hapus berkas yang sudah tidak diperlukan.
