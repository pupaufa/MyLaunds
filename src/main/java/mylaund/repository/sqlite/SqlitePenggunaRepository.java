package mylaund.repository.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mylaund.model.Kasir;
import mylaund.model.Owner;
import mylaund.model.Pekerja;
import mylaund.model.Pelanggan;
import mylaund.model.Pengguna;
import mylaund.repository.PenggunaRepository;

public class SqlitePenggunaRepository implements PenggunaRepository {
    private static final String SELECT =
            "SELECT id_pengguna, nama, username, password_hash, peran, no_hp, alamat FROM pengguna";

    private final DatabaseConnection dbConnection;

    public SqlitePenggunaRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void simpan(Pengguna pengguna) {
        String sql = """
            INSERT INTO pengguna (id_pengguna, nama, username, password_hash, peran, no_hp, alamat)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id_pengguna) DO UPDATE SET
                nama = excluded.nama, username = excluded.username, password_hash = excluded.password_hash,
                peran = excluded.peran, no_hp = excluded.no_hp, alamat = excluded.alamat
            """;
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, pengguna.getIdPengguna());
            ps.setString(2, pengguna.getNama());
            ps.setString(3, pengguna.getUsername());
            ps.setString(4, pengguna.getPasswordHash());
            ps.setString(5, pengguna.getPeran());
            ps.setString(6, pengguna instanceof Pelanggan ? ((Pelanggan) pengguna).getNoHp() : null);
            ps.setString(7, pengguna instanceof Pelanggan ? ((Pelanggan) pengguna).getAlamat() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan pengguna: " + e.getMessage(), e);
        }
    }

    @Override
    public Pengguna cariByUsername(String username) {
        return cariSatu(SELECT + " WHERE username = ?", username);
    }

    @Override
    public Pengguna cariById(String idPengguna) {
        return cariSatu(SELECT + " WHERE id_pengguna = ?", idPengguna);
    }

    @Override
    public List<Pengguna> ambilSemua() {
        List<Pengguna> hasil = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(SELECT + " ORDER BY rowid");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(petakan(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca pengguna: " + e.getMessage(), e);
        }
        return hasil;
    }

    private Pengguna cariSatu(String sql, String parameter) {
        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca pengguna: " + e.getMessage(), e);
        }
    }

    private Pengguna petakan(ResultSet rs) throws SQLException {
        String id = rs.getString("id_pengguna");
        String nama = rs.getString("nama");
        String username = rs.getString("username");
        String hash = rs.getString("password_hash");
        switch (rs.getString("peran")) {
            case "PELANGGAN":
                return new Pelanggan(id, nama, username, hash, rs.getString("no_hp"), rs.getString("alamat"));
            case "KASIR":
                return new Kasir(id, nama, username, hash);
            case "PEKERJA":
                return new Pekerja(id, nama, username, hash);
            case "OWNER":
                return new Owner(id, nama, username, hash);
            default:
                throw new IllegalStateException("Peran pengguna tidak dikenali untuk " + username);
        }
    }
}
