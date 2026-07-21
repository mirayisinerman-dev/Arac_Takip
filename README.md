<div align="center">
  <h1>Araç Takip ve Görevlendirme Sistemi</h1>
  <p>
    <b>Java ve Swing Kullanılarak Geliştirilmiş, Kapsamlı Otomasyon ve Raporlama Sistemi</b>
  </p>
  
  <p>
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java Badge"/>
    <img src="https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge" alt="Swing Badge"/>
    <img src="https://img.shields.io/badge/File_I%2FO-Database-green?style=for-the-badge" alt="File IO Badge"/>
    <img src="https://img.shields.io/badge/Status-Completed-success?style=for-the-badge" alt="Status Badge"/>
  </p>
</div>

---

## Proje Hakkında
Bu proje, araç filolarını yönetmek ve araçlara atanan görevleri takip etmek amacıyla **Nesne Yönelimli Programlama (OOP)** prensipleri kullanılarak geliştirilmiş bir masaüstü Java uygulamasıdır. 

Harici bir veritabanına ihtiyaç duymadan, Java'nın dahili **File I/O (Dosya Okuma/Yazma)** mimarisiyle verileri güvenli ve kalıcı bir şekilde depolar. 

## Temel Özellikler

* **Araç Yönetimi (CRUD):** Sisteme yeni araçlar ekleyebilir, silebilir veya bilgileri güncelleyebilirsiniz.
* **Görev Atama:** Araçlara şoför, görev yeri (il/ilçe) ve açıklama girilerek dinamik görevler tanımlanabilir.
* **Akıllı Raporlama:** Tüm araçlar ve görevler arka planda eşleştirilerek tek bir ekranda listelenir.
* **Haftalık Rapor Filtresi:** Sadece tek tuşla, **son 7 gün içerisinde** yapılan tüm görevler analiz edilip rapora dökülür. Görevi olmayan araçlar akıllıca tespit edilir.
* **Kapsülleme (Encapsulation) & Hata Yönetimi:** Girilen veriler korunur, hatalı tarih formatları ve sistem çökmeleri (Exception) otomatik engellenir.

## Kullanılan Teknolojiler

* **Dil:** Java (JDK 8+)
* **Arayüz (GUI):** Java Swing (JFrame, JTabbedPane, JTable, GridLayout, BorderLayout)
* **Veri Yönetimi:** File I/O (`BufferedReader`, `BufferedWriter`, `FileWriter`)
* **Tarih Kütüphaneleri:** `java.time.LocalDate`

## Kurulum ve Çalıştırma

Projeyi bilgisayarınızda çalıştırmak için aşağıdaki adımları izleyebilirsiniz:

1. Bu depoyu bilgisayarınıza klonlayın:
   ```bash
   git clone https://github.com/mirayisinerman-dev/Arac_Takip.git
   ```
2. Projeyi **IntelliJ IDEA**, **Eclipse** veya herhangi bir Java IDE'sinde açın.
3. `src/` klasörü içerisindeki `Main.java` dosyasını çalıştırın.
4. Program açıldığında araçları kaydedip, görevlendirmeleri test edebilirsiniz!

---

<div align="center">
  <i>Bu proje staj eğitimi kapsamında geliştirilmiş olup, temiz kod (Clean Code) ve OOP prensiplerine sadık kalınarak tasarlanmıştır.</i>
</div>