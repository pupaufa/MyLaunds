/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

public class LayananKiloan extends LayananLaundry {
    private double minimalBerat;

    public LayananKiloan(String idLayanan, String namaLayanan, String deskripsi, double tarif, int durasiHari, double minimalBerat) {
        super(idLayanan, namaLayanan, deskripsi, tarif, durasiHari);
        this.minimalBerat = minimalBerat;
    }

    @Override
    public double hitungHarga(double jumlah) {
        double beratDihitung = Math.max(jumlah, minimalBerat);
        return getTarif() * beratDihitung;
    }

    public double getMinimalBerat() {
        return minimalBerat;
    }

    @Override
    public String getJenis() {
        return "Kiloan";
    }
}
