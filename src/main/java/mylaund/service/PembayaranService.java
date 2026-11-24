/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.service;

import java.time.LocalDateTime;
import java.util.List;
import mylaund.model.ITransaksi;
import mylaund.model.Pembayaran;
import mylaund.model.Pesanan;
import mylaund.repository.TransaksiRepository;

public class PembayaranService {
    private TransaksiRepository transaksiRepo;

    public PembayaranService(TransaksiRepository transaksiRepo) {
        this.transaksiRepo = transaksiRepo;
    }

    public Pembayaran prosesPembayaran(String idPembayaran, Pesanan pesanan, double jumlahBayar, String metode) {
        if (pesanan.getDaftarDetail().isEmpty()) {
            throw new IllegalStateException("Pesanan " + pesanan.getIdPesanan() + " belum memiliki item layanan.");
        }
        if (sudahDibayar(pesanan.getIdPesanan())) {
            throw new IllegalStateException("Pesanan " + pesanan.getIdPesanan() + " sudah dibayar.");
        }
        if (jumlahBayar <= 0) {
            throw new IllegalArgumentException("Jumlah pembayaran harus lebih dari 0.");
        }
        Pembayaran pembayaran = new Pembayaran(idPembayaran, pesanan, LocalDateTime.now(), jumlahBayar, metode);
        if (!pembayaran.catat()) {
            throw new IllegalArgumentException("Jumlah pembayaran kurang dari total biaya pesanan.");
        }
        transaksiRepo.simpanPembayaran(pembayaran);
        return pembayaran;
    }

    public boolean sudahDibayar(String idPesanan) {
        return transaksiRepo.sudahLunas(idPesanan);
    }

    /** Pembayaran lunas milik pesanan, atau null bila belum dibayar. */
    public Pembayaran cariPembayaran(String idPesanan) {
        return transaksiRepo.cariPembayaranLunas(idPesanan);
    }

    public List<ITransaksi> lihatRiwayatTransaksi() {
        return transaksiRepo.ambilSemuaRiwayat();
    }
}