# 📦 OfflineBox
<img width="986" height="673" alt="Screenshot 2026-10-06 070235" src="https://github.com/user-attachments/assets/e0d53d02-33cb-4c08-b304-b55c11331cd5" />


**Satu aplikasi, banyak alat semuanya 100% offline.**
OfflineBox adalah "Swiss Army Knife" desktop berisi kumpulan utilitas yang berguna untuk programmer, pelajar, teknisi, pekerja IT, dan pengguna komputer biasa. Tampilannya bergaya **2D pixel art** dan sudah dilengkapi **tutorial di dalam aplikasi**.

Berjalan di **Windows, Linux, dan macOS**,ditulis dengan Java murni (Swing), tanpa dependensi eksternal.

## Features (v1.0)

| Kategori | Alat |
|----------|------|
| SYSTEM   | System Monitor (CPU, RAM, Disk) |
| FILES    | File Hasher (MD5, SHA-1, SHA-256, SHA-512) |
| TEXT     | JSON Formatter, Base64 Encoder/Decoder, URL Encoder/Decoder |
| NETWORK  | Subnet Calculator (IPv4 + CIDR) |
| UTILITY  | Password Generator, Timestamp Converter |

## Requirements

- **JDK 17 atau lebih baru** (dikembangkan untuk JDK 25).
- Tidak perlu internet, tidak perlu library tambahan.

## How to run it

### Dari terminal (Windows / Linux / macOS)

```bash
# 1. Compile
javac -d out src/main/java/offlinebox/Main.java src/main/java/offlinebox/Modules.java

# 2. Jalankan
java -cp out offlinebox.Main
```

### Making JAR file (optional)

```bash
jar --create --file offlinebox.jar --main-class offlinebox.Main -C out .
java -jar offlinebox.jar
```

### Dari IntelliJ IDEA

1. `File > Open` → pilih folder proyek ini.
2. Klik kanan `src/main/java` → **Mark Directory as → Sources Root**.
3. Buka `Main.java`, klik tombol ▶ di samping `main`.

##  How to use it (Indonesia language only)

1. Pilih alat di daftar sebelah kiri.
2. Isi kotak **INPUT**, lalu klik tombol aksi (ENCODE, FORMAT, HITUNG, dll).
3. Hasil muncul di **OUTPUT**. Klik **COPY** untuk menyalin, **CLEAR** untuk membersihkan.
4. Kotak kuning di bawah menjelaskan cara pakai alat yang sedang aktif.
5. Teks merah = input tidak valid; baca pesannya dan coba lagi.

Contoh cepat:
- **Subnet Calculator**: ketik `192.168.1.10/24` → klik HITUNG.
- **Timestamp Converter**: ketik `1760000000` → klik DETIK -> TANGGAL.
- **File Hasher**: pilih SHA-256 → PILIH FILE → bandingkan hash dengan yang diberikan situs pengunduh.

## Installer Maker (without Java in your devices)

OfflineBox dibungkus dengan `jpackage` (sudah ada di dalam JDK) sehingga membawa runtime Java-nya sendiri.
`jpackage` **tidak bisa cross-compile**: installer Windows harus dibuat di Windows, `.dmg` di macOS, `.deb` di Linux.

### Otomatis lewat GitHub Actions (disarankan)

File `.github/workflows/release.yml` membuat installer untuk ketiga OS sekaligus:

```
git tag v1.0.0
git push origin v1.0.0
```

Hasilnya (`.exe`, `.dmg`, `.deb`) otomatis muncul di halaman **Releases**. Bisa juga dijalankan manual lewat tab **Actions → Build installers → Run workflow**.

### Lokal (untuk uji coba di Linux)

```
mkdir -p out dist
javac --release 25 -d out src/main/java/offlinebox/*.java
jar --create --file dist/offlinebox.jar --main-class offlinebox.Main -C out .
jpackage --name OfflineBox --app-version 1.0.0 --input dist --main-jar offlinebox.jar \
  --main-class offlinebox.Main --add-modules java.desktop,java.management,jdk.management \
  --type app-image --dest installers
./installers/OfflineBox/bin/OfflineBox
```

Catatan:
- Installer belum ditandatangani, jadi Windows (SmartScreen) dan macOS (Gatekeeper) akan menampilkan peringatan saat pertama dibuka.
- `macos-latest` menghasilkan build Apple Silicon (M1 dan seterusnya).
- Untuk ikon sendiri, tambahkan `--icon` (`.ico` di Windows, `.icns` di macOS, `.png` di Linux).

## 🔒 Security

- Tidak ada koneksi jaringan dan tidak ada telemetri.
- Tidak menjalankan program/perintah sistem lain, tidak memakai `eval` atau reflection.
- File hanya **dibaca**, tidak pernah diubah atau dihapus.
- Input teks dibatasi 2 juta karakter agar aplikasi tidak hang.
- Semua input diperiksa ketat (misalnya IPv4 menolak oktet berawalan nol).
- Password dibuat dengan `SecureRandom` dan tidak disimpan.

> Catatan: tidak ada software yang 100% kebal. Karena ini aplikasi open source di komputer pengguna sendiri, "keamanan" di sini berarti aplikasi tidak melakukan hal berbahaya dan menangani input dengan aman. Jika menemukan celah atau bug, silakan buka *Issue*.

##  Menambah Modul Baru

1. Buat fungsi di `Modules.java` yang mengembalikan `JPanel` (untuk alat teks cukup pakai `io(new Op("NAMA", fungsi))`).
2. Daftarkan di `all()` dengan kategori, nama, dan teks cara pakai.

##  Roadmap

Process Manager, Battery Health, File Renamer, Duplicate Finder, Folder Analyzer, Markdown Preview, Regex Tester, Port Checker, DNS Lookup, CSV Viewer, SQLite Viewer, Log Analyzer, QR Generator, Unit Converter, Color Picker.

## Screenshots
<img width="986" height="673" alt="Screenshot 2026-10-06 070516" src="https://github.com/user-attachments/assets/eb314c97-ecde-4491-8f0c-2f15485aa96d" />
<br>
<img width="986" height="673" alt="Screenshot 2026-10-06 070329" src="https://github.com/user-attachments/assets/504d12f2-0d6e-414e-848b-10639c5dd877" />
<br>
<img width="986" height="673" alt="Screenshot 2026-10-06 070313" src="https://github.com/user-attachments/assets/dbe3e6e7-75c5-4f1a-90ed-d88a443c40d0" />

