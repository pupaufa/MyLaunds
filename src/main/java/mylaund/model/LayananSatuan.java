/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

public class LayananSatuan extends LayananLaundry {
    private String kategoriItem;

    public LayananSatuan(String idLayanan, String namaLayanan, String deskripsi, double tarif, int durasiHari, String kategoriItem) {
        super(idLayanan, namaLayanan, deskripsi, tarif, durasiHari);
        this.kategoriItem = kategoriItem;
    }

    @Override
    public double hitungHarga(double jumlah) {
        return getTarif() * jumlah;
    }

    public String getKategoriItem() {
        return kategoriItem;
    }

    @Override
    public String getJenis() {
        return "Satuan";
    }

    @Override
    public String getSatuan() {
        return "pcs";
    }
}
