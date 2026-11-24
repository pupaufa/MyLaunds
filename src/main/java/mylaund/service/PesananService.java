/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.service;

import java.time.LocalDateTime;
import java.util.List;
import mylaund.model.LayananLaundry;
import mylaund.model.Pelanggan;
import mylaund.model.Pesanan;
import mylaund.model.StatusPesanan;
import mylaund.repository.LayananRepository;
import mylaund.repository.PesananRepository;
import mylaund.repository.TransaksiRepository;

public class PesananService {
    private PesananRepository pesananRepo;
    private LayananRepository layananRepo;
    private NotifikasiService notifikasiService;
    private TransaksiRepository transaksiRepo;

    public PesananService(PesananRepository pesananRepo, LayananRepository layananRepo,
            NotifikasiService notifikasiService, TransaksiRepository transaksiRepo) {
        this.transaksiRepo = transaksiRepo;
        this.pesananRepo = pesananRepo;
        this.layananRepo = layananRepo;
        this.notifikasiService = notifikasiService;
    }

    public Pesanan buatPesanan(String idPesanan, Pelanggan pelanggan) {
        Pesanan pesanan = new Pesanan(idPesanan, pelanggan, LocalDateTime.now());
        pesananRepo.simpan(pesanan);
        return pesanan;
    }

    public void tambahItemPesanan(Pesanan pesanan, LayananLaundry layanan, double jumlah) {
        pesanan.tambahDetail(layanan, jumlah);
        pesananRepo.simpan(pesanan); // detail, total, dan estimasi ikut tersimpan
    }

    public void perbaruiStatusPesanan(Pesanan pesanan, StatusPesanan statusBaru) {
        // Status dibaca ulang dari database, karena objek di layar bisa sudah usang.
        Pesanan tersimpan = pesananRepo.cariById(pesanan.getIdPesanan());
        if (tersimpan == null) {
            throw new IllegalArgumentException("Pesanan " + pesanan.getIdPesanan() + " tidak ditemukan.");
        }
        if (statusBaru.ordinal() <= tersimpan.getStatus().ordinal()) {
            throw new IllegalStateException("Status pesanan tidak boleh mundur atau tetap sama. Status saat ini: "
                    + tersimpan.getStatus() + ".");
        }
        if (statusBaru == StatusPesanan.SELESAI && !transaksiRepo.sudahLunas(pesanan.getIdPesanan())) {
            throw new IllegalStateException("Pesanan " + pesanan.getIdPesanan()
                    + " belum dibayar. Pelanggan harus membayar di kasir sebelum mengambil cucian.");
        }
        pesananRepo.perbaruiStatus(pesanan.getIdPesanan(), statusBaru);
        pesanan.ubahStatus(statusBaru); // objek di layar mengikuti data yang sudah tersimpan
        notifikasiService.kirimNotifikasiStatus(pesanan);
    }

    public List<LayananLaundry> lihatDaftarLayanan() {
        return layananRepo.ambilSemua();
    }

    public List<Pesanan> lihatPesananPelanggan(String idPelanggan) {
        return pesananRepo.cariByPelanggan(idPelanggan);
    }

    public List<Pesanan> lihatSemuaPesanan() {
        return pesananRepo.ambilSemua();
    }
}