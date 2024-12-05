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
    private TextView name,price,discountt,mrp,about_this_item,technical_details,additional_information,product_details,productSpecifications;
    private String amazonUrl,productName,discount,productPrice,discountPercentage,mrpPrice,formattedText,aboutThisItem,oprice,productInfoText;
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
        name = findViewById(R.id.name);
        price = findViewById(R.id.price);
        discountt = findViewById(R.id.discount);
        mrp = findViewById(R.id.mrp);
        about_this_item = findViewById(R.id.about_this_item);
        technical_details = findViewById(R.id.technical_details);
        additional_information = findViewById(R.id.additional_information);
        product_details = findViewById(R.id.product_details);
        productSpecifications = findViewById(R.id.productSpecifications);
        productDetails = findViewById(R.id.productDetails);

        amazonUrl = "https://www.amazon.in/Carlton-London-Women-Limited-Parfum/dp/B09MTR2HRP?th=1"; // Product URL



        // Web Scraping to fetch product data
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to Amazon product page
                    doc = Jsoup.connect(amazonUrl)
                            //.userAgent("Chrome/117.0.5938.92 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.5938.92 Safari/537.36")
                            .get();


                    //=========================================================================================== ok
                    // Extract product details  iske niche 3 element hai, 1=price,2=offer,3=mrp-price
                    productName = doc.select("span#productTitle").text();

                    // डिस्काउंट की जानकारी निकालें
                    discount = doc.select("span.savingsPercentage").text();

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
                    //=========================================================================================== ok





                    //=========================================================================================== ok
                    // "About this item" ke liye section select karein, ye jewellery ke liye hai.
                    Element aboutThisItemElement = doc.select("div.a-expander-content.a-expander-partial-collapse-content").first();

                    if (aboutThisItemElement != null) {
                        // Sabhi list items extract karein
                        Elements listItems = aboutThisItemElement.select("ul.a-unordered-list li");

                        // Extracted text ko format karein
                        final StringBuilder aboutThisItemText = new StringBuilder();

                        // Title "About this item" add karein aur ek line chhodhein
                        aboutThisItemText.append("About this item\n\n");

                        // Har item ke aage bullet point add karein
                        for (Element item : listItems) {
                            aboutThisItemText.append("• ").append(item.text()).append("\n");
                        }

                        // UI thread par TextView update karein
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                about_this_item.setText(aboutThisItemText.toString());
                            }
                        });
                    } else {
                        // Agar section nahi mila
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //about_this_item.setText("About this item\n\n• About this item not available.");
                            }
                        });
                    }
                    //=========================================================================================== ok


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
                        }

                        // UI thread par TextView update karein
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                additional_information.setText(additionalInfoText.toString());
                            }
                        });
                    } else {
                        Log.d(TAG, "Additional Information section not found.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                additional_information.setText("Additional Information\n\n• Additional Information not available.");
                            }
                        });
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
                            //product_details.setText(extractedText.toString());
                        }
                    });
                    //===========================================================================================



                    // Extract "Additional Information" section ye jewellery  ke liye hai.
                    //=========================================================================================== ok
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
                            }
                        }

                        // Update TextView in UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                additional_information.setText(additionalInfoText.toString());
                            }
                        });
                    } else {
                        Log.d(TAG, "Additional Information section not found.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                additional_information.setText("Additional Information not available.");
                            }
                        });
                    }
                    //===========================================================================================

                    //===========================================================================================
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
                            }
                        }

                        // Update TextView in UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                productSpecifications.setText(productSpecificationsText.toString());
                            }
                        });
                    } else {
                        Log.d(TAG, "Product specifications section not found.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                productSpecifications.setText("Product specifications not available.");
                            }
                        });
                    }

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
                            //about_this_item.setText("");
                        }
                    });
                }
            }
        }).start();*/



        new MainActivity.FetchAmazonDataTask().execute(amazonUrl);

    }

    private class FetchAmazonDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            try {
                // HTTP कनेक्शन सेट करें
                URL urlObj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                connection.setRequestProperty("Referrer", "https://www.google.com");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                // HTTP Response Code चेक करें
                int responseCode = connection.getResponseCode();
                Log.d("HTTP Response Code", String.valueOf(responseCode));

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // HTML कंटेंट पढ़ें
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder htmlContent = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        htmlContent.append(line);
                    }
                    reader.close();

                    // लॉग करें कि HTML क्या मिला है
                    Log.d("HTML_Response", htmlContent.toString());

                    // डेटा निकालें (Regex के जरिए)
                    productName = extractData(htmlContent.toString(), "<span id=\"productTitle\".*?>(.*?)</span>");
                    productPrice = extractData(htmlContent.toString(), "<span class=\"a-price-whole\">([\\d,]+)</span>");
                    discountPercentage = extractData(htmlContent.toString(), "class=\"a-size-large a-color-price savingPriceOverride.*?\">(-?\\d+%)</span>");
                    mrpPrice = extractData(htmlContent.toString(), "M\\.R\\.P\\.:.*?<span class=\"a-offscreen\">₹([\\d,]+)</span>");
                    aboutThisItem = extractAndFormatAboutThisItem(htmlContent.toString());

                    formattedText = String.format("About This Item:\n\n%s", aboutThisItem);




                    // "About This Item" को format करें
                    aboutThisItem = aboutThisItem != null ? extractAndFormatTable(aboutThisItem) : "Not Found";

                    // फॉर्मेटेड डेटा रिटर्न करें
                    return "Product Name: " + (productName != null ? productName.trim() : "Not Found") +
                            "\nPrice: " + (productPrice != null ? productPrice.trim() : "Not Found") +
                            "\nAbout This Item: \n" + aboutThisItem;
                } else {
                    return "Failed to fetch data. Response code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Unable to fetch data. Please try again later.";
            }
        }

        @Override
        protected void onPostExecute(String result) {


            if (result.equals(result)) {

                // isme jo hai wo "HttpURLConnection" se aa raha hai.
                name.setText(productName);
                price.setText(productPrice);
                discountt.setText(discountPercentage);
                mrp.setText(mrpPrice);
                about_this_item.setText(formattedText);

            } else {
                // isme jo hai wo "JSOUP" se aa raha hai.
                name.setText(productName);//ok
                price.setText(productPrice);//ok
                discountt.setText(discount);//ok
                mrp.setText(oprice);//ok
                about_this_item.setText(aboutThisItem);//ok
            }

        }


        private String extractAndFormatAboutThisItem(String html) {
            StringBuilder formattedText = new StringBuilder();

            // Extract the "ul" tag content within the "feature-bullets" div
            String regex = "<ul class=\"a-unordered-list a-vertical a-spacing-mini\">(.*?)</ul>";
            String ulContent = extractData(html, regex);

            if (ulContent != null) {
                // Extract each "li" tag content within the "ul" tag
                Pattern pattern = Pattern.compile("<li.*?>\\s*<span.*?>(.*?)</span>\\s*</li>", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(ulContent);

                // Header for "About This Item"
                formattedText.append("******************************\n");
                formattedText.append("About This Item\n");
                formattedText.append("******************************\n\n");

                int count = 1; // For numbering items
                while (matcher.find()) {
                    String listItem = matcher.group(1).trim();
                    // Clean up HTML tags within the list item
                    listItem = listItem.replaceAll("<[^>]*>", ""); // Remove any remaining HTML tags
                    // Add to formatted text with newline

                    //text ke samne number lagane ke liye ye code use hoga.
                    //formattedText.append(count).append("• ").append(listItem).append("\n\n");
                    formattedText.append("• ").append(listItem).append("\n\n");
                    count++;
                }
            } else {
                formattedText.append("******************************\n");
                formattedText.append("About This Item: Not Found\n");
                formattedText.append("******************************");
            }

            // Convert the plain text to a SpannableString with line breaks
            SpannableString spannableText = new SpannableString(formattedText.toString());

            // Example to apply a color to the text, you can modify this as per your need
            spannableText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return spannableText.toString();
        }

        private String extractData(String html, String regex) {
            try {
                // Trim and process the data
                productPrice = productPrice != null ? productPrice.replace(",","").trim() : "Price not found";
                discountPercentage = discountPercentage != null ? discountPercentage.replace("-", "").replace("%", "").trim() : "Discount not found";
                mrpPrice = mrpPrice != null ? mrpPrice.replace(",","").trim() : "MRP not found";
                productName = productName != null ? productName.trim().replaceAll("\\s+", " ") : "Not Found";


                Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    return matcher.group(1); // HTML कंटेंट को रिटर्न करें
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private String cleanHTMLTags(String html) {
            // HTML टैग्स हटाने के लिए Regex
            String cleanedHtml = html.replaceAll("<[^>]*>", ""); // हटाए HTML टैग्स
            cleanedHtml = cleanedHtml.replaceAll("&lrm;", ""); // हटाए &lrm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("&rlm;", ""); // हटाए &rlm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("About This Item", "");
            cleanedHtml = cleanedHtml.replaceAll("See more product details", "");
            cleanedHtml = cleanedHtml.replaceAll("       ", "");

            return cleanedHtml.trim();
        }

        private String extractAndFormatTable(String html) {
            // Extracting and formatting the table or list content
            StringBuilder formattedText = new StringBuilder();

            // Clean HTML tags first
            String cleanedHtml = cleanHTMLTags(html);

            // Split the content by bullet points or newlines
            String[] items = cleanedHtml.split("•|\\n");

            // Format the extracted data with bullet points
            for (String item : items) {
                item = item.trim();
                if (!item.isEmpty()) {
                    // Append each item with a bullet point to make it look neat
                    formattedText.append("• ").append(item).append("\n");
                }
            }

            // Return the formatted content
            return formattedText.toString().trim();
        }
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
