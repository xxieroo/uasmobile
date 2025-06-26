package com.example.uts_a22202302984;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Product implements Serializable {

    @SerializedName("id_produk")
    private String id_produk;

    @SerializedName("jumlah_pengunjung")
    private int jumlah_pengunjung;

    @SerializedName("foto")
    private String foto;

    @SerializedName("nama")
    private String nama;

    @SerializedName("hargajual")
    private double hargajual;

    @SerializedName("stok")
    private int stok;

    private int quantity;

    @SerializedName("kategori")
    private String kategori;

    @SerializedName("deskripsi")
    private String deskripsi;

    public Product(String id_produk, String foto, String nama, double hargajual, int stok, String kategori, String deskripsi, int jumlah_pengunjung) {
        this.id_produk = id_produk;
        this.foto = foto;
        this.nama = nama;
        this.hargajual = hargajual;
        this.stok = stok;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.jumlah_pengunjung = jumlah_pengunjung;
    }


    public String getId_produk() { return id_produk; }
    public void setId_produk(String id_produk) { this.id_produk = id_produk; }

    public int getJumlah_pengunjung() { return jumlah_pengunjung; }
    public void setJumlah_pengunjung(int jumlah_pengunjung) { this.jumlah_pengunjung = jumlah_pengunjung; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public double getHargajual() { return hargajual; }
    public void setHargajual(double hargajual) { this.hargajual = hargajual; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() { return hargajual * quantity; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

}
