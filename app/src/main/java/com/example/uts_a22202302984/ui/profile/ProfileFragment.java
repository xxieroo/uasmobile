package com.example.uts_a22202302984.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uts_a22202302984.ChangePassword;
import com.example.uts_a22202302984.EditProfil;
import com.example.uts_a22202302984.HistoryActivity;
import com.example.uts_a22202302984.MainKontak;
import com.example.uts_a22202302984.MainLogin;
import com.example.uts_a22202302984.R;
import com.example.uts_a22202302984.RegisterAPI;
import com.example.uts_a22202302984.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private TextView tvNama, tvEmail;
    private ImageView ivAvatar;
    private Button btnEditProfile, btnKontakKami, btnHistory, btnLogout, btnChangePassword;
    private String email;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        if (email.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Akses Ditolak")
                    .setMessage("Jika ingin mengakses profil, Anda harus login terlebih dahulu.")
                    .setCancelable(false)
                    .setPositiveButton("Login Sekarang", (dialog, which) -> {
                        Intent intent = new Intent(getActivity(), MainLogin.class);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .show();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvNama = view.findViewById(R.id.tvNama);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnKontakKami = view.findViewById(R.id.btnKontakKami);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        tvEmail.setText(email);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfil.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePassword.class);
            startActivity(intent);
        });

        btnKontakKami.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainKontak.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Logout")
                    .setMessage("Apakah kamu yakin ingin logout?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        Intent intent = new Intent(getActivity(), MainLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData(email);
    }

    private void loadProfileData(String email) {
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

                            tvNama.setText(data.getString("nama"));
                            tvEmail.setText(data.getString("email"));

                            // Ambil nama file foto
                            String foto = data.getString("foto");
                            if (foto != null && !foto.isEmpty()) {
                                String fullImageUrl = ServerAPI.BASE_URL + "image_profile/" + foto;
                                Glide.with(requireContext())
                                        .load(fullImageUrl)
                                        .centerCrop()
                                        .placeholder(R.drawable.profile)
                                        .error(R.drawable.profile)
                                        .into(ivAvatar);
                            } else {
                                ivAvatar.setImageResource(R.drawable.profile);
                            }

                            // Simpan nama ke SharedPreferences jika diperlukan
                            sharedPreferences.edit().putString("nama", data.getString("nama")).apply();
                        } else {
                            Toast.makeText(getContext(), "Profil tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Gagal mengambil data profil", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
