 CashFlowku (Java Edition) 📈💼

**CashFlowku** adalah aplikasi kasir (POS) dan pembukuan keuangan mandiri (*offline-first*) yang dirancang khusus untuk pelaku UMKM. Aplikasi ini dibangun secara murni menggunakan native Android Java dan database lokal SQLite, menawarkan performa tinggi, privasi data 100%, serta tampilan antarmuka *Minimalist White & Zinc* yang elegan dalam mode terang maupun gelap.

---

## 🌟 Fitur Utama (Key Features)

### 1. Dashboard Ringkasan Finansial Hari Ini (*Bento Grid Dashboard*)
* Memantau kesehatan keuangan harian secara seketika (*real-time*) lewat tata letak grid modern.
* Menampilkan informasi krusial:
  * **Laba Bersih**: Keuntungan bersih harian (Pemasukan dikurangi harga modal barang terjual & pengeluaran).
  * **Pemasukan**: Omzet kotor dari transaksi penjualan hari ini.
  * **Harga Modal (HPP)**: Akumulasi harga pokok penjualan dari barang yang terjual.
  * **Laba Kotor**: Pemasukan dikurangi akumulasi HPP.
  * **Pengeluaran**: Biaya operasional tambahan harian.
* Grafik batang interaktif visual mingguan (tren pemasukan vs pengeluaran 7 hari terakhir).
* Daftar transaksi terbaru khusus hari ini lengkap dengan foto produk.

### 2. POS Pintar & Manajemen Inventaris (*Smart POS & Inventory*)
* Pengurangan stok otomatis begitu transaksi penjualan berhasil disimpan.
* Form pendaftaran produk dengan opsi input harga modal (opsional), harga jual, kategori, jumlah stok, serta lampiran **foto produk** menggunakan kamera langsung atau galeri HP.
* Tombol **Restock** cepat untuk memperbarui stok barang yang menipis secara instan.

### 3. Pengelolaan Pengeluaran Toko (*Expense Tracker*)
* Pencatatan biaya operasional berdasarkan kategori (Bahan Baku, Utilitas/Listrik, Gaji Karyawan, Sewa Tempat, Lain-lain) lengkap dengan deskripsi catatan.
* Keuntungan bersih harian langsung dipotong secara otomatis untuk menjaga keakuratan neraca keuangan.

### 4. Laporan Premium & Ekspor Dokumen (*Premium Reports & Export*)
* Penyaringan data transaksi gabungan (penjualan & pengeluaran) berdasarkan rentang tanggal atau pencarian kata kunci.
* **Ekspor PDF Premium**: Membuat file PDF berdesain elegan dengan kartu ringkasan keuangan berbentuk *Bento Grid* dan tabel bergaris dinamis untuk dibagikan langsung via WhatsApp.
* **Ekspor Excel Interaktif (CSV)**: Laporan spreadsheet yang dilengkapi rumus matematika dinamis (seperti penjumlahan otomatis `=SUM(...)` dan perkalian `=Qty*Harga`). Data keuangan akan otomatis terhitung ulang saat Anda mengedit angka di Excel atau Google Sheets.

### 5. Sinkronisasi Profil & Pengaturan Terpadu (*Profile & Settings Sync*)
* Halaman profil bisnis untuk mengatur nama toko, email, dan nomor HP.
* Penyimpanan pengaturan ke dalam tabel SQL key-value terintegrasi yang disinkronkan secara dua arah dengan `SharedPreferences` pada saat booting aplikasi (*splash screen*).
* Peralihan tema gelap (*Dark Mode*) instan tanpa efek flickering visual pada saat aplikasi dimuat.

