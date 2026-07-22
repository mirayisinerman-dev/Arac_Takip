import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServisEkrani extends JDialog {
    private String plaka;
    private JTable tabloServis;
    private DefaultTableModel modelServis;
    private JComboBox<String> cbTur;
    private JTextField txtTutar, txtAciklama, txtTarih;

    public ServisEkrani(JFrame parent, String plaka) {
        super(parent, "Servis ve Gider Yönetimi: " + plaka, true);
        this.plaka = plaka;
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        olusturArayuz();
        verileriYukle();
    }

    private void olusturArayuz() {
        // Üst panel (Veri Giriş Formu)
        JPanel pnlGirdi = new JPanel(new GridLayout(5, 2, 5, 5));
        pnlGirdi.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlGirdi.add(new JLabel("Gider Türü:"));
        cbTur = new JComboBox<>(new String[]{"Bakım / Onarım", "Trafik Cezası", "Yakıt", "Sigorta / Kasko", "Diğer"});
        pnlGirdi.add(cbTur);

        pnlGirdi.add(new JLabel("Tutar (TL):"));
        txtTutar = new JTextField();
        pnlGirdi.add(txtTutar);

        pnlGirdi.add(new JLabel("Açıklama:"));
        txtAciklama = new JTextField();
        pnlGirdi.add(txtAciklama);

        pnlGirdi.add(new JLabel("Tarih (GG/AA/YYYY):"));
        txtTarih = new JTextField();
        pnlGirdi.add(txtTarih);

        JButton btnKaydet = new JButton("Gideri Kaydet");
        btnKaydet.addActionListener(e -> kaydet());
        
        JButton btnKapat = new JButton("Kapat");
        btnKapat.addActionListener(e -> dispose());

        pnlGirdi.add(btnKaydet);
        pnlGirdi.add(btnKapat);

        add(pnlGirdi, BorderLayout.NORTH);

        // Orta panel (Tablo)
        modelServis = new DefaultTableModel(new String[]{"ID", "Tür", "Tutar (TL)", "Açıklama", "Tarih"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloServis = new JTable(modelServis);
        add(new JScrollPane(tabloServis), BorderLayout.CENTER);

        // Alt panel (Silme)
        JPanel pnlAlt = new JPanel(new FlowLayout());
        JButton btnSil = new JButton("Seçili Kaydı Sil");
        btnSil.addActionListener(e -> sil());
        pnlAlt.add(btnSil);
        add(pnlAlt, BorderLayout.SOUTH);
    }

    private void verileriYukle() {
        modelServis.setRowCount(0);
        String sql = "SELECT * FROM Servis WHERE plaka = ?";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             pstmt.setString(1, plaka);
             ResultSet rs = pstmt.executeQuery();
             
             while (rs.next()) {
                 modelServis.addRow(new Object[]{
                     rs.getInt("id"), rs.getString("tur"), rs.getDouble("tutar"), 
                     rs.getString("aciklama"), rs.getString("tarih")
                 });
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kayıtlar yüklenirken hata: " + e.getMessage());
        }
    }

    private void kaydet() {
        String tur = cbTur.getSelectedItem().toString();
        String tutarStr = txtTutar.getText().trim();
        String aciklama = txtAciklama.getText().trim();
        String tarih = txtTarih.getText().trim();

        if (tutarStr.isEmpty() || aciklama.isEmpty() || tarih.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double tutar;
        try {
            tutarStr = tutarStr.replace(",", "."); // Türkçe virgülü noktaya çevir
            tutar = Double.parseDouble(tutarStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tutar geçerli bir sayı olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Servis(plaka, tur, tutar, aciklama, tarih) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, plaka);
            pstmt.setString(2, tur);
            pstmt.setDouble(3, tutar);
            pstmt.setString(4, aciklama);
            pstmt.setString(5, tarih);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Gider başarıyla kaydedildi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            verileriYukle();
            
            txtTutar.setText("");
            txtAciklama.setText("");
            txtTarih.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kayıt hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sil() {
        int row = tabloServis.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek kaydı tablodan seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int id = (int) modelServis.getValueAt(row, 0);
        int onay = JOptionPane.showConfirmDialog(this, "Seçili harcamayı silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Servis WHERE id = ?";
            try (Connection conn = Veritabani.baglan();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Kayıt silindi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                verileriYukle();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
