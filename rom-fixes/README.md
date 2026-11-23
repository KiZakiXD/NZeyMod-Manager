# Perbaikan ROM — NZeyMod

File di folder ini adalah versi yang diperbaiki dari file di repositori `nzeymod-rom`. File ini tidak mengubah isi ROM biner; file ini memperbaiki nama berkas, konfigurasi, dan skrip pendukung agar konsisten.

## 1. `operators.txt` — nama berkas diperbaiki

Masalah: `scripts/nzeymod.sh` membaca `$SYS/operators.txt`, tetapi file di repositori bernama `scripts/operators_id.txt`. Akibatnya, skrip tidak menemukan daftar operator.

Perbaikan: file ini bernama `operators.txt` dan berisi 6 operator dengan format `nama|numeric|alpha`:

- Telkomsel|51010|Telkomsel
- Indosat Ooredoo|51021|Indosat Ooredoo
- XL Axiata|51011|XL
- AXIS|51008|AXIS
- Tri|51089|3
- Smartfren|51009|Smartfren

Cara memakai: salin file ini menjadi `/system/nzeymod/operators.txt` di dalam ROM (atau sertakan saat repack). Aplikasi NZeyMod Identity juga memakai `/sdcard/NZeyMod/operators.txt` dengan format yang sama.

## 2. `config.prop` — kunci dipisah dengan jelas

Masalah: aplikasi NZeyMod Identity menulis kunci `identity_mode`, `keybox_path`, `randomize_on_boot`, `wipe_profiles_on_randomize`, dan `show_toast`, tetapi `nzeymod.sh` hanya membaca `operator_mode`. Kunci lain tidak berpengaruh pada skrip boot.

Perbaikan: `config.prop` di folder ini memberi komentar pada tiap kunci, yang menjelaskan kunci mana yang dibaca skrip dan mana yang hanya dipakai aplikasi. Nilai default tetap sama: `operator_mode=random`.

## 3. Identitas perangkat — gunakan `redfin`, bukan `flame`

Masalah: `props/vendor-build.prop` menulis `ro.product.board=flame` (Pixel 4), padahal seluruh identitas lain adalah Pixel 5 (`redfin`): model, device, fingerprint, dan `board` di partisi system.

Perbaikan: gunakan `redfin` secara konsisten di seluruh partisi:

- `ro.product.model=Pixel 5`
- `ro.product.device=redfin`
- `ro.product.board=redfin`
- `ro.build.fingerprint=google/redfin/redfin:11/RQ3A.211001.001/7641976:user/release-keys`

Catatan: `ro.hardware.*=master` di vendor sengaja tidak diubah. Nilai tersebut adalah nama HAL fungsional VM; menggantinya dapat membuat HAL tidak berfungsi.

## 4. `join.sh` — sama dengan skrip asal, ditambah penjelasan

`join.sh` di folder ini memiliki logika yang sama dengan `rom/join.sh` di repositori asal: menggabungkan 5 part dengan `cat`, lalu memeriksa SHA-256. Nilai SHA-256 yang benar:

`1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c`

Total ukuran gabungan sekitar 405 MB (90 + 90 + 90 + 90 + 44,9 MB).

## 5. Catatan perbaikan dalam bentuk patch

`nzeymod-fixed.patch` berisi perubahan yang sama dalam format patch, agar dapat ditinjau baris demi baris sebelum diterapkan ke repositori asal. Patch tersebut mencakup:

- Penambahan `operators.txt` dengan isi yang benar (pengganti nama `operators_id.txt` untuk keperluan skrip).
- Komentar penjelas di `config.prop`.
- Perbaikan `ro.product.board` dari `flame` menjadi `redfin` di `vendor-build.prop`.

## 6. Yang tidak diperbaiki di sini

- Modul Magisk (ZIP/APK) dan `keybox.xml` tidak disertakan, karena tidak ada di repositori asal dan tidak boleh dibagikan secara sembarangan. Lihat `docs/batasan.md`.
- File ROM biner tidak diubah. Perbaikan ini hanya menyangkut berkas pendukung.
