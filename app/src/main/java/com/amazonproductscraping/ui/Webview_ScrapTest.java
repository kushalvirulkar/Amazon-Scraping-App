package com.amazonproductscraping.ui;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Webview_ScrapTest extends AppCompatActivity {

    private static final String TAG = "Webview_Scrap";

    private WebView webView;
    private TextView productTitleTextView;
    private TextView productPriceTextView;
    private TextView savingsPercentageTextView;
    private TextView mrpPriceTextView;
    private TextView productDetailsTextView;
    private TextView aboutThisItemTextView;
    private TextView additionalInfoTextView;
    private TextView productInfoTextView;
    private TextView importantInformationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_scrap_test);

        // Initialize WebView and TextViews
        webView = findViewById(R.id.webView);
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
                        productTitleTextView.setText("Title: " + title);
                    } else {
                        productTitleTextView.setText("Title not found");
                    }

                    if (price != null && !price.isEmpty()) {
                        productPriceTextView.setText("Price: " + price);
                    } else {
                        productPriceTextView.setText("Price not found");
                    }

                    if (savings != null && !savings.isEmpty()) {
                        savingsPercentageTextView.setText("Savings: " + savings);
                    } else {
                        savingsPercentageTextView.setText("Savings not found");
                    }

                    if (mrp != null && !mrp.isEmpty()) {
                        mrpPriceTextView.setText("MRP: " + mrp);
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

                    Toast.makeText(Webview_ScrapTest.this, "Scraping Complete", Toast.LENGTH_LONG).show();
                    webView.setVisibility(WebView.GONE); // Hide WebView
                });
            }
        }, "Android");

        // Load the Amazon product page
        String productUrl = "https://www.amazon.in/elegante-Branded-Lightweight-Sunglasses-Silver-Black/dp/B089FNHYND/ref=sr_1_6?crid=1JGTIPZ2WIEZ5&dib=eyJ2IjoiMSJ9.JVGewI2l8TKd6x6PM5I_VDbKPkcW02YQgSKfW8PgJbRK86WHRzvYWxOx5B7TKha48iKZUathyUfjGhFFGdpz5xgHUiWJCPBUFernys9jK_oVmP1GJoE3yh3E4Tc86EK6E9brFeqnS9tleUDlqKqpqg-50M-VZJytQtSNfoD3c3bPzTEP0kKDHm-ErZZvkmaQf7SO210oCCzrO_xADYkgj90Y3kbsBxadAxhwXAWWQovC3o_JA61PdZLSIhqggxgAky59b-PX4ffzGPuVFjzDC4wZhu5Y4oSQavlKreAY4Zc.hAwOeeqcVuavbcowZdFbFnvxgOJ5kB5L21I790w46lk&dib_tag=se&keywords=sunglasses+for+men+stylish&qid=1734090067&sprefix=sungl%2Caps%2C310&sr=8-6"; // Example Amazon product URL
        webView.loadUrl(productUrl);

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
                                    "var mrpElement = document.querySelector('span.a-price.a-text-price span.a-offscreen');" +
                                    "var mrp = mrpElement ? mrpElement.innerText.trim().replace('M.R.P.: ₹', '').replace('₹', '') : 'MRP not found';" +

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
