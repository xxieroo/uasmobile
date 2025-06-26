package com.example.uts_a22202302984;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private Spinner spinnerProvinsi, spinnerKota;
    private TextView tvNama, tvAlamat, tvTelp, tvKodepos, tvOngkir, tvEstimasi, tvTotalBayar;
    private RadioGroup radioGroupPembayaran;
    private Button btnProsesCheckout;
    private LinearLayout layoutProduk;

    private ArrayList<String> kotaIdList = new ArrayList<>();
    private ArrayList<String> provinsiIdList = new ArrayList<>();
    private ArrayList<Product> cartList = new ArrayList<>();

    private int ongkir = 0;
    private int totalProduk = 0;
    private int totalBayar = 0;

    private final int beratTotal = 1000;
    private final String originCityId = "501";
    private final String kurir = "jne";

    private String estimasiPengiriman = "-";
    private String userEmail = "", userNama = "", kodepos = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        spinnerProvinsi = findViewById(R.id.spinnerProvinsi);
        spinnerKota = findViewById(R.id.spinnerKota);
        tvNama = findViewById(R.id.tvNama);
        tvAlamat = findViewById(R.id.tvAlamat);
        tvTelp = findViewById(R.id.tvTelp);
        tvKodepos = findViewById(R.id.tvKodepos);
        tvOngkir = findViewById(R.id.tvOngkir);
        tvEstimasi = findViewById(R.id.tvEstimasi);
        tvTotalBayar = findViewById(R.id.tvTotalBayar);
        radioGroupPembayaran = findViewById(R.id.radioGroupPembayaran);
        btnProsesCheckout = findViewById(R.id.btnProsesCheckout);
        layoutProduk = findViewById(R.id.layoutProduk);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userEmail = sharedPreferences.getString("email", "-");
        userNama = sharedPreferences.getString("nama", "-");
        tvNama.setText("Nama: " + userNama);

        loadUserProfile(userEmail);
        loadProvinsi();

        SharedPreferences pref = getSharedPreferences("pref_product", Context.MODE_PRIVATE);
        String json = pref.getString("listorder", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        cartList = gson.fromJson(json, type);
        if (cartList == null) cartList = new ArrayList<>();

        tampilkanProdukKeLayout();
        for (Product p : cartList) totalProduk += p.getSubtotal();
        updateTotal();

        // Tambahan log untuk debugging id_produk
        for (Product p : cartList) {
            Log.d("CheckoutActivity", "id_produk dari SharedPreferences: " + p.getId_produk());
        }

        btnProsesCheckout.setOnClickListener(v -> {
            int selectedId = radioGroupPembayaran.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = findViewById(selectedId);
            String metode = selectedRadio.getText().toString();
            totalBayar = totalProduk + ongkir;

            simpanOrder(metode, estimasiPengiriman);
        });
    }

    private void tampilkanProdukKeLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        layoutProduk.removeAllViews();

        for (Product p : cartList) {
            View itemView = inflater.inflate(R.layout.list_checkout_produk, layoutProduk, false);

            ((TextView) itemView.findViewById(R.id.tvNama)).setText(p.getNama());
            ((TextView) itemView.findViewById(R.id.tvHarga)).setText("Harga: " + formatRupiah((int) p.getHargajual()));
            ((TextView) itemView.findViewById(R.id.tvJumlah)).setText("Jumlah: x" + p.getQuantity());
            ((TextView) itemView.findViewById(R.id.tvSubtotal)).setText("Subtotal: " + formatRupiah((int) p.getSubtotal()));
            Glide.with(this)
                    .load(ServerAPI.BASE_URL + "img/" + p.getFoto())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into((ImageView) itemView.findViewById(R.id.imgProduk));

            layoutProduk.addView(itemView);
        }
    }

    private void loadUserProfile(String email) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ServerAPI.BASE_URL + "get_profile.php?email=" + email)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json = new JSONObject(res);
                    if (json.getInt("result") == 1) {
                        JSONObject data = json.getJSONObject("data");
                        String alamat = data.getString("alamat");
                        String telp = data.getString("telp");
                        kodepos = data.optString("kodepos", "-");

                        runOnUiThread(() -> {
                            tvAlamat.setText("Alamat: " + alamat);
                            tvTelp.setText("Telepon: " + telp);
                            tvKodepos.setText("Kode Pos: " + kodepos);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadProvinsi() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ServerAPI.BASE_URL + "get_provinsi.php")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Gagal ambil provinsi", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json = new JSONObject(res);
                    JSONArray results = json.getJSONObject("rajaongkir").getJSONArray("results");

                    ArrayList<String> provinsiList = new ArrayList<>();
                    provinsiIdList.clear();

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject p = results.getJSONObject(i);
                        provinsiList.add(p.getString("province"));
                        provinsiIdList.add(p.getString("province_id"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, provinsiList);
                        spinnerProvinsi.setAdapter(adapter);
                        spinnerProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                loadKota(provinsiIdList.get(position));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadKota(String provId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ServerAPI.BASE_URL + "get_kota.php?province_id=" + provId)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Gagal ambil kota", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json = new JSONObject(res);
                    JSONArray results = json.getJSONObject("rajaongkir").getJSONArray("results");

                    ArrayList<String> kotaList = new ArrayList<>();
                    kotaIdList.clear();

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject k = results.getJSONObject(i);
                        kotaList.add(k.getString("city_name"));
                        kotaIdList.add(k.getString("city_id"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, kotaList);
                        spinnerKota.setAdapter(adapter);
                        spinnerKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                getOngkir(originCityId, kotaIdList.get(position));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getOngkir(String origin, String destination) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("origin", origin)
                .add("destination", destination)
                .add("weight", String.valueOf(beratTotal))
                .add("courier", kurir)
                .build();

        Request request = new Request.Builder()
                .url(ServerAPI.BASE_URL + "api_ongkir.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Gagal hitung ongkir", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json = new JSONObject(res);
                    JSONArray costs = json.getJSONObject("rajaongkir")
                            .getJSONArray("results")
                            .getJSONObject(0)
                            .getJSONArray("costs");

                    if (costs.length() > 0) {
                        JSONObject costObj = costs.getJSONObject(0)
                                .getJSONArray("cost").getJSONObject(0);
                        ongkir = costObj.getInt("value");
                        estimasiPengiriman = costObj.getString("etd");

                        runOnUiThread(() -> {
                            tvOngkir.setText("Ongkos Kirim: " + formatRupiah(ongkir));
                            tvEstimasi.setText("Estimasi Pengiriman: " + estimasiPengiriman + " Hari");
                            updateTotal();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void simpanOrder(String metode, String estimasi) {
        String alamat = tvAlamat.getText().toString().replace("Alamat: ", "");
        String telp = tvTelp.getText().toString().replace("Telepon: ", "");
        String provinsi = spinnerProvinsi.getSelectedItem().toString();
        String kota = spinnerKota.getSelectedItem().toString();

        RegisterAPI api = ApiClient.getClient(ServerAPI.BASE_URL).create(RegisterAPI.class);
        String status = metode.equalsIgnoreCase("COD") ? "diproses" : "menunggu";

        Call<ResponseBody> call = api.postOrder(
                userEmail, userNama, alamat, telp, kodepos, provinsi, kota,
                metode, totalProduk, ongkir, estimasi, totalBayar, status
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();

                        // Tambahkan log response
                        android.util.Log.d("CheckoutDebug", "Response: " + responseBody);

                        // Parse JSON
                        JSONObject json = new JSONObject(responseBody);

                        // Gunakan getInt untuk result (bukan getString)
                        if (json.getInt("result") == 1) {
                            int orderId = json.getInt("order_id");
                            simpanOrderDetail(orderId);
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Gagal simpan order", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CheckoutActivity.this, "Gagal menerima response dari server", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("CheckoutError", "Parsing Error: " + e.getMessage());
                    Toast.makeText(CheckoutActivity.this, "Terjadi kesalahan parsing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void simpanOrderDetail(int orderId) {
        RegisterAPI api = ApiClient.getClient(ServerAPI.BASE_URL).create(RegisterAPI.class);

        for (Product p : cartList) {
            // ✅ Tambahkan log untuk mengecek id_produk
            Log.d("Checkout", "ID produk yang dikirim: " + p.getId_produk());

            api.postOrderDetail(
                    orderId,
                    p.getId_produk(), // id_produk: harusnya string seperti "P001"
                    p.getNama(),
                    (int) p.getHargajual(),
                    p.getQuantity(),
                    (int) p.getSubtotal()
            ).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // Optional: log berhasil
                    Log.d("Checkout", "Order detail berhasil dikirim");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(CheckoutActivity.this, "Gagal simpan detail", Toast.LENGTH_SHORT).show();
                    Log.e("Checkout", "Gagal kirim order detail: " + t.getMessage());
                }
            });
        }

        // Hapus cart
        getSharedPreferences("pref_product", MODE_PRIVATE)
                .edit().remove("listorder").apply();

        Toast.makeText(this, "Checkout berhasil!", Toast.LENGTH_LONG).show();
        finish();
    }


    private void updateTotal() {
        totalBayar = totalProduk + ongkir;
        tvTotalBayar.setText("Total Bayar: " + formatRupiah(totalBayar));
    }

    private String formatRupiah(int value) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(value);
    }
}
