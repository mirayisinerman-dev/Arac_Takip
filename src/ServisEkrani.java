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
    private JTextField txtTL, txtKurus, txtAciklama, txtTarih;

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
        JPanel pnlGirdi = new JPanel(new GridLayout(5, 2, 5, 5));
        pnlGirdi.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlGirdi.add(new JLabel("Gider Türü:"));
        cbTur = new JComboBox<>(new String[]{"Bakım / Onarım", "Trafik Cezası", "Yakıt", "Sigorta / Kasko", "Diğer"});
        pnlGirdi.add(cbTur);

        pnlGirdi.add(new JLabel("Tutar:"));
        JPanel pnlTutar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        txtTL = new JTextField(5);
        txtKurus = new JTextField(3);
        pnlTutar.add(txtTL);
        pnlTutar.add(new JLabel("TL"));
        pnlTutar.add(txtKurus);
        pnlTutar.add(new JLabel("Krş"));
        pnlGirdi.add(pnlTutar);

        pnlGirdi.add(new JLabel("Açıklama:"));
        txtAciklama = new JTextField();
        pnlGirdi.add(txtAciklama);

        pnlGirdi.add(new JLabel("Tarih (GG/AA/YYYY):"));
        txtTarih = new JTextField();
        txtTarih.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = txtTarih.getText();
                if (e.getKeyCode() != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    if (text.length() == 2 || text.length() == 5) {
                        txtTarih.setText(text + "/");
                    }
                }
            }
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (txtTarih.getText().length() >= 10) {
                    e.consume();
                }
            }
        });
        pnlGirdi.add(txtTarih);

        JButton btnTemizle = new JButton("Formu Temizle");
        btnTemizle.addActionListener(e -> alanlariTemizle());

        JButton btnKaydet = new JButton("Yeni Gider Kaydet");
        btnKaydet.addActionListener(e -> kaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> guncelle());

        JPanel pnlButonSol = new JPanel(new GridLayout(1, 1, 5, 0));
        pnlButonSol.add(btnTemizle);

        JPanel pnlButonSag = new JPanel(new GridLayout(1, 2, 5, 0));
        pnlButonSag.add(btnGuncelle);
        pnlButonSag.add(btnKaydet);

        pnlGirdi.add(pnlButonSol);
        pnlGirdi.add(pnlButonSag);

        add(pnlGirdi, BorderLayout.NORTH);


        modelServis = new DefaultTableModel(new String[]{"ID", "Tür", "Tutar (TL)", "Açıklama", "Tarih"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloServis = new JTable(modelServis);
        
        tabloServis.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabloServis.getSelectedRow() != -1) {
                int row = tabloServis.getSelectedRow();
                cbTur.setSelectedItem(modelServis.getValueAt(row, 1).toString());
                
                double tutar = Double.parseDouble(modelServis.getValueAt(row, 2).toString());
                int tl = (int) tutar;
                int kurus = (int) Math.round((tutar - tl) * 100);
                txtTL.setText(String.valueOf(tl));
                if (kurus > 0) {
                    txtKurus.setText(String.format("%02d", kurus));
                } else {
                    txtKurus.setText("00");
                }
                
                txtAciklama.setText(modelServis.getValueAt(row, 3).toString());
                txtTarih.setText(modelServis.getValueAt(row, 4).toString());
            }
        });
        
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
        String tlStr = txtTL.getText().trim();
        String krStr = txtKurus.getText().trim();
        String aciklama = txtAciklama.getText().trim();
        String tarih = txtTarih.getText().trim();

        if ((tlStr.isEmpty() && krStr.isEmpty()) || aciklama.isEmpty() || tarih.isEmpty()) {
            Toast.hata(this, "Lütfen tüm alanları doldurun!");
            return;
        }

        if (tlStr.isEmpty()) tlStr = "0";
        if (krStr.isEmpty()) krStr = "00";

        double tutar;
        try {
            int tl = Integer.parseInt(tlStr);
            int kr = Integer.parseInt(krStr);
            tutar = tl + (kr / 100.0);
        } catch (NumberFormatException e) {
            Toast.hata(this, "Geçersiz Tutar! Sadece rakam giriniz.");
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
            
            Toast.basarili(this, "Gider başarıyla kaydedildi.");
            verileriYukle();
            alanlariTemizle();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Kayıt hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alanlariTemizle() {
        cbTur.setSelectedIndex(0);
        txtTL.setText("");
        txtKurus.setText("");
        txtAciklama.setText("");
        txtTarih.setText("");
        tabloServis.clearSelection();
    }

    private void guncelle() {
        int row = tabloServis.getSelectedRow();
        if (row == -1) {
            Toast.hata(this, "Lütfen güncellenecek kaydı tablodan seçin!");
            return;
        }

        String tur = cbTur.getSelectedItem().toString();
        String tlStr = txtTL.getText().trim();
        String krStr = txtKurus.getText().trim();
        String aciklama = txtAciklama.getText().trim();
        String tarih = txtTarih.getText().trim();

        if ((tlStr.isEmpty() && krStr.isEmpty()) || aciklama.isEmpty() || tarih.isEmpty()) {
            Toast.hata(this, "Lütfen tüm alanları doldurun!");
            return;
        }

        if (tlStr.isEmpty()) tlStr = "0";
        if (krStr.isEmpty()) krStr = "00";

        double tutar;
        try {
            int tl = Integer.parseInt(tlStr);
            int kr = Integer.parseInt(krStr);
            tutar = tl + (kr / 100.0);
        } catch (NumberFormatException e) {
            Toast.hata(this, "Geçersiz Tutar! Sadece rakam giriniz.");
            return;
        }

        int id = (int) modelServis.getValueAt(row, 0);

        String sql = "UPDATE Servis SET tur = ?, tutar = ?, aciklama = ?, tarih = ? WHERE id = ?";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, tur);
            pstmt.setDouble(2, tutar);
            pstmt.setString(3, aciklama);
            pstmt.setString(4, tarih);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
            
            Toast.basarili(this, "Kayıt güncellendi.");
            verileriYukle();
            alanlariTemizle();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sil() {
        int row = tabloServis.getSelectedRow();
        if (row == -1) {
            Toast.hata(this, "Lütfen silinecek kaydı tablodan seçin!");
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
                
                Toast.basarili(this, "Kayıt silindi.");
                verileriYukle();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
