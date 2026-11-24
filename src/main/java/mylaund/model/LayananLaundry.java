/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

import java.time.LocalDateTime;

public abstract class LayananLaundry {
    private String idLayanan;
    private String namaLayanan;
    private String deskripsi;
    private double tarif;
    private int durasiHari;

    public LayananLaundry(String idLayanan, String namaLayanan, String deskripsi, double tarif, int durasiHari) {
        if (tarif <= 0 || durasiHari <= 0) {
            throw new IllegalArgumentException("Tarif dan durasi hari harus lebih dari 0.");
        }
        this.idLayanan = idLayanan;
        this.namaLayanan = namaLayanan;
        this.deskripsi = deskripsi;
        this.tarif = tarif;
        this.durasiHari = durasiHari;
    }

    public abstract double hitungHarga(double jumlah);

    public LocalDateTime hitungEstimasi(LocalDateTime tanggalMasuk) {
        return tanggalMasuk.plusDays(durasiHari);
    }

    public String getIdLayanan() {
        return idLayanan;
    }

    public String getNamaLayanan() {
        return namaLayanan;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public double getTarif() {
        return tarif;
    }

    public int getDurasiHari() {
        return durasiHari;
    }

    public abstract String getJenis();

    @Override
    public boolean equals(Object o) {
        return o instanceof LayananLaundry && idLayanan.equals(((LayananLaundry) o).idLayanan);
    }

    @Override
    public int hashCode() {
        return idLayanan.hashCode();
    }

    public String getSatuan() {
        return "kg";
    }
}