### 6. Keamanan & Cadangan Data Lokal (*WAL Backup & Restore*)
* **Offline-First**: Seluruh transaksi dan gambar disimpan secara lokal di ruang penyimpanan pribadi perangkat Anda.
* **WAL Checkpointing**: Memaksa pencatatan SQLite WAL ke file database utama (`PRAGMA wal_checkpoint(FULL)`) sebelum dicadangkan untuk menjamin 100% data terbaru ikut tersimpan.
* **Database Signature Verification**: Verifikasi file restore dengan membaca 16 byte header awal untuk mendeteksi kesesuaian format `"SQLite format 3"`, mencegah kerusakan basis data dari berkas yang tidak dikenal.

---

## 🛠️ Tech Stack & Library

* **Bahasa Pemrograman**: Java (JDK 17)
* **SDK Minimum**: Android 8.0 (API Level 26 / Oreo)
* **Target SDK**: Android 14 (API Level 34)
* **Database**: SQLite (menggunakan `SQLiteOpenHelper`)
* **Layouting**: XML (menggunakan `ConstraintLayout`, `CardView`, `RecyclerView`)
* **Build System**: Gradle 8.4.1
* **Penyedia File**: AndroidX FileProvider (untuk sharing dokumen PDF & CSV ekspor)

---

## 📂 Struktur Proyek (Directory Structure)

```text
CashFlowKuJava/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cashflowkujava/
│   │   │   │   ├── activities/       # Logika aktivitas UI (Splash, Sales, Expense, Backup, dll)
│   │   │   │   ├── adapters/         # Pengatur data daftar RecyclerView (Product, Transaction, Sales, dll)
│   │   │   │   ├── database/         # DatabaseHelper (Skema SQL, Kueri gabungan, & Sinkronisasi)
│   │   │   │   ├── models/           # Objek data (Product, Sale, Expense, BackupLog)
│   │   │   │   ├── utils/            # Utilitas pembantu (ExportUtil untuk PDF/CSV, FormatUtil untuk uang/tanggal)
│   │   │   │   └── MainActivity.java # Halaman Utama Dashboard & Grafik Finansial
│   │   │   │
│   │   │   └── res/
│   │   │       ├── drawable/         # Aset ikon dan custom background (bg_pill, bg_input, bg_spinner)
│   │   │       ├── layout/           # Desain halaman XML
│   │   │       ├── values/           # Konstanta teks, tema dialog, dan warna mode terang (White/Zinc)
│   │   │       └── values-night/     # Konstanta warna tema gelap (Zinc Dark)
│   │   │
│   │   └── AndroidManifest.xml       # Konfigurasi FileProvider & izin deklarasi aplikasi
│   │
│   └── build.gradle                  # Depedensi Gradle modul aplikasi
│
└── build.gradle                      # Gradle konfigurasi proyek
```

---

## 🚀 Cara Setup & Mengompilasi Proyek

1. **Clone repositori ini**:
   ```bash
   git clone https://github.com/sntrpmks/CashFlowku.git
   ```
2. **Buka di Android Studio**:
   * Pilih menu **File > Open**, lalu arahkan ke direktori hasil kloning.
   * Biarkan Gradle melakukan sinkronisasi dependensi otomatis.
3. **Konfigurasi JDK**:
   * Pastikan Gradle menggunakan JDK 17 (dapat disesuaikan lewat *Settings > Build, Execution, Deployment > Build Tools > Gradle*).
4. **Kompilasi dan Membuat APK**:
   * Untuk membersihkan build lama:
     ```bash
     ./gradlew clean
     ```
   * Untuk mengompilasi file Java:
     ```bash
     ./gradlew compileDebugJavaWithJavac
     ```
   * Untuk membuat file APK debug (`app-debug.apk`):
     ```bash
     ./gradlew assembleDebug
     ```
     Hasil APK dapat ditemukan di direktori `app/build/outputs/apk/debug/`.

---

## 👨‍💻 Developer

Aplikasi ini dikembangkan dan disempurnakan oleh:
* **Sinatria Pamungkas**

---

## 📄 Lisensi

Proyek ini berada di bawah lisensi **MIT License** - lihat file [LICENSE](LICENSE) untuk detail lebih lanjut.
#   C a s h F l o w k u 
 
 
