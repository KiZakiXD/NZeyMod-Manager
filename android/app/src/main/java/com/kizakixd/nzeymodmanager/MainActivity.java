package com.kizakixd.nzeymodmanager;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * NZeyMod Manager — satu Activity, Java saja, tanpa pustaka eksternal.
 *
 * Catatan API: registerForActivityResult dan ActivityResultContracts berasal
 * dari androidx.activity, yang tidak tersedia tanpa dependensi eksternal.
 * Karena aplikasi ini dibangun tanpa Gradle dan tanpa dependensi, Activity
 * ini memakai onActivityResult / startActivityForResult secara konsisten
 * di seluruh bagian (API android.app.Activity, tersedia di API 34).
 * Metode tersebut memang ditandai deprecated sejak API 30, tetapi tetap
 * berfungsi dan tetap dapat dikompilasi dengan android.jar API 34.
 */
public class MainActivity extends Activity {

    private static final int REQ_PICK_PARTS = 101;
    private static final int REQ_PICK_OUTPUT = 102;
    private static final int REQ_PICK_KEYBOX = 103;
    private static final int REQ_PICK_WHOLE_ROM = 104;

    private static final String EXPECTED_ROM_SHA256 =
            "1d8e0031a1076b4de7192274fb156a7ab88c97f1eb3b4bf3be98adf2e65c364c";
    private static final String WHOLE_ROM_FILENAME =
            "rom_11_0_0_1036_3281_M27_NZeyMod.zip";
    private static final long WHOLE_ROM_MIN_BYTES = 380L * 1024L * 1024L;
    private static final long WHOLE_ROM_MAX_BYTES = 430L * 1024L * 1024L;
    // NZey Master hasil repackage (v2.1-beta): paket baru agar dapat
    // terpasang berdampingan dengan Virtual Master lama, tanpa uninstall.
    // Paket lama tetap didukung sebagai fallback.
    private static final String NZEY_PACKAGE = "com.kizakixd.nzeymaster";
    private static final String NZEY_PACKAGE_LEGACY = "com.clone.android.dual.space";
    private static final String NZEY_VERSION = "3.2.68";
    private static final String NZEY_DOWNLOAD_URL =
            "https://129.226.154.152.sslip.io/download/NZeyMaster.apk";
    // SHA-256 NZeyMaster-nzey.apk hasil repackage (139 MB, 144.895.840 byte,
    // arm64 saja, tanpa lib armeabi-v7a).
    private static final String NZEY_SHA256 =
            "224083a8cd2920f67d9c8a67a9f6746afa62820a218d9cb5f7174760a46bb55c";
    // SHA-256 APK asli (paket lama, 145 MB) — hanya untuk referensi fallback.
    private static final String NZEY_SHA256_LEGACY =
            "2d70516e1f7bd85ca105341b8bd13760fe7484d6b1983e0446a436253d90c602";

    private static final String[] IMEI_TACS = {
            "35254410", "35482510", "35737210"
    };

    private static final Map<String, String> OPERATORS = new LinkedHashMap<>();
    static {
        OPERATORS.put("telkomsel", "51010");
        OPERATORS.put("indosat", "51021");
        OPERATORS.put("xl", "51011");
        OPERATORS.put("axis", "51008");
        OPERATORS.put("tri", "51089");
        OPERATORS.put("smartfren", "51009");
    }

    private static final String[] SU_PATHS = {
            "/system/bin/su", "/system/xbin/su", "/sbin/su"
    };

    private final SecureRandom random = new SecureRandom();

    private String currentAndroidId = "";
    private String currentSerial = "";
    private String currentImei = "";
    private String currentOperatorKey = "";
    private String currentOperatorCode = "";

    private List<Uri> pickedPartUris = new ArrayList<>();
    private List<String> pickedPartNames = new ArrayList<>();
    private List<String> profileNames = new ArrayList<>();

    private Uri wholeRomUri = null;
    private String wholeRomName = "";
    private long wholeRomSize = 0;
    private boolean wholeRomVerified = false;
    private String wholeRomPreparedPath = "";

