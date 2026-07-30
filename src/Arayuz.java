import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.MaskFormatter;
public class Arayuz extends JFrame {
    private JPanel kartPaneli;
    private CardLayout kartLayout;
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

        // --- UI GÖRSEL AYARLARI ---
        setGlobalFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        // Ultra-Modern FlatLaf Teması (Mac/Web tarzı)
        try {
            // JAR dosyası IntelliJ'den eklendiğinde tam Mac/Windows 11 tasarımına geçer.
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            System.err.println("FlatLaf JAR bulunamadı. Uygulama standart görünümde açılıyor.");
        }

        setTitle("Araç Takip ve Görevlendirme Sistemi");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        olusturPanelArac();
        olusturPanelGorev();
        olusturPanelRapor();

        kartLayout = new CardLayout();
        kartPaneli = new JPanel(kartLayout);
        
        kartPaneli.add(panelArac, "Arac");
        kartPaneli.add(panelGorev, "Gorev");
        kartPaneli.add(panelRapor, "Rapor");

        olusturHeader();
        olusturSidebar();

        add(kartPaneli, BorderLayout.CENTER);
        
        araclariYukle();
        gorevleriYukle();

        setVisible(true);
    }

    private void olusturHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185)); // Kurumsal Mavi
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel lblSirket = new JLabel("Araç Yönetim Sistemi");
        lblSirket.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSirket.setForeground(Color.WHITE);
        
        headerPanel.add(lblSirket, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void olusturSidebar() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(44, 62, 80)); // Koyu Lacivert (Dark Navy)
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnArac = createSidebarButton("Araç Yönetimi");
        JButton btnGorev = createSidebarButton("Görevlendirme");
        JButton btnRapor = createSidebarButton("Genel Bakış / Raporlar");

        btnArac.addActionListener(e -> kartLayout.show(kartPaneli, "Arac"));
        btnGorev.addActionListener(e -> kartLayout.show(kartPaneli, "Gorev"));
        btnRapor.addActionListener(e -> { kartLayout.show(kartPaneli, "Rapor"); raporlariYukle(); });

        sidebarPanel.add(btnArac);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnGorev);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnRapor);

        add(sidebarPanel, BorderLayout.WEST);
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 94));
            }
        });
        return btn;
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
        pnlGirdiAlanlari.setBackground(Color.WHITE);
        
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

        // Enter tuşu ile kaydetme dinleyicisi (UX)
        java.awt.event.ActionListener aracEnterAction = e -> {
            if (seciliAracPlakasi == null) {
                aracKaydet();
            } else {
                aracGuncelle();
            }
        };
        txtMarka.addActionListener(aracEnterAction);
        txtModel.addActionListener(aracEnterAction);
        txtPlaka.addActionListener(aracEnterAction);
        txtSaseNo.addActionListener(aracEnterAction);

        JButton btnTemizle = new JButton("Formu Temizle");
        btnTemizle.setBackground(new Color(149, 165, 166)); // Gri
        btnTemizle.setForeground(Color.WHITE);
        btnTemizle.addActionListener(e -> alanlariTemizleArac());

        JButton btnKaydet = new JButton("Yeni Araç Kaydet");
        btnKaydet.setBackground(new Color(41, 128, 185)); // Mavi
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.addActionListener(e -> aracKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.setBackground(new Color(39, 174, 96)); // Yeşil
        btnGuncelle.setForeground(Color.WHITE);
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
        tabloAracGorev.setRowHeight(30); // Ferah tablo tasarımı
        tabloAracGorev.getTableHeader().setReorderingAllowed(false);
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
        btnSil.setBackground(new Color(231, 76, 60)); // Kırmızı
        btnSil.setForeground(Color.WHITE);
        btnSil.addActionListener(e -> aracSil());
        
        JButton btnServis = new JButton("Servis ve Giderleri Yönet");
        btnServis.setBackground(new Color(142, 68, 173)); // Mor
        btnServis.setForeground(Color.WHITE);
        btnServis.addActionListener(e -> servisEkraniniAc());

        pnlAlt.add(btnSil);
        pnlAlt.add(btnServis);
        panelArac.add(pnlAlt, BorderLayout.SOUTH);
    }

    private void olusturPanelGorev() {
        panelGorev = new JPanel(new BorderLayout(10, 10));
        panelGorev.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlSol = new JPanel(new BorderLayout(0, 15));
        pnlSol.setPreferredSize(new Dimension(280, 0));
        pnlSol.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Görev Kayıt / Düzenleme"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Sol paneldeki kutucuklar alt alta (Etiket üstte, kutu altta)
        JPanel pnlGirdiAlanlari = new JPanel(new GridLayout(12, 1, 0, 5));
        pnlGirdiAlanlari.setBackground(Color.WHITE);
        
        pnlGirdiAlanlari.add(new JLabel("Araç Plakası:"));
        cbAracPlaka = new JComboBox<>();
        cbAracPlaka.setEditable(true); // Elle giriş özelliği (Arama/Yazma)
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
        JButton btnRaporDetay = new JButton("📝");
        btnRaporDetay.setBackground(new Color(243, 156, 18)); // Turuncu
        btnRaporDetay.setForeground(Color.WHITE);
        btnRaporDetay.setToolTipText("Açıklama Oku/Yaz (Pop-up)");
        btnRaporDetay.addActionListener(e -> gosterAciklamaPopup("Görev Açıklaması Yaz/Düzenle", txtRapor.getText(), metin -> txtRapor.setText(metin)));
        pnlRaporGirdi.add(txtRapor, BorderLayout.CENTER);
        pnlRaporGirdi.add(btnRaporDetay, BorderLayout.EAST);
        pnlGirdiAlanlari.add(pnlRaporGirdi);

        pnlGirdiAlanlari.add(new JLabel("Tarih (GG/AA/YYYY):"));
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            JFormattedTextField formattedDate = new JFormattedTextField(dateMask);
            formattedDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtTarih = formattedDate;
        } catch (Exception ex) {
            txtTarih = new JTextField();
        }
        pnlGirdiAlanlari.add(txtTarih);

        // Enter tuşu ile kaydetme dinleyicisi (Görev UX)
        java.awt.event.ActionListener gorevEnterAction = e -> {
            if (tabloGorev.getSelectedRow() == -1) {
                gorevKaydet();
            } else {
                gorevGuncelle();
            }
        };
        cbAracPlaka.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (tabloGorev.getSelectedRow() == -1) gorevKaydet(); else gorevGuncelle();
                }
            }
        });
        txtSoforAdi.addActionListener(gorevEnterAction);
        txtIl.addActionListener(gorevEnterAction);
        txtIlce.addActionListener(gorevEnterAction);
        txtRapor.addActionListener(gorevEnterAction);
        txtTarih.addActionListener(gorevEnterAction);

        JButton btnTemizle = new JButton("Formu Temizle");
        btnTemizle.setBackground(new Color(149, 165, 166)); // Gri
        btnTemizle.setForeground(Color.WHITE);
        btnTemizle.addActionListener(e -> alanlariTemizleGorev());

        JButton btnKaydet = new JButton("Yeni Görev Kaydet");
        btnKaydet.setBackground(new Color(41, 128, 185)); // Mavi
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.addActionListener(e -> gorevKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.setBackground(new Color(39, 174, 96)); // Yeşil
        btnGuncelle.setForeground(Color.WHITE);
        btnGuncelle.addActionListener(e -> gorevGuncelle());

        JPanel pnlButonlar = new JPanel(new GridLayout(3, 1, 0, 8));
        pnlButonlar.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        pnlButonlar.add(btnKaydet);
        pnlButonlar.add(btnGuncelle);
        pnlButonlar.add(btnTemizle);
        
        JPanel pnlIcerik = new JPanel(new BorderLayout());
        pnlIcerik.add(pnlGirdiAlanlari, BorderLayout.NORTH);
        pnlIcerik.add(pnlButonlar, BorderLayout.CENTER);

        pnlSol.add(pnlIcerik, BorderLayout.NORTH);

        panelGorev.add(pnlSol, BorderLayout.WEST);

        modelGorev = new DefaultTableModel(new String[]{"ID", "Plaka", "Şoför", "İl", "İlçe", "Açıklama", "Tarih"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabloGorev = new JTable(modelGorev);
        tabloGorev.setRowHeight(30); // Ferah tablo tasarımı
        tabloGorev.getTableHeader().setReorderingAllowed(false);
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

        JScrollPane scrollPaneGorev = new JScrollPane(tabloGorev);
        scrollPaneGorev.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelGorev.add(scrollPaneGorev, BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout());
        JButton btnSil = new JButton("Seçili Görevi Sil");
        btnSil.setBackground(new Color(231, 76, 60)); // Kırmızı
        btnSil.setForeground(Color.WHITE);
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
        tabloRapor.setRowHeight(30); // Ferah tablo tasarımı
        tabloRapor.getTableHeader().setReorderingAllowed(false);
        tabloAciklamaDinleyicisiEkle(tabloRapor, 7);
        
        JScrollPane scrollRapor = new JScrollPane(tabloRapor);
        scrollRapor.setBorder(BorderFactory.createEmptyBorder()); // Sınırları sil
        panelRapor.add(scrollRapor, BorderLayout.CENTER);

        JPanel pnlFiltre = new JPanel(new FlowLayout());
        JButton btnTumu = new JButton("Tüm Görevleri Göster");
        btnTumu.setBackground(new Color(52, 152, 219)); // Açık Mavi
        btnTumu.setForeground(Color.WHITE);
        
        JButton btnHaftalik = new JButton("Haftalık Rapor (Son 7 Gün)");
        btnHaftalik.setBackground(new Color(230, 126, 34)); // Havuç Rengi
        btnHaftalik.setForeground(Color.WHITE);
        
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
        
        // Hızlı akış için onay kutusu kaldırıldı
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
        // Hızlı akış için onay kutusu kaldırıldı
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

    public static void setGlobalFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }
}