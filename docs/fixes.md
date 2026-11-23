# Perbaikan Ketidakcocokan di nzeymod-rom

Ditemukan saat membaca isi repositori pada 23 November 2026.
Kedua perbaikan ini diterapkan di Manager, bukan diasumsikan sudah
benar di repo.

## 1. Nama file operator

- Script `nzeymod.sh` membaca `$SYS/operators.txt`.
- File yang ada di repositori: `scripts/operators_id.txt`.
- Perbaikan: file operator harus bernama `operators.txt` (bukan
  `operators_id.txt`), karena `nzeymod.sh` membaca `$SYS/operators.txt`.
  Manager memakai `operators.txt` dengan format `nama|numeric|alpha`
  (lihat `rom-fixes/operators.txt`). Jika skrip repo dipakai apa adanya
  dengan nama file lama, skrip akan gagal karena file tidak ditemukan.

## 2. Board di vendor-build.prop

- `props/vendor-build.prop` menulis `ro.product.board=flame`
  (Pixel 4).
- Semua identitas lain di repo: `redfin` (Pixel 5).
- Perbaikan: Manager memakai `redfin` secara konsisten untuk semua
  properti board / device / model Pixel 5.

## 3. File yang tidak ada di repo (bukan bug, tetapi harus diimpor)

- ZIP modul Magisk (Zygisk Next, Play Integrity Fork, Shamiko,
  LSPosed, Tricky Store) dan `keybox.xml` **tidak ada** di repo.
  Yang ada hanya daftar dan hash di `docs/modules-report.md`.
- Manager tidak menganggap file tersebut sudah ada; pengguna
  mengimpor file tersebut dari penyimpanan HP.
