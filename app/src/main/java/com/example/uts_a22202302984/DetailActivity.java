package com.example.uts_a22202302984;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgProduk;
    private TextView tvNama, tvHarga, tvStok, tvKategori, tvDeskripsi, tvJumlahPengunjung;
    private Product product;
    private RegisterAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgProduk = findViewById(R.id.imgDetail);
        tvNama = findViewById(R.id.tvNamaDetail);
        tvHarga = findViewById(R.id.tvHargaDetail);
        tvStok = findViewById(R.id.tvStokDetail);
        tvKategori = findViewById(R.id.tvKategoriDetail);
        tvDeskripsi = findViewById(R.id.tvDeskripsiDetail);
        tvJumlahPengunjung = findViewById(R.id.tvJumlahPengunjung);

        product = (Product) getIntent().getSerializableExtra("produk");

        if (product != null) {
            Log.d("DETAIL_INTENT", new Gson().toJson(product));

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            tvNama.setText(product.getNama());
            tvHarga.setText(formatRupiah.format(product.getHargajual()));
            tvStok.setText("Stok: " + product.getStok());
            tvKategori.setText("Kategori: " + (product.getKategori() != null ? product.getKategori() : "-"));
            tvDeskripsi.setText("Deskripsi: " + (product.getDeskripsi() != null ? product.getDeskripsi() : "-"));
            tvJumlahPengunjung.setText("Jumlah Pengunjung: " + product.getJumlah_pengunjung());

            Glide.with(this)
                    .load(ServerAPI.BASE_URL_Image + product.getFoto())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imgProduk);

            // Panggil API untuk update viewer
            api = RetrofitClient.getClient().create(RegisterAPI.class);
            Call<ResponseBody> call = api.updateViewer(product.getId_produk());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("UPDATE_VIEWER", "Viewer updated");
                    // Jika ingin, fetch ulang produk di sini untuk ambil jumlah terbaru
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("UPDATE_VIEWER", "Gagal update viewer: " + t.getMessage());
                }
            });

        } else {
            Toast.makeText(this, "Data produk tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }
}
