// HistoryActivity.java (Lengkap dengan fitur upload gambar bukti)
package com.example.uts_a22202302984;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202302984.model.OrderHistory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OnUploadClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private OrderHistory selectedOrder;

    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        RegisterAPI api = ApiClient.getClient(ServerAPI.BASE_URL).create(RegisterAPI.class);
        Call<List<OrderHistory>> call = api.getOrderHistory(email);

        call.enqueue(new Callback<List<OrderHistory>>() {
            @Override
            public void onResponse(Call<List<OrderHistory>> call, Response<List<OrderHistory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new OrderHistoryAdapter(HistoryActivity.this, response.body(), HistoryActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(HistoryActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderHistory>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUploadClicked(OrderHistory order) {
        selectedOrder = order;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Bukti Transfer"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadBuktiTransfer(imageUri);
        }
    }

    private void uploadBuktiTransfer(Uri fileUri) {
        File file = getFileFromUri(fileUri);
        if (file == null) return;

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("bukti", file.getName(), requestFile);
        RequestBody orderId = RequestBody.create(MultipartBody.FORM, selectedOrder.getOrderId());

        RegisterAPI api = ApiClient.getClient(ServerAPI.BASE_URL).create(RegisterAPI.class);
        Call<ResponseBody> call = api.uploadBuktiTransfer(orderId, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(HistoryActivity.this, "Upload berhasil", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Upload gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        File file = null;
        try {
            String fileName = System.currentTimeMillis() + ".jpg";
            InputStream inputStream = getContentResolver().openInputStream(uri);
            file = new File(getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
