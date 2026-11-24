package mylaund.repository;

import java.util.List;
import mylaund.model.Notifikasi;

public interface NotifikasiRepository {
    void simpan(Notifikasi notifikasi);
    List<Notifikasi> cariByPelanggan(String idPelanggan);
}
