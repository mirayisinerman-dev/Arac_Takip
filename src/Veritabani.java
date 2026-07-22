import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Veritabani {
    private static final String DB_URL = "jdbc:sqlite:arac_takip.db";

    public static Connection baglan() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Veritabanı bağlantı hatası: " + e.getMessage());
        }
        return conn;
    }

    public static void tablolariOlustur() {
        String sqlAraclar = "CREATE TABLE IF NOT EXISTS Araclar (" + "plaka TEXT PRIMARY KEY, " + "marka TEXT NOT NULL, " + "model TEXT NOT NULL, " + "sase TEXT NOT NULL" + ");";
        String sqlGorevler = "CREATE TABLE IF NOT EXISTS Gorevler (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "plaka TEXT NOT NULL, " + "sofor TEXT NOT NULL, " + "il TEXT NOT NULL, " + "ilce TEXT NOT NULL, " + "rapor TEXT NOT NULL, " + "tarih TEXT NOT NULL, " + "FOREIGN KEY(plaka) REFERENCES Araclar(plaka) ON DELETE CASCADE" + ");";
        String sqlServis = "CREATE TABLE IF NOT EXISTS Servis (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "plaka TEXT NOT NULL, " + "tur TEXT NOT NULL, " + "tutar REAL NOT NULL, " + "aciklama TEXT NOT NULL, " + "tarih TEXT NOT NULL, " + "FOREIGN KEY(plaka) REFERENCES Araclar(plaka) ON DELETE CASCADE" + ");";

        try (Connection conn = baglan();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(sqlAraclar);
            stmt.execute(sqlGorevler);
            stmt.execute(sqlServis);
            
        } catch (SQLException e) {
            System.out.println("Tablo oluşturma hatası: " + e.getMessage());
        }
    }
}
