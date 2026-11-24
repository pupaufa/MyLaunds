/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

import java.time.LocalDateTime;

public class Notifikasi {
    private String idNotifikasi;
    private Pelanggan penerima;
    private String pesan;
    private LocalDateTime waktuKirim;
    private boolean terkirim;

    public Notifikasi(String idNotifikasi, Pelanggan penerima, String pesan) {
        this.idNotifikasi = idNotifikasi;
        this.penerima = penerima;
        this.pesan = pesan;
        this.waktuKirim = LocalDateTime.now();
        this.terkirim = false;
    }

    /** Memulihkan notifikasi dari database. */
    public Notifikasi(String idNotifikasi, Pelanggan penerima, String pesan,
            LocalDateTime waktuKirim, boolean terkirim) {
        this.idNotifikasi = idNotifikasi;
        this.penerima = penerima;
        this.pesan = pesan;
        this.waktuKirim = waktuKirim;
        this.terkirim = terkirim;
    }

    public void kirim() {
        this.waktuKirim = LocalDateTime.now();
        this.terkirim = true;
        System.out.println("[NOTIFIKASI -> " + penerima.getNama() + "] " + pesan);
    }

    public String getIdNotifikasi() {
        return idNotifikasi;
    }

    public Pelanggan getPenerima() {
        return penerima;
    }

    public String getPesan() {
        return pesan;
    }

    public LocalDateTime getWaktuKirim() {
        return waktuKirim;
    }

    public boolean isTerkirim() {
        return terkirim;
    }
}
