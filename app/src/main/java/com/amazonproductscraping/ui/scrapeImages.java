package com.amazonproductscraping.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amazonproductscraping.ui.Adapter.ImageAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class scrapeImages extends AppCompatActivity {
    private static final String TAG = "scrapeImages";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String amazonUrl_STRng = "https://www.amazon.in/dp/B0CY5HQ8V1/ref=sspa_dk_detail_5?pd_rd_i=B0CY5HQ8V1&pd_rd_w=qHMwZ&content-id=amzn1.sym.9f1cb690-f0b7-44de-b6ff-1bad1e37d3f0&pf_rd_p=9f1cb690-f0b7-44de-b6ff-1bad1e37d3f0&pf_rd_r=DX8TBDHZ0G7GJ6ZYXSKJ&pd_rd_wg=zFpxl&pd_rd_r=1fdec38c-ebde-42af-b950-5a0717500643&sp_csd=d2lkZ2V0TmFtZT1zcF9kZXRhaWxfdGhlbWF0aWM&th=1";
        scrapeImagess(amazonUrl_STRng);
    }

    //Image Scraping
    private void scrapeImagess(String urlString) {

        executorService.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<String> imageUrls = new ArrayList<>();

            try {
                if (urlString.equals(urlString)){
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    connection.connect();

                    // Read the response
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder html = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        html.append(line);
                    }

                    String htmlContent = html.toString();

                    // Regex to extract hiRes image URLs
                    Pattern pattern = Pattern.compile("\"hiRes\":\"(https:\\/\\/m\\.media-amazon\\.com\\/images\\/I\\/[^\" ]+)\"");
                    Matcher matcher = pattern.matcher(htmlContent);

                    while (matcher.find()) {
                        String imageUrl = matcher.group(1).replace("\\/", "/"); // Fix escaped slashes
                        if (!imageUrls.contains(imageUrl)) {
                            imageUrls.add(imageUrl);
                        }

                        // Limit the number of images to 6
                        if (imageUrls.size() >= 1) {
                            break; // Stop adding images once 6 are collected
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

            // Update the UI on the main thread
            runOnUiThread(() -> {
                if (imageUrls.isEmpty()) {
                    Toast.makeText(scrapeImages.this, "No images found!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded Images: " + imageUrls.size());
                    //imageAdapter = new ImageAdapter(imageUrls);
                    //recyclerView.setAdapter(imageAdapter);

                    Log.i(TAG, "Images_Loaded: " + imageUrls);
                }
            });
        });
    }

}