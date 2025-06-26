package com.example.uts_a22202302984.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.uts_a22202302984.Product;
import com.example.uts_a22202302984.R;
import com.example.uts_a22202302984.RegisterAPI;
import com.example.uts_a22202302984.ServerAPI;
import com.example.uts_a22202302984.ProductAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    private ImageSlider imageSlider;
    private Spinner spinnerKategori;

    private RecyclerView rvProdukTerlaris, rvProdukRekomendasi;
    private ProductAdapter adapterTerlaris, adapterRekomendasi;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Image Slider
        imageSlider = view.findViewById(R.id.imageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.bannerdext1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.bannerdext2, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        // Spinner kategori
        spinnerKategori = view.findViewById(R.id.spinner_kategori);
        String[] kategoriList = {"Kategori Gaming Gear", "Mouse", "Headset", "Monitor", "Keyboard", "Gamepad"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                kategoriList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(adapter);

        // RecyclerView Produk
        rvProdukTerlaris = view.findViewById(R.id.rv_produk_terlaris);
        rvProdukRekomendasi = view.findViewById(R.id.rv_produk_rekomendasi);

        rvProdukTerlaris.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvProdukRekomendasi.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        loadProduk();

        return view;
    }

    private void loadProduk() {
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
                    List<Product> allProducts = response.body();

                    List<Product> produkRekomendasi = new ArrayList<>(allProducts);
                    Collections.sort(produkRekomendasi, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return Integer.compare(p2.getJumlah_pengunjung(), p1.getJumlah_pengunjung()); // descending
                        }
                    });
                    if (produkRekomendasi.size() > 5) {
                        produkRekomendasi = produkRekomendasi.subList(0, 5);
                    }

                    List<Product> produkTerlaris = new ArrayList<>(allProducts);
                    Collections.sort(produkTerlaris, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return Integer.compare(p1.getStok(), p2.getStok()); // ascending
                        }
                    });
                    if (produkTerlaris.size() > 5) {
                        produkTerlaris = produkTerlaris.subList(0, 5);
                    }

                    adapterTerlaris = new ProductAdapter(getContext(), produkTerlaris);
                    adapterRekomendasi = new ProductAdapter(getContext(), produkRekomendasi);

                    rvProdukTerlaris.setAdapter(adapterTerlaris);
                    rvProdukRekomendasi.setAdapter(adapterRekomendasi);

                } else {
                    Toast.makeText(getContext(), "Data kosong/tidak tersedia", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }
}
