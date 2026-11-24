package mylaund.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import mylaund.model.LayananExpress;
import mylaund.model.LayananKiloan;
import mylaund.model.LayananLaundry;
import mylaund.model.LayananSatuan;
import mylaund.repository.LayananRepository;

public class SqliteLayananRepository implements LayananRepository {
    private static final String SELECT = """
        SELECT id_layanan, nama_layanan, deskripsi, tarif, durasi_hari, jenis_layanan,
               minimal_berat, kategori_item, biaya_tambahan
        FROM layanan
        """;

    private final DatabaseConnection dbConnection;

    public SqliteLayananRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void simpan(LayananLaundry layanan) {
        String sql = """
            INSERT INTO layanan (id_layanan, nama_layanan, deskripsi, tarif, durasi_hari, jenis_layanan,
                                 minimal_berat, kategori_item, biaya_tambahan)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id_layanan) DO UPDATE SET
                nama_layanan = excluded.nama_layanan, deskripsi = excluded.deskripsi, tarif = excluded.tarif,
                durasi_hari = excluded.durasi_hari, jenis_layanan = excluded.jenis_layanan,
                minimal_berat = excluded.minimal_berat, kategori_item = excluded.kategori_item,
                biaya_tambahan = excluded.biaya_tambahan
            """;
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, layanan.getIdLayanan());
            ps.setString(2, layanan.getNamaLayanan());
            ps.setString(3, layanan.getDeskripsi());
            ps.setDouble(4, layanan.getTarif());
            ps.setInt(5, layanan.getDurasiHari());
            ps.setString(6, layanan.getJenis().toUpperCase());
            if (layanan instanceof LayananKiloan) {
                ps.setDouble(7, ((LayananKiloan) layanan).getMinimalBerat());
            } else {
                ps.setNull(7, Types.REAL);
            }
            if (layanan instanceof LayananSatuan) {
                ps.setString(8, ((LayananSatuan) layanan).getKategoriItem());
            } else {
                ps.setNull(8, Types.VARCHAR);
            }
            if (layanan instanceof LayananExpress) {
                ps.setDouble(9, ((LayananExpress) layanan).getBiayaTambahan());
            } else {
                ps.setNull(9, Types.REAL);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan layanan: " + e.getMessage(), e);
        }
    }

    @Override
    public LayananLaundry cariById(String idLayanan) {
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(SELECT + " WHERE id_layanan = ?")) {
            ps.setString(1, idLayanan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca layanan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LayananLaundry> ambilSemua() {
        List<LayananLaundry> hasil = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(SELECT + " ORDER BY rowid");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(petakan(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca layanan: " + e.getMessage(), e);
        }
        return hasil;
    }

    private LayananLaundry petakan(ResultSet rs) throws SQLException {
        String id = rs.getString("id_layanan");
        String nama = rs.getString("nama_layanan");
        String deskripsi = rs.getString("deskripsi");
        double tarif = rs.getDouble("tarif");
        int durasi = rs.getInt("durasi_hari");
        switch (rs.getString("jenis_layanan")) {
            case "KILOAN":
                return new LayananKiloan(id, nama, deskripsi, tarif, durasi, rs.getDouble("minimal_berat"));
            case "SATUAN":
                return new LayananSatuan(id, nama, deskripsi, tarif, durasi, rs.getString("kategori_item"));
            case "EXPRESS":
                return new LayananExpress(id, nama, deskripsi, tarif, durasi, rs.getDouble("biaya_tambahan"));
            default:
                throw new IllegalStateException("Jenis layanan tidak dikenali untuk " + id);
        }
    }
}
