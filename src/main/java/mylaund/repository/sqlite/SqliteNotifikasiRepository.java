package mylaund.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import mylaund.model.Notifikasi;
import mylaund.model.Pelanggan;
import mylaund.model.Pengguna;
import mylaund.repository.NotifikasiRepository;
import mylaund.repository.PenggunaRepository;

public class SqliteNotifikasiRepository implements NotifikasiRepository {
    private record BarisNotifikasi(String id, String pesan, String waktu, boolean terkirim) { }

    private final DatabaseConnection dbConnection;
    private final PenggunaRepository penggunaRepo;

    public SqliteNotifikasiRepository(DatabaseConnection dbConnection, PenggunaRepository penggunaRepo) {
        this.dbConnection = dbConnection;
        this.penggunaRepo = penggunaRepo;
    }

    @Override
    public void simpan(Notifikasi notifikasi) {
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement("""
                INSERT INTO notifikasi (id_notifikasi, id_pelanggan, pesan, waktu_kirim, terkirim)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            ps.setString(1, notifikasi.getIdNotifikasi());
            ps.setString(2, notifikasi.getPenerima().getIdPengguna());
            ps.setString(3, notifikasi.getPesan());
            ps.setString(4, notifikasi.getWaktuKirim().toString());
            ps.setInt(5, notifikasi.isTerkirim() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan notifikasi: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notifikasi> cariByPelanggan(String idPelanggan) {
        Pengguna pengguna = penggunaRepo.cariById(idPelanggan);
        List<Notifikasi> hasil = new ArrayList<>();
        if (!(pengguna instanceof Pelanggan)) {
            return hasil;
        }
        List<BarisNotifikasi> baris = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement("""
                SELECT id_notifikasi, pesan, waktu_kirim, terkirim
                FROM notifikasi WHERE id_pelanggan = ? ORDER BY rowid
                """)) {
            ps.setString(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    baris.add(new BarisNotifikasi(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4) == 1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca notifikasi: " + e.getMessage(), e);
        }
        for (BarisNotifikasi b : baris) {
            hasil.add(new Notifikasi(b.id(), (Pelanggan) pengguna, b.pesan(),
                    LocalDateTime.parse(b.waktu()), b.terkirim()));
        }
        return hasil;
    }
}
