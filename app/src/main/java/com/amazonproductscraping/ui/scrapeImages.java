package com.amazonproductscraping.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonproductscraping.ui.Adapter.ImageAdapter;
import com.amazonproductscraping.ui.Interface.OnGet_ItemListener;
import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class scrapeImages extends AppCompatActivity implements OnGet_ItemListener {
    private static final String TAG = "scrapeImages";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private OnGet_ItemListener mListner;
    private String ReceiveData;

    @Override
    public final String onGetItem(String getvalue) {

        return getvalue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrape_images);

        mListner = this;
        recyclerView = findViewById(R.id.recyclerView);

        String amazonUrl_STRng = "https://www.amazon.in/Beardo-Whisky-Perfume-PARFUM-Lasting/dp/B09V4ZRFPT";
        scrapeImagess(amazonUrl_STRng);

        // Now that RecyclerView is initialized, set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);


        Button button = findViewById(R.id.check);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(scrapeImages.this, ReceiveData, Toast.LENGTH_SHORT).show();
            }
        });


    }

    //Image Scraping
    private void scrapeImagess(String urlString) {
        executorService.execute(() -> {
            List<String> imageUrls = new ArrayList<>();
            try {
                // Jsoup se URL se HTML fetch karna
                Document document = Jsoup.connect(urlString)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .referrer("https://www.amazon.in/")
                        .timeout(10000) // 10-second timeout
                        .get();

                // HTML content ko string mein convert karna
                String htmlContent = document.html();

                // Regex se hiRes image URLs ko extract karna
                Pattern pattern = Pattern.compile("\"hiRes\":\"(https://m\\.media-amazon\\.com/images/I/[^\"]+)\"");
                Matcher matcher = pattern.matcher(htmlContent);

                while (matcher.find()) {
                    String imageUrl = matcher.group(1).replace("\\/", "/");
                    if (!imageUrls.contains(imageUrl)) {
                        imageUrls.add(imageUrl);
                    }

                    // Limit the number of images to 6
                    if (imageUrls.size() >= 1) {
                        break; // Stop adding images once 6 are collected
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "Error scraping images", e);
            }

            // UI ko update karna (Main Thread par)
            runOnUiThread(() -> {
                if (imageUrls.isEmpty()) {
                    Toast.makeText(scrapeImages.this, "No images found!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded Images: " + imageUrls.size());
                    imageAdapter = new ImageAdapter(imageUrls,mListner);
                    recyclerView.setAdapter(imageAdapter);

                    ReceiveData = imageUrls.toString();
                }
            });
        });
    }




}