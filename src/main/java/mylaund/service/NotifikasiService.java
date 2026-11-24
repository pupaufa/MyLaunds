package mylaund.service;

import java.util.List;
import java.util.UUID;
import mylaund.model.Notifikasi;
import mylaund.model.Pesanan;
import mylaund.repository.NotifikasiRepository;

public class NotifikasiService {
    private final NotifikasiRepository notifikasiRepo;

    public NotifikasiService(NotifikasiRepository notifikasiRepo) {
        this.notifikasiRepo = notifikasiRepo;
    }

    public Notifikasi kirimNotifikasiStatus(Pesanan pesanan) {
        String idNotif = "NTF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String pesan = "Status pesanan " + pesanan.getIdPesanan() + " berubah menjadi: " + pesanan.getStatus();
        Notifikasi notifikasi = new Notifikasi(idNotif, pesanan.getPelanggan(), pesan);
        notifikasi.kirim();
        notifikasiRepo.simpan(notifikasi);
        return notifikasi;
    }

    public List<Notifikasi> ambilNotifikasiPelanggan(String idPelanggan) {
        return notifikasiRepo.cariByPelanggan(idPelanggan);
    }
}
