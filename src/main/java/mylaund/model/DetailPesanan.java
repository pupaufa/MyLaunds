/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

public class DetailPesanan {
    private LayananLaundry layanan; // Agregasi: objek LayananLaundry diterima dari luar
    private double jumlah;
    private double subtotal;

    public DetailPesanan(LayananLaundry layanan, double jumlah) {
        if (layanan == null || jumlah <= 0) {
            throw new IllegalArgumentException("Layanan tidak valid atau jumlah <= 0.");
        }
        this.layanan = layanan;
        this.jumlah = jumlah;
        this.subtotal = hitungSubtotal();
    }

    /** Memulihkan detail dari database dengan subtotal yang tersimpan (tidak dihitung ulang). */
    public DetailPesanan(LayananLaundry layanan, double jumlah, double subtotal) {
        if (layanan == null || jumlah <= 0) {
            throw new IllegalArgumentException("Layanan tidak valid atau jumlah <= 0.");
        }
        this.layanan = layanan;
        this.jumlah = jumlah;
        this.subtotal = subtotal;
    }

    public double hitungSubtotal() {
        this.subtotal = layanan.hitungHarga(jumlah);
        return this.subtotal;
    }

    public LayananLaundry getLayanan() {
        return layanan;
    }

    public double getJumlah() {
        return jumlah;
    }

    public double getSubtotal() {
        return subtotal;
    }
}