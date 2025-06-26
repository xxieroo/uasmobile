package com.example.uts_a22202302984;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduk;
        TextView txtNama, txtHarga, txtStok, txtStatus, txtPengunjung;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduk = itemView.findViewById(R.id.imgProduk);
            txtNama = itemView.findViewById(R.id.tvNama);
            txtHarga = itemView.findViewById(R.id.tvHarga);
            txtStok = itemView.findViewById(R.id.tvStok);
            txtStatus = itemView.findViewById(R.id.tvStatus);
            txtPengunjung = itemView.findViewById(R.id.tvJumlahPengunjung);
        }
    }
}
