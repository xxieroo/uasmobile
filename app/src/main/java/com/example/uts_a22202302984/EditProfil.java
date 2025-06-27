package com.example.uts_a22202302984;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfil extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageView ivProfilePhoto;
    private Uri imageUri;
    private String currentPhotoPath;
    private String email;
    private String imageUrl = "";

    private TextView tvProfilWelcome;
    private TextInputEditText etNama, etAlamat, etKota, etProvinsi, etTelp, etKodepos, etPassword;
    private Button btnBack, btnSimpan;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profil);

        tvProfilWelcome = findViewById(R.id.tvProfil_Welcome);
        etNama = findViewById(R.id.etProfileNama);
        etAlamat = findViewById(R.id.etProfile_Alamat);
        etKota = findViewById(R.id.etProfile_Kota);
        etProvinsi = findViewById(R.id.etProfile_Provinsi);
        etTelp = findViewById(R.id.etProfile_Telp);
        etKodepos = findViewById(R.id.etProfile_Kodepos);
        btnBack = findViewById(R.id.tvProfile_Back);
        btnSimpan = findViewById(R.id.btnSubmit);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

        sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        if (!email.isEmpty()) {
            tvProfilWelcome.setText(email);
            getProfile(email);
        } else {
            Toast.makeText(this, "Email tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
        }

        ivProfilePhoto.setOnClickListener(view -> showImagePickerDialog());

        btnBack.setOnClickListener(v -> finish());

        btnSimpan.setOnClickListener(v -> {
            DataPelanggan data = new DataPelanggan();
            data.setNama(etNama.getText().toString().trim());
            data.setAlamat(etAlamat.getText().toString().trim());
            data.setKota(etKota.getText().toString().trim());
            data.setProvinsi(etProvinsi.getText().toString().trim());
            data.setTelp(etTelp.getText().toString().trim());
            data.setKodepos(etKodepos.getText().toString().trim());
            data.setEmail(email);

            if (data.getNama().isEmpty() || data.getAlamat().isEmpty() || data.getKota().isEmpty() ||
                    data.getProvinsi().isEmpty() || data.getTelp().isEmpty() || data.getKodepos().isEmpty()) {
                Toast.makeText(EditProfil.this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(data);
        });
    }

    private void showImagePickerDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Pilih Gambar");
        builder.setItems(new CharSequence[]{"Galeri", "Kamera"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    openGallery();
                    break;
                case 1:
                    openCamera();
                    break;
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                ivProfilePhoto.setImageURI(selectedImageUri);
                uploadImage(selectedImageUri);
            } else if (requestCode == CAMERA_REQUEST) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivProfilePhoto.setImageBitmap(bitmap);
                    uploadImage(Uri.fromFile(imgFile));
                }
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunggah foto...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Ambil input stream dari URI
            Log.d("UploadImage", "Uri: " + imageUri.toString());
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                progressDialog.dismiss();
                Log.e("UploadImage", "Input stream null");
                Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show();
                return;
            }

            File imageFile = getFileFromUri(imageUri);

            // Buat RequestBody dan MultipartBody
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData("foto", imageFile.getName(), requestFile);
            RequestBody emailPart = RequestBody.create(MediaType.parse("text/plain"), email);

            // Inisialisasi Retrofit
            ServerAPI urlAPI = new ServerAPI();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urlAPI.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RegisterAPI api = retrofit.create(RegisterAPI.class);
            api.uploadFoto(emailPart, fotoPart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressDialog.dismiss();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String resString = response.body().string();
                            JSONObject json = new JSONObject(resString);
                            Toast.makeText(EditProfil.this, json.getString("message"), Toast.LENGTH_SHORT).show();

                            if (json.getInt("result") == 1) {
                                // Refresh profil atau aksi lain
                                getProfile(email);
                            }
                        } else {
                            // Coba baca errorBody jika response.body() null
                            String error = response.errorBody() != null ? response.errorBody().string() : "Tidak diketahui";
                            Log.e("UploadFoto", "Error body: " + error);
                            Toast.makeText(EditProfil.this, "Gagal mengunggah: " + error, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditProfil.this, "Gagal parsing response", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfil.this, "Gagal terhubung: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Terjadi kesalahan saat memproses gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        return tempFile;
    }

//    private void checkPermissionAndPickImage() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
//            } else {
//                pickImageFromGallery();
//            }
//        } else {
//            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
//            } else {
//                pickImageFromGallery();
//            }
//        }
//    }

//    private void pickImageFromGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                pickImageFromGallery();
//            } else {
//                Toast.makeText(this, "Izin ditolak, tidak bisa akses galeri", Toast.LENGTH_SHORT).show();
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    private String getRealPathFromURI(Uri uri) {
//        Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            String result = cursor.getString(index);
//            cursor.close();
//            return result;
//        } else {
//            try {
//                File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
//                InputStream inputStream = getContentResolver().openInputStream(uri);
//                FileOutputStream outputStream = new FileOutputStream(tempFile);
//                byte[] buffer = new byte[1024];
//                int len;
//                while ((len = inputStream.read(buffer)) > 0) {
//                    outputStream.write(buffer, 0, len);
//                }
//                inputStream.close();
//                outputStream.close();
//                return tempFile.getAbsolutePath();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            Glide.with(this).load(imageUri).circleCrop().into(ivProfilePhoto);
//        }
//    }

    private void getProfile(String email) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = api.getProfile(email);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getString("result").equals("1")) {
                            JSONObject data = json.getJSONObject("data");
                            etNama.setText(data.getString("nama"));
                            etAlamat.setText(data.getString("alamat"));
                            etKota.setText(data.getString("kota"));
                            etProvinsi.setText(data.getString("provinsi"));
                            etTelp.setText(data.getString("telp"));
                            etKodepos.setText(data.getString("kodepos"));
                            imageUrl = data.getString("foto");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                String fullUrl = ServerAPI.BASE_URL + "image_profile/" + imageUrl;
                                Glide.with(EditProfil.this).load(fullUrl).circleCrop().into(ivProfilePhoto);
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfil.this, "Gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateProfile(DataPelanggan data) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);

        MultipartBody.Part fotoPart = null;
        RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), data.getNama());
        RequestBody alamat = RequestBody.create(MediaType.parse("text/plain"), data.getAlamat());
        RequestBody kota = RequestBody.create(MediaType.parse("text/plain"), data.getKota());
        RequestBody provinsi = RequestBody.create(MediaType.parse("text/plain"), data.getProvinsi());
        RequestBody telp = RequestBody.create(MediaType.parse("text/plain"), data.getTelp());
        RequestBody kodepos = RequestBody.create(MediaType.parse("text/plain"), data.getKodepos());
        RequestBody emailReq = RequestBody.create(MediaType.parse("text/plain"), data.getEmail());


        Call<ResponseBody> call = api.updateProfile(
                nama, alamat, kota, provinsi, telp, kodepos, emailReq, fotoPart
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        String result = json.getString("result");
                        String message = json.getString("message");

                        if (result.equals("1")) {
                            Toast.makeText(EditProfil.this, message, Toast.LENGTH_SHORT).show();
                            finish(); // hanya tutup jika berhasil
                        } else {
                            Toast.makeText(EditProfil.this, "Gagal menyimpan: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditProfil.this, "Gagal menyimpan (server error)", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(EditProfil.this, "Kesalahan parsing respon", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                new AlertDialog.Builder(EditProfil.this)
                        .setMessage("Gagal menyimpan: " + t.getMessage())
                        .setNegativeButton("Tutup", null)
                        .show();
            }
        });
    }

}
