package com.amazonproductscraping.ui;

import static android.webkit.URLUtil.isValidUrl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amazonproductscraping.ui.Adapter.ImageAdapter;
import com.amazonproductscraping.ui.Interface.OnGet_ItemListener;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button startButton,show;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText editText;
    private WebView webView;
    private EditText productTitleTextView;
    private TextView productPriceTextView;
    private TextView savingsPercentageTextView;
    private EditText mrpPriceTextView;
    private TextView productDetailsTextView;
    private TextView aboutThisItemTextView;
    private TextView additionalInfoTextView;
    private TextView productInfoTextView;
    private TextView importantInformationTextView;
    private String amazonUrl_STRng;
    private OnGet_ItemListener mListener;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView and TextViews
        startButton = findViewById(R.id.start);
        show = findViewById(R.id.show);
        webView = findViewById(R.id.webView);
        editText = findViewById(R.id.edittext);
        recyclerView = findViewById(R.id.recyclerView);
        productTitleTextView = findViewById(R.id.productTitleTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        savingsPercentageTextView = findViewById(R.id.savingsPercentageTextView);
        mrpPriceTextView = findViewById(R.id.mrpPriceTextView);
        productDetailsTextView = findViewById(R.id.productDetailsTextView);
        aboutThisItemTextView = findViewById(R.id.aboutThisItemTextView);
        additionalInfoTextView = findViewById(R.id.additionalInfoTextView);
        productInfoTextView = findViewById(R.id.productInfoTextView);
        importantInformationTextView = findViewById(R.id.importantInformationTextView);

        // Enable JavaScript in WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());


        startButton.setOnClickListener(v -> {
            String enteredUrl = editText.getText().toString().trim();

            webView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
            show.setVisibility(View.VISIBLE);

            if (isValidUrl(enteredUrl)) {
                amazonUrl_STRng = enteredUrl;
                scrapeImages(amazonUrl_STRng); // Start scraping
                // Start product data scraping
                Webview_Scraping(amazonUrl_STRng); // Start product data scraping

            } else {
                Toast.makeText(MainActivity.this, "Please enter a valid URL starting with http:// or https://", Toast.LENGTH_SHORT).show();
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
    /*private void scrapeImages(String urlString) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
                                if (imageUrls.size() >= 6) {
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
                            Toast.makeText(MainActivity.this, "No images found!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Loaded Images: " + imageUrls.size());
                            imageAdapter = new ImageAdapter(imageUrls,mListener);
                            recyclerView.setAdapter(imageAdapter);

                            Log.i(TAG, "Images Loaded: " + imageUrls);
                        }
                    });
                });

            }
        }, 5000); // 5000 milliseconds = 5 seconds
    }*/

    private void scrapeImages(String urlString) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                executorService.execute(() -> {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    List<String> imageUrls = new ArrayList<>();
                    List<String> imageNames = new ArrayList<>(); // To store filenames

                    try {
                        if (urlString.equals(urlString)) {
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

                                    // Extract image filename from URL
                                    String[] urlParts = imageUrl.split("/"); // Split by '/'
                                    String imageName = urlParts[urlParts.length - 1]; // Get the last part
                                    imageNames.add(imageName); // Add to the filenames list
                                }

                                // Limit the number of images to Mininum 6 And Maximum 12
                                if (imageUrls.size() >= 12) {
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
                            Toast.makeText(MainActivity.this, "No images found!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Loaded Images: " + imageUrls.size());

                            // Display filenames in the log (or use elsewhere)
                            for (String imageName : imageNames) {
                                Log.d(TAG, "Image Name: " + imageName);
                            }

                            // Update the RecyclerView with image URLs
                            imageAdapter = new ImageAdapter(imageUrls, mListener);
                            recyclerView.setAdapter(imageAdapter);

                            Log.i(TAG, "Images Loaded: " + imageUrls);
                        }
                    });
                });
            }
        }, 5000); // 5000 milliseconds = 5 seconds
    }
    private void Webview_Scraping(String amazonUrlStRng) {
        // Set custom User-Agent
        webView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // Add JavaScript interface to interact with JavaScript in the WebView
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void processContent(String title, String price, String savings, String mrp, String productDetails, String aboutThisItem,
                                       String additionalInfo, String productInfo,String importantInformation) {
                runOnUiThread(() -> {
                    if (title != null && !title.isEmpty()) {
                        productTitleTextView.setText(title);
                        Log.i(TAG, "productTitleTextView : "+title);
                    } else {
                        productTitleTextView.setText("Title not found");
                    }

                    if (price != null && !price.isEmpty()) {
                        String p = price.replace("₹","").replace(",","").trim();
                        productPriceTextView.setText(p);
                    } else {
                        productPriceTextView.setText("Price not found");
                    }

                    if (savings != null && !savings.isEmpty()) {
                        String s = savings.replace("-","").replace("%","").trim();
                        savingsPercentageTextView.setText(s);
                    } else {
                        savingsPercentageTextView.setText("Savings not found");
                    }

                    if (mrp != null && !mrp.isEmpty()) {
                        String m = mrp.replace(",","").trim();
                        mrpPriceTextView.setText(m);
                    } else {
                        mrpPriceTextView.setText("MRP not found");
                    }

                    if (productDetails != null && !productDetails.isEmpty()) {
                        productDetailsTextView.setText("Product Details: \n" + productDetails);
                    } else {
                        productDetailsTextView.setText("Product Details not found");
                    }

                    if (aboutThisItem != null && !aboutThisItem.isEmpty()) {
                        aboutThisItemTextView.setText("About This Item: \n" + aboutThisItem);
                    } else {
                        aboutThisItemTextView.setText("About This Item not found");
                    }

                    if (additionalInfo != null && !additionalInfo.isEmpty()) {
                        additionalInfoTextView.setText("Additional Information: \n" + additionalInfo);
                    } else {
                        additionalInfoTextView.setText("Additional Information not found");
                    }

                    if (productInfo != null && !productInfo.isEmpty()) {
                        productInfoTextView.setText("Product Information: \n" + productInfo);
                    } else {
                        productInfoTextView.setText("Product Information not found");
                    }

                    if (productInfo != null && !productInfo.isEmpty()) {
                        productInfoTextView.setText("Product Information: \n" + productInfo);

                        Log.e(TAG, "productInfoproductInfo : "+productInfo);
                    } else {
                        productInfoTextView.setText("Product Information not found");
                    }

                    if (importantInformation != null && !importantInformation.isEmpty()) {
                        importantInformationTextView.setText("Important Information: \n" + importantInformation);
                    } else {
                        importantInformationTextView.setText("Important Information not found");
                    }

                    Toast.makeText(MainActivity.this, "Scraping Complete", Toast.LENGTH_LONG).show();
                    webView.setVisibility(WebView.GONE); // Hide WebView
                });
            }
        }, "Android");

        // Load the Amazon product page

        webView.loadUrl(amazonUrlStRng);

        // Inject JavaScript after the page has finished loading with a delay
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);



                // Add a delay to ensure content is fully loaded
                new Handler().postDelayed(() -> {
                    webView.evaluateJavascript(
                            "(function() { " +
                                    "var titleElement = document.querySelector('span#productTitle.a-size-large.product-title-word-break');" +
                                    "var title = titleElement ? titleElement.innerText.trim() : 'Title not found';" +

                                    "var priceElement = document.querySelector('span.a-price.aok-align-center.reinventPricePriceToPayMargin.priceToPay span.a-price-whole');" +
                                    "var priceSymbol = document.querySelector('span.a-price-symbol');" +
                                    "var price = (priceSymbol && priceElement) ? priceSymbol.innerText.trim() + priceElement.innerText.trim() : 'Price not found';" +

                                    "var savingsElement = document.querySelector('span.savingPriceOverride.aok-align-center.reinventPriceSavingsPercentageMargin.savingsPercentage');" +
                                    "var savings = savingsElement ? savingsElement.innerText.trim() : 'Savings not found';" +

                                    // Extract MRP Price and Remove ₹ Symbol
                                    "var mrpParent = document.querySelector('div.a-section.a-spacing-small.aok-align-center');\n" +
                                    "var mrpElement = mrpParent ? mrpParent.querySelector('.a-price.a-text-price .a-offscreen') : null;\n" +
                                    "var mrp = mrpElement ? mrpElement.innerText.trim().replace('₹', '').trim() : 'MRP not found';" +

                                    // Extract Product Details, About This Item, and Additional Information
                                    "var productDetails = '-----------  Product Details  -----------\\n';\n" +
                                    "var productDetailsElements = document.querySelectorAll('.product-facts-detail');\n" +
                                    "\n" +
                                    "if (productDetailsElements.length === 0) {\n" +
                                    "    // Agar '.product-facts-detail' elements nahi milte, tab naya logic chalega\n" +
                                    "    var rows = document.querySelectorAll('table tbody tr');\n" +
                                    "\n" +
                                    "    if (rows.length === 0) {\n" +
                                    "        // Agar rows bhi nahi milte to 'Not Found' return kare\n" +
                                    "        productDetails += '•  Not Found\\n';\n" +
                                    "    } else {\n" +
                                    "        rows.forEach(function(row) {\n" +
                                    "            // Key span: <span class=\"a-size-base a-text-bold\">\n" +
                                    "            var key = row.querySelector('td.a-span3 span.a-size-base.a-text-bold');\n" +
                                    "            // Value span: <span class=\"a-size-base po-break-word\">\n" +
                                    "            var value = row.querySelector('td.a-span9 span.a-size-base.po-break-word');\n" +
                                    "\n" +
                                    "            if (key && value) {\n" +
                                    "                productDetails += '•  ' + key.innerText.trim() + ': ' + value.innerText.trim() + '\\n';\n" +
                                    "            }\n" +
                                    "        });\n" +
                                    "    }\n" +
                                    "} else {\n" +
                                    "    // Agar '.product-facts-detail' elements milte hain, yeh code chalega\n" +
                                    "    productDetailsElements.forEach(function(element) {\n" +
                                    "        var key = element.querySelector('.a-color-base'); // Get the key\n" +
                                    "        var value = element.querySelector('.a-color-base'); // Get the value\n" +
                                    "\n" +
                                    "        if (key && value) {\n" +
                                    "            productDetails += '•  ' + key.innerText.trim() + ': ' + value.innerText.trim() + '\\n';\n" +
                                    "        }\n" +
                                    "    });\n" +
                                    "}\n" +
                                    "\n" +
                                    "productDetails += '-------------------------------------\\n';" +


                                    //About This Item
                                    "var aboutThisItem = '--------About this item--------\\n\\n';\n" +
                                    "\n" +
                                    "// Correct selector ke liye '.a-unordered-list.a-vertical.a-spacing-small li' use karenge\n" +
                                    "var aboutThisItemElements = document.querySelectorAll('.a-unordered-list.a-vertical.a-spacing-small li');\n" +
                                    "\n" +
                                    "if (aboutThisItemElements.length === 0) {\n" +
                                    "    // Agar elements nahi milte to 'Not Found' return kare\n" +
                                    "    \n" +
                                    "    // Aapka dusra code jisme 'a-spacing-mini' selector use hota hai\n" +
                                    "    var aboutThisItemElementsMini = document.querySelectorAll('.a-unordered-list.a-vertical.a-spacing-mini li');\n" +
                                    "    \n" +
                                    "    if (aboutThisItemElementsMini.length === 0) {\n" +
                                    "        // Agar mini elements bhi nahi milte to 'Not Found' return kare\n" +
                                    "        aboutThisItem += '• Not Found in mini\\n';\n" +
                                    "    } else {\n" +
                                    "        aboutThisItemElementsMini.forEach(function(element) {\n" +
                                    "            var item = element.querySelector('.a-list-item'); // Yeh text ko fetch karega\n" +
                                    "            if (item) {\n" +
                                    "                aboutThisItem += '• ' + item.innerText.trim() + '\\n\\n'; // Add bullet point before each item with double line breaks\n" +
                                    "            }\n" +
                                    "        });\n" +
                                    "    }\n" +
                                    "} else {\n" +
                                    "    aboutThisItemElements.forEach(function(element) {\n" +
                                    "        var item = element.querySelector('.a-list-item'); // Yeh text ko fetch karega\n" +
                                    "        if (item) {\n" +
                                    "            aboutThisItem += '• ' + item.innerText.trim() + '\\n\\n'; // Add bullet point before each item with double line breaks\n" +
                                    "        }\n" +
                                    "    });\n" +
                                    "}\n" +
                                    "\n" +
                                    "aboutThisItem += '--------------------------------------\\n';" +



                                    // Scrape Additional Information
                                    "var additionalInfo = '--------- Additional Information ---------\\n\\n';\n" +
                                    "\n" +
                                    "// Select all the product-facts-detail elements inside the Additional Information section\n" +
                                    "var additionalInfoElements = document.querySelectorAll('.product-facts-detail');\n" +
                                    "\n" +
                                    "// Check if no elements are found\n" +
                                    "if (additionalInfoElements.length === 0) {\n" +
                                    "    // If no elements found, fallback to scrape from another section\n" +
                                    "\n" +
                                    "    // Select all the rows within the Additional Information table\n" +
                                    "    var additionalInfoRows = document.querySelectorAll('#productDetails_detailBullets_sections1 tr');\n" +
                                    "\n" +
                                    "    // Check if no rows are found\n" +
                                    "    if (additionalInfoRows.length === 0) {\n" +
                                    "        additionalInfo += '• Not Found\\n';\n" +
                                    "    } else {\n" +
                                    "        additionalInfoRows.forEach(function(row) {\n" +
                                    "            // Select the key (th) and value (td) for each row\n" +
                                    "            var keyElement = row.querySelector('th.a-color-secondary');\n" +
                                    "            var valueElement = row.querySelector('td.a-size-base, td');\n" +
                                    "\n" +
                                    "            // Check if both key and value exist, then add them to the output\n" +
                                    "            if (keyElement && valueElement) {\n" +
                                    "                // Clean and format the key and value for better readability\n" +
                                    "                var key = keyElement.innerText.trim();\n" +
                                    "                var value = valueElement.innerText.trim().replace(/\\n\\s*/g, ' ');\n" +
                                    "\n" +
                                    "                // Add key-value pair to the final result with an extra newline for spacing\n" +
                                    "                additionalInfo += '• ' + key + ': ' + value + '\\n\\n';\n" +
                                    "            }\n" +
                                    "        });\n" +
                                    "    }\n" +
                                    "} else {\n" +
                                    "    // If elements are found in the first section, proceed with scraping from it\n" +
                                    "    additionalInfoElements.forEach(function(element) {\n" +
                                    "        // Select the key (left column) and value (right column) for each product fact\n" +
                                    "        var keyElement = element.querySelector('.a-fixed-left-grid-col.a-col-left .a-color-base');  // Select key from left column\n" +
                                    "        var valueElement = element.querySelector('.a-fixed-left-grid-col.a-col-right .a-color-base');  // Select value from right column\n" +
                                    "\n" +
                                    "        // Check if both key and value exist, then add them to the output\n" +
                                    "        if (keyElement && valueElement) {\n" +
                                    "            // Clean and format the key and value for better readability\n" +
                                    "            var key = keyElement.innerText.trim();\n" +
                                    "            var value = valueElement.innerText.trim().replace(/\\n\\s*/g, ' ');  // Handle multiline values\n" +
                                    "\n" +
                                    "            // Add key-value pair to the final result with an extra newline for spacing\n" +
                                    "            additionalInfo += '• ' + key + ': ' + value + '\\n\\n';\n" +
                                    "        }\n" +
                                    "    });\n" +
                                    "}\n" +
                                    "\n" +
                                    "// Add a separator at the end\n" +
                                    "additionalInfo += '--------------------------------------------';" +


                                    // Extract Item Details (like Customer Reviews, ASIN, etc.)
                                    "var productInfo = '\\n-----------  Product Information  -----------\\n';\n" +
                                    "\n" +
                                    "// Scrape Customer Reviews\n" +
                                    "var reviewsElement = document.querySelector('span#acrPopover');\n" +
                                    "var customerReviews = reviewsElement ? reviewsElement.getAttribute('title').trim() : 'Customer Reviews not found';\n" +
                                    "productInfo += '•  Customer Reviews : ' + customerReviews + '\\n';\n" +
                                    "\n" +
                                    "// Scrape ASIN using Method 1\n" +
                                    "var asinElement = document.querySelector('[data-asin]');\n" +
                                    "var asin = asinElement ? asinElement.getAttribute('data-asin').trim() : 'ASIN not found';\n" +
                                    "productInfo += '•  ASIN             : ' + asin + '\\n';\n" +
                                    "\n" +
                                    "// Scrape Brand Name\n" +
                                    "var brandElement = document.querySelector('a#bylineInfo');\n" +
                                    "var brand = brandElement ? brandElement.innerText.replace('Visit the ', '').replace('Store', '').trim() : 'Brand not found';\n" +
                                    "productInfo += '•  Brand            : ' + brand + '\\n';\n" +
                                    "\n" +
                                    "productInfo += '------------------------------------------\\n';" +

                                    // Scrape Important Information
                                    "var importantInfoContainer = document.querySelector('#important-information');\n" +
                                    "var importantInformation = '';\n" +
                                    "\n" +
                                    "// Check if the container exists\n" +
                                    "if (importantInfoContainer) {\n" +
                                    "    // Get all sections within the container\n" +
                                    "    var sections = importantInfoContainer.querySelectorAll('.content');\n" +
                                    "\n" +
                                    "    // Loop through each section to extract heading and content\n" +
                                    "    sections.forEach(function (section) {\n" +
                                    "        // Extract the heading, if it exists\n" +
                                    "        var headingElement = section.querySelector('h4');\n" +
                                    "        var heading = headingElement ? `• ${headingElement.innerText.trim()}:\\n` : '';\n" +
                                    "\n" +
                                    "        // Generate an underline using \"=\"\n" +
                                    "        var underline = headingElement ? '='.repeat(headingElement.innerText.trim().length + 2) + '\\n' : '';\n" +
                                    "\n" +
                                    "        // Extract the content from paragraphs\n" +
                                    "        var content = '';\n" +
                                    "        var paragraphs = section.querySelectorAll('p');\n" +
                                    "\n" +
                                    "        paragraphs.forEach(function (paragraph) {\n" +
                                    "            var paragraphText = paragraph.innerText.trim();\n" +
                                    "            \n" +
                                    "            // Add a bullet point (•) before each paragraph if it has text\n" +
                                    "            if (paragraphText) {\n" +
                                    "                content += `  ${paragraphText}\\n`;\n" +
                                    "            }\n" +
                                    "        });\n" +
                                    "\n" +
                                    "        // Combine heading, underline, and content if content is not empty\n" +
                                    "        if (content.trim()) {\n" +
                                    "            importantInformation += `${heading}${underline}${content.trim()}\\n`;\n" +
                                    "        }\n" +
                                    "    });\n" +
                                    "}\n" +
                                    "\n" +
                                    "// Add a separator line at the end\n" +
                                    "importantInformation += '------------------------------------------\\n';"+


                                    "Android.processContent(title, price, savings, mrp, productDetails, aboutThisItem, additionalInfo, productInfo, importantInformation);" +
                                    "})()", null);
                }, 5000); // 5-second delay
            }
        });
    }
}