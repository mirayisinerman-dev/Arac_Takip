public class Gorev{
    private String aracPlakasi;
    private String soforAdi;
    private String il;
    private String ilce;
    private String rapor;
    private String tarih;

    public Gorev(String aracPlaka, String soforAd, String il, String ilce, String aciklama, String tarih) {
        this.aracPlakasi = aracPlaka;
        this.soforAdi = soforAd;
        this.il = il;
        this.ilce = ilce;
        this.rapor = aciklama;
        this.tarih = tarih;
    }
    /*public String gkaydet() {
        return aracPlakasi + "|" + soforAdi + "|" + il + "|" + ilce + "|" + rapor + "|" + tarih;
    }
    public String getTarih() { return tarih; }
    public String getAracPlakasi() { return aracPlakasi; }
    public String  getSoforAdi() { return soforAdi; }
    public String getRapor(){ return rapor; }
    public String  getIl(){ return il; }
    public String getIlce() { return ilce; }*/
}
