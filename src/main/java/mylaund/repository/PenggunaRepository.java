/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.repository;

import java.util.List;
import mylaund.model.Pengguna;

public interface PenggunaRepository {
    void simpan(Pengguna pengguna);
    Pengguna cariByUsername(String username);
    Pengguna cariById(String idPengguna);
    List<Pengguna> ambilSemua();
}