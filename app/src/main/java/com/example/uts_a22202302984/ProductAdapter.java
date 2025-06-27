package com.example.uts_a22202302984;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_produk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.txtNama.setText(product.getNama());

        // Format harga ke dalam format Rupiah
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        holder.txtHarga.setText(formatRupiah.format(product.getHargajual()));

        holder.txtStok.setText("Stok: " + product.getStok());
        holder.txtPengunjung.setText("Dilihat: " + product.getJumlah_pengunjung() + "x");

        if (product.getStok() > 0) {
            holder.txtStatus.setText("Tersedia");
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.txtStatus.setText("Tidak tersedia");
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        // Load gambar menggunakan Glide
        Glide.with(context)
                .load(ServerAPI.BASE_URL_Image + product.getFoto())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgProduk);

        holder.btnDetail.setOnClickListener(v -> {
            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_detail, null);
            BottomSheetDialog dialog = new BottomSheetDialog(context);
            dialog.setContentView(bottomSheetView);

            ImageView imgDetail = bottomSheetView.findViewById(R.id.imgDetail);
            TextView tvNama = bottomSheetView.findViewById(R.id.tvNamaDetail);
            TextView tvHarga = bottomSheetView.findViewById(R.id.tvHargaDetail);
            TextView tvStok = bottomSheetView.findViewById(R.id.tvStokDetail);
            TextView tvKategori = bottomSheetView.findViewById(R.id.tvKategoriDetail);
            TextView tvDeskripsi = bottomSheetView.findViewById(R.id.tvDeskripsiDetail);
            TextView tvJumlahPengunjung = bottomSheetView.findViewById(R.id.tvJumlahPengunjung); // pastikan ada di layout

            tvNama.setText(product.getNama());
            tvHarga.setText(formatRupiah.format(product.getHargajual()));
            tvStok.setText("Stok: " + product.getStok());
            tvKategori.setText("Kategori: " + (product.getKategori() != null ? product.getKategori() : "-"));
            tvDeskripsi.setText("Deskripsi: " + (product.getDeskripsi() != null ? product.getDeskripsi() : "-"));
            tvJumlahPengunjung.setText("Jumlah Pengunjung: " + product.getJumlah_pengunjung());

            Glide.with(context)
                    .load(ServerAPI.BASE_URL_Image + product.getFoto())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imgDetail);

            dialog.show();

            // Panggil API untuk update viewer
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ServerAPI.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RegisterAPI api = retrofit.create(RegisterAPI.class);

            Call<ResponseBody> call = api.updateViewer(product.getId_produk());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        int updatedViewer = product.getJumlah_pengunjung() + 1;
                        product.setJumlah_pengunjung(updatedViewer);
                        tvJumlahPengunjung.setText("Jumlah Pengunjung: " + updatedViewer);
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, "Gagal update jumlah pengunjung", Toast.LENGTH_SHORT).show();
                }
            });
        });

        holder.btnOrder.setOnClickListener(v -> {
            // Simpan ke keranjang (contoh sederhana, sesuaikan dengan implementasi Anda)
            addToCart(product);
            Toast.makeText(context, "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
        });

    }

    private void addToCart(Product product) {
        if (product.getStok() <= 0) {
            Toast.makeText(context, "Produk tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = context.getSharedPreferences("pref_product", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("listorder", null);
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        ArrayList<Product> orderList = gson.fromJson(json, type);
        if (orderList == null) orderList = new ArrayList<>();

        boolean found = false;
        for (Product p : orderList) {
            if (p.getNama() != null && p.getNama().equals(product.getNama())) {
                if (p.getQuantity() < p.getStok()) {
                    p.setQuantity(p.getQuantity() + 1);
                    Toast.makeText(context, "Ditambahkan ke checkout", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
                }
                found = true;
                break;
            }
        }

        if (!found) {
            Product newProduct = new Product(product.getId_produk(),product.getFoto(), product.getNama(), product.getHargajual(), product.getStok(), product.getKategori(), product.getDeskripsi()
                    ,product.getJumlah_pengunjung() );
            newProduct.setQuantity(1);
            orderList.add(newProduct);
            Toast.makeText(context, "Ditambahkan ke checkout", Toast.LENGTH_SHORT).show();
        }

        pref.edit().putString("listorder", gson.toJson(orderList)).apply();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduk;
        TextView txtNama, txtHarga, txtStok, txtStatus, txtPengunjung;
        ImageView btnOrder, btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduk = itemView.findViewById(R.id.imgProduk);
            txtNama = itemView.findViewById(R.id.tvNama);
            txtHarga = itemView.findViewById(R.id.tvHarga);
            txtStok = itemView.findViewById(R.id.tvStok);
            txtStatus = itemView.findViewById(R.id.tvStatus);
            txtPengunjung = itemView.findViewById(R.id.tvJumlahPengunjung);
            btnOrder = itemView.findViewById(R.id.btnOrder);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}
