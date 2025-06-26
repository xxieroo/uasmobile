package com.example.uts_a22202302984;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainKontak extends AppCompatActivity {

    private LinearLayout layoutMaps, layoutInstagram, layoutTokopedia, layoutTikTok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_kontak);

        // Inisialisasi layout yang dapat diklik
        layoutMaps = findViewById(R.id.layoutMaps);
        layoutInstagram = findViewById(R.id.layoutInstagram);
        layoutTokopedia = findViewById(R.id.layoutTokopedia);
        layoutTikTok = findViewById(R.id.layoutTikTok);

        // Set klik Maps
        layoutMaps.setOnClickListener(v -> {
            String url = "https://maps.google.com/?q=Jl.+Karang+Anyar+No.13,+Brumbungan,+Semarang";
            bukaLink(url);
        });

        // Set klik Instagram
        layoutInstagram.setOnClickListener(v -> {
            String url = "https://instagram.com/dexttech";
            bukaLink(url);
        });

        // Set klik Tokopedia
        layoutTokopedia.setOnClickListener(v -> {
            String url = "https://www.tokopedia.com/dextsemarang";
            bukaLink(url);
        });

        // Set klik TikTok
        layoutTikTok.setOnClickListener(v -> {
            String url = "https://www.tiktok.com/@dext.tech";
            bukaLink(url);
        });
    }

    private void bukaLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
