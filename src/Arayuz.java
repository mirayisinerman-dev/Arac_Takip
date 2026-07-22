import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//entera basıldığında kaydet
//tarih girerken otomatik / atsın
//onaylama kutularını sil
//araç plakasında ekstra elle girilebilecek bir alan
public class Arayuz extends JFrame {
    private JTabbedPane sekme;
    private JPanel panelArac;
    private JPanel panelGorev;
    private JPanel panelRapor;

    private JTextField txtMarka, txtModel, txtPlaka, txtSaseNo;
    private JTable tabloArac;
    private DefaultTableModel modelArac;
    private JComboBox<String> cbAracPlaka;

    private JTextField txtSoforAdi, txtIl, txtIlce, txtRapor, txtTarih;
    private JTable tabloGorev;
    private DefaultTableModel modelGorev;

    private JTable tabloRapor;
    private DefaultTableModel modelRapor;
    private boolean haftalikRaporGoster = false;

    public Arayuz() {
        Veritabani.tablolariOlustur();

        setTitle("Araç Takip ve Görevlendirme Sistemi");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        sekme = new JTabbedPane();

        olusturPanelArac();
        olusturPanelGorev();
        olusturPanelRapor();

        sekme.addTab("Araç Yönetimi", panelArac);
        sekme.addTab("Görevlendirme", panelGorev);
        sekme.addTab("Genel Bakış / Raporlar", panelRapor);

        sekme.addChangeListener(e -> {
            if (sekme.getSelectedIndex() == 2) {
                raporlariYukle();
            }
        });

        add(sekme);
        
        araclariYukle();
        gorevleriYukle();

        setVisible(true);
    }

    private void olusturPanelArac() {
        panelArac = new JPanel(new BorderLayout(10, 10));
        panelArac.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel btnGirdisi = new JPanel(new GridLayout(6, 2, 5, 5));
        btnGirdisi.add(new JLabel("Marka:"));
        txtMarka = new JTextField();
        btnGirdisi.add(txtMarka);

        btnGirdisi.add(new JLabel("Model:"));
        txtModel = new JTextField();
        btnGirdisi.add(txtModel);

        btnGirdisi.add(new JLabel("Plaka:"));
        txtPlaka = new JTextField();
        btnGirdisi.add(txtPlaka);

        btnGirdisi.add(new JLabel("Şase No:"));
        txtSaseNo = new JTextField();
        btnGirdisi.add(txtSaseNo);

        JButton btnKaydet = new JButton("Yeni Araç Kaydet");
        btnKaydet.addActionListener(e -> aracKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> aracGuncelle());

        btnGirdisi.add(btnGuncelle);
        btnGirdisi.add(btnKaydet);

        panelArac.add(btnGirdisi, BorderLayout.NORTH);

        modelArac = new DefaultTableModel(new String[]{"Plaka", "Marka", "Model", "Şase No"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloArac = new JTable(modelArac);
        
        tabloArac.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabloArac.getSelectedRow() != -1) {
                int row = tabloArac.getSelectedRow();
                txtPlaka.setText(modelArac.getValueAt(row, 0).toString());
                txtMarka.setText(modelArac.getValueAt(row, 1).toString());
                txtModel.setText(modelArac.getValueAt(row, 2).toString());
                txtSaseNo.setText(modelArac.getValueAt(row, 3).toString());
                txtPlaka.setEnabled(false);
            }
        });

        panelArac.add(new JScrollPane(tabloArac), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout());
        JButton btnSil = new JButton("Seçili Aracı Sil");
        btnSil.addActionListener(e -> aracSil());
        
        JButton btnServis = new JButton("Servis ve Giderleri Yönet");
        btnServis.addActionListener(e -> servisEkraniniAc());

