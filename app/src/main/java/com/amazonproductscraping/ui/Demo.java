package com.amazonproductscraping.ui;

import static android.webkit.URLUtil.isValidUrl;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private Button startButton,show;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText editText;
    private WebView webView;
    private TextView productPriceTextView;
    private TextView savingsPercentageTextView;
    private EditText mrpPriceTextView;
    private String amazonUrl_STRng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        // Initialize WebView and TextViews
        // Initialize WebView and TextViews
        startButton = findViewById(R.id.start);
        show = findViewById(R.id.show);
        webView = findViewById(R.id.webView);
        editText = findViewById(R.id.edittext);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        savingsPercentageTextView = findViewById(R.id.savingsPercentageTextView);
        mrpPriceTextView = findViewById(R.id.mrpPriceTextView);

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
                // Start product data scraping
                Webview_Scraping(amazonUrl_STRng); // Start product data scraping

            } else {
                Toast.makeText(Demo.this, "Please enter a valid URL starting with http:// or https://", Toast.LENGTH_SHORT).show();
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
    }


    private void Webview_Scraping(String amazonUrlStRng) {
        // Set custom User-Agent
        webView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // Add JavaScript interface to interact with JavaScript in the WebView
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void processContent( String price, String savings, String mrp) {
                runOnUiThread(() -> {
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

                    Toast.makeText(Demo.this, "Scraping Complete", Toast.LENGTH_LONG).show();
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

                // javascript Add a delay to ensure content is fully loaded
                new Handler().postDelayed(() -> {
                    webView.evaluateJavascript(
                            "(function() { " +

                                    "var priceElement = document.querySelector('span.a-price.aok-align-center.reinventPricePriceToPayMargin.priceToPay span.a-price-whole');\n" +
                                    "var priceSymbol = document.querySelector('span.a-price-symbol');\n" +
                                    "var price = (priceSymbol && priceElement) ? priceSymbol.innerText.trim().replace('₹', '').replace(',', '') + priceElement.innerText.trim().replace(',', '') : 'Price not found';\n" +

                                    "var savingsElement = document.querySelector('span.savingPriceOverride.aok-align-center.reinventPriceSavingsPercentageMargin.savingsPercentage');" +
                                    "var savings = savingsElement ? savingsElement.innerText.trim() : 'Savings not found';" +

                                    // Extract MRP Price and Remove ₹ Symbol
                                    "var mrpParent = document.querySelector('div.a-section.a-spacing-small.aok-align-center');\n" +
                                    "var mrpElement = mrpParent ? mrpParent.querySelector('.a-price.a-text-price .a-offscreen') : null;\n" +
                                    "var mrp = mrpElement ? mrpElement.innerText.trim().replace('₹', '').trim() : 'MRP not found';" +

                                    "Android.processContent(price, savings, mrp);" +
                                    "})()", null);
                }, 500); // 5-second delay
            }
        });
    }
}