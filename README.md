# DATZON Liquid Calculator

Kalkulator premium dengan visual dark **liquid glass**, aksen mint, tombol besar yang ramah layar sentuh, animasi hasil, dan mesin aritmetika lokal. Tidak menggunakan API, tidak perlu login, dan tidak mengirim perhitungan ke server.

## Isi paket

- `android-native/` — aplikasi **native Android** memakai Kotlin + Jetpack Compose. Ini bukan WebView dan bukan website yang dibungkus menjadi aplikasi.
- `pwa/` — build alternatif yang bisa dibuka di browser atau dipasang sebagai PWA setelah di-host lewat HTTPS. PWA ikut disertakan supaya tampilan bisa dicoba dulu dengan cepat.
- `.github/workflows/build-android.yml` — workflow untuk membuat APK debug lewat GitHub Actions.

## Cara membangun APK native

Build tool Android tidak disertakan di paket ini, jadi APK belum dikompilasi di lingkungan pembuat. Source native dan workflow build sudah disiapkan.

**Lewat Android Studio:**
1. Ekstrak ZIP ini.
2. Buka folder `android-native` di Android Studio.
3. Izinkan Gradle menyinkronkan dependency, lalu pilih **Build → Build APK(s)**.
4. APK debug akan berada di `android-native/app/build/outputs/apk/debug/app-debug.apk`.

**Lewat GitHub Actions:**
1. Buat repository GitHub kosong, lalu upload seluruh isi ZIP ini ke root repository (termasuk folder `.github`).
2. Buka tab **Actions**, pilih **Build Android APK**, lalu jalankan workflow bila belum terpicu otomatis.
3. Setelah workflow sukses, buka run terbaru dan unduh artifact `DATZON-Liquid-Calculator-debug-apk`. Artifact itu berisi `app-debug.apk` yang bisa dipindahkan ke Android untuk instalasi.

APK debug ditujukan untuk uji coba. Untuk distribusi publik yang rapi, buat signing key milik sendiri dan build release bertanda tangan.

## Coba versi PWA

- Buka `pwa/index.html` di browser untuk mencoba kalkulator. File ini tidak butuh API atau koneksi internet.
- Untuk memasang sebagai aplikasi dari Chrome Android, host isi folder `pwa/` di HTTPS (misalnya Vercel atau GitHub Pages), buka URL di Chrome, lalu pilih **Install app** atau **Tambahkan ke layar utama**.

## Fitur

- Operasi tambah, kurang, kali, bagi, persen, desimal, hapus karakter, dan ubah tanda.
- Urutan operasi matematika standar.
- Penanganan pembagian dengan nol dan ekspresi yang belum lengkap.
- UI responsif, latar ber-gradasi, panel kaca transparan, dan aksen mint.
- Berjalan offline; tidak membutuhkan izin Android atau API key.

## Identitas aplikasi

- Nama: DATZON Calculator
- Package: `com.datzon.liquidcalculator`
- Versi awal: `1.0.0`
- Minimum Android: Android 8.0 (API 26)
