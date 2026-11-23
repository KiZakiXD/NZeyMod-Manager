#!/bin/sh
# Gabungkan part jadi zip ROM utuh, lalu cek hash-nya.
# Pakai: sh join.sh (jalankan di folder ini, di samping file .part*)
cat rom_11_0_0_1036_3281_M27_NZeyMod.zip.part* > rom_11_0_0_1036_3281_M27_NZeyMod.zip
echo "Hasil: rom_11_0_0_1036_3281_M27_NZeyMod.zip"
echo "SHA256 seharusnya: 1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c"
command -v sha256sum >/dev/null && sha256sum rom_11_0_0_1036_3281_M27_NZeyMod.zip
