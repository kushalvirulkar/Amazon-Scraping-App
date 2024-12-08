package com.amazonproductscraping.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText productDetails;
    private TextView name_Txt,price_Txt,discount_Txt,mrp_Txt,about_this_item_Txt,product_information_Txt,technical_details_Txt,additional_information_Txt,product_details_Txt,productSpecifications_Txt;
    private String amazonUrl_STRng,productName_STRng,discount_STRng,productPrice_STRng,mrpPrice_STRng,formattedText_STRng,aboutThisItem_STRng
            ,oprice_STRng,productInformation_STRng,additionalInformation_STRng,technical_details_STRng,product_details_STRng,productSpecifications_STRng;

    private boolean isSuccessful = false;
    //private String productName,  aboutThisItem, productInformation, additionalInformation;
    private Document doc;

    private double calculateOriginalPrice(double discountedPrice, double discountPercentage) {
        // Formula to calculate original price
        return discountedPrice / (1 - (discountPercentage / 100));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextViews
        name_Txt = findViewById(R.id.name);
        price_Txt = findViewById(R.id.price);
        discount_Txt = findViewById(R.id.discount);
        mrp_Txt = findViewById(R.id.mrp);
        about_this_item_Txt = findViewById(R.id.about_this_item);
        product_information_Txt = findViewById(R.id.product_information);
        technical_details_Txt = findViewById(R.id.technical_details);
        additional_information_Txt = findViewById(R.id.additional_information);
        product_details_Txt = findViewById(R.id.product_details);
        productSpecifications_Txt = findViewById(R.id.productSpecifications);

        amazonUrl_STRng = "https://www.amazon.in/ZENEME-Rhodium-Plated-Silver-Toned-Zirconia-Jewellery/dp/B0BQJL99KR"; // Product URL



        // Web Scraping to fetch product data
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to Amazon product page
                    doc = Jsoup.connect(amazonUrl_STRng)
                            //.userAgent("Chrome/117.0.5938.92 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.5938.92 Safari/537.36")
                            .referrer("https://www.google.com")
                            .timeout(10000)
                            .get();


                    //1========================================================================================== ok
                    // Extract product details  iske niche 3 element hai, 1=price,2=offer,3=mrp-price

                    // Extract product name
                    productName_STRng = doc.select("span#productTitle").text();


                    // डिस्काउंट की जानकारी निकालें
                    String discount = doc.select("span.savingsPercentage").text();

                    discount = discount.replace("%", "").replace("-","").trim();
                    discount_STRng = discount;

                    // CSS Selector के माध्यम से प्रोडक्ट का price प्राप्त करें
                    Element priceElement = doc.select("span.a-price-whole").first();
                    //String cleanedPrice = productPrice_STRng.replace(",", "").replace(".","")

                    if (priceElement != null) {
                        productPrice_STRng = priceElement.text();  // Price को Text के रूप में प्राप्त करें
                        productPrice_STRng = productPrice_STRng.replace(",", "").replace(".","").trim();

                        try {
                            double discountedPrice = Double.parseDouble(productPrice_STRng);  // Convert cleaned price to double
                            Log.d("DiscountedPrice", "Discounted price: ₹" + discountedPrice);

                            // If discount is available, calculate the original price
                            if (discount != null && !discount.isEmpty()) {
                                // Clean the discount percentage string by removing the '%' symbol
                                double discountPercentage = parseDiscountPercentage(discount);  // Convert discount to double



                                // Calculate the original price using the formula
                                double originalPrice = calculateOriginalPrice(discountedPrice, discountPercentage);
                                int originalPriceWithoutDecimal = (int) originalPrice;  // Round off the original price to nearest integer
                                mrpPrice_STRng = String.valueOf(originalPriceWithoutDecimal);  // Convert to string for displaying

                                // Print the result in logcat
                                Log.d("OriginalPrice", "The original M.R.P of the perfume is: ₹" + originalPriceWithoutDecimal);

                                // Update MRP TextView
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //mrp_Txt.setText("M.R.P: ₹" + oprice_STRng);  // Set MRP value in the TextView
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
                    //1========================================================================================== ok



                    //2========================================================================================== ok
                    // "About this item" ke liye section select karein, ye jewellery ke liye hai.
                    Element aboutThisItemElement = doc.select("div.a-expander-content.a-expander-partial-collapse-content").first();

                    if (aboutThisItemElement != null) {
                        // Sabhi list items extract karein
                        Elements listItems = aboutThisItemElement.select("ul.a-unordered-list li");

                        // Extracted text ko format karein
                        final StringBuilder aboutThisItemText = new StringBuilder();

                        // Title "About this item" add karein aur ek line chhodhein
                        //aboutThisItemText.append("About this item\n\n");

                        // Har item ke aage bullet point add karein
                        for (Element item : listItems) {
                            aboutThisItemText.append("• ").append(item.text()).append("\n");

                            aboutThisItem_STRng = aboutThisItemText.toString();

                            //Log.i(TAG, "aboooo : "+ aboutThisItemText);
                        }
                    }
                    //2========================================================================================== ok



                    //3 Additional Information==========================================================================================
                    // "Additional Information" section extract karein
                    Elements additionalInfoElements = doc.select("div#qpeTitleTag_feature_div");
                    if (!additionalInfoElements.isEmpty()) {
                        final StringBuilder additionalInfoText = new StringBuilder();

                        // Title add karein aur ek line chhodhein
                        additionalInfoText.append("Additional Information\n\n");

                        // Har row ke key-value pair ko extract karein
                        for (Element item : additionalInfoElements.select("tr")) {
                            String key = item.select("th").text(); // Header text
                            String value = item.select("td").text(); // Value text

                            // Bullet point ke sath format karein
                            additionalInfoText.append("• ").append(key).append(": ").append(value).append("\n");

                            additionalInformation_STRng = additionalInfoText.toString();

                        }
                    }
                    //3 Additional Information==========================================================================================


                    // Extract "Additional Information" section ye jewellery  ke liye hai.
                    //3 Additional Information========================================================================================== ok
                    Elements additionalInfoElementss = doc.select("div.a-fixed-left-grid.product-facts-detail");
                    if (!additionalInfoElementss.isEmpty()) {
                        final StringBuilder additionalInfoText = new StringBuilder();
                        Log.d(TAG, "Additional Information section found.");

                        for (Element grid : additionalInfoElementss) {
                            // Extract the key (left column text)
                            String key = grid.select("div.a-fixed-left-grid-col.a-col-left span.a-color-base").text();
                            // Extract the value (right column text)
                            String value = grid.select("div.a-fixed-left-grid-col.a-col-right span.a-color-base").text();

                            // Append key-value pair to the StringBuilder
                            if (!key.isEmpty() && !value.isEmpty()) {
                                additionalInfoText.append(key).append(": ").append(value).append("\n");
                                Log.d(TAG, "Key: " + key + ", Value: " + value); // Debug log

                                additionalInformation_STRng = additionalInfoText.toString();
                            }
                        }
                    }
                    //3 Additional Information========================================================================================== ok


                    //4 Product specifications===========================================================================================
                    // Extract "Product Specifications" section ye jewellery  ke liye hai.
                    Elements productSpecificationsElements = doc.select("div.a-row");  // We will extract data inside div.a-row

                    if (!productSpecificationsElements.isEmpty()) {
                        final StringBuilder productSpecificationsText = new StringBuilder();
                        Log.d(TAG, "Product specifications section found.");

                        // Loop through each section in the product specifications
                        for (Element section : productSpecificationsElements) {
                            // Extract the heading of each section like "Jewellery Information", "Rhodium Plated Brass", etc.
                            String sectionHeading = section.select("h5.a-spacing-small.a-spacing-top-small").text();

                            // Only process sections that have a heading and a table with specifications
                            if (!sectionHeading.isEmpty()) {
                                productSpecificationsText.append("******************************\n");
                                productSpecificationsText.append(sectionHeading).append("\n");
                                productSpecificationsText.append("******************************\n");

                                // Extract each row of the table (key-value pairs)
                                Elements specificationRows = section.select("table.a-keyvalue tbody tr");

                                for (Element row : specificationRows) {
                                    String key = row.select("th.a-span5.a-size-base").text();  // Extract the key (e.g., "Brand", "Clasp")
                                    String value = row.select("td.a-span7.a-size-base").text();  // Extract the value (e.g., "ZENEME", "Hook Clasp")

                                    if (!key.isEmpty() && !value.isEmpty()) {
                                        productSpecificationsText.append("• ").append(key).append(": ").append(value).append("\n");
                                    }
                                }

                                // Add a separator for better readability between sections
                                productSpecificationsText.append("\n");

                                productSpecifications_STRng = productSpecificationsText.toString();
                            }
                        }
                    }

                    //4 Product specifications===========================================================================================





                    //Product details
                    //=========================================================================================== ok
                    Elements listItems = doc.select("ul.a-unordered-list.a-nostyle.a-vertical.a-spacing-none.detail-bullet-list li");

                    final StringBuilder extractedText = new StringBuilder();
                    for (Element item : listItems) {
                        // Extract visible text and remove unwanted HTML tags
                        String itemText = Jsoup.parse(item.html()).text().trim();

                        // Check if the item contains a title and description (i.e., a colon)
                        if (itemText.contains(":")) {
                            String[] parts = itemText.split(":");
                            String title = parts[0].trim();
                            String description = parts.length > 1 ? parts[1].trim() : "";

                            extractedText.append("• ").append(title).append(": ").append(description).append("\n");
                        } else {
                            extractedText.append("• ").append(itemText).append("\n\n");
                        }

                        product_details_STRng = extractedText.toString();
                    }

                    //===========================================================================================


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            name_Txt.setText(productName_STRng);
                            price_Txt.setText(productPrice_STRng);
                            discount_Txt.setText(discount_STRng);
                            mrp_Txt.setText(mrpPrice_STRng);
                            about_this_item_Txt.setText(aboutThisItem_STRng);
                            additional_information_Txt.setText(additionalInformation_STRng);
                            product_details_Txt.setText(product_details_STRng);
                            productSpecifications_Txt.setText(productSpecifications_STRng);

                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Error fetching data", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            name_Txt.setText("");
                            price_Txt.setText("");
                            discount_Txt.setText("");
                            mrp_Txt.setText("");
                            about_this_item_Txt.setText("");
                            additional_information_Txt.setText("");
                            product_details_Txt.setText("");
                            productSpecifications_Txt.setText("");
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
