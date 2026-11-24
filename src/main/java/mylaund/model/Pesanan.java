/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pesanan implements ITransaksi {
    private String idPesanan;
    private Pelanggan pelanggan; 
    private LocalDateTime tanggalMasuk;
    private LocalDateTime estimasiSelesai;
    private double totalBiaya;
    private StatusPesanan status;
    private List<DetailPesanan> daftarDetail; // Komposisi: DetailPesanan diciptakan di dalam Pesanan

    public Pesanan(String idPesanan, Pelanggan pelanggan, LocalDateTime tanggalMasuk) {
        if (pelanggan == null) {
            throw new IllegalArgumentException("Pelanggan tidak boleh null.");
        }
        this.idPesanan = idPesanan;
        this.pelanggan = pelanggan;
        this.tanggalMasuk = tanggalMasuk;
        this.estimasiSelesai = tanggalMasuk;
        this.totalBiaya = 0.0;
        this.status = StatusPesanan.DITERIMA;
        this.daftarDetail = new ArrayList<>();
    }

    /** Memulihkan pesanan utuh dari database (status, total, estimasi, dan detail apa adanya). */
    public Pesanan(String idPesanan, Pelanggan pelanggan, LocalDateTime tanggalMasuk,
            LocalDateTime estimasiSelesai, double totalBiaya, StatusPesanan status,
            List<DetailPesanan> detail) {
        if (pelanggan == null) {
            throw new IllegalArgumentException("Pelanggan tidak boleh null.");
        }
        this.idPesanan = idPesanan;
        this.pelanggan = pelanggan;
        this.tanggalMasuk = tanggalMasuk;
        this.estimasiSelesai = estimasiSelesai;
        this.totalBiaya = totalBiaya;
        this.status = status;
        this.daftarDetail = new ArrayList<>(detail);
    }

    // Komposisi: instansiasi DetailPesanan dikelola penuh oleh kelas induk (Pesanan)
    public void tambahDetail(LayananLaundry layanan, double jumlah) {
        DetailPesanan detailBaru = new DetailPesanan(layanan, jumlah);
        daftarDetail.add(detailBaru);
        hitungTotal();

        LocalDateTime estimasiItem = layanan.hitungEstimasi(tanggalMasuk);
        if (estimasiItem.isAfter(this.estimasiSelesai)) {
            this.estimasiSelesai = estimasiItem;
        }
    }

    public double hitungTotal() {
        double total = 0.0;
        for (DetailPesanan detail : daftarDetail) {
            total += detail.hitungSubtotal();
        }
        this.totalBiaya = total;
        return this.totalBiaya;
    }

    public void ubahStatus(StatusPesanan statusBaru) {
        if (statusBaru == null) {
            throw new IllegalArgumentException("Status baru tidak boleh null.");
        }
        this.status = statusBaru;
    }

    @Override
    public LocalDateTime getTanggal() {
        return tanggalMasuk;
    }

    @Override
    public double getNominal() {
        return totalBiaya;
    }

    @Override
    public String getDeskripsiTransaksi() {
        return "Pesanan " + idPesanan + " - Pelanggan: " + pelanggan.getNama() + " (" + status + ")";
    }

    public String getIdPesanan() {
        return idPesanan;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public LocalDateTime getTanggalMasuk() {
        return tanggalMasuk;
    }

    public LocalDateTime getEstimasiSelesai() {
        return estimasiSelesai;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public StatusPesanan getStatus() {
        return status;
    }

    public List<DetailPesanan> getDaftarDetail() {
        return Collections.unmodifiableList(daftarDetail);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pesanan && idPesanan.equals(((Pesanan) o).idPesanan);
    }

    @Override
    public int hashCode() {
        return idPesanan.hashCode();
    }
}