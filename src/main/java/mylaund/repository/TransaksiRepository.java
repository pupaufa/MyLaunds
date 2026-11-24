/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.repository;

import java.util.List;
import mylaund.model.ITransaksi;
import mylaund.model.Pembayaran;

public interface TransaksiRepository {
    void simpanPembayaran(Pembayaran pembayaran);
    List<ITransaksi> ambilSemuaRiwayat();
    Pembayaran cariPembayaranLunas(String idPesanan);
    boolean sudahLunas(String idPesanan);
}