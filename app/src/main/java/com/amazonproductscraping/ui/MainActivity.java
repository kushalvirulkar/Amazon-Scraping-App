package com.amazonproductscraping.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText productDetails;
    private TextView name,price,discountt,mrp,about_this_item,technical_details,additional_information,product_details;
    private String productName,productPrice,discountPercentage,mrpPrice,formattedText,aboutThisItem,oprice,productInfoText;

    private double calculateOriginalPrice(double discountedPrice, double discountPercentage) {
        // Formula to calculate original price
        return discountedPrice / (1 - (discountPercentage / 100));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextViews
        name = findViewById(R.id.name);
        price = findViewById(R.id.price);
        discountt = findViewById(R.id.discount);
        mrp = findViewById(R.id.mrp);
        about_this_item = findViewById(R.id.about_this_item);
        technical_details = findViewById(R.id.technical_details);
        additional_information = findViewById(R.id.additional_information);
        product_details = findViewById(R.id.product_details);

        // Web Scraping to fetch product data
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to Amazon product page
                    Document doc = Jsoup.connect("https://www.amazon.in/dp/B0CBTTCJL6")
                            //.userAgent("Chrome/117.0.5938.92 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.5938.92 Safari/537.36")
                            .get();


                    // Extract product details
                    productName = doc.select("span#productTitle").text();

                    // डिस्काउंट की जानकारी निकालें
                    String discount = doc.select("span.savingsPercentage").text();

                    // CSS Selector के माध्यम से प्रोडक्ट का price प्राप्त करें
                    Element priceElement = doc.select("span.a-price-whole").first();
                    if (priceElement != null) {
                        productName = priceElement.text();  // Price को Text के रूप में प्राप्त करें
                        Log.d("ProductPrice", "Product price: ₹" + productName);

                        // Clean the price string by removing commas
                        String cleanedPrice = productName.replace(",", "");  // Remove commas from price string
                        try {
                            double discountedPrice = Double.parseDouble(cleanedPrice);  // Convert cleaned price to double
                            Log.d("DiscountedPrice", "Discounted price: ₹" + discountedPrice);

                            // If discount is available, calculate the original price
                            if (discount != null && !discount.isEmpty()) {
                                // Clean the discount percentage string by removing the '%' symbol
                                double discountPercentage = parseDiscountPercentage(discount);  // Convert discount to double

                                // Calculate the original price using the formula
                                double originalPrice = calculateOriginalPrice(discountedPrice, discountPercentage);
                                int originalPriceWithoutDecimal = (int) originalPrice;  // Round off the original price to nearest integer
                                oprice = String.valueOf(originalPriceWithoutDecimal);  // Convert to string for displaying

                                // Print the result in logcat
                                Log.d("OriginalPrice", "The original M.R.P of the perfume is: ₹" + originalPriceWithoutDecimal);

                                // Update MRP TextView
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mrp.setText("M.R.P: ₹" + oprice);  // Set MRP value in the TextView
                                    }
                                });

                            } else {
                                Log.e(TAG, "Discount percentage not found or invalid.");
                            }

                        } catch (NumberFormatException e) {
                            Log.e("Error", "Invalid price format: " + e.getMessage());
                        }
                    } else {
                        Log.d("ProductPrice", "Price not found.");
                    }



                    // Extract "About this item" section
                    aboutThisItem = doc.select("div#feature-bullets ul").text();



                    //Product details
                    //=========================================================================================== ok
                    Elements listItems = doc.select("div#detailBulletsWrapper_feature_div");

                    // Har ek li item ko loop karke extract karna
                    final StringBuilder extractedText = new StringBuilder();
                    for (Element item : listItems) {
                        String itemText = item.text();
                        extractedText.append(itemText).append("\n");
                    }

                    // Main UI thread pe TextView update karna
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI update here
                            product_details.setText(extractedText.toString());
                        }
                    });
                    //===========================================================================================


                    // Extract "Product information" section
                    productInfoText = doc.select("span.a-section").text();



                    // Log data (optional for debugging)
                    Log.d(TAG, "Product Name: " + productName);
                    Log.d(TAG, "Price: " + price);

                    // Update UI with product details in main thread
                    final String finalProductName = productName;
                    final String finalProductPrice = productPrice;
                    final String finalProductInfoText = productInfoText;
                    final String finalAboutThisItemText = aboutThisItem;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            name.setText(finalProductName);//ok
                            price.setText(finalProductPrice);//ok
                            discountt.setText(discount);//ok
                            mrp.setText(oprice);//ok
                            about_this_item.setText(finalAboutThisItemText);//ok

                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Error fetching data", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            name.setText("Error fetching data");
                            price.setText("");
                            mrp.setText("");
                            about_this_item.setText("");
                        }
                    });
                }
            }
        }).start();
    }

    // Function to clean and parse the discount percentage
    private double parseDiscountPercentage(String discountPercentageString) {
        // Remove '%' symbol and handle negative sign
        discountPercentageString = discountPercentageString.replace("%", "").trim();

        // If the percentage has a negative sign, remove it
        if (discountPercentageString.startsWith("-")) {
            discountPercentageString = discountPercentageString.substring(1); // Remove the minus sign
        }

        // Convert the cleaned value to double and return
        try {
            Log.i(TAG, "discountPercentageString"+discountPercentageString);
            return Double.parseDouble(discountPercentageString);
        } catch (NumberFormatException e) {
            Log.e("Error", "Invalid discount percentage: " + discountPercentageString);
            return 0; // Return 0 if the percentage is invalid
        }
    }


}
