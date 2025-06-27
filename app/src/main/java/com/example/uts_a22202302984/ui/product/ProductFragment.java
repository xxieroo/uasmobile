package com.example.uts_a22202302984.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uts_a22202302984.Product;
import com.example.uts_a22202302984.R;
import com.example.uts_a22202302984.RegisterAPI;
import com.example.uts_a22202302984.ServerAPI;
import com.example.uts_a22202302984.databinding.FragmentProductBinding;
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

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private ProductAdapter adapter;
    private ArrayList<Product> productList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(productList, requireContext());
        binding.recyclerProduct.setAdapter(adapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        loadProducts();
        return view;
    }

    private void loadProducts() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<List<Product>> call = api.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.setOriginalList(new ArrayList<>(productList));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private ArrayList<Product> list;
        private ArrayList<Product> originalList;
        private Context context;

        public ProductAdapter(ArrayList<Product> list, Context context) {
            this.list = new ArrayList<>(list);
            this.originalList = new ArrayList<>(list);
            this.context = context;
        }

        public void setOriginalList(ArrayList<Product> originalList) {
            this.originalList = new ArrayList<>(originalList);
            this.list = new ArrayList<>(originalList);
        }

        public void filter(String query) {
            list.clear();
            if (query == null || query.trim().isEmpty()) {
                list.addAll(originalList);
            } else {
                String lowerQuery = query.toLowerCase();
                for (Product p : originalList) {
                    if (p.getNama().toLowerCase().contains(lowerQuery)) {
                        list.add(p);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_produk, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = list.get(position);

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            holder.tvNama.setText(product.getNama());
            holder.tvHarga.setText(formatRupiah.format(product.getHargajual()));
            holder.tvStok.setText("Stok: " + product.getStok());
            holder.tvJumlahPengunjung.setText("Jumlah Pengunjung: " + product.getJumlah_pengunjung());

            if (product.getStok() > 0) {
                holder.tvStatus.setText("Tersedia");
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                holder.tvStatus.setText("Tidak tersedia");
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
            }


            Glide.with(context)
                    .load(ServerAPI.BASE_URL_Image + product.getFoto())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgProduk);

            holder.btnOrder.setOnClickListener(v -> {
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
            });

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

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNama, tvHarga, tvStok, tvStatus, tvJumlahPengunjung;
            ImageView imgProduk, btnOrder, btnDetail;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvHarga = itemView.findViewById(R.id.tvHarga);
                tvStok = itemView.findViewById(R.id.tvStok);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvJumlahPengunjung = itemView.findViewById(R.id.tvJumlahPengunjung);
                imgProduk = itemView.findViewById(R.id.imgProduk);
                btnOrder = itemView.findViewById(R.id.btnOrder);
                btnDetail = itemView.findViewById(R.id.btnDetail);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
