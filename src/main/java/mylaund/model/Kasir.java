/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mylaund.model;

public class Kasir extends Pengguna {
    public Kasir(String idPengguna, String nama, String username, String passwordHash) {
        super(idPengguna, nama, username, passwordHash);
    }

    @Override
    public String getPeran() {
        return "KASIR";
    }
}