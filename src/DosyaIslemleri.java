import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DosyaIslemleri {
    public static boolean Kaydet(String dosyaAdi, String satir) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dosyaAdi, true))) {
            bw.write(satir);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean TumunuKaydet(String dosyaAdi, List<String> satirlar) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dosyaAdi, false))) {
            for (String satir : satirlar) {
                bw.write(satir);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> Oku(String dosyaAdi) {
        List<String> satirlar = new ArrayList<>();
        File dosya = new File(dosyaAdi);
        if (!dosya.exists()) return satirlar;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            String satir;
            while ((satir = br.readLine()) != null) satirlar.add(satir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return satirlar;
    }
}
