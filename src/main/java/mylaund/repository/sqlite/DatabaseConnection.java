/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.repository.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private String dbFile;
    private String url;
    private Connection connection;

    public DatabaseConnection(String dbFile) {
        this.dbFile = dbFile;
        this.url = "jdbc:sqlite:" + dbFile;
        System.setProperty("org.sqlite.tmpdir", System.getProperty("user.home"));
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return connection;
    }

    public String getDbFile() {
        return dbFile;
    }

    /** Aksi SQL yang dijalankan di dalam satu transaksi. */
    @FunctionalInterface
    public interface AksiSql {
        void jalankan(Connection conn) throws SQLException;
    }

    /** Menjalankan beberapa perintah sebagai satu kesatuan: semua tersimpan atau semua dibatalkan. */
    public void dalamTransaksi(AksiSql aksi) {
        try {
            Connection conn = getConnection();
            boolean autoCommitAwal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                aksi.jalankan(conn);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitAwal);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Transaksi database gagal: " + e.getMessage(), e);
        }
    }

    private static String ddlPesanan(String namaTabel) {
        return """
            CREATE TABLE IF NOT EXISTS %s (
                id_pesanan       TEXT PRIMARY KEY,
                id_pelanggan     TEXT NOT NULL,
                tanggal_masuk    TEXT NOT NULL,
                estimasi_selesai TEXT NOT NULL,
                total_biaya      REAL NOT NULL DEFAULT 0,
                status           TEXT NOT NULL CHECK (status IN ('DITERIMA', 'DIPROSES', 'SIAP_DIAMBIL', 'SELESAI')),
                FOREIGN KEY (id_pelanggan) REFERENCES pengguna(id_pengguna)
            );
            """.formatted(namaTabel);
    }

    /**
     * Database lama memakai enam status (DICUCI, DIKERINGKAN, DISETRIKA). CHECK pada tabel tidak bisa
     * diubah dengan ALTER, sehingga tabel dibuat ulang dan ketiga status lama dipetakan ke DIPROSES.
     */
    private void migrasiStatusPesanan(Connection conn) throws SQLException {
        String ddlLama = null;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'pesanan'")) {
            if (rs.next()) {
                ddlLama = rs.getString(1);
            }
        }
        if (ddlLama == null || !ddlLama.contains("DICUCI")) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF;");
            conn.setAutoCommit(false);
            try {
                stmt.execute(ddlPesanan("pesanan_baru"));
                stmt.execute("""
                    INSERT INTO pesanan_baru
                    SELECT id_pesanan, id_pelanggan, tanggal_masuk, estimasi_selesai, total_biaya,
                           CASE status
                               WHEN 'DICUCI' THEN 'DIPROSES'
                               WHEN 'DIKERINGKAN' THEN 'DIPROSES'
                               WHEN 'DISETRIKA' THEN 'DIPROSES'
                               ELSE status
                           END
                    FROM pesanan;
                """);
                stmt.execute("DROP TABLE pesanan;");
                stmt.execute("ALTER TABLE pesanan_baru RENAME TO pesanan;");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
    }

    /** Database lama masih punya kolom shift dan bagian di tabel pengguna; keduanya sudah tidak dipakai. */
    private void hapusKolomLama(Connection conn) throws SQLException {
        List<String> ada = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(pengguna)")) {
            while (rs.next()) {
                String nama = rs.getString("name");
                if ("shift".equals(nama) || "bagian".equals(nama)) {
                    ada.add(nama);
                }
            }
        }
        for (String kolom : ada) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE pengguna DROP COLUMN " + kolom + ";");
            }
        }
    }

    public void inisialisasiSkema() {
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS pengguna (
                        id_pengguna   TEXT PRIMARY KEY,
                        nama          TEXT NOT NULL,
                        username      TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        peran         TEXT NOT NULL CHECK (peran IN ('PELANGGAN', 'KASIR', 'PEKERJA', 'OWNER')),
                        no_hp         TEXT,
                        alamat        TEXT
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS layanan (
                        id_layanan     TEXT PRIMARY KEY,
                        nama_layanan   TEXT NOT NULL,
                        deskripsi      TEXT,
                        tarif          REAL NOT NULL CHECK (tarif > 0),
                        durasi_hari    INTEGER NOT NULL CHECK (durasi_hari > 0),
                        jenis_layanan  TEXT NOT NULL CHECK (jenis_layanan IN ('KILOAN', 'SATUAN', 'EXPRESS')),
                        minimal_berat  REAL,
                        kategori_item  TEXT,
                        biaya_tambahan REAL
                    );
                """);

                stmt.execute(ddlPesanan("pesanan"));

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS detail_pesanan (
                        id_detail   INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_pesanan  TEXT NOT NULL,
                        id_layanan  TEXT NOT NULL,
                        jumlah      REAL NOT NULL CHECK (jumlah > 0),
                        subtotal    REAL NOT NULL CHECK (subtotal >= 0),
                        FOREIGN KEY (id_pesanan) REFERENCES pesanan(id_pesanan) ON DELETE CASCADE,
                        FOREIGN KEY (id_layanan) REFERENCES layanan(id_layanan)
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS pembayaran (
                        id_pembayaran TEXT PRIMARY KEY,
                        id_pesanan    TEXT NOT NULL,
                        tanggal_bayar TEXT NOT NULL,
                        jumlah_bayar  REAL NOT NULL CHECK (jumlah_bayar > 0),
                        metode        TEXT NOT NULL,
                        lunas         INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (id_pesanan) REFERENCES pesanan(id_pesanan)
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS notifikasi (
                        id_notifikasi TEXT PRIMARY KEY,
                        id_pelanggan  TEXT NOT NULL,
                        pesan         TEXT NOT NULL,
                        waktu_kirim   TEXT NOT NULL,
                        terkirim      INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (id_pelanggan) REFERENCES pengguna(id_pengguna)
                    );
                """);

                migrasiStatusPesanan(conn);
                hapusKolomLama(conn);

                stmt.execute("""
                    INSERT OR IGNORE INTO pengguna (id_pengguna, nama, username, password_hash, peran, no_hp, alamat) VALUES
                    ('U01', 'Wati', 'wati', 'HASH_1234', 'PELANGGAN', '08123456789', 'Jl. Merpati No. 10'),
                    ('U02', 'Rina', 'kasir', 'HASH_1234', 'KASIR', NULL, NULL),
                    ('U03', 'Budi', 'pekerja', 'HASH_1234', 'PEKERJA', NULL, NULL),
                    ('U04', 'Pak Hendra', 'owner', 'HASH_1234', 'OWNER', NULL, NULL);
                """);

                // Database lama masih memakai username kasir1 dan pekerja1; seed di atas tidak menimpanya.
                stmt.execute("UPDATE pengguna SET username = 'kasir' WHERE id_pengguna = 'U02' AND username = 'kasir1';");
                stmt.execute("UPDATE pengguna SET username = 'pekerja' WHERE id_pengguna = 'U03' AND username = 'pekerja1';");

                stmt.execute("""
                    INSERT OR IGNORE INTO layanan (id_layanan, nama_layanan, deskripsi, tarif, durasi_hari, jenis_layanan, minimal_berat, kategori_item, biaya_tambahan) VALUES
                    ('L01', 'Cuci Setrika Kiloan', 'Cuci kering dan setrika rapi per kg', 7000, 3, 'KILOAN', 2.0, NULL, NULL),
                    ('L02', 'Cuci Kemeja Kantor', 'Perawatan khusus kemeja kerja satuan', 12000, 2, 'SATUAN', NULL, 'Kemeja', NULL),
                    ('L03', 'Cuci Express 1 Hari', 'Layanan kilat selesai dalam 1 hari', 10000, 1, 'EXPRESS', NULL, NULL, 5000);
                """);
            }
            System.out.println("Database SQLite siap digunakan: " + dbFile);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menginisialisasi database SQLite: " + e.getMessage(), e);
        }
    }
}