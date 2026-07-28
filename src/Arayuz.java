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
    private JPanel pnlAracListesi;
    private String seciliAracPlakasi = null;
    private JComboBox<String> cbAracPlaka;
    
    private JTable tabloAracGorev;
    private DefaultTableModel modelAracGorev;
    private JPanel pnlAracGorev;

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

        JPanel pnlSol = new JPanel(new BorderLayout(0, 15));
        pnlSol.setPreferredSize(new Dimension(280, 0));
        pnlSol.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Araç Kayıt / Düzenleme"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Sol paneldeki kutucuklar alt alta daha şık durur (Etiket üstte, kutu altta)
        JPanel pnlGirdiAlanlari = new JPanel(new GridLayout(8, 1, 0, 5));
        
        pnlGirdiAlanlari.add(new JLabel("Marka:"));
        txtMarka = new JTextField();
        pnlGirdiAlanlari.add(txtMarka);

        pnlGirdiAlanlari.add(new JLabel("Model:"));
        txtModel = new JTextField();
        pnlGirdiAlanlari.add(txtModel);

        pnlGirdiAlanlari.add(new JLabel("Plaka:"));
        txtPlaka = new JTextField();
        pnlGirdiAlanlari.add(txtPlaka);

        pnlGirdiAlanlari.add(new JLabel("Şase No:"));
        txtSaseNo = new JTextField();
        pnlGirdiAlanlari.add(txtSaseNo);

        JButton btnTemizle = new JButton("Formu Temizle");
        btnTemizle.addActionListener(e -> alanlariTemizleArac());

        JButton btnKaydet = new JButton("Yeni Araç Kaydet");
        btnKaydet.addActionListener(e -> aracKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> aracGuncelle());

        // Yan menü dar olduğu için butonları alt alta (3 satır) dizmek daha ergonomiktir
        JPanel pnlButonlar = new JPanel(new GridLayout(3, 1, 0, 8));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        pnlButonlar.add(btnKaydet);
        pnlButonlar.add(btnGuncelle);
        pnlButonlar.add(btnTemizle);
        
        JPanel pnlIcerik = new JPanel(new BorderLayout());
        pnlIcerik.add(pnlGirdiAlanlari, BorderLayout.NORTH);
        pnlIcerik.add(pnlButonlar, BorderLayout.CENTER);

        pnlSol.add(pnlIcerik, BorderLayout.NORTH);

        panelArac.add(pnlSol, BorderLayout.WEST);

        modelAracGorev = new DefaultTableModel(new String[]{"Şoför", "İl", "İlçe", "Tarih", "Açıklama"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloAracGorev = new JTable(modelAracGorev);
        tabloAciklamaDinleyicisiEkle(tabloAracGorev, 4);
        
        pnlAracGorev = new JPanel(new BorderLayout());
        pnlAracGorev.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 25, 15, 25), // İçeri doğru girinti (Indent)
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true)
        ));
        pnlAracGorev.setPreferredSize(new Dimension(0, 160));
        pnlAracGorev.setBackground(Color.WHITE);
        pnlAracGorev.add(new JScrollPane(tabloAracGorev), BorderLayout.CENTER);
        
        pnlAracGorev.setVisible(false);
        
        pnlAracListesi = new JPanel();
        pnlAracListesi.setLayout(new BoxLayout(pnlAracListesi, BoxLayout.Y_AXIS));
        pnlAracListesi.setBackground(Color.WHITE);
        pnlAracListesi.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Listenin dış boşluğu

        JScrollPane scrollPane = new JScrollPane(pnlAracListesi);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panelArac.add(scrollPane, BorderLayout.CENTER);

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

        JPanel pnlGirdiAlanlari = new JPanel(new GridLayout(6, 2, 5, 5));
        
        pnlGirdiAlanlari.add(new JLabel("Araç Plakası:"));
        cbAracPlaka = new JComboBox<>();
        pnlGirdiAlanlari.add(cbAracPlaka);

        pnlGirdiAlanlari.add(new JLabel("Şoför Adı:"));
        txtSoforAdi = new JTextField();
        pnlGirdiAlanlari.add(txtSoforAdi);

        pnlGirdiAlanlari.add(new JLabel("İl:"));
        txtIl = new JTextField();
        pnlGirdiAlanlari.add(txtIl);

        pnlGirdiAlanlari.add(new JLabel("İlçe:"));
        txtIlce = new JTextField();
        pnlGirdiAlanlari.add(txtIlce);

        pnlGirdiAlanlari.add(new JLabel("Açıklama (Rapor):"));
        JPanel pnlRaporGirdi = new JPanel(new BorderLayout(5, 0));
        txtRapor = new JTextField();
        txtRapor.setEditable(false);
        JButton btnRaporDetay = new JButton("📝");
        btnRaporDetay.setToolTipText("Açıklama Oku/Yaz (Pop-up)");
        btnRaporDetay.addActionListener(e -> gosterAciklamaPopup("Görev Açıklaması Yaz/Düzenle", txtRapor.getText(), metin -> txtRapor.setText(metin)));
        pnlRaporGirdi.add(txtRapor, BorderLayout.CENTER);
        pnlRaporGirdi.add(btnRaporDetay, BorderLayout.EAST);
        pnlGirdiAlanlari.add(pnlRaporGirdi);

        pnlGirdiAlanlari.add(new JLabel("Tarih (GG/AA/YYYY):"));
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
        pnlGirdiAlanlari.add(txtTarih);

        JButton btnTemizle = new JButton("Formu Temizle");
        btnTemizle.addActionListener(e -> alanlariTemizleGorev());

        JButton Kaydet = new JButton("Yeni Görev Kaydet");
        Kaydet.addActionListener(e -> gorevKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> gorevGuncelle());

        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        pnlButonlar.add(btnTemizle);
        pnlButonlar.add(btnGuncelle);
        pnlButonlar.add(Kaydet);
        
        JPanel pnlKuzey = new JPanel(new BorderLayout());
        pnlKuzey.add(pnlGirdiAlanlari, BorderLayout.CENTER);
        pnlKuzey.add(pnlButonlar, BorderLayout.SOUTH);

        panelGorev.add(pnlKuzey, BorderLayout.NORTH);

        modelGorev = new DefaultTableModel(new String[]{"ID", "Plaka", "Şoför", "İl", "İlçe", "Açıklama", "Tarih"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloGorev = new JTable(modelGorev);
        tabloAciklamaDinleyicisiEkle(tabloGorev, 5);

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

    private void tabloAciklamaDinleyicisiEkle(JTable tablo, int aciklamaSutunIndeksi) {
        tablo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tablo.rowAtPoint(e.getPoint());
                    int col = tablo.columnAtPoint(e.getPoint());
                    if (row >= 0 && col == aciklamaSutunIndeksi) {
                        Object val = tablo.getValueAt(row, col);
                        String metin = (val != null) ? val.toString() : "";
                        gosterAciklamaPopup("Açıklama Detayı (Okuma Modu)", metin, null);
                    }
                }
            }
        });
    }

    private void gosterAciklamaPopup(String baslik, String mevcutMetin, java.util.function.Consumer<String> onKaydet) {
        JDialog dialog = new JDialog(this, baslik, false); // false = Non-Modal (sistemi kilitlemez)
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea taDetay = new JTextArea(mevcutMetin);
        taDetay.setLineWrap(true);
        taDetay.setWrapStyleWord(true);
        taDetay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taDetay.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (onKaydet == null) {
            taDetay.setEditable(false);
            taDetay.setBackground(new Color(245, 245, 245));
        }

        dialog.add(new JScrollPane(taDetay), BorderLayout.CENTER);

        if (onKaydet != null) {
            JButton btnTamam = new JButton("Kaydet ve Kapat");
            btnTamam.addActionListener(e -> {
                onKaydet.accept(taDetay.getText());
                dialog.dispose();
            });
            JPanel pnlAlt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlAlt.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
            pnlAlt.add(btnTamam);
            dialog.add(pnlAlt, BorderLayout.SOUTH);
        }

        dialog.setVisible(true);
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
        tabloAciklamaDinleyicisiEkle(tabloRapor, 7);
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
        if (pnlAracListesi == null) return;
        pnlAracListesi.removeAll();
        cbAracPlaka.removeAllItems();
        
        String sql = "SELECT * FROM Araclar";
        try (Connection conn = Veritabani.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
             while (rs.next()) {
                 String plaka = rs.getString("plaka");
                 String marka = rs.getString("marka");
                 String model = rs.getString("model");
                 String sase = rs.getString("sase");
                 
                 cbAracPlaka.addItem(plaka);
                 
                 JPanel pnlAracItem = new JPanel(new BorderLayout(15, 0));
                 pnlAracItem.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createEmptyBorder(5, 10, 5, 10),
                         BorderFactory.createCompoundBorder(
                                 BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                                 BorderFactory.createEmptyBorder(10, 15, 10, 15)
                         )
                 ));
                 pnlAracItem.setBackground(new Color(245, 247, 250));
                 pnlAracItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                 
                 JLabel lblPlaka = new JLabel(plaka);
                 lblPlaka.setFont(new Font("Segoe UI", Font.BOLD, 16));
                 lblPlaka.setForeground(new Color(30, 70, 140));
                 
                 JLabel lblMarkaModel = new JLabel(marka + " " + model);
                 lblMarkaModel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                 lblMarkaModel.setForeground(Color.DARK_GRAY);
                 
                 JLabel lblSase = new JLabel("Şase: " + sase);
                 lblSase.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                 lblSase.setForeground(Color.GRAY);
                 
                 pnlAracItem.add(lblPlaka, BorderLayout.WEST);
                 pnlAracItem.add(lblMarkaModel, BorderLayout.CENTER);
                 pnlAracItem.add(lblSase, BorderLayout.EAST);
                 pnlAracItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                 
                 JPanel pnlKapsayici = new JPanel(new BorderLayout());
                 pnlKapsayici.add(pnlAracItem, BorderLayout.NORTH);
                 pnlKapsayici.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Default closed height
                 pnlKapsayici.setAlignmentX(Component.LEFT_ALIGNMENT); // Sola hizala (kırılmaları önler)
                 pnlKapsayici.setBackground(Color.WHITE);
                 
                 java.awt.event.MouseAdapter clickAdapter = new java.awt.event.MouseAdapter() {
                     public void mouseClicked(java.awt.event.MouseEvent evt) {
                         // Eğer zaten açık olan bir araca (akordeona) tıkladıysa kapat (Toggle Off)
                         if (plaka.equals(seciliAracPlakasi) && pnlAracGorev.isVisible() && pnlAracGorev.getParent() == pnlKapsayici) {
                             pnlKapsayici.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                             pnlAracItem.setBackground(new Color(245, 247, 250));
                             pnlAracGorev.setVisible(false);
                             
                             // Formu temizle ve seçimi kaldır
                             seciliAracPlakasi = null;
                             txtPlaka.setText("");
                             txtMarka.setText("");
                             txtModel.setText("");
                             txtSaseNo.setText("");
                             txtPlaka.setEnabled(true);
                             
                             pnlAracListesi.revalidate();
                             pnlAracListesi.repaint();
                             return; // Açma kodlarına geçmeden metodu bitir
                         }
                         
                         // Farklı bir araca tıkladıysa veya kapalıysa aç (Toggle On)
                         seciliAracPlakasi = plaka;
                         txtPlaka.setText(plaka);
                         txtMarka.setText(marka);
                         txtModel.setText(model);
                         txtSaseNo.setText(sase);
                         txtPlaka.setEnabled(false);
                         
                         aracaAitGorevleriTabloyaDoldur(plaka);
                         pnlAracGorev.setVisible(true);
                         
                         pnlKapsayici.add(pnlAracGorev, BorderLayout.CENTER);
                         
                         // Diğer tüm panelleri kapat ve varsayılan renge döndür
                         for (Component c : pnlAracListesi.getComponents()) {
                             if (c instanceof JPanel && c != pnlKapsayici) {
                                 ((JPanel) c).setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                                 ((JPanel) c).getComponent(0).setBackground(new Color(245, 247, 250));
                             }
                         }
                         
                         pnlKapsayici.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240)); // Genişlet
                         pnlAracItem.setBackground(new Color(220, 235, 255)); // Mavi vurgu rengi
                         
                         pnlAracListesi.revalidate();
                         pnlAracListesi.repaint();
                     }
                 };
                 
                 pnlAracItem.addMouseListener(clickAdapter);
                 lblPlaka.addMouseListener(clickAdapter);
                 lblMarkaModel.addMouseListener(clickAdapter);
                 lblSase.addMouseListener(clickAdapter);
                 
                 pnlAracListesi.add(pnlKapsayici);
                 pnlAracListesi.add(Box.createVerticalStrut(5)); // Spacer
             }
             pnlAracListesi.add(Box.createVerticalGlue()); // Push to top
             pnlAracListesi.revalidate();
             pnlAracListesi.repaint();
             
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
            Toast.hata(this, "Lütfen tüm alanları doldurun!");
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
            Toast.hata(this, "Lütfen güncellenecek aracı seçin!");
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
            
            Toast.basarili(this, "Araç güncellendi.");
            araclariYukle();
            raporlariYukle();
            alanlariTemizleArac();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aracSil() {
        if (seciliAracPlakasi == null) {
            Toast.hata(this, "Lütfen silinecek aracı listeden seçin!");
            return;
        }
        
        int onay = JOptionPane.showConfirmDialog(this, "Seçili aracı silmek istediğinize emin misiniz?" , "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay == JOptionPane.YES_OPTION) {
            String plaka = seciliAracPlakasi;
            String sql = "DELETE FROM Araclar WHERE plaka = ?";
            try (Connection conn = Veritabani.baglan();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                Statement pragmaStmt = conn.createStatement();
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
                
                pstmt.setString(1, plaka);
                pstmt.executeUpdate();
                
                Toast.basarili(this, "Araç silindi.");
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
        if (seciliAracPlakasi == null) {
            Toast.uyari(this, "Önce bir araç seçmelisiniz!");
            return;
        }
        String plaka = seciliAracPlakasi;
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
        
        // Eğer Araç Yönetimi sekmesinde bir araç seçiliyse alt tabloyu da güncelle
        if (seciliAracPlakasi != null) {
            aracaAitGorevleriTabloyaDoldur(seciliAracPlakasi);
        }
    }

    private void aracaAitGorevleriTabloyaDoldur(String plaka) {
        if (modelAracGorev == null) return;
        modelAracGorev.setRowCount(0);
        String sql = "SELECT * FROM Gorevler WHERE plaka = ?";
        try (Connection conn = Veritabani.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             pstmt.setString(1, plaka);
             ResultSet rs = pstmt.executeQuery();
             
             while (rs.next()) {
                 modelAracGorev.addRow(new Object[]{
                     rs.getString("sofor"), rs.getString("il"), rs.getString("ilce"), 
                     rs.getString("tarih"), rs.getString("rapor")
                 });
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Araç görevleri yüklenirken hata: " + e.getMessage());
        }
    }

    private void gorevKaydet() {
        if (cbAracPlaka.getSelectedItem() == null) {
            Toast.hata(this, "Lütfen önce bir araç kaydedin veya seçin!");
            return;
        }

        String plaka = cbAracPlaka.getSelectedItem().toString();
        String sofor = txtSoforAdi.getText().trim();
        String il = txtIl.getText().trim();
        String ilce = txtIlce.getText().trim();
        String rapor = txtRapor.getText().trim();
        String tarih = txtTarih.getText().trim();

        if (sofor.isEmpty() || il.isEmpty() || ilce.isEmpty() || rapor.isEmpty() || tarih.isEmpty()) {
            Toast.hata(this, "Lütfen tüm alanları doldurun!");
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
            
            Toast.basarili(this, "Görev başarıyla kaydedildi.");
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
            Toast.hata(this, "Lütfen güncellenecek görevi tablodan seçin!");
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
            
            Toast.basarili(this, "Görev güncellendi.");
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
                
                Toast.basarili(this, "Görev silindi.");
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
        seciliAracPlakasi = null;
        if (pnlAracGorev != null) {
            pnlAracGorev.setVisible(false);
        }
        araclariYukle(); // Listeyi sıfırlamak ve açılan akordeonu kapatmak için yeniden yükle
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