    private Spinner spinnerOperatorMode;
    private Spinner spinnerProfiles;
    private TextView tvAndroidId;
    private TextView tvSerial;
    private TextView tvImei;
    private TextView tvOperator;
    private TextView tvIdentityPath;
    private TextView tvProfiles;
    private TextView tvPartsList;
    private TextView tvRomProgress;
    private TextView tvRomResult;
    private TextView tvWholeRomInfo;
    private TextView tvWholeRomProgress;
    private TextView tvWholeRomResult;
    private TextView tvImportSteps;
    private TextView tvMagiskStatus;
    private TextView tvModules;
    private TextView tvDenyList;
    private TextView tvKeyboxStatus;
    private TextView tvLeakResult;
    private TextView tvNzeyInstalled;
    private EditText etProfileName;
    private ProgressBar progressBarRom;
    private ProgressBar progressBarWholeRom;
    private Switch switchMagisk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerOperatorMode = findViewById(R.id.spinnerOperatorMode);
        spinnerProfiles = findViewById(R.id.spinnerProfiles);
        tvAndroidId = findViewById(R.id.tvAndroidId);
        tvSerial = findViewById(R.id.tvSerial);
        tvImei = findViewById(R.id.tvImei);
        tvOperator = findViewById(R.id.tvOperator);
        tvIdentityPath = findViewById(R.id.tvIdentityPath);
        tvProfiles = findViewById(R.id.tvProfiles);
        tvPartsList = findViewById(R.id.tvPartsList);
        tvRomProgress = findViewById(R.id.tvRomProgress);
        tvRomResult = findViewById(R.id.tvRomResult);
        tvWholeRomInfo = findViewById(R.id.tvWholeRomInfo);
        tvWholeRomProgress = findViewById(R.id.tvWholeRomProgress);
        tvWholeRomResult = findViewById(R.id.tvWholeRomResult);
        tvImportSteps = findViewById(R.id.tvImportSteps);
        tvMagiskStatus = findViewById(R.id.tvMagiskStatus);
        tvModules = findViewById(R.id.tvModules);
        tvDenyList = findViewById(R.id.tvDenyList);
        tvKeyboxStatus = findViewById(R.id.tvKeyboxStatus);
        tvLeakResult = findViewById(R.id.tvLeakResult);
        tvNzeyInstalled = findViewById(R.id.tvNzeyInstalled);
        etProfileName = findViewById(R.id.etProfileName);
        progressBarRom = findViewById(R.id.progressBarRom);
        progressBarWholeRom = findViewById(R.id.progressBarWholeRom);
        switchMagisk = findViewById(R.id.switchMagisk);

        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(
                this, R.array.operator_modes, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOperatorMode.setAdapter(modeAdapter);

        Button btnPickWholeRom = findViewById(R.id.btnPickWholeRom);
        btnPickWholeRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickWholeRom();
            }
        });

        Button btnVerifyWholeRom = findViewById(R.id.btnVerifyWholeRom);
        btnVerifyWholeRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyWholeRom();
            }
        });

        Button btnPrepareWholeRom = findViewById(R.id.btnPrepareWholeRom);
        btnPrepareWholeRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareWholeRom();
            }
        });

        Button btnLaunchNzey = findViewById(R.id.btnLaunchNzey);
        btnLaunchNzey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNzeyMaster();
            }
        });

        Button btnGenerate = findViewById(R.id.btnGenerate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateIdentity();
            }
        });

        Button btnSaveIdentity = findViewById(R.id.btnSaveIdentity);
        btnSaveIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIdentity();
            }
        });

        Button btnShareIdentity = findViewById(R.id.btnShareIdentity);
        btnShareIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIdentity();
            }
        });

        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        Button btnLoadProfile = findViewById(R.id.btnLoadProfile);
        btnLoadProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSelectedProfile();
            }
        });

        Button btnDeleteProfile = findViewById(R.id.btnDeleteProfile);
        btnDeleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedProfile();
            }
        });

        Button btnRefreshProfiles = findViewById(R.id.btnRefreshProfiles);
        btnRefreshProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshProfileList();
            }
        });

        Button btnPickParts = findViewById(R.id.btnPickParts);
        btnPickParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickRomParts();
            }
        });

        Button btnPickOutput = findViewById(R.id.btnPickOutput);
        btnPickOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickRomOutput();
            }
        });

        Button btnPickKeybox = findViewById(R.id.btnPickKeybox);
        btnPickKeybox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickKeybox();
            }
        });

        Button btnLeakCheck = findViewById(R.id.btnLeakCheck);
        btnLeakCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runLeakCheck();
            }
        });

        Button btnDownloadNzey = findViewById(R.id.btnDownloadNzey);
        btnDownloadNzey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(NZEY_DOWNLOAD_URL));
                startActivity(intent);
            }
        });

        Button btnCheckNzey = findViewById(R.id.btnCheckNzey);
        btnCheckNzey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNzeyInstalled();
            }
        });

        switchMagisk.setOnCheckedChangeListener(
                new android.widget.CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            android.widget.CompoundButton buttonView, boolean isChecked) {
                        updateMagiskUi(isChecked);
                    }
                });

        updateMagiskUi(false);
        refreshProfileList();
        checkNzeyInstalled();
    }

    // ------------------------------------------------------------------
    // Identitas
    // ------------------------------------------------------------------

    private void generateIdentity() {
        currentAndroidId = randomHex(8);
        currentSerial = randomHex(8);
        currentImei = generateImei();

        String mode = String.valueOf(spinnerOperatorMode.getSelectedItem());
        if ("acak".equals(mode)) {
            List<String> keys = new ArrayList<>(OPERATORS.keySet());
            currentOperatorKey = keys.get(random.nextInt(keys.size()));
        } else {
            currentOperatorKey = mode;
        }
        currentOperatorCode = OPERATORS.get(currentOperatorKey);

        tvAndroidId.setText("Android ID: " + currentAndroidId);
        tvSerial.setText("Serial: " + currentSerial);
        tvImei.setText("IMEI: " + currentImei);
        tvOperator.setText("Operator: " + operatorDisplayName(currentOperatorKey)
                + " (" + currentOperatorCode + ")");
        toast("Identitas baru dibuat");
    }

    private String operatorDisplayName(String key) {
        if ("telkomsel".equals(key)) return "Telkomsel";
        if ("indosat".equals(key)) return "Indosat Ooredoo";
        if ("xl".equals(key)) return "XL Axiata";
        if ("axis".equals(key)) return "AXIS";
        if ("tri".equals(key)) return "Tri";
        if ("smartfren".equals(key)) return "Smartfren";
        return key;
    }

    /** Menghasilkan 16 karakter hex (8 byte acak) dengan SecureRandom. */
    private String randomHex(int bytes) {
        byte[] data = new byte[bytes];
        random.nextBytes(data);
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * IMEI 15 digit: TAC (8 digit) + 6 digit acak + 1 digit pemeriksa Luhn.
     * Digit pemeriksa dihitung dengan menambahkan 0 sementara, lalu
     * menerapkan jumlah Luhn pada 15 digit tersebut.
     */
    private String generateImei() {
        String tac = IMEI_TACS[random.nextInt(IMEI_TACS.length)];
        StringBuilder base = new StringBuilder(tac);
        for (int i = 0; i < 6; i++) {
            base.append(random.nextInt(10));
        }
        int check = luhnCheckDigit(base.toString());
        return base.toString() + check;
    }

    private int luhnCheckDigit(String fourteenDigits) {
        String withZero = fourteenDigits + "0";
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = withZero.length() - 1; i >= 0; i--) {
            int digit = withZero.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /** Memeriksa apakah sebuah IMEI lengkap 15 digit lolos Luhn. */
    private static boolean isValidImei(String imei) {
        if (imei == null || imei.length() != 15) {
            return false;
        }
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = imei.length() - 1; i >= 0; i--) {
            char c = imei.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            int digit = c - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    private String buildIdentityJson() {
        return "{\n"
                + "  \"android_id\": \"" + escapeJson(currentAndroidId) + "\",\n"
                + "  \"serial\": \"" + escapeJson(currentSerial) + "\",\n"
                + "  \"imei\": \"" + escapeJson(currentImei) + "\",\n"
                + "  \"operator_name\": \"" + escapeJson(operatorDisplayName(currentOperatorKey)) + "\",\n"
                + "  \"operator_numeric\": \"" + escapeJson(currentOperatorCode) + "\",\n"
                + "  \"operator_alpha\": \"" + escapeJson(operatorAlpha(currentOperatorKey)) + "\"\n"
                + "}\n";
    }

    /** Alpha operator, sesuai operators.txt (format nama|numeric|alpha). */
    private String operatorAlpha(String key) {
        if ("telkomsel".equals(key)) return "Telkomsel";
        if ("indosat".equals(key)) return "Indosat Ooredoo";
        if ("xl".equals(key)) return "XL";
        if ("axis".equals(key)) return "AXIS";
        if ("tri".equals(key)) return "3";
        if ("smartfren".equals(key)) return "Smartfren";
        return key;
    }

    private String buildConfigProp() {
        return "# NZeyMod user config\n"
                + "operator_mode=" + currentOperatorKey + "\n"
                + "identity_mode=full\n"
                + "keybox_path=/sdcard/NZeyMod/keybox.xml\n"
                + "randomize_on_boot=true\n"
                + "wipe_profiles_on_randomize=false\n"
                + "show_toast=true\n";
    }

    private void saveIdentity() {
        if (currentAndroidId.isEmpty() || currentImei.isEmpty()) {
            toast("Buat identitas terlebih dahulu");
            return;
        }
        File dir = getNzeyModDir();
        if (dir == null) {
            toast("Penyimpanan eksternal aplikasi tidak tersedia");
            return;
        }
        try {
            writeTextFile(new File(dir, "identity.json"), buildIdentityJson());
            writeTextFile(new File(dir, "config.prop"), buildConfigProp());
            tvIdentityPath.setText("Lokasi penyimpanan:\n"
                    + new File(dir, "identity.json").getAbsolutePath() + "\n"
                    + new File(dir, "config.prop").getAbsolutePath());
            toast("Identitas disimpan di NZeyMod/");
        } catch (IOException e) {
            toast("Gagal menyimpan: " + e.getMessage());
        }
    }

    /**
     * Berbagi identitas sebagai teks melalui ACTION_SEND. File tidak dibagikan
     * sebagai lampiran Uri file:// karena FileProvider berasal dari androidx
     * dan tidak tersedia tanpa dependensi; isi JSON dibagikan sebagai teks,
     * sedangkan jalur file lengkap ditampilkan di layar.
     */
    private void shareIdentity() {
        if (currentAndroidId.isEmpty() || currentImei.isEmpty()) {
            toast("Buat identitas terlebih dahulu");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Identitas NZeyMod");
        intent.putExtra(Intent.EXTRA_TEXT, buildIdentityJson());
        startActivity(Intent.createChooser(intent, "Bagikan identitas"));
    }

    // ------------------------------------------------------------------
    // Profil
    // ------------------------------------------------------------------

    private File getNzeyModDir() {
        File base = getExternalFilesDir(null);
        if (base == null) {
            return null;
        }
        File dir = new File(base, "NZeyMod");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        return dir;
    }

    private File getProfilesDir() {
        File base = getNzeyModDir();
        if (base == null) {
            return null;
        }
        File dir = new File(base, "profiles");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        return dir;
    }

    private void saveProfile() {
        if (currentAndroidId.isEmpty() || currentImei.isEmpty()) {
            toast("Buat identitas terlebih dahulu");
            return;
        }
        String name = etProfileName.getText().toString().trim();
        if (name.isEmpty()) {
            toast("Isi nama profil terlebih dahulu");
            return;
        }
        File dir = getProfilesDir();
        if (dir == null) {
            toast("Penyimpanan eksternal aplikasi tidak tersedia");
            return;
        }
        String safeName = name.replaceAll("[^A-Za-z0-9._\\- ]", "_").trim();
        try {
            writeTextFile(new File(dir, safeName + ".json"), buildIdentityJson());
            toast("Profil \"" + safeName + "\" disimpan");
            refreshProfileList();
        } catch (IOException e) {
            toast("Gagal menyimpan profil: " + e.getMessage());
        }
    }

    private void refreshProfileList() {
        profileNames = new ArrayList<>();
        File dir = getProfilesDir();
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
                for (File f : files) {
                    if (f.isFile() && f.getName().toLowerCase(Locale.US).endsWith(".json")) {
                        String name = f.getName();
                        profileNames.add(name.substring(0, name.length() - 5));
                    }
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, profileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProfiles.setAdapter(adapter);
        if (profileNames.isEmpty()) {
            tvProfiles.setText(getString(R.string.no_profiles));
        } else {
            StringBuilder sb = new StringBuilder("Profil tersimpan:\n");
            for (String name : profileNames) {
                sb.append("• ").append(name).append("\n");
            }
            tvProfiles.setText(sb.toString().trim());
        }
    }

    private String selectedProfileName() {
        Object selected = spinnerProfiles.getSelectedItem();
        return selected == null ? null : String.valueOf(selected);
    }

    private void loadSelectedProfile() {
        String name = selectedProfileName();
        if (name == null) {
            toast("Tidak ada profil yang dipilih");
            return;
        }
        File dir = getProfilesDir();
        if (dir == null) {
            return;
        }
        File file = new File(dir, name + ".json");
        try {
            String json = readTextFile(file);
            String androidId = jsonValue(json, "android_id");
            String serial = jsonValue(json, "serial");
            String imei = jsonValue(json, "imei");
            String operator = jsonValue(json, "operator_name");
            if (operator == null) {
                operator = jsonValue(json, "operator");
            }
            String operatorCode = jsonValue(json, "operator_numeric");
            if (operatorCode == null) {
                operatorCode = jsonValue(json, "operator_code");
            }
            // operator_name berisi nama tampilan (mis. "Indosat Ooredoo");
            // petakan kembali ke kunci mode (mis. "indosat").
            String operatorKey = operatorKeyFromName(operator);
            if (androidId == null || imei == null) {
                toast("File profil tidak valid");
                return;
            }
            currentAndroidId = androidId;
            currentSerial = serial == null ? "" : serial;
            currentImei = imei;
            currentOperatorKey = operatorKey == null ? "" : operatorKey;
            currentOperatorCode = operatorCode == null ? "" : operatorCode;
            tvAndroidId.setText("Android ID: " + currentAndroidId);
            tvSerial.setText("Serial: " + currentSerial);
            tvImei.setText("IMEI: " + currentImei);
            tvOperator.setText("Operator: " + operatorDisplayName(currentOperatorKey)
                    + " (" + currentOperatorCode + ")");
            etProfileName.setText(name);
            toast("Profil \"" + name + "\" dimuat");
        } catch (IOException e) {
            toast("Gagal memuat profil: " + e.getMessage());
        }
    }

    /** Petakan nama tampilan / kunci lama ke kunci mode operator. */
    private String operatorKeyFromName(String name) {
        if (name == null) return "";
        String n = name.trim().toLowerCase(Locale.US);
        if (n.equals("telkomsel")) return "telkomsel";
        if (n.equals("indosat ooredoo") || n.equals("indosat")) return "indosat";
        if (n.equals("xl axiata") || n.equals("xl")) return "xl";
        if (n.equals("axis")) return "axis";
        if (n.equals("tri") || n.equals("3")) return "tri";
        if (n.equals("smartfren")) return "smartfren";
        return n;
    }

    private void deleteSelectedProfile() {
        String name = selectedProfileName();
        if (name == null) {
            toast("Tidak ada profil yang dipilih");
            return;
        }
        File dir = getProfilesDir();
        if (dir == null) {
            return;
        }
        File file = new File(dir, name + ".json");
        if (file.delete()) {
            toast("Profil \"" + name + "\" dihapus");
            refreshProfileList();
        } else {
            toast("Gagal menghapus profil");
        }
    }

    // ------------------------------------------------------------------
    // Jalankan ROM utuh (impor dari penyimpanan, lalu NZey Master)
    //
    // Aplikasi ini BUKAN mesin virtual. Mesinnya adalah NZey Master.
    // Launcher hanya: memilih ROM utuh, memverifikasi SHA-256,
    // menyalinnya ke folder NZeyMod/, lalu meluncurkan NZey Master
    // agar pengguna mengimpornya lewat Import Local ROM.
    // ------------------------------------------------------------------

    private void pickWholeRom() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(
                Intent.createChooser(intent, "Pilih ROM utuh"), REQ_PICK_WHOLE_ROM);
    }

    private void handlePickedWholeRom(Uri uri) {
        wholeRomUri = uri;
        wholeRomName = displayName(uri);
        wholeRomSize = contentLength(uri);
        wholeRomVerified = false;
        wholeRomPreparedPath = "";
        tvImportSteps.setVisibility(View.GONE);
        StringBuilder sb = new StringBuilder();
        sb.append("ROM utuh: ").append(wholeRomName).append("\n");
        sb.append("Ukuran: ").append(formatSize(wholeRomSize))
                .append(" (").append(wholeRomSize).append(" byte)")
                .append(" — diharapkan sekitar 405 MB (380–430 MB): ")
                .append(wholeRomSize >= WHOLE_ROM_MIN_BYTES
                        && wholeRomSize <= WHOLE_ROM_MAX_BYTES ? "sesuai" : "DI LUAR RENTANG");
        if (wholeRomName.toLowerCase(Locale.US).matches(".*\\.part\\d+$")) {
            sb.append("\nPERINGATAN: nama berkas berakhiran .part — "
                    + "ini tampak seperti bagian ROM terpisah, bukan ROM utuh. "
                    + "Gabungkan dahulu di bagian Gabung ROM.");
        }
        tvWholeRomInfo.setText(sb.toString());
        tvWholeRomResult.setText("Berkas dipilih. Tekan Verifikasi ROM Utuh "
                + "untuk menghitung SHA-256.");
        toast("ROM utuh dipilih");
    }

    private void verifyWholeRom() {
        if (wholeRomUri == null) {
            toast("Pilih ROM utuh terlebih dahulu");
            tvWholeRomResult.setText("GAGAL — tidak ada berkas yang dipilih.");
            return;
        }
        progressBarWholeRom.setVisibility(View.VISIBLE);
        progressBarWholeRom.setProgress(0);
        tvWholeRomProgress.setText("Menghitung SHA-256…");
        tvWholeRomResult.setText("");
        final Uri uri = wholeRomUri;
        final long total = wholeRomSize;
        final Context context = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    long readTotal = 0;
                    InputStream in = null;
                    try {
                        in = context.getContentResolver().openInputStream(uri);
                        if (in == null) {
                            throw new IOException("Tidak dapat membuka berkas ROM");
                        }
                        byte[] buffer = new byte[64 * 1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            digest.update(buffer, 0, read);
                            readTotal += read;
                            final int percent = total > 0
                                    ? (int) (readTotal * 100 / total) : 0;
                            final long done = readTotal;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarWholeRom.setProgress(percent);
                                    tvWholeRomProgress.setText("Menghitung SHA-256… "
                                            + percent + "% (" + formatSize(done) + ")");
                                }
                            });
                        }
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ignored) {
                                // Abaikan kegagalan penutupan stream.
                            }
                        }
                    }
                    final String actualHash = bytesToHex(digest.digest());
                    final long finalSize = readTotal;
                    final boolean sizeOk = finalSize >= WHOLE_ROM_MIN_BYTES
                            && finalSize <= WHOLE_ROM_MAX_BYTES;
                    final boolean hashOk = EXPECTED_ROM_SHA256.equalsIgnoreCase(actualHash);
                    final boolean ok = sizeOk && hashOk;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wholeRomVerified = ok;
                            tvImportSteps.setVisibility(View.GONE);
                            progressBarWholeRom.setProgress(100);
                            tvWholeRomProgress.setText("Selesai: " + formatSize(finalSize));
                            StringBuilder sb = new StringBuilder();
                            sb.append("Nama: ").append(wholeRomName).append("\n");
                            sb.append("Ukuran: ").append(formatSize(finalSize))
                                    .append(" (").append(finalSize).append(" byte)")
                                    .append(" — diharapkan sekitar 405 MB: ")
                                    .append(sizeOk ? "sesuai" : "DI LUAR RENTANG").append("\n");
                            sb.append("SHA-256: ").append(actualHash).append("\n");
                            sb.append("Hasil: ").append(ok ? "LOLOS" : "GAGAL — impor ditolak");
                            if (!ok) {
                                sb.append("\nKemungkinan penyebab:");
                                if (!hashOk) {
                                    sb.append("\n- Berkas rusak / corrupt saat diunduh atau disalin.");
                                    sb.append("\n- Berkas ini bukan ROM NZeyMod utuh.");
                                }
                                if (wholeRomName.toLowerCase(Locale.US)
                                        .matches(".*\\.part\\d+$")) {
                                    sb.append("\n- Berkas ini masih berupa bagian ROM terpisah "
                                            + "(.part00–.part04). Gabungkan dahulu di bagian "
                                            + "Gabung ROM.");
                                }
                                if (!sizeOk) {
                                    sb.append("\n- Ukuran berkas jauh dari 405 MB: kemungkinan "
                                            + "berkas terpotong atau belum selesai diunduh.");
                                }
                                sb.append("\nJangan mengimpor berkas yang gagal verifikasi.");
                            }
                            tvWholeRomResult.setText(sb.toString());
                            toast(ok ? "ROM utuh lolos verifikasi"
                                    : "ROM utuh gagal verifikasi");
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvWholeRomProgress.setText("Gagal memverifikasi");
                            tvWholeRomResult.setText("Galat: " + e.getMessage());
                            toast("Gagal memverifikasi ROM: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void prepareWholeRom() {
        if (wholeRomUri == null) {
            toast("Pilih ROM utuh terlebih dahulu");
            return;
        }
        if (!wholeRomVerified) {
            toast("Verifikasi ROM utuh terlebih dahulu hingga LOLOS");
            tvWholeRomResult.setText("Impor ditolak: ROM belum lolos verifikasi SHA-256. "
                    + "Tekan Verifikasi ROM Utuh terlebih dahulu.");
            return;
        }
        File dir = getNzeyModDir();
        if (dir == null) {
            toast("Penyimpanan eksternal aplikasi tidak tersedia");
            return;
        }
        final File dest = new File(dir, WHOLE_ROM_FILENAME);
        final Uri uri = wholeRomUri;
        final Context context = this;
        progressBarWholeRom.setVisibility(View.VISIBLE);
        progressBarWholeRom.setProgress(0);
        tvWholeRomProgress.setText("Menyalin ke NZeyMod/…");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    long copied = 0;
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = context.getContentResolver().openInputStream(uri);
                        if (in == null) {
                            throw new IOException("Tidak dapat membuka berkas ROM");
                        }
                        out = new FileOutputStream(dest);
                        byte[] buffer = new byte[64 * 1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            digest.update(buffer, 0, read);
                            copied += read;
                            final int percent = wholeRomSize > 0
                                    ? (int) (copied * 100 / wholeRomSize) : 0;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarWholeRom.setProgress(percent);
                                }
                            });
                        }
                        out.flush();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ignored) {
                                // Abaikan kegagalan penutupan stream.
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ignored) {
                                // Abaikan kegagalan penutupan stream.
                            }
                        }
                    }
                    final String copyHash = bytesToHex(digest.digest());
                    final long finalSize = copied;
                    final boolean ok = EXPECTED_ROM_SHA256.equalsIgnoreCase(copyHash);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ok) {
                                tvWholeRomProgress.setText("Gagal menyiapkan");
                                tvWholeRomResult.setText("GAGAL — hash salinan tidak cocok ("
                                        + copyHash + "). Hapus salinan dan verifikasi ulang.");
                                toast("Salinan ROM gagal verifikasi");
                                return;
                            }
                            wholeRomPreparedPath = dest.getAbsolutePath();
                            tvWholeRomProgress.setText("Siap: " + formatSize(finalSize));
                            tvWholeRomResult.setText("ROM utuh disiapkan di:\n"
                                    + wholeRomPreparedPath + "\n"
                                    + "SHA-256 salinan: " + copyHash + " (LOLOS)");
                            tvImportSteps.setVisibility(View.VISIBLE);
                            toast("ROM siap diimpor ke NZey Master");
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvWholeRomProgress.setText("Gagal menyiapkan");
                            tvWholeRomResult.setText("Galat: " + e.getMessage());
                            toast("Gagal menyiapkan ROM: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void launchNzeyMaster() {
        PackageManager pm = getPackageManager();
        // Utama: paket baru hasil repackage; fallback: paket lama.
        Intent launch = pm.getLaunchIntentForPackage(NZEY_PACKAGE);
        String usedPackage = NZEY_PACKAGE;
        if (launch == null) {
            launch = pm.getLaunchIntentForPackage(NZEY_PACKAGE_LEGACY);
            usedPackage = NZEY_PACKAGE_LEGACY;
        }
        if (launch == null) {
            tvNzeyInstalled.setText("Status pemasangan: belum terinstal. "
                    + "Pasang NZeyMaster-nzey.apk hasil repackage " + NZEY_VERSION
                    + " (139 MB, SHA-256 " + NZEY_SHA256 + "). "
                    + "Paket barunya " + NZEY_PACKAGE + ", jadi dapat terpasang "
                    + "berdampingan dengan Virtual Master lama tanpa uninstall. "
                    + "Tautan unduh di bawah ini masih mengarah ke APK paket lama "
                    + "(145 MB, SHA-256 " + NZEY_SHA256_LEGACY + ") sebagai fallback.");
            toast("NZey Master belum terpasang — pasang NZeyMaster-nzey.apk dahulu");
            return;
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launch);
        toast("Membuka NZey Master (" + usedPackage + ") — impor ROM lewat + > titik tiga > Import Local ROM");
    }

    // ------------------------------------------------------------------
    // Gabung ROM (Storage Access Framework)
    // ------------------------------------------------------------------

    private void pickRomParts() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Pilih bagian ROM"), REQ_PICK_PARTS);
    }

    private void pickRomOutput() {
        if (pickedPartUris.isEmpty()) {
            toast("Pilih bagian ROM terlebih dahulu");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "nzeymod-rom-joined.bin");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQ_PICK_OUTPUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQ_PICK_PARTS) {
            handlePickedParts(data);
        } else if (requestCode == REQ_PICK_OUTPUT) {
            Uri output = data.getData();
            if (output != null) {
                joinRomParts(output);
            }
        } else if (requestCode == REQ_PICK_KEYBOX) {
            Uri uri = data.getData();
            if (uri != null) {
                handlePickedKeybox(uri);
            }
        } else if (requestCode == REQ_PICK_WHOLE_ROM) {
            Uri uri = data.getData();
            if (uri != null) {
                handlePickedWholeRom(uri);
            }
        }
    }

    private void handlePickedParts(Intent data) {
        List<Uri> uris = new ArrayList<>();
        ClipData clip = data.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                Uri uri = clip.getItemAt(i).getUri();
                if (uri != null) {
                    uris.add(uri);
                }
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        if (uris.isEmpty()) {
            return;
        }
        // Urutkan berdasarkan nama tampilan agar bagian 1..5 tersusun benar.
        Collections.sort(uris, new Comparator<Uri>() {
            @Override
            public int compare(Uri a, Uri b) {
                return displayName(a).compareToIgnoreCase(displayName(b));
            }
        });
        pickedPartUris = uris;
        pickedPartNames = new ArrayList<>();
        StringBuilder sb = new StringBuilder("Bagian yang dipilih:\n");
        for (Uri uri : uris) {
            String name = displayName(uri);
            pickedPartNames.add(name);
            sb.append("• ").append(name).append("\n");
        }
        tvPartsList.setText(sb.toString().trim());
        tvRomResult.setText("");
        toast(pickedPartUris.size() + " bagian dipilih");
    }

    private String displayName(Uri uri) {
        String name = uri.getLastPathSegment();
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri,
                    new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String value = cursor.getString(index);
                    if (value != null && !value.isEmpty()) {
                        name = value;
                    }
                }
            }
        } catch (Exception ignored) {
            // Tetap memakai nama dari path bila kueri gagal.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return name == null ? "(tanpa nama)" : name;
    }

    private void joinRomParts(final Uri outputUri) {
        progressBarRom.setVisibility(View.VISIBLE);
        progressBarRom.setProgress(0);
        tvRomProgress.setText("Menggabungkan…");
        tvRomResult.setText("");
        final List<Uri> parts = new ArrayList<>(pickedPartUris);
        final Context context = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long totalBytes = 0;
                    for (Uri part : parts) {
                        totalBytes += contentLength(part);
                    }
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    long copied = 0;
                    ContentResolver resolver = context.getContentResolver();
                    OutputStream out = null;
                    try {
                        out = resolver.openOutputStream(outputUri);
                        if (out == null) {
                            throw new IOException("Tidak dapat membuka file keluaran");
                        }
                        byte[] buffer = new byte[64 * 1024];
                        for (Uri part : parts) {
                            InputStream in = null;
                            try {
                                in = resolver.openInputStream(part);
                                if (in == null) {
                                    throw new IOException(
                                            "Tidak dapat membuka bagian: " + displayName(part));
                                }
                                int read;
                                while ((read = in.read(buffer)) != -1) {
                                    out.write(buffer, 0, read);
                                    digest.update(buffer, 0, read);
                                    copied += read;
                                    final int percent = totalBytes > 0
                                            ? (int) (copied * 100 / totalBytes) : 0;
                                    final long done = copied;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBarRom.setProgress(percent);
                                            tvRomProgress.setText("Menggabungkan… "
                                                    + percent + "% ("
                                                    + formatSize(done) + ")");
                                        }
                                    });
                                }
                            } finally {
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException ignored) {
                                        // Abaikan kegagalan penutupan stream.
                                    }
                                }
                            }
                        }
                        out.flush();
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ignored) {
                                // Abaikan kegagalan penutupan stream.
                            }
                        }
                    }
                    final String actualHash = bytesToHex(digest.digest());
                    final long finalSize = copied;
                    final boolean ok = EXPECTED_ROM_SHA256.equalsIgnoreCase(actualHash);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarRom.setProgress(100);
                            tvRomProgress.setText("Selesai: " + formatSize(finalSize));
                            tvRomResult.setText("Ukuran: " + formatSize(finalSize)
                                    + " (" + finalSize + " byte)\n"
                                    + "SHA-256: " + actualHash + "\n"
                                    + "Hasil: " + (ok ? "Lolos" : "Gagal")
                                    + (ok ? "" : " — hash tidak cocok dengan yang diharapkan"));
                            toast(ok ? "ROM lolos verifikasi" : "ROM gagal verifikasi");
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvRomProgress.setText("Gagal menggabungkan");
                            tvRomResult.setText("Galat: " + e.getMessage());
                            toast("Gagal menggabungkan ROM: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private long contentLength(Uri uri) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri,
                    new String[]{OpenableColumns.SIZE}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception ignored) {
            // Ukuran tidak diketahui; progres memakai persen 0 sampai selesai.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    // ------------------------------------------------------------------
    // Magisk
    // ------------------------------------------------------------------

    private void updateMagiskUi(boolean enabled) {
        if (enabled) {
            tvMagiskStatus.setText(getString(R.string.magisk_on_text));
            tvModules.setVisibility(View.VISIBLE);
            tvDenyList.setVisibility(View.VISIBLE);
        } else {
            tvMagiskStatus.setText(getString(R.string.magisk_off_text));
            tvModules.setVisibility(View.GONE);
            tvDenyList.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------------
    // Keybox
    // ------------------------------------------------------------------

    private void pickKeybox() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Pilih file keybox"), REQ_PICK_KEYBOX);
    }

    private void handlePickedKeybox(Uri uri) {
        String name = displayName(uri);
        if (name == null || !name.toLowerCase(Locale.US).endsWith(".xml")) {
            tvKeyboxStatus.setText("Keybox: gagal — file harus berekstensi .xml");
            toast("File keybox harus berekstensi .xml");
            return;
        }
        File dir = getNzeyModDir();
        if (dir == null) {
            toast("Penyimpanan eksternal aplikasi tidak tersedia");
            return;
        }
        try {
            String content = readTextFromUri(uri);
            if (content == null
                    || !content.toLowerCase(Locale.US).contains("keybox")) {
                tvKeyboxStatus.setText("Keybox: gagal — isi file tidak mengandung keybox");
                toast("Isi file tidak mengandung keybox");
                return;
            }
            File dest = new File(dir, "keybox.xml");
            writeTextFile(dest, content);
            tvKeyboxStatus.setText("Keybox: tersimpan di "
                    + dest.getAbsolutePath()
                    + "\nPeringatan: keybox ini hanya ECDSA dan kedaluwarsa pada 28 Nov 2026.");
            toast("Keybox disimpan");
        } catch (IOException e) {
            tvKeyboxStatus.setText("Keybox: gagal — " + e.getMessage());
            toast("Gagal membaca keybox: " + e.getMessage());
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        if (in == null) {
            throw new IOException("Tidak dapat membuka file");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }

    // ------------------------------------------------------------------
    // Pemeriksaan kebocoran (host — bukan di dalam VM)
    // ------------------------------------------------------------------

    private void runLeakCheck() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pemeriksaan host (bukan di dalam VM):\n");
        String tags = Build.TAGS == null ? "" : Build.TAGS;
        boolean testKeys = tags.contains("test-keys");
        sb.append("• Build.TAGS mengandung test-keys: ")
                .append(testKeys ? "Ya" : "Tidak").append("\n");
        for (String path : SU_PATHS) {
            sb.append("• ").append(path).append(": ")
                    .append(new File(path).exists() ? "Ada" : "Tidak ada").append("\n");
        }
        sb.append("Catatan: pemeriksaan yang sebenarnya hanya berlaku di dalam VM. "
                + "Hasil di atas hanya menggambarkan ponsel host ini.");
        tvLeakResult.setText(sb.toString().trim());
    }

    // ------------------------------------------------------------------
    // NZey Master
    // ------------------------------------------------------------------

    private void checkNzeyInstalled() {
        PackageManager pm = getPackageManager();
        String foundPackage = installedPackage(pm, NZEY_PACKAGE);
        if (foundPackage == null) {
            foundPackage = installedPackage(pm, NZEY_PACKAGE_LEGACY);
        }
        if (foundPackage != null) {
            String installedVersion = installedVersion(pm, foundPackage);
            tvNzeyInstalled.setText("Status pemasangan: terinstal (versi "
                    + installedVersion + ", paket " + foundPackage + "). "
                    + "Versi yang diharapkan: " + NZEY_VERSION + ". "
                    + (NZEY_PACKAGE.equals(foundPackage)
                            ? "Ini paket baru hasil repackage — dapat berdampingan dengan Virtual Master lama."
                            : "Ini paket lama — tetap dapat dipakai sebagai fallback."));
            return;
        }
        tvNzeyInstalled.setText("Status pemasangan: belum terinstal. "
                + "Pasang NZeyMaster-nzey.apk hasil repackage " + NZEY_VERSION
                + " (139 MB, SHA-256 " + NZEY_SHA256 + "). "
                + "Paket barunya " + NZEY_PACKAGE + ", jadi dapat terpasang "
                + "berdampingan dengan Virtual Master lama tanpa uninstall.");
    }

    /** Mengembalikan nama paket bila terpasang, atau null bila tidak ada. */
    private String installedPackage(PackageManager pm, String pkg) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0));
            } else {
                @SuppressWarnings("deprecation")
                PackageInfo legacy = pm.getPackageInfo(pkg, 0);
            }
            return pkg;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /** Mengembalikan versionName paket yang terpasang, atau "?" bila gagal. */
    private String installedVersion(PackageManager pm, String pkg) {
        try {
            PackageInfo info;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                info = pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0));
            } else {
                @SuppressWarnings("deprecation")
                PackageInfo legacy = pm.getPackageInfo(pkg, 0);
                info = legacy;
            }
            return info.versionName == null ? "?" : info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "?";
        }
    }

    // ------------------------------------------------------------------
    // Utilitas
    // ------------------------------------------------------------------

    private void writeTextFile(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } finally {
            out.close();
        }
    }

    private String readTextFile(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            in.close();
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Mengambil nilai string sederhana "kunci": "nilai" dari JSON satu tingkat. */
    private static String jsonValue(String json, String key) {
        if (json == null) {
            return null;
        }
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) {
            return null;
        }
        int colon = json.indexOf(':', keyIndex + marker.length());
        if (colon < 0) {
            return null;
        }
        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) {
            return null;
        }
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.US, "%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024L * 1024L) {
            return String.format(Locale.US, "%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024L * 1024L * 1024L) {
            return String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
