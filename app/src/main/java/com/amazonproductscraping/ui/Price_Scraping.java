package com.amazonproductscraping.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Price_Scraping extends AppCompatActivity {

    private TextView productDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_scraping);

        productDetails = findViewById(R.id.productDetails);

        String amazonUrl = "https://www.amazon.in/dp/B09MTR2HRP"; // Product URL
        new FetchAmazonDataTask().execute(amazonUrl);
    }

    private class FetchAmazonDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            try {
                // HTTP Connection Setup
                URL urlObj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                Log.d("HTTP Response Code", String.valueOf(responseCode));

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder htmlContent = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        htmlContent.append(line);
                    }
                    reader.close();

                    // Log HTML for debugging
                    Log.d("HTML_Response", htmlContent.toString());

                    // Extract Data
                    String productPrice = extractData(htmlContent.toString(), "<span class=\"a-price-whole\">([\\d,]+)</span>");
                    String discountPercentage = extractData(htmlContent.toString(), "class=\"a-size-large a-color-price savingPriceOverride.*?\">(-?\\d+%)</span>");
                    String mrpPrice = extractData(htmlContent.toString(), "M\\.R\\.P\\.:.*?<span class=\"a-offscreen\">₹([\\d,]+)</span>");

                    // Trim and process the data
                    productPrice = productPrice != null ? productPrice.trim() : "Price not found";
                    discountPercentage = discountPercentage != null ? discountPercentage.replace("-", "").replace("%", "").trim() : "Discount not found";
                    mrpPrice = mrpPrice != null ? mrpPrice.replace(",","").trim() : "MRP not found";

                    return String.format("Price: %s\nDiscount: %s\nMRP: %s", productPrice, discountPercentage, mrpPrice);
                } else {
                    return "Failed to fetch data. Response Code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Unable to fetch data.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            productDetails.setText(result);
        }

        private String extractData(String html, String regex) {
            try {
                Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
