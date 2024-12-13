package com.amazonproductscraping.ui;

import static android.webkit.URLUtil.isValidUrl;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonproductscraping.ui.Adapter.ImageAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
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
    private ImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText editText;
    private Button startButton,show;
    private TextView name_Txt,price_Txt,discount_Txt,mrp_Txt,about_this_item_Txt,product_information_Txt,technical_details_Txt,additional_information_Txt,product_details_Txt,productSpecifications_Txt;
    private String amazonUrl_STRng,productName_STRng,discount_STRng,productPrice_STRng,mrpPrice_STRng,aboutThisItem_STRng
            ,productInformation_STRng,additionalInformation_STRng,technical_details_STRng,product_details_STRng,productSpecifications_STRng;
    private boolean isSuccessful = false;
    //private String productName,  aboutThisItem, productInformation, additionalInformation;
    private Document doc;
    // Database Helper
    //private DatabaseHelper dbHelper = new DatabaseHelper(this);

    private double calculateOriginalPrice(double discountedPrice, double discountPercentage) {
        // Formula to calculate original price
        return discountedPrice / (1 - (discountPercentage / 100));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_scraping);

        // Initialize RecyclerView
        startButton = findViewById(R.id.start);
        show = findViewById(R.id.show);
        editText = findViewById(R.id.edittext);
        recyclerView = findViewById(R.id.recyclerView);
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


        amazonUrl_STRng = editText.getText().toString(); // Product URL


        startButton.setOnClickListener(v -> {
            String enteredUrl = editText.getText().toString().trim();

            editText.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
            show.setVisibility(View.VISIBLE);

            if (isValidUrl(enteredUrl)) {
                amazonUrl_STRng = enteredUrl;
                scrapeImages(amazonUrl_STRng); // Start scraping
                startWebScrapingJSoup(amazonUrl_STRng); // Start product data scraping
                //startWebScrapingHTML(amazonUrl_STRng); // Start product data scraping

            } else {
                Toast.makeText(ImageScraping.this, "Please enter a valid URL starting with http:// or https://", Toast.LENGTH_SHORT).show();
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.VISIBLE);
                show.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    //Image Scraping
    private void scrapeImages(String urlString) {

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
                    imageAdapter = new ImageAdapter(imageUrls);
                    recyclerView.setAdapter(imageAdapter);

                    Log.i(TAG, "Images_Loads : "+imageUrls);

                    //textView.setText(imageUrls.toString());
                }
            });
        });
    }
    private void startWebScrapingJSoup(String amazonUrlStRng) {
        // Web Scraping to fetch product data
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to Amazon product page
                    doc = Jsoup.connect(amazonUrlStRng)
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

    private void startWebScrapingHTML(String amazonUrlStRng) {
        new FetchAmazonDataTask().execute(amazonUrlStRng);
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
                        productName_STRng = extractData(htmlContent.toString(), "<span id=\"productTitle\".*?>(.*?)</span>");//ok
                        productPrice_STRng = extractData(htmlContent.toString(), "<span class=\"a-price-whole\">([\\d,]+)</span>");//ok
                        discount_STRng = extractData(htmlContent.toString(), "class=\"a-size-large a-color-price savingPriceOverride.*?\">(-?\\d+%)</span>");//ok
                        mrpPrice_STRng = extractData(htmlContent.toString(), "M\\.R\\.P\\.:.*?<span class=\"a-offscreen\">₹([\\d,]+)</span>");//ok
                        aboutThisItem_STRng = extractData(htmlContent.toString(), "<div id=\"feature-bullets\".*?>(.*?)</div>");//1 se 2 niche;
                        aboutThisItem_STRng = aboutThisItem_STRng != null ? formatAboutThisItem(aboutThisItem_STRng) : "Not Found";//2;
                        productInformation_STRng = extractAndFormatTable(htmlContent.toString(), "<table id=\"productDetails_techSpec_section_1\".*?>(.*?)</table>");//1 se 2 niche;
                        productInformation_STRng = productInformation_STRng != null ? cleanHTMLTags(productInformation_STRng) : "Not Found";//2;
                        //additionalInformation_STRng = extractAndFormatTableUsingJsoup(htmlContent.toString());//ok


                        product_details_STRng = extractData(htmlContent.toString(), "<ul class=\"a-unordered-list a-nostyle a-vertical a-spacing-none detail-bullet-list\">(.*?)</ul>");//1 to 5
                        product_details_STRng = formatProductDetailsPlainText(product_details_STRng);//2
                        product_details_STRng = product_details_STRng != null ? product_details_STRng.replace("&lrm;","").trim() : "Name not found";//3
                        product_details_STRng = product_details_STRng != null ? product_details_STRng.replace("&rlm;","").trim() : "Name not found";//4
                        product_details_STRng = product_details_STRng.replaceAll("\\s{2,}", " ");//5




                        additionalInformation_STRng = extractData(htmlContent.toString(), "<div id=\"productDetails_db_sections\".*?>(.*?)</div>");
                        additionalInformation_STRng = formatAdditionalInformationPlainText(additionalInformation_STRng);
                        additionalInformation_STRng = additionalInformation_STRng.replaceAll("<[^>]*>", ""); // Remove all HTML tags
                        product_details_STRng = product_details_STRng.replaceAll("\\s{2,}", " ");




                        if (aboutThisItem_STRng.equals("Not Found") == productInformation_STRng.equals("Not Found") == additionalInformation_STRng.equals("Not Found")
                                == technical_details_STRng.equals("Not Found") == product_details_STRng.equals("Not Found") == productSpecifications_STRng.equals("Not Found")){
                            retryCount++;
                            Log.i(TAG, "Retrying... Attempt " + (retryCount + 1));
                            continue; // Retry fetching data
                        }else {
                            isSuccessful = true; // Successfully retrieved data
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
                name_Txt.setText(productName_STRng);
                price_Txt.setText(productPrice_STRng);
                discount_Txt.setText(discount_STRng);
                mrp_Txt.setText(mrpPrice_STRng);

                // Check and set visibility for 'aboutThisItem_STRng' and 'about_this_item_Txt'
                if (aboutThisItem_STRng == null || aboutThisItem_STRng.trim().isEmpty()) {
                    about_this_item_Txt.setVisibility(View.GONE); // Make TextView gone
                } else {
                    about_this_item_Txt.setVisibility(View.VISIBLE); // If not null, make it visible
                    about_this_item_Txt.setText(aboutThisItem_STRng); // Set text if needed
                    //about_this_item_Txt.setText(Html.fromHtml(aboutThisItem_STRng, Html.FROM_HTML_MODE_COMPACT));
                }

                // Check and set visibility for 'productInformation_STRng' and 'product_information_Txt'
                if (productInformation_STRng == null || productInformation_STRng.trim().isEmpty()) {
                    product_information_Txt.setVisibility(View.GONE);
                } else {
                    product_information_Txt.setVisibility(View.VISIBLE);
                    product_information_Txt.setText(productInformation_STRng);
                    //product_information_Txt.setText(Html.fromHtml(productInformation_STRng, Html.FROM_HTML_MODE_COMPACT));
                }

                if (technical_details_STRng == null || technical_details_STRng.trim().isEmpty()) {
                    technical_details_Txt.setVisibility(View.GONE);
                } else {
                    technical_details_Txt.setVisibility(View.VISIBLE);
                    technical_details_Txt.setText(technical_details_STRng);
                    //technical_details_STRng.setText(Html.fromHtml(technical_details_STRng, Html.FROM_HTML_MODE_COMPACT));
                }

                // Check and set visibility for 'additionalInformation_STRng' and 'additional_information_Txt'
                if (additionalInformation_STRng == null || additionalInformation_STRng.trim().isEmpty()) {
                    additional_information_Txt.setVisibility(View.GONE);
                } else {
                    additional_information_Txt.setVisibility(View.VISIBLE);
                    additional_information_Txt.setText(additionalInformation_STRng);
                    //additional_information_Txt.setText(Html.fromHtml(additionalInformation_STRng, Html.FROM_HTML_MODE_COMPACT));
                }

                // Check and set visibility for 'product_details_STRng' and 'product_details_Txt'
                if (product_details_STRng == null || product_details_STRng.trim().isEmpty()) {
                    product_details_Txt.setVisibility(View.GONE);
                } else {
                    product_details_Txt.setVisibility(View.VISIBLE);
                    product_details_Txt.setText(product_details_STRng);
                    //product_details_Txt.setText(Html.fromHtml(result, Html.FROM_HTML_MODE_COMPACT));
                }

                if (productSpecifications_STRng == null || productSpecifications_STRng.trim().isEmpty()) {
                    productSpecifications_Txt.setVisibility(View.GONE);
                } else {
                    productSpecifications_Txt.setVisibility(View.VISIBLE);
                    productSpecifications_Txt.setText(productSpecifications_STRng);
                    //product_details_Txt.setText(Html.fromHtml(productSpecifications_STRng, Html.FROM_HTML_MODE_COMPACT));
                }


                Toast.makeText(ImageScraping.this, "Image Load Successful...", Toast.LENGTH_SHORT).show();
            } else {
                // All attempts failed
                Toast.makeText(ImageScraping.this, "Failed to retrieve data after multiple attempts.", Toast.LENGTH_LONG).show();
            }
        }

        private String extractData(String html, String regex) {
            try {
                // Trim and process the data
                productName_STRng = productName_STRng != null ? productName_STRng.replace("","").trim() : "Name not found";
                productPrice_STRng = productPrice_STRng != null ? productPrice_STRng.replace(",","").trim() : "Price not found";
                discount_STRng = discount_STRng != null ? discount_STRng.replace("-", "").replace("%", "").trim() : "Discount not found";
                mrpPrice_STRng = mrpPrice_STRng != null ? mrpPrice_STRng.replace(",","").trim() : "MRP not found";

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

        private String formatProductDetailsPlainText(String productdetails) {
            // Check if product details are empty
            if (productdetails == null || productdetails.isEmpty()) {
                return "Not Found";
            }

            // Regex to match <li> elements for key-value pairs
            Pattern liPattern = Pattern.compile("<li.*?>\\s*<span.*?>\\s*<span class=\"a-text-bold\">(.*?)</span>\\s*<span>(.*?)</span>\\s*</span>\\s*</li>", Pattern.DOTALL);
            Matcher liMatcher = liPattern.matcher(productdetails);

            // Build the formatted output
            StringBuilder formattedDetails = new StringBuilder();
            formattedDetails.append("-----------Product Details-----------\n");

            // Loop through each matched <li> and create bullet points
            while (liMatcher.find()) {
                String key = liMatcher.group(1).trim();
                String value = liMatcher.group(2).trim();

                // Add bullet point and key-value pair
                formattedDetails.append("• ").append(key).append(": ").append(value).append("\n");
            }

            formattedDetails.append("-------------------------------------\n");
            return formattedDetails.toString().trim();
        }


        private String formatAdditionalInformationPlainText(String additionalInformation) {
            // Check if additional information is empty
            if (additionalInformation == null || additionalInformation.isEmpty()) {
                return "Not Found";
            }

            // Regex to match the key-value pairs in the table
            Pattern liPattern = Pattern.compile("<tr>\\s*<th.*?>\\s*(.*?)\\s*</th>\\s*<td.*?>\\s*(.*?)\\s*</td>\\s*</tr>", Pattern.DOTALL);
            Matcher liMatcher = liPattern.matcher(additionalInformation);

            // Build the formatted output
            StringBuilder formattedDetails = new StringBuilder();
            formattedDetails.append("----------- Additional Information -----------\n");

            // Loop through each matched <tr> and create bullet points
            while (liMatcher.find()) {
                String key = liMatcher.group(1).trim();
                String value = liMatcher.group(2).trim();

                // Add bullet point and key-value pair
                formattedDetails.append("• ").append(key).append(": ").append(value).append("\n");
            }

            formattedDetails.append("---------------------------------------------\n");
            return formattedDetails.toString().trim();
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
            String[] paragraphs = cleanedText.split("(?<=\\.|\\|)"); // '.' or '|' Partition

            StringBuilder formattedText = new StringBuilder();

            // Define total width for alignment
            int totalWidth = 50; // Customize this width as needed
            String header = "------------- About This Item -------------";

            // Calculate padding for center alignment
            int padding = (totalWidth - header.length()) / 2;

            // Add spaces for center alignment and append the header
            formattedText.append(" ".repeat(Math.max(0, padding))); // Add spaces
            formattedText.append(header).append("\n");

            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    formattedText.append("• ").append(paragraph.trim()).append("\n\n");
                }
            }

            // Add footer
            String footer = "-------------------------------------";
            padding = (totalWidth - footer.length()) / 2; // Recalculate padding for footer
            formattedText.append(" ".repeat(Math.max(0, padding))); // Add spaces for footer alignment
            formattedText.append(footer).append("\n");

            return formattedText.toString().trim();

        }

        private String cleanHTMLTags(String html) {
            // HTML टैग्स हटाने के लिए Regex और &lrm; को हटाने के लिए
            String cleanedHtml = html.replaceAll("<[^>]*>", ""); // remove HTML
            cleanedHtml = cleanedHtml.replaceAll("&lrm;", ""); // remove &lrm;
            cleanedHtml = cleanedHtml.replaceAll("&rlm;", ""); // remove &rlm;
            cleanedHtml = cleanedHtml.replaceAll("       ", "");
            cleanedHtml = cleanedHtml.replaceAll("About this item", "");
            cleanedHtml = cleanedHtml.replaceAll("[;\\[\\]]", "");

            return cleanedHtml.trim();
        }
    }

}
