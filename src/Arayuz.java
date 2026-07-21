import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Arayuz extends JFrame {
    private JTabbedPane sekme;
    private JPanel panelArac;
    private JPanel panelGorev;
    private JPanel panelRapor;

    // Araç Yönetimi Bileşenleri
    private JTextField txtMarka, txtModel, txtPlaka, txtSaseNo;
    private JTable tabloArac;
    private DefaultTableModel modelArac;
    private JComboBox<String> cbAracPlaka;

    // Görevlendirme Bileşenleri
    private JTextField txtSoforAdi, txtIl, txtIlce, txtRapor, txtTarih;
    private JTable tabloGorev;
    private DefaultTableModel modelGorev;

    // Raporlar
    private JTable tabloRapor;
    private DefaultTableModel modelRapor;
    private boolean haftalikRaporGoster = false;

    public Arayuz() {
        setTitle("Araç Takip ve Görevlendirme Sistemi");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        sekme = new JTabbedPane();

        olusturPanelArac();
        olusturPanelGorev();
        olusturPanelRapor();

        sekme.addTab("Araç Yönetimi", panelArac);
        sekme.addTab("Görevlendirme", panelGorev);
        sekme.addTab("Genel Bakış / Raporlar", panelRapor);

        // Sekme değiştiğinde Raporlar sekmesine geçilirse tabloyu yenile
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

        JPanel pnlGirdi = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlGirdi.add(new JLabel("Marka:"));
        txtMarka = new JTextField();
        pnlGirdi.add(txtMarka);

        pnlGirdi.add(new JLabel("Model:"));
        txtModel = new JTextField();
        pnlGirdi.add(txtModel);

        pnlGirdi.add(new JLabel("Plaka:"));
        txtPlaka = new JTextField();
        pnlGirdi.add(txtPlaka);

        pnlGirdi.add(new JLabel("Şase No:"));
        txtSaseNo = new JTextField();
        pnlGirdi.add(txtSaseNo);

        JButton btnKaydet = new JButton("Yeni Araç Kaydet");
        btnKaydet.addActionListener(e -> aracKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> aracGuncelle());

        pnlGirdi.add(btnGuncelle);
        pnlGirdi.add(btnKaydet);

        panelArac.add(pnlGirdi, BorderLayout.NORTH);

        modelArac = new DefaultTableModel(new String[]{"Plaka", "Marka", "Model", "Şase No"}, 0);
        tabloArac = new JTable(modelArac);
        
        // Tablodan seçileni form alanlarına doldur
        tabloArac.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabloArac.getSelectedRow() != -1) {
                int row = tabloArac.getSelectedRow();
                txtPlaka.setText(modelArac.getValueAt(row, 0).toString());
                txtMarka.setText(modelArac.getValueAt(row, 1).toString());
                txtModel.setText(modelArac.getValueAt(row, 2).toString());
                txtSaseNo.setText(modelArac.getValueAt(row, 3).toString());
            }
        });

        panelArac.add(new JScrollPane(tabloArac), BorderLayout.CENTER);

        JButton btnSil = new JButton("Seçili Aracı Sil");
        btnSil.addActionListener(e -> aracSil());
        panelArac.add(btnSil, BorderLayout.SOUTH);
    }

    private void olusturPanelGorev() {
        panelGorev = new JPanel(new BorderLayout(10, 10));
        panelGorev.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlGirdi = new JPanel(new GridLayout(8, 2, 5, 5));
        
        pnlGirdi.add(new JLabel("Araç Plakası:"));
        cbAracPlaka = new JComboBox<>();
        pnlGirdi.add(cbAracPlaka);

        pnlGirdi.add(new JLabel("Şoför Adı:"));
        txtSoforAdi = new JTextField();
        pnlGirdi.add(txtSoforAdi);

        pnlGirdi.add(new JLabel("İl:"));
        txtIl = new JTextField();
        pnlGirdi.add(txtIl);

        pnlGirdi.add(new JLabel("İlçe:"));
        txtIlce = new JTextField();
        pnlGirdi.add(txtIlce);

        pnlGirdi.add(new JLabel("Açıklama (Rapor):"));
        txtRapor = new JTextField();
        pnlGirdi.add(txtRapor);

        pnlGirdi.add(new JLabel("Tarih (GG/AA/YYYY):"));
        txtTarih = new JTextField();
        pnlGirdi.add(txtTarih);

        JButton btnKaydet = new JButton("Yeni Görev Kaydet");
        btnKaydet.addActionListener(e -> gorevKaydet());
        
        JButton btnGuncelle = new JButton("Seçileni Güncelle");
        btnGuncelle.addActionListener(e -> gorevGuncelle());

        pnlGirdi.add(btnGuncelle);
        pnlGirdi.add(btnKaydet);

        panelGorev.add(pnlGirdi, BorderLayout.NORTH);

        modelGorev = new DefaultTableModel(new String[]{"Plaka", "Şoför", "İl", "İlçe", "Açıklama", "Tarih"}, 0);
        tabloGorev = new JTable(modelGorev);

        // Tablodan seçileni form alanlarına doldur
        tabloGorev.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabloGorev.getSelectedRow() != -1) {
                int row = tabloGorev.getSelectedRow();
                cbAracPlaka.setSelectedItem(modelGorev.getValueAt(row, 0).toString());
                txtSoforAdi.setText(modelGorev.getValueAt(row, 1).toString());
                txtIl.setText(modelGorev.getValueAt(row, 2).toString());
                txtIlce.setText(modelGorev.getValueAt(row, 3).toString());
                txtRapor.setText(modelGorev.getValueAt(row, 4).toString());
                txtTarih.setText(modelGorev.getValueAt(row, 5).toString());
            }
        });

        panelGorev.add(new JScrollPane(tabloGorev), BorderLayout.CENTER);

        JButton btnSil = new JButton("Seçili Görevi Sil");
        btnSil.addActionListener(e -> gorevSil());
        panelGorev.add(btnSil, BorderLayout.SOUTH);
    }

    private void olusturPanelRapor() {
        panelRapor = new JPanel(new BorderLayout(10, 10));
        panelRapor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblBaslik = new JLabel("Araç Durumları ve Görev Kayıtları");
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 16));
        lblBaslik.setHorizontalAlignment(SwingConstants.CENTER);
        panelRapor.add(lblBaslik, BorderLayout.NORTH);

        modelRapor = new DefaultTableModel(new String[]{"Plaka", "Marka", "Model", "Şoför", "Görev Yeri (İl/İlçe)", "Tarih", "Açıklama"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tablo sadece okuma amaçlı (Read-Only)
            }
        };
        tabloRapor = new JTable(modelRapor);
        panelRapor.add(new JScrollPane(tabloRapor), BorderLayout.CENTER);

        // Haftalık Rapor Butonları
        JPanel pnlFiltre = new JPanel(new FlowLayout());
        JButton btnTumu = new JButton("Tüm Raporları Göster");
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

    private void aracKaydet() {
        String marka = txtMarka.getText().trim();
        String model = txtModel.getText().trim();
        String plaka = txtPlaka.getText().trim();
        String saseNo = txtSaseNo.getText().trim();

        if (marka.isEmpty() || model.isEmpty() || plaka.isEmpty() || saseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Arac arac = new Arac(marka, model, plaka, saseNo);
        boolean basarili = DosyaIslemleri.Kaydet("araclar.txt", arac.kaydet());
        
        if (basarili) {
            modelArac.addRow(new Object[]{plaka, marka, model, saseNo});
            cbAracPlaka.addItem(plaka);
            alanlariTemizleArac();
            JOptionPane.showMessageDialog(this, "Araç başarıyla kaydedildi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Kayıt sırasında bir hata oluştu. Dosya izinlerini kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aracGuncelle() {
        int row = tabloArac.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek aracı tablodan seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String eskiPlaka = modelArac.getValueAt(row, 0).toString();
        
        String marka = txtMarka.getText().trim();
        String model = txtModel.getText().trim();
        String plaka = txtPlaka.getText().trim();
        String saseNo = txtSaseNo.getText().trim();

        if (marka.isEmpty() || model.isEmpty() || plaka.isEmpty() || saseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tabloyu güncelle
        modelArac.setValueAt(plaka, row, 0);
        modelArac.setValueAt(marka, row, 1);
        modelArac.setValueAt(model, row, 2);
        modelArac.setValueAt(saseNo, row, 3);

        boolean basarili = araclariDosyayaYaz();
        if (basarili) {
            // ComboBox'ı yenile
            cbAracPlaka.removeItem(eskiPlaka);
            cbAracPlaka.addItem(plaka);
            alanlariTemizleArac();
            JOptionPane.showMessageDialog(this, "Araç güncellendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Güncelleme sırasında bir hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aracSil() {
        int row = tabloArac.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek aracı tablodan seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int onay = JOptionPane.showConfirmDialog(this, "Seçili aracı silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay == JOptionPane.YES_OPTION) {
            String plaka = modelArac.getValueAt(row, 0).toString();
            modelArac.removeRow(row);
            boolean basarili = araclariDosyayaYaz();
            
            if (basarili) {
                cbAracPlaka.removeItem(plaka);
                alanlariTemizleArac();
                JOptionPane.showMessageDialog(this, "Araç silindi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Silme işlemi sırasında hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean araclariDosyayaYaz() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < modelArac.getRowCount(); i++) {
            String plaka = modelArac.getValueAt(i, 0).toString();
            String marka = modelArac.getValueAt(i, 1).toString();
            String model = modelArac.getValueAt(i, 2).toString();
            String sase = modelArac.getValueAt(i, 3).toString();
            Arac a = new Arac(marka, model, plaka, sase);
            list.add(a.kaydet());
        }
        return DosyaIslemleri.TumunuKaydet("araclar.txt", list);
    }

    private void gorevKaydet() {
        if (cbAracPlaka.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir araç kaydedin veya seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
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

        Gorev gorev = new Gorev(plaka, sofor, il, ilce, rapor, tarih);
        boolean basarili = DosyaIslemleri.Kaydet("gorevler.txt", gorev.gkaydet());

        if (basarili) {
            modelGorev.addRow(new Object[]{plaka, sofor, il, ilce, rapor, tarih});
            alanlariTemizleGorev();
            JOptionPane.showMessageDialog(this, "Görev başarıyla kaydedildi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Kayıt sırasında bir hata oluştu. Dosya izinlerini kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gorevGuncelle() {
        int row = tabloGorev.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek görevi tablodan seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cbAracPlaka.getSelectedItem() == null) return;
        
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

        modelGorev.setValueAt(plaka, row, 0);
        modelGorev.setValueAt(sofor, row, 1);
        modelGorev.setValueAt(il, row, 2);
        modelGorev.setValueAt(ilce, row, 3);
        modelGorev.setValueAt(rapor, row, 4);
        modelGorev.setValueAt(tarih, row, 5);

        boolean basarili = gorevleriDosyayaYaz();
        if (basarili) {
            alanlariTemizleGorev();
            JOptionPane.showMessageDialog(this, "Görev güncellendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Güncelleme sırasında bir hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gorevSil() {
        int row = tabloGorev.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek görevi tablodan seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int onay = JOptionPane.showConfirmDialog(this, "Seçili görevi silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay == JOptionPane.YES_OPTION) {
            modelGorev.removeRow(row);
            boolean basarili = gorevleriDosyayaYaz();
            
            if (basarili) {
                alanlariTemizleGorev();
                JOptionPane.showMessageDialog(this, "Görev silindi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Silme işlemi sırasında hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean gorevleriDosyayaYaz() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < modelGorev.getRowCount(); i++) {
            String plaka = modelGorev.getValueAt(i, 0).toString();
            String sofor = modelGorev.getValueAt(i, 1).toString();
            String il = modelGorev.getValueAt(i, 2).toString();
            String ilce = modelGorev.getValueAt(i, 3).toString();
            String rapor = modelGorev.getValueAt(i, 4).toString();
            String tarih = modelGorev.getValueAt(i, 5).toString();
            Gorev g = new Gorev(plaka, sofor, il, ilce, rapor, tarih);
            list.add(g.gkaydet());
        }
        return DosyaIslemleri.TumunuKaydet("gorevler.txt", list);
    }

    private void alanlariTemizleArac() {
        txtMarka.setText("");
        txtModel.setText("");
        txtPlaka.setText("");
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

    private void araclariYukle() {
        List<String> satirlar = DosyaIslemleri.Oku("araclar.txt");
        for (String satir : satirlar) {
            String[] parcalar = satir.split("\\|");
            if (parcalar.length == 1 && satir.contains(",")) parcalar = satir.split(",");
            if (parcalar.length == 4) {
                modelArac.addRow(new Object[]{parcalar[0], parcalar[1], parcalar[2], parcalar[3]});
                cbAracPlaka.addItem(parcalar[0]);
            }
        }
    }

    private void gorevleriYukle() {
        List<String> satirlar = DosyaIslemleri.Oku("gorevler.txt");
        for (String satir : satirlar) {
            String[] parcalar = satir.split("\\|");
            if (parcalar.length == 1 && satir.contains(",")) parcalar = satir.split(",");
            if (parcalar.length == 6) {
                modelGorev.addRow(new Object[]{parcalar[0], parcalar[1], parcalar[2], parcalar[3], parcalar[4], parcalar[5]});
            }
        }
    }

    private void raporlariYukle() {
        modelRapor.setRowCount(0); // Tabloyu temizle

        List<String> aracSatirlar = DosyaIslemleri.Oku("araclar.txt");
        List<String> gorevSatirlar = DosyaIslemleri.Oku("gorevler.txt");

        for (String aracSatiri : aracSatirlar) {
            String[] aracParcalar = aracSatiri.split("\\|");
            if (aracParcalar.length == 1 && aracSatiri.contains(",")) aracParcalar = aracSatiri.split(",");
            
            if (aracParcalar.length >= 4) {
                String plaka = aracParcalar[0];
                String marka = aracParcalar[1];
                String model = aracParcalar[2];

                boolean gorevBulundu = false;

                for (String gorevSatiri : gorevSatirlar) {
                    String[] gorevParcalar = gorevSatiri.split("\\|");
                    if (gorevParcalar.length == 1 && gorevSatiri.contains(",")) gorevParcalar = gorevSatiri.split(",");

                    if (gorevParcalar.length >= 6 && gorevParcalar[0].equals(plaka)) {
                        String sofor = gorevParcalar[1];
                        String yer = gorevParcalar[2] + " / " + gorevParcalar[3];
                        String aciklama = gorevParcalar[4];
                        String tarih = gorevParcalar[5];

                        if (haftalikRaporGoster && !sonYediGunIcindemi(tarih)) {
                            continue;
                        }

                        modelRapor.addRow(new Object[]{plaka, marka, model, sofor, yer, tarih, aciklama});
                        gorevBulundu = true;
                    }
                }

                if (!gorevBulundu) {
                    String uyari = haftalikRaporGoster ? "Bu Hafta Görev Yok" : "Görev Yok";
                    modelRapor.addRow(new Object[]{plaka, marka, model, uyari, uyari, "-", "-"});
                }
            }
        }
    }

    private boolean sonYediGunIcindemi(String tarihStr) {
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
}