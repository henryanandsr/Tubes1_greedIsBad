# Tugas Besar I - Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan “Galaxio”
## Deskripsi Tugas
Galaxio adalah sebuah game battle royale yang mempertandingkan bot kapal anda dengan beberapa bot kapal yang lain. Setiap pemain akan memiliki sebuah bot kapal dan tujuan dari permainan adalah agar bot kapal anda yang tetap hidup hingga akhir permainan. Implementasikan algoritma greedy untuk menentukan gerakan bot terbaik pada setiap waktu.
## Anggota Kelompok
| NIM | Nama |
| :---: | :---: |
| 13521003 | Bintang Hijriawan Jachja |
| 13521004 | Henry Anand Septian Radityo |
| 13521009 | Christophorus Dharma Winata |
## Implementasi Algoritma Greedy
Algoritma greedy adalah algoritma yang mencari aksi terbaik yang dapat dilakukan secara lokal dengan harapan didapatkan keuntungan terbaik secara global. Implementasi algoritma ini terhadap bot bekerja dengan mencari aksi yang paling sesuai pada setiap ticknya. Aksi - aksi tersebut dibagi menjadi dua bagian besar yaitu combat dan non - combat. Sesuai namanya combat berfokus pada serangan dan bertahan sedangkan non-combat berfokus untuk menghindari objek yang merugikan maupun mencari makanan.
## Instalasi (Windows)
1. Lakukan clone repository
2. Lakukan juga clone pada repository `https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2`
3. Seuaikan pemain dengan mengubah botcount yang berada pada file `appsettings.json` pada direktori `runner-publish` dan `engine-publish`
4. Buatlah file .bat dan masukkan kode dibawah
```
@echo off
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
timeout /t 3
start "" java -jar :: ../target/greedIsBad.jar
:: sesuaikan dengan jumlah pemain
cd ../


pause

```
5. Jalankan file `run.bat`
## Struktur Program
```
.
│   README.md
|   Dockerfile
|   pom.xml
├───target
│       greedIsBad.jar
│
├───doc
│       greedIsBad.pdf
│       
│
└───src
        main.java
```
