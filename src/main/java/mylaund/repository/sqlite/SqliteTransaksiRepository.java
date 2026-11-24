package mylaund.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mylaund.model.ITransaksi;
import mylaund.model.Pembayaran;
import mylaund.model.Pesanan;
import mylaund.repository.PesananRepository;
import mylaund.repository.TransaksiRepository;

public class SqliteTransaksiRepository implements TransaksiRepository {
    private static final String SELECT = """
        SELECT id_pembayaran, id_pesanan, tanggal_bayar, jumlah_bayar, metode, lunas
        FROM pembayaran
        """;

    private record BarisPembayaran(String id, String idPesanan, String tanggal, double jumlah,
            String metode, boolean lunas) { }

    private final DatabaseConnection dbConnection;
    private final PesananRepository pesananRepo;

    public SqliteTransaksiRepository(DatabaseConnection dbConnection, PesananRepository pesananRepo) {
        this.dbConnection = dbConnection;
        this.pesananRepo = pesananRepo;
    }

    @Override
    public void simpanPembayaran(Pembayaran pembayaran) {
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement("""
                INSERT INTO pembayaran (id_pembayaran, id_pesanan, tanggal_bayar, jumlah_bayar, metode, lunas)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            ps.setString(1, pembayaran.getIdPembayaran());
            ps.setString(2, pembayaran.getPesanan().getIdPesanan());
            ps.setString(3, pembayaran.getTanggalBayar().toString());
            ps.setDouble(4, pembayaran.getJumlahBayar());
            ps.setString(5, pembayaran.getMetode());
            ps.setInt(6, pembayaran.isLunas() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan pembayaran: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ITransaksi> ambilSemuaRiwayat() {
        List<ITransaksi> hasil = new ArrayList<>();
        Map<String, Pesanan> cachePesanan = new HashMap<>();
        for (BarisPembayaran b : bacaBaris(" ORDER BY rowid", null)) {
            hasil.add(bangun(b, cachePesanan));
        }
        return hasil;
    }

    @Override
    public Pembayaran cariPembayaranLunas(String idPesanan) {
        List<BarisPembayaran> baris = bacaBaris(" WHERE id_pesanan = ? AND lunas = 1 ORDER BY rowid LIMIT 1", idPesanan);
        return baris.isEmpty() ? null : bangun(baris.get(0), new HashMap<>());
    }

    @Override
    public boolean sudahLunas(String idPesanan) {
        try (PreparedStatement ps = dbConnection.getConnection()
                .prepareStatement("SELECT 1 FROM pembayaran WHERE id_pesanan = ? AND lunas = 1 LIMIT 1")) {
            ps.setString(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memeriksa status pembayaran: " + e.getMessage(), e);
        }
    }

    private List<BarisPembayaran> bacaBaris(String klausa, String parameter) {
        List<BarisPembayaran> hasil = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(SELECT + klausa)) {
            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisPembayaran(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getDouble(4), rs.getString(5), rs.getInt(6) == 1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca pembayaran: " + e.getMessage(), e);
        }
        return hasil;
    }

    private Pembayaran bangun(BarisPembayaran b, Map<String, Pesanan> cachePesanan) {
        Pesanan pesanan = cachePesanan.get(b.idPesanan());
        if (pesanan == null) {
            pesanan = pesananRepo.cariById(b.idPesanan());
            if (pesanan == null) {
                throw new IllegalStateException("Pesanan " + b.idPesanan() + " untuk pembayaran " + b.id() + " tidak ditemukan.");
            }
            cachePesanan.put(b.idPesanan(), pesanan);
        }
        return new Pembayaran(b.id(), pesanan, LocalDateTime.parse(b.tanggal()), b.jumlah(), b.metode(), b.lunas());
    }
}
