package com.amazonproductscraping.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonproductscraping.ui.Adapter.ImageAdapter;

import java.io.BufferedReader;
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

public class scrapeImages extends AppCompatActivity {
    private static final String TAG = "scrapeImages";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view before accessing views
        setContentView(R.layout.activity_scrape_images); // Make sure this layout contains the RecyclerView

        recyclerView = findViewById(R.id.recyclerView);
        webView = findViewById(R.id.webView);

        String amazonUrl_STRng = "https://www.amazon.in/gp/product/B0C1YH6BLR";
        scrapeImagess(amazonUrl_STRng);

        // Now that RecyclerView is initialized, set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    //Image Scraping
    private void scrapeImagess(String urlString) {
        /*executorService.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<String> imageUrls = new ArrayList<>();

            try {
                // Amazon URL se data fetch karte hain
                if (urlString.equals(urlString)) {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    connection.connect();

                    // Response read karte hain
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder html = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        html.append(line);
                    }

                    String htmlContent = html.toString();

                    // Regex se hiRes image URLs ko extract karna
                    Pattern pattern = Pattern.compile("\"hiRes\":\"(https:\\/\\/m\\.media-amazon\\.com\\/images\\/I\\/[^\" ]+)\"");
                    Matcher matcher = pattern.matcher(htmlContent);

                    // Sabhi image URLs ko collect karte hain
                    while (matcher.find()) {
                        String imageUrl = matcher.group(1).replace("\\/", "/"); // Escaped slashes ko fix karte hain
                        if (!imageUrls.contains(imageUrl)) {
                            imageUrls.add(imageUrl);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error scraping images", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            // UI ko update karte hain main thread par
            runOnUiThread(() -> {
                if (imageUrls.isEmpty()) {
                    Toast.makeText(scrapeImages.this, "No images found!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded Images: " + imageUrls.size());
                    // UI ko image URLs ke saath update karte hain (RecyclerView adapter)
                    imageAdapter = new ImageAdapter(imageUrls);
                    recyclerView.setAdapter(imageAdapter);

                    Log.i(TAG, "Images Loaded: " + imageUrls);
                }
            });
        });*/

        executorService.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<String> imageUrls = new ArrayList<>();

            try {
                // Amazon URL से डेटा फेच करना
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                connection.connect();

                // पेज का HTML पढ़ना
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder html = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    html.append(line);
                }

                String htmlContent = html.toString();

                // Regex से hiRes इमेज URLs निकालना
                Pattern pattern = Pattern.compile("\"hiRes\":\"(https://m\\.media-amazon\\.com/images/I/[^\"]+)\"");
                Matcher matcher = pattern.matcher(htmlContent);

                while (matcher.find()) {
                    String imageUrl = matcher.group(1).replace("\\/", "/"); // Escaped स्लैशेस को सही करना
                    if (!imageUrls.contains(imageUrl)) {
                        imageUrls.add(imageUrl);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error scraping images", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            // UI को अपडेट करना (Main Thread पर)
            runOnUiThread(() -> {
                if (imageUrls.isEmpty()) {
                    Toast.makeText(scrapeImages.this, "No images found!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded Images: " + imageUrls.size());
                    imageAdapter = new ImageAdapter(imageUrls);
                    recyclerView.setAdapter(imageAdapter);
                }
            });
        });


    }


}