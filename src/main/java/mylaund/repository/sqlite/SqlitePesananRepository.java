package mylaund.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import mylaund.model.DetailPesanan;
import mylaund.model.LayananLaundry;
import mylaund.model.Pelanggan;
import mylaund.model.Pengguna;
import mylaund.model.Pesanan;
import mylaund.model.StatusPesanan;
import mylaund.repository.LayananRepository;
import mylaund.repository.PenggunaRepository;
import mylaund.repository.PesananRepository;

public class SqlitePesananRepository implements PesananRepository {
    private static final String SELECT = """
        SELECT id_pesanan, id_pelanggan, tanggal_masuk, estimasi_selesai, total_biaya, status
        FROM pesanan
        """;

    /** Baris mentah dari tabel pesanan, dibaca penuh dulu agar ResultSet tidak terbuka saat query lain berjalan. */
    private record BarisPesanan(String id, String idPelanggan, String masuk, String estimasi,
            double total, String status) { }

    private record BarisDetail(String idLayanan, double jumlah, double subtotal) { }

    private final DatabaseConnection dbConnection;
    private final PenggunaRepository penggunaRepo;
    private final LayananRepository layananRepo;

    public SqlitePesananRepository(DatabaseConnection dbConnection, PenggunaRepository penggunaRepo,
            LayananRepository layananRepo) {
        this.dbConnection = dbConnection;
        this.penggunaRepo = penggunaRepo;
        this.layananRepo = layananRepo;
    }

    /** Menyimpan pesanan beserta seluruh detailnya. Jika pesanan sudah ada, datanya diperbarui. */
    @Override
    public void simpan(Pesanan pesanan) {
        dbConnection.dalamTransaksi(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO pesanan (id_pesanan, id_pelanggan, tanggal_masuk, estimasi_selesai, total_biaya, status)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id_pesanan) DO UPDATE SET
                    id_pelanggan = excluded.id_pelanggan, tanggal_masuk = excluded.tanggal_masuk,
                    estimasi_selesai = excluded.estimasi_selesai, total_biaya = excluded.total_biaya,
                    status = excluded.status
                """)) {
                ps.setString(1, pesanan.getIdPesanan());
                ps.setString(2, pesanan.getPelanggan().getIdPengguna());
                ps.setString(3, pesanan.getTanggalMasuk().toString());
                ps.setString(4, pesanan.getEstimasiSelesai().toString());
                ps.setDouble(5, pesanan.getTotalBiaya());
                ps.setString(6, pesanan.getStatus().name());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM detail_pesanan WHERE id_pesanan = ?")) {
                ps.setString(1, pesanan.getIdPesanan());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO detail_pesanan (id_pesanan, id_layanan, jumlah, subtotal) VALUES (?, ?, ?, ?)
                """)) {
                for (DetailPesanan d : pesanan.getDaftarDetail()) {
                    ps.setString(1, pesanan.getIdPesanan());
                    ps.setString(2, d.getLayanan().getIdLayanan());
                    ps.setDouble(3, d.getJumlah());
                    ps.setDouble(4, d.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    @Override
    public Pesanan cariById(String idPesanan) {
        List<BarisPesanan> baris = bacaBaris(" WHERE id_pesanan = ?", idPesanan);
        return baris.isEmpty() ? null : bangun(baris.get(0));
    }

    @Override
    public List<Pesanan> cariByPelanggan(String idPelanggan) {
        return bangunSemua(bacaBaris(" WHERE id_pelanggan = ? ORDER BY rowid", idPelanggan));
    }

    @Override
    public List<Pesanan> ambilSemua() {
        return bangunSemua(bacaBaris(" ORDER BY rowid", null));
    }

    @Override
    public void perbaruiStatus(String idPesanan, StatusPesanan statusBaru) {
        try (PreparedStatement ps = dbConnection.getConnection()
                .prepareStatement("UPDATE pesanan SET status = ? WHERE id_pesanan = ?")) {
            ps.setString(1, statusBaru.name());
            ps.setString(2, idPesanan);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Pesanan " + idPesanan + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui status pesanan: " + e.getMessage(), e);
        }
    }

    private List<BarisPesanan> bacaBaris(String klausa, String parameter) {
        List<BarisPesanan> hasil = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(SELECT + klausa)) {
            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisPesanan(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getDouble(5), rs.getString(6)));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca pesanan: " + e.getMessage(), e);
        }
        return hasil;
    }

    private List<Pesanan> bangunSemua(List<BarisPesanan> baris) {
        List<Pesanan> hasil = new ArrayList<>();
        for (BarisPesanan b : baris) {
            hasil.add(bangun(b));
        }
        return hasil;
    }

    private Pesanan bangun(BarisPesanan b) {
        Pengguna pengguna = penggunaRepo.cariById(b.idPelanggan());
        if (!(pengguna instanceof Pelanggan)) {
            throw new IllegalStateException("Pelanggan " + b.idPelanggan() + " untuk pesanan " + b.id() + " tidak ditemukan.");
        }
        List<DetailPesanan> detail = new ArrayList<>();
        for (BarisDetail d : bacaDetail(b.id())) {
            LayananLaundry layanan = layananRepo.cariById(d.idLayanan());
            if (layanan == null) {
                throw new IllegalStateException("Layanan " + d.idLayanan() + " untuk pesanan " + b.id() + " tidak ditemukan.");
            }
            detail.add(new DetailPesanan(layanan, d.jumlah(), d.subtotal()));
        }
        return new Pesanan(b.id(), (Pelanggan) pengguna, LocalDateTime.parse(b.masuk()),
                LocalDateTime.parse(b.estimasi()), b.total(), StatusPesanan.valueOf(b.status()), detail);
    }

    private List<BarisDetail> bacaDetail(String idPesanan) {
        List<BarisDetail> hasil = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(
                "SELECT id_layanan, jumlah, subtotal FROM detail_pesanan WHERE id_pesanan = ? ORDER BY id_detail")) {
            ps.setString(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisDetail(rs.getString(1), rs.getDouble(2), rs.getDouble(3)));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca detail pesanan: " + e.getMessage(), e);
        }
        return hasil;
    }
}
