package com.amazonproductscraping.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ImageScraping extends AppCompatActivity {

    private static final String TAG = "ImageScraping";

    private RecyclerView recyclerView;
    private com.amazonproductscraping.ui.ImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_scraping);

        // Initialize RecyclerView
        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start the image scraping task
        scrapeImages("https://www.amazon.in/Midnight-Perfume-Lasting-Fragrance-Cedarwood/dp/B0CJTTY5FS/ref=pd_sim_d_sccl_4_7/260-6553988-5776102?pd_rd_w=FpI6y&content-id=amzn1.sym.1f02df7f-f362-4e6d-9a92-f270d75713e4&pf_rd_p=1f02df7f-f362-4e6d-9a92-f270d75713e4&pf_rd_r=MVCEHD71GZ0CQPFSDB8C&pd_rd_wg=xQi5k&pd_rd_r=dd414cca-fca8-4357-bf40-83ad1d0b23ab&pd_rd_i=B0CJTTY5FS&th=1"); // Replace with actual URL
    }

    private void scrapeImages(String urlString) {

        executorService.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<String> imageUrls = new ArrayList<>();




            try {
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
                    Toast.makeText(ImageScraping.this, "No images found!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded Images: " + imageUrls.size());
                    imageAdapter = new com.amazonproductscraping.ui.ImageAdapter(imageUrls);
                    recyclerView.setAdapter(imageAdapter);

                    Log.i(TAG, "Images_Loads : "+imageUrls);

                    //textView.setText(imageUrls.toString());
                }
            });
        });
    }
}
