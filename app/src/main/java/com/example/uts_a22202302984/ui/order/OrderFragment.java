package com.example.uts_a22202302984.ui.order;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uts_a22202302984.CheckoutActivity;
import com.example.uts_a22202302984.MainLogin;
import com.example.uts_a22202302984.Product;
import com.example.uts_a22202302984.R;
import com.example.uts_a22202302984.ServerAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Product> orderList;
    private TextView tvBayar;
    private Button btnCheckout;
    private OrderAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrder);
        tvBayar = view.findViewById(R.id.tvBayar);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("pref_product", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("listorder", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        orderList = gson.fromJson(json, type);

        if (orderList == null) orderList = new ArrayList<>();

        adapter = new OrderAdapter(orderList, getContext());
        recyclerView.setAdapter(adapter);

        updateTotal();

        btnCheckout.setOnClickListener(v -> {
            // Ambil status login dari SharedPreferences
            SharedPreferences userPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
            boolean isGuest = userPrefs.getBoolean("is_guest", false);
            boolean isLoggedIn = userPrefs.getBoolean("is_logged_in", false);

            if (isGuest || !isLoggedIn) {
                Toast.makeText(getContext(), "Silakan login terlebih dahulu untuk checkout.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), MainLogin.class));
            } else {
                Toast.makeText(getContext(), "Ke Halaman Checkout!", Toast.LENGTH_SHORT).show();
                // Logika checkout lanjutan bisa ditambahkan di sini
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                startActivity(intent);
          }
        });
        return view;
    }

    private void simpanData(ArrayList<Product> list) {
        SharedPreferences preferences = requireContext().getSharedPreferences("pref_product", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("listorder", gson.toJson(list)).apply();
    }

    private void updateTotal() {
        double totalBayar = 0;
        for (Product product : orderList) {
            totalBayar += product.getSubtotal();
        }
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvBayar.setText("Total Bayar\n" + rupiah.format(totalBayar));
    }

    public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

        private final ArrayList<Product> list;
        private final Context context;

        public OrderAdapter(ArrayList<Product> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
            Product product = list.get(position);
            if (product != null) {
                NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                holder.tvNama.setText(product.getNama());
                holder.tvHarga.setText("Harga: " + rupiah.format(product.getHargajual()));
                holder.tvJumlah.setText("Jumlah: " + product.getQuantity());
                holder.tvTotal.setText("Total: " + rupiah.format(product.getSubtotal()));

                Glide.with(context)
                        .load(ServerAPI.BASE_URL_Image + product.getFoto())
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.stat_notify_error)
                        .into(holder.imgProduk);

                // Tombol hapus item
                holder.btnHapus.setOnClickListener(v -> {
                    list.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, list.size());
                    simpanData(list);
                    Toast.makeText(context, "Item dihapus dari pesanan", Toast.LENGTH_SHORT).show();
                    updateTotal();
                });

                // Tombol tambah quantity
                holder.btnTambah.setOnClickListener(v -> {
                    if (product.getQuantity() < product.getStok()) {
                        product.setQuantity(product.getQuantity() + 1);
                        notifyItemChanged(position);
                        simpanData(list);
                        updateTotal();
                    } else {
                        Toast.makeText(context, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
                    }
                });

                // Tombol kurangi quantity
                holder.btnKurang.setOnClickListener(v -> {
                    if (product.getQuantity() > 1) {
                        product.setQuantity(product.getQuantity() - 1);
                        notifyItemChanged(position);
                        simpanData(list);
                        updateTotal();
                    } else {
                        // Kalau dikurangi jadi 0, hapus saja
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        simpanData(list);
                        updateTotal();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNama, tvHarga, tvJumlah, tvTotal;
            ImageView imgProduk;
            ImageButton btnHapus, btnTambah, btnKurang;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgProduk = itemView.findViewById(R.id.imgProduk);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvHarga = itemView.findViewById(R.id.tvHarga);
                tvJumlah = itemView.findViewById(R.id.tvJumlah);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                btnHapus = itemView.findViewById(R.id.btnHapus);
                btnTambah = itemView.findViewById(R.id.btnTambah);
                btnKurang = itemView.findViewById(R.id.btnKurang);
            }
        }
    }
}
