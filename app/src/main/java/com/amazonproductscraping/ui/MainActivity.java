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
    private TextView name_Txt,price_Txt,discount_Txt,mrp_Txt,about_this_item_Txt,technical_details_Txt,additional_information_Txt,product_details_Txt,productSpecifications_Txt;
    private String amazonUrl_STRng,productName_STRng,discount_STRng,productPrice_STRng,discountPercentage_STRng,mrpPrice_STRng,formattedText_STRng,aboutThisItem_STRng
            ,oprice_STRng,productInfoText_STRng,productInformation_STRng,additionalInformation_STRng;

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
        technical_details_Txt = findViewById(R.id.technical_details);
        additional_information_Txt = findViewById(R.id.additional_information);
        product_details_Txt = findViewById(R.id.product_details);
        productSpecifications_Txt = findViewById(R.id.productSpecifications);
        productDetails = findViewById(R.id.productDetails);

        amazonUrl_STRng = "https://www.amazon.in/Carlton-London-Women-Limited-Parfum/dp/B09MTR2HRP?th=1"; // Product URL



        // Web Scraping to fetch product data
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to Amazon product page
                    doc = Jsoup.connect(amazonUrl_STRng)
                            //.userAgent("Chrome/117.0.5938.92 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.5938.92 Safari/537.36")
                            .get();


                    //=========================================================================================== ok
                    // Extract product details  iske niche 3 element hai, 1=price,2=offer,3=mrp-price
                    productName_STRng = doc.select("span#productTitle").text();

                    // डिस्काउंट की जानकारी निकालें
                    discount_STRng = doc.select("span.savingsPercentage").text();

                    // CSS Selector के माध्यम से प्रोडक्ट का price प्राप्त करें
                    Element priceElement = doc.select("span.a-price-whole").first();
                    if (priceElement != null) {
                        productName_STRng = priceElement.text();  // Price को Text के रूप में प्राप्त करें
                        Log.d("ProductPrice", "Product price: ₹" + productName_STRng);

                        // Clean the price string by removing commas
                        String cleanedPrice = productName_STRng.replace(",", "");  // Remove commas from price string
                        try {
                            double discountedPrice = Double.parseDouble(cleanedPrice);  // Convert cleaned price to double
                            Log.d("DiscountedPrice", "Discounted price: ₹" + discountedPrice);

                            // If discount is available, calculate the original price
                            if (discount_STRng != null && !discount_STRng.isEmpty()) {
                                // Clean the discount percentage string by removing the '%' symbol
                                double discountPercentage = parseDiscountPercentage(discount_STRng);  // Convert discount to double

                                // Calculate the original price using the formula
                                double originalPrice = calculateOriginalPrice(discountedPrice, discountPercentage);
                                int originalPriceWithoutDecimal = (int) originalPrice;  // Round off the original price to nearest integer
                                oprice_STRng = String.valueOf(originalPriceWithoutDecimal);  // Convert to string for displaying

                                // Print the result in logcat
                                Log.d("OriginalPrice", "The original M.R.P of the perfume is: ₹" + originalPriceWithoutDecimal);

                                // Update MRP TextView
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mrp_Txt.setText("M.R.P: ₹" + oprice_STRng);  // Set MRP value in the TextView
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
                                about_this_item_Txt.setText(aboutThisItemText.toString());
                            }
                        });
                    } else {
                        // Agar section nahi mila
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //about_this_item_Txt.setText("About this item\n\n• About this item not available.");
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
                                additional_information_Txt.setText(additionalInfoText.toString());
                            }
                        });
                    } else {
                        Log.d(TAG, "Additional Information section not found.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                additional_information_Txt.setText("Additional Information\n\n• Additional Information not available.");
                            }
                        });
                    }



                    // Extract "About this item" section
                    aboutThisItem_STRng = doc.select("div#feature-bullets ul").text();


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
                            //product_details_Txt.setText(extractedText.toString());
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
                                additional_information_Txt.setText(additionalInfoText.toString());
                            }
                        });
                    } else {
                        Log.d(TAG, "Additional Information section not found.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                additional_information_Txt.setText("Additional Information not available.");
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
                                productSpecifications_Txt.setText(productSpecificationsText.toString());
                            }
                        });
                    } else {
                        Log.d(TAG, "Product specifications section not found.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                productSpecifications_Txt.setText("Product specifications not available.");
                            }
                        });
                    }

                    //===========================================================================================





                    // Extract "Product information" section
                    productInfoText_STRng = doc.select("span.a-section").text();



                    // Log data (optional for debugging)
                    Log.d(TAG, "Product Name: " + productName_STRng);
                    Log.d(TAG, "Price: " + productPrice_STRng);

                    // Update UI with product details in main thread
                    final String finalProductName = productName_STRng;
                    final String finalProductPrice = productPrice_STRng;
                    final String finalProductInfoText = productInfoText_STRng;
                    final String finalAboutThisItemText = aboutThisItem_STRng;

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
                            name_Txt.setText("Error fetching data");
                            price_Txt.setText("");
                            mrp_Txt.setText("");
                            //about_this_item_Txt.setText("");
                        }
                    });
                }
            }
        }).start();



        new MainActivity.FetchAmazonDataTask().execute(amazonUrl_STRng);

    }

    private class FetchAmazonDataTask extends AsyncTask<String, Void, String> {

        private boolean isSuccessful = false; // Track if the data was successfully retrieved
        private static final int MAX_RETRY_COUNT = 3; // Maximum retry count

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            int retryCount = 0;
            String result = "Not Found";

            while (retryCount < MAX_RETRY_COUNT) {
                try {
                    // HTTP connection setup
                    URL urlObj = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                    connection.setRequestProperty("Referrer", "https://www.amazon.in");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setUseCaches(false);

                    // Check HTTP response code
                    int responseCode = connection.getResponseCode();
                    Log.d("HTTP Response Code", String.valueOf(responseCode));

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read HTML content
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder htmlContent = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            htmlContent.append(line);
                        }
                        reader.close();

                        // Extract data using Jsoup and regular expressions
                        productName_STRng = extractData(htmlContent.toString(), "<span id=\"productTitle\".*?>(.*?)</span>");
                        aboutThisItem_STRng = extractData(htmlContent.toString(), "<div id=\"feature-bullets\".*?>(.*?)</div>");
                        productInformation_STRng = extractAndFormatTable(htmlContent.toString(), "<table id=\"productDetails_techSpec_section_1\".*?>(.*?)</table>");
                        additionalInformation_STRng = extractAndFormatTableUsingJsoup(htmlContent.toString());

                        aboutThisItem_STRng = aboutThisItem_STRng != null ? cleanHTMLTags(aboutThisItem_STRng) : "Not Found";
                        productInformation_STRng = productInformation_STRng != null ? cleanHTMLTags(productInformation_STRng) : "Not Found";
                        aboutThisItem_STRng = aboutThisItem_STRng != null ? formatAboutThisItem(aboutThisItem_STRng) : "Not Found";

                        // Retry if "Not Found" is returned for additional information
                        if (additional_information_Txt.equals("Not Found")) {
                            retryCount++;
                            Log.i(TAG, "Retrying... Attempt " + (retryCount + 1));
                            continue; // Retry fetching data
                        }

                        // If all data is successfully retrieved
                        if (productName_STRng != null || productName_STRng != null || productName_STRng != null || productName_STRng != null) {
                            isSuccessful = true; // Successfully retrieved data
                            result = formatProductData(productName_STRng, aboutThisItem_STRng, productInformation_STRng, additionalInformation_STRng);
                            break; // Exit loop after success
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Error fetching data: " + e.getMessage());
                    retryCount++; // Increment retry count on error
                    Log.i(TAG, "Retrying... Attempt " + (retryCount + 1));
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (isSuccessful) {
                productDetails.setText(result);
                // Data retrieval successful
                Toast.makeText(MainActivity.this, "Image Load Successful...", Toast.LENGTH_SHORT).show();
            } else {
                // All attempts failed
                Toast.makeText(MainActivity.this, "Failed to retrieve data after multiple attempts.", Toast.LENGTH_LONG).show();
            }
        }

        private String formatProductData(String productName, String aboutThisItem, String productInformation, String additionalInformation) {

            StringBuilder formattedData = new StringBuilder();

            // Product Name
            formattedData.append("******************************\n");
            formattedData.append("Product Name:\n");
            formattedData.append("******************************\n");
            formattedData.append(productName != null ? productName.trim() : "Not Found");
            formattedData.append("\n\n");

            /*// Product Price
            formattedData.append("******************************\n");
            formattedData.append("Price:\n");
            formattedData.append("******************************\n");
            formattedData.append(productPrice != null ? productPrice.trim() : "Not Found");
            formattedData.append("\n\n");*/

            // About This Item
            formattedData.append("******************************\n");
            formattedData.append("About This Item:\n");
            formattedData.append("******************************\n");
            formattedData.append(aboutThisItem != null ? cleanHTMLTags(aboutThisItem.trim()) : "Not Found");
            formattedData.append("\n\n");

            // Product Information (Table)
            formattedData.append("******************************\n");
            formattedData.append("Product Information:\n");
            formattedData.append("******************************\n");
            formattedData.append(productInformation != null ? productInformation.trim() : "Not Found");
            formattedData.append("\n\n");

            // Additional Information
            formattedData.append("******************************\n");
            formattedData.append("Additional Information:\n");
            formattedData.append("******************************\n");
            formattedData.append(additionalInformation != null ? cleanHTMLTags(additionalInformation.trim()) : "Not Found");
            formattedData.append("\n\n");

            /*// Product Details Section
            formattedData.append("******************************\n");
            formattedData.append("Product Details:\n");
            formattedData.append("******************************\n");
            formattedData.append(productDetailsSection != null ? cleanHTMLTags(productDetailsSection.trim()) : "Not Found");
            formattedData.append("\n\n");*/

            return formattedData.toString();
        }

        private String extractData(String html, String regex) {
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        private String extractAndFormatTable(String html, String regex) {
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                String tableHtml = matcher.group(1);

                // Extract rows (tr)
                Pattern rowPattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
                Matcher rowMatcher = rowPattern.matcher(tableHtml);

                StringBuilder formattedDetails = new StringBuilder();
                formattedDetails.append("-------- Product Information --------\n");

                while (rowMatcher.find()) {
                    String row = rowMatcher.group(1);

                    // Extract header (th) and data (td)
                    String header = extractData(row, "<th.*?>(.*?)</th>");
                    String data = extractData(row, "<td.*?>(.*?)</td>");

                    if (header != null && data != null) {
                        // Clean and format
                        formattedDetails.append("• ").append(header.trim()).append(": ").append(cleanHTMLTags(data.trim())).append("\n");
                    }
                }

                formattedDetails.append("---------------------------------");
                return formattedDetails.toString();
            }
            return null;
        }

        private String extractAndFormatTableUsingJsoup(String htmlContent) {
            // Jsoup से HTML डॉक्यूमेंट बनाएं
            Document doc = Jsoup.parse(htmlContent);

            // productDetails_db_sections को ढूंढें
            Element productDetailsSection = doc.select("div#productDetails_db_sections").first();

            if (productDetailsSection != null) {
                // अगर पाया गया तो इसे return करें
                return productDetailsSection.text();
            }
            return "Not Found"; // अगर नहीं मिला तो "Not Found" लौटाएं
        }

        private String formatAboutThisItem(String aboutText) {
            String cleanedText = cleanHTMLTags(aboutText);
            String[] paragraphs = cleanedText.split("(?<=\\.|\\|)"); // '.' या '|' पर विभाजन

            StringBuilder formattedText = new StringBuilder();
            formattedText.append("******************************\n");
            formattedText.append("About This Item:\n");
            formattedText.append("******************************\n");
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    formattedText.append("• ").append(paragraph.trim()).append("\n\n");
                }
            }
            return formattedText.toString().trim();
        }

        private String cleanHTMLTags(String html) {
            // HTML टैग्स हटाने के लिए Regex और &lrm; को हटाने के लिए
            String cleanedHtml = html.replaceAll("<[^>]*>", ""); // हटाए HTML टैग्स
            cleanedHtml = cleanedHtml.replaceAll("&lrm;", ""); // हटाए &lrm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("&rlm;", ""); // हटाए &rlm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("       ", "");

            return cleanedHtml.trim(); // सफाई के बाद ट्रिम कर देना
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
