package com.example.uts_a22202302984.model;

public class OrderHistory {
    private String order_id, tanggal, alamat, kota, provinsi, metode_pembayaran, estimasi, status;
    private int subtotal, ongkir, total;

    // Getter & Setter
    public String getOrderId() { return order_id; }
    public String getTanggal() { return tanggal; }
    public String getAlamat() { return alamat; }
    public String getKota() { return kota; }
    public String getProvinsi() { return provinsi; }
    public String getMetode() { return metode_pembayaran; }
    public String getEstimasi() { return estimasi; }
    public String getStatus() { return status; }
    public int getSubtotal() { return subtotal; }
    public int getOngkir() { return ongkir; }
    public int getTotal() { return total; }

    public void setOrderId(String order_id) { this.order_id = order_id; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public void setKota(String kota) { this.kota = kota; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }
    public void setMetode(String metode_pembayaran) { this.metode_pembayaran = metode_pembayaran; }
    public void setEstimasi(String estimasi) { this.estimasi = estimasi; }
    public void setStatus(String status) { this.status = status; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public void setOngkir(int ongkir) { this.ongkir = ongkir; }
    public void setTotal(int total) { this.total = total; }
}
