/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

public class LayananExpress extends LayananLaundry {
    private double biayaTambahan;

    public LayananExpress(String idLayanan, String namaLayanan, String deskripsi, double tarif, int durasiHari, double biayaTambahan) {
        super(idLayanan, namaLayanan, deskripsi, tarif, durasiHari);
        this.biayaTambahan = biayaTambahan;
    }

    @Override
    public double hitungHarga(double jumlah) {
        return (getTarif() * jumlah) + biayaTambahan;
    }

    public double getBiayaTambahan() {
        return biayaTambahan;
    }

    @Override
    public String getJenis() {
        return "Express";
    }
}
