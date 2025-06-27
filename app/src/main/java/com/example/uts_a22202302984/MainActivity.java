package com.example.uts_a22202302984;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cek status login atau tamu
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.contains("email");
        boolean isGuest = sharedPreferences.getBoolean("is_guest", false);

        // Splash screen 3 detik
        new Handler().postDelayed(() -> {
            Intent intent;
            if (isLoggedIn || isGuest) {
                intent = new Intent(MainActivity.this, MainHome.class);
            } else {
                intent = new Intent(MainActivity.this, MainHome.class);
                SharedPreferences isGuestSharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = isGuestSharedPreferences.edit();
                editor.putBoolean("is_guest", true);
                editor.apply();
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}