        pnlAlt.add(btnSil);
        pnlAlt.add(btnServis);
        panelArac.add(pnlAlt, BorderLayout.SOUTH);
    }

    private void olusturPanelGorev() {
        panelGorev = new JPanel(new BorderLayout(10, 10));
        panelGorev.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel Girdi = new JPanel(new GridLayout(8, 2, 5, 5));
        
        Girdi.add(new JLabel("Araç Plakası:"));
        cbAracPlaka = new JComboBox<>();
        Girdi.add(cbAracPlaka);

        Girdi.add(new JLabel("Şoför Adı:"));
        txtSoforAdi = new JTextField();
        Girdi.add(txtSoforAdi);

        Girdi.add(new JLabel("İl:"));
        txtIl = new JTextField();
        Girdi.add(txtIl);

        Girdi.add(new JLabel("İlçe:"));
        txtIlce = new JTextField();
        Girdi.add(txtIlce);

        Girdi.add(new JLabel("Açıklama (Rapor):"));
        txtRapor = new JTextField();
        Girdi.add(txtRapor);

        Girdi.add(new JLabel("Tarih (GG/AA/YYYY):"));
        txtTarih = new JTextField();
        Girdi.add(txtTarih);

        JButton Kaydet = new JButton("Yeni Görev Kaydet");
        Kaydet.addActionListener(e -> gorevKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> gorevGuncelle());

        Girdi.add(btnGuncelle);
        Girdi.add(Kaydet);

        panelGorev.add(Girdi, BorderLayout.NORTH);

        modelGorev = new DefaultTableModel(new String[]{"ID", "Plaka", "Şoför", "İl", "İlçe", "Açıklama", "Tarih"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloGorev = new JTable(modelGorev);

        tabloGorev.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabloGorev.getSelectedRow() != -1) {
                int row = tabloGorev.getSelectedRow();
                cbAracPlaka.setSelectedItem(modelGorev.getValueAt(row, 1).toString());
                txtSoforAdi.setText(modelGorev.getValueAt(row, 2).toString());
                txtIl.setText(modelGorev.getValueAt(row, 3).toString());
                txtIlce.setText(modelGorev.getValueAt(row, 4).toString());
                txtRapor.setText(modelGorev.getValueAt(row, 5).toString());
                txtTarih.setText(modelGorev.getValueAt(row, 6).toString());
            }
        });

        panelGorev.add(new JScrollPane(tabloGorev), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout());
        JButton btnSil = new JButton("Seçili Görevi Sil");
        btnSil.addActionListener(e -> gorevSil());

        pnlAlt.add(btnSil);
        panelGorev.add(pnlAlt, BorderLayout.SOUTH);
    }

    private void olusturPanelRapor() {
        panelRapor = new JPanel(new BorderLayout(10, 10));
        panelRapor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblBaslik = new JLabel("Araç Durumları ve Görev Kayıtları");
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 16));
        lblBaslik.setHorizontalAlignment(SwingConstants.CENTER);
        panelRapor.add(lblBaslik, BorderLayout.NORTH);

        modelRapor = new DefaultTableModel(new String[]{"Görev ID", "Plaka", "Marka", "Model", "Şoför", "Görev Yeri (İl/İlçe)", "Tarih", "Açıklama"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabloRapor = new JTable(modelRapor);
        panelRapor.add(new JScrollPane(tabloRapor), BorderLayout.CENTER);

        JPanel pnlFiltre = new JPanel(new FlowLayout());
        JButton btnTumu = new JButton("Tüm Görevleri Göster");
        JButton btnHaftalik = new JButton("Haftalık Rapor (Son 7 Gün)");
        
        btnTumu.addActionListener(e -> {
            haftalikRaporGoster = false;
            raporlariYukle();
        });
        
        btnHaftalik.addActionListener(e -> {
            haftalikRaporGoster = true;
            raporlariYukle();
        });
        
        pnlFiltre.add(btnTumu);
        pnlFiltre.add(btnHaftalik);
        panelRapor.add(pnlFiltre, BorderLayout.SOUTH);
    }

    private void araclariYukle() {
        modelArac.setRowCount(0);
        cbAracPlaka.removeAllItems();
        
        String sql = "SELECT * FROM Araclar";
        try (Connection conn = Veritabani.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
             while (rs.next()) {
                 String plaka = rs.getString("plaka");
                 modelArac.addRow(new Object[]{
                     plaka, rs.getString("marka"), rs.getString("model"), rs.getString("sase")
                 });
                 cbAracPlaka.addItem(plaka);
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Araçlar yüklenirken hata: " + e.getMessage());
        }
    }

    private void aracKaydet() {
        String marka = txtMarka.getText().trim();
        String model = txtModel.getText().trim();
        String plaka = txtPlaka.getText().trim();
        String saseNo = txtSaseNo.getText().trim();

        if (marka.isEmpty() || model.isEmpty() || plaka.isEmpty() || saseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Araclar(plaka, marka, model, sase) VALUES(?, ?, ?, ?)";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, plaka);
            pstmt.setString(2, marka);
            pstmt.setString(3, model);
            pstmt.setString(4, saseNo);
            pstmt.executeUpdate();

            araclariYukle();
            alanlariTemizleArac();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kayıt hatası: Bu plaka zaten mevcut olabilir.\n" + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aracGuncelle() {
        if (txtPlaka.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek aracı seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE Araclar SET marka = ?, model = ?, sase = ? WHERE plaka = ?";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, txtMarka.getText().trim());
            pstmt.setString(2, txtModel.getText().trim());
            pstmt.setString(3, txtSaseNo.getText().trim());
            pstmt.setString(4, txtPlaka.getText().trim());
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Araç güncellendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            araclariYukle();
            raporlariYukle();
            alanlariTemizleArac();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aracSil() {
        int row = tabloArac.getSelectedRow();
        if (row == -1) return;
        
        int onay = JOptionPane.showConfirmDialog(this, "Seçili aracı silmek istediğinize emin misiniz?" , "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay == JOptionPane.YES_OPTION) {
            String plaka = modelArac.getValueAt(row, 0).toString();
            String sql = "DELETE FROM Araclar WHERE plaka = ?";
            try (Connection conn = Veritabani.baglan();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                Statement pragmaStmt = conn.createStatement();
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
                
                pstmt.setString(1, plaka);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Araç silindi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                araclariYukle();
                gorevleriYukle();
                raporlariYukle();
                alanlariTemizleArac();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void servisEkraniniAc() {
        int row = tabloArac.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen servis/gider kaydı girmek istediğiniz aracı tablodan seçin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String plaka = modelArac.getValueAt(row, 0).toString();
        new ServisEkrani(this, plaka).setVisible(true);
    }

    private void gorevleriYukle() {
        modelGorev.setRowCount(0);
        String sql = "SELECT * FROM Gorevler";
        try (Connection conn = Veritabani.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
             while (rs.next()) {
                 modelGorev.addRow(new Object[]{
                     rs.getInt("id"), rs.getString("plaka"), rs.getString("sofor"), 
                     rs.getString("il"), rs.getString("ilce"), rs.getString("rapor"), rs.getString("tarih")
                 });
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Görevler yüklenirken hata: " + e.getMessage());
        }
    }

    private void gorevKaydet() {
        if (cbAracPlaka.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir araç kaydedin veya seçin,", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String plaka = cbAracPlaka.getSelectedItem().toString();
        String sofor = txtSoforAdi.getText().trim();
        String il = txtIl.getText().trim();
        String ilce = txtIlce.getText().trim();
        String rapor = txtRapor.getText().trim();
        String tarih = txtTarih.getText().trim();

        if (sofor.isEmpty() || il.isEmpty() || ilce.isEmpty() || rapor.isEmpty() || tarih.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Gorevler(plaka, sofor, il, ilce, rapor, tarih) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, plaka);
            pstmt.setString(2, sofor);
            pstmt.setString(3, il);
            pstmt.setString(4, ilce);
            pstmt.setString(5, rapor);
            pstmt.setString(6, tarih);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Görev başarıyla kaydedildi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            gorevleriYukle();
            raporlariYukle();
            alanlariTemizleGorev();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kayıt hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gorevGuncelle() {
        int row = tabloGorev.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek görevi tablodan seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) modelGorev.getValueAt(row, 0);
        
        String sql = "UPDATE Gorevler SET plaka = ?, sofor = ?, il = ?, ilce = ?, rapor = ?, tarih = ? WHERE id = ?";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, cbAracPlaka.getSelectedItem().toString());
            pstmt.setString(2, txtSoforAdi.getText().trim());
            pstmt.setString(3, txtIl.getText().trim());
            pstmt.setString(4, txtIlce.getText().trim());
            pstmt.setString(5, txtRapor.getText().trim());
            pstmt.setString(6, txtTarih.getText().trim());
            pstmt.setInt(7, id);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Görev güncellendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            gorevleriYukle();
            raporlariYukle();
            alanlariTemizleGorev();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gorevSil() {
        int row = tabloGorev.getSelectedRow();
        if (row == -1) return;
        
        int id = (int) modelGorev.getValueAt(row, 0);
        int onay = JOptionPane.showConfirmDialog(this, "Seçili görevi silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Gorevler WHERE id = ?";
            try (Connection conn = Veritabani.baglan();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Görev silindi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                gorevleriYukle();
                raporlariYukle();
                alanlariTemizleGorev();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void raporlariYukle() {
        modelRapor.setRowCount(0);

        try (Connection conn = Veritabani.baglan();
             Statement stmt = conn.createStatement()) {
             

             ResultSet rsArac = stmt.executeQuery("SELECT * FROM Araclar");
             List<String[]> aracList = new ArrayList<>();
             while(rsArac.next()){
                 aracList.add(new String[]{rsArac.getString("plaka"), rsArac.getString("marka"), rsArac.getString("model")});
             }
             rsArac.close();

             for (String[] arac : aracList) {
                 String plaka = arac[0];
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM Gorevler WHERE plaka = ?");
                 ps.setString(1, plaka);
                 ResultSet rsGorev = ps.executeQuery();
                 
                 boolean gosterilecekGorevVar = false;
                 
                 while(rsGorev.next()){
                     String tarih = rsGorev.getString("tarih");
                     if (haftalikRaporGoster && !sonYediGunIcindemi(tarih)) {
                         continue;
                     }
                     gosterilecekGorevVar = true;
                     String yer = rsGorev.getString("il") + " / " + rsGorev.getString("ilce");
                     modelRapor.addRow(new Object[]{
                         rsGorev.getInt("id"), plaka, arac[1], arac[2], rsGorev.getString("sofor"), yer, tarih, rsGorev.getString("rapor")
                     });
                 }
                 rsGorev.close();
                 ps.close();
                 
                 if (!gosterilecekGorevVar) {
                     String uyari = haftalikRaporGoster ? "Bu Hafta Görev Yok" : "Görev Yok";
                     modelRapor.addRow(new Object[]{"-", plaka, arac[1], arac[2], uyari, uyari, "-", "-"});
                 }
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Raporlar yüklenirken hata: " + e.getMessage());
        }
    }

    private boolean sonYediGunIcindemi(String tarihStr) {
        if (tarihStr == null || tarihStr.isEmpty()) return false;
        tarihStr = tarihStr.replace(".", "/").replace("-", "/").trim();
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate gorevTarihi = java.time.LocalDate.parse(tarihStr, formatter);
            java.time.LocalDate bugun = java.time.LocalDate.now();
            java.time.LocalDate yediGunOnce = bugun.minusDays(7);
            return !gorevTarihi.isBefore(yediGunOnce) && !gorevTarihi.isAfter(bugun);
        } catch (Exception e) {
            return false;
        }
    }

    private void alanlariTemizleArac() {
        txtMarka.setText("");
        txtModel.setText("");
        txtPlaka.setText("");
        txtPlaka.setEnabled(true);
        txtSaseNo.setText("");
        tabloArac.clearSelection();
    }

    private void alanlariTemizleGorev() {
        txtSoforAdi.setText("");
        txtIl.setText("");
        txtIlce.setText("");
        txtRapor.setText("");
        txtTarih.setText("");
        tabloGorev.clearSelection();
    }
}