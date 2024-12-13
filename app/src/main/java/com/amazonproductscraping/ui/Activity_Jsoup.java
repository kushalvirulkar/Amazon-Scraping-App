package com.amazonproductscraping.ui;

import static android.webkit.URLUtil.isValidUrl;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class Activity_Jsoup extends AppCompatActivity {

    private static final String TAG = "Activity_Jsoup";

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

    private double calculateOriginalPrice(double discountedPrice, double discountPercentage) {
        // Formula to calculate original price
        return discountedPrice / (1 - (discountPercentage / 100));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_jsoup);


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


        amazonUrl_STRng = "https://www.amazon.in/Carlton-London-Women-Limited-Parfum/dp/B09MTR2HRP?th=1"; // Product URL
        //amazonUrl_STRng = editText.getText().toString(); // Product URL
        startWebScrapingJSoup(amazonUrl_STRng);

        startButton.setOnClickListener(v -> {
            String enteredUrl = editText.getText().toString().trim();

            editText.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
            show.setVisibility(View.VISIBLE);

            if (isValidUrl(enteredUrl)) {
                amazonUrl_STRng = enteredUrl;
                scrapeImages(amazonUrl_STRng); // Start scraping
                 // Start product data scraping
                //startWebScrapingHTML(amazonUrl_STRng); // Start product data scraping

            } else {
                Toast.makeText(Activity_Jsoup.this, "Please enter a valid URL starting with http:// or https://", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Activity_Jsoup.this, "No images found!", Toast.LENGTH_SHORT).show();
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

    private void startWebScrapingJSoup(String amazonUrlStr) {
        new Thread(() -> {
            try {
                // JSoup से HTML पेज को लोड करें
                doc = Jsoup.connect(amazonUrlStr)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.5938.92 Safari/537.36")
                        .referrer("https://www.google.com")
                        .timeout(10000)
                        .get();

                // "About this item" डेटा एक्स्ट्रेक्ट करें
                String aboutThisItem = extractAboutThisItem(doc);

                // "Additional Information" डेटा एक्स्ट्रेक्ट करें
                String additionalInfo = extractAdditionalInformation(doc);



                // "Product details" डेटा एक्स्ट्रेक्ट करें
                product_details_STRng = extractProductDetails(doc);

                // "Product information" डेटा एक्स्ट्रेक्ट करें
                String productInformation = extractProductDetailsFromUL(doc);

                // सभी डेटा को एक TextView में सेट करें
                String finalData = "About This Item:\n" + aboutThisItem + "\n\n" +
                        "Additional Information:\n" + additionalInfo + "\n\n" +
                        "Product Details:\n" + product_details_STRng + "\n\n" +
                        "Product Information:\n" + productInformation;

                runOnUiThread(() -> {
                    TextView combinedTextView = findViewById(R.id.combined_textview); // सुनिश्चित करें कि XML में TextView का ID "combined_textview" हो
                    combinedTextView.setText(finalData);
                });

            } catch (IOException e) {
                Log.e(TAG, "Error fetching data", e);
                runOnUiThread(() -> Toast.makeText(Activity_Jsoup.this, "Error loading data!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String extractAboutThisItem(Document doc) {
        // "About this item" के लिए सही सेलेक्टर

        Element aboutThisItemElement = doc.select("div.a-expander-content.a-expander-partial-collapse-content").first();

        if (aboutThisItemElement != null && !aboutThisItemElement.text().isEmpty()) {
            // Sabhi list items extract karein
            Elements listItems = aboutThisItemElement.select("ul.a-unordered-list li");

            // Check if listItems has data
            if (listItems.size() > 0) {
                // Extracted text ko format karein
                final StringBuilder aboutThisItemText = new StringBuilder();

                // Har item ke aage bullet point add karein
                for (Element item : listItems) {
                    aboutThisItemText.append("• ").append(item.text()).append("\n");
                }

                return aboutThisItemText.toString();

            } else {

                //Agar Upar wala code kaam naa kare to ye wala code.
                Element aboutThisItemElement1 = doc.select("div#feature-bullets").first();

                if (aboutThisItemElement1 != null) {
                    // Sabhi list items extract karein
                    Elements listItems1 = aboutThisItemElement1.select("ul.a-unordered-list li");

                    if (!listItems1.isEmpty()) {
                        // Extracted text ko format karein
                        final StringBuilder aboutThisItemText = new StringBuilder();

                        for (Element item : listItems1) {
                            // Extract text from each list item
                            String listItemText = item.select("span.a-list-item").text();
                            aboutThisItemText.append("• ").append(listItemText).append("\n");
                        }

                        return aboutThisItemText.toString();


                    } else {
                        Log.d("DEBUG", "listItems is empty.");
                    }
                } else {
                    Log.d("DEBUG", "aboutThisItemElement is null or not found.");
                }

            }
        } else {

        }

        return "About this item not found.";
    } //ok

    private String extractAdditionalInformation(Document doc) {
        // "Additional Information" के लिए सही सेलेक्टर
        Element additionalInfoElement = doc.selectFirst("h3.product-facts-title:contains(Additional Information)");

        // यदि "Additional Information" सेक्शन पाया जाता है
        if (additionalInfoElement != null) {
            Elements rows = additionalInfoElement.parent().select("div.a-fixed-left-grid.product-facts-detail");

            StringBuilder additionalText = new StringBuilder();

            // प्रत्येक रेकॉर्ड के लिए
            for (Element row : rows) {
                String key = row.select("div.a-col-left span.a-color-base").text();  // Key (जैसे Manufacturer, Packer, आदि)
                String value = row.select("div.a-col-right span.a-color-base").text();  // Value (जैसे H K JEWELS, etc.)

                if (!key.isEmpty() && !value.isEmpty()) {
                    additionalText.append(key).append(": ").append(value).append("\n");
                }
            }
            return additionalText.toString();
        }else {

            return extractAdditionalInformationWithRetry(doc, 2);
        }
        //return "Additional Information not found.";
    } //ok

    private String extractAdditionalInformationWithRetry(Document doc, int i) {
        String result = extractAdditionalInformationn(doc);

        // Retry logic: Agar first attempt mein "not found" mile, toh ek baar aur try karo
        int retryCount = 1;
        while (result.equals("No Additional Information Found") && retryCount < 2) {
            Log.d("Retry", "Retrying... Attempt " + (retryCount + 1));
            result = extractAdditionalInformation(doc); // Retry logic
            retryCount++;
        }

        return result;
    }

    private String extractAdditionalInformationn(Document doc) {
        // Extract additional information section using regex from the HTML content
        String htmlContent = doc.html();
        String additionalInformation_STRng = extractData(htmlContent, "<div id=\"productDetails_db_sections\".*?>(.*?)</div>");
        additionalInformation_STRng = formatAdditionalInformationPlainText(additionalInformation_STRng);
        additionalInformation_STRng = additionalInformation_STRng.replaceAll("<[^>]*>", ""); // Remove all HTML tags

        // Null check before replacing spaces
        if (product_details_STRng != null) {
            product_details_STRng = product_details_STRng.replaceAll("\\s{2,}", " "); // Clean up multiple spaces
        }

        return additionalInformation_STRng;
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
    private String formatAdditionalInformationPlainText(String additionalInformation) {
        // Check if additional information is empty
        if (additionalInformation == null || additionalInformation.isEmpty()) {
            return "No Additional Information Found";
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

    private String extractProductDetails(Document doc) {
        // "Product details" के लिए सेलेक्टर
        Element productDetailsElement = doc.selectFirst("h3.product-facts-title:contains(Product details)");

        // यदि "Product details" सेक्शन पाया जाता है
        if (productDetailsElement != null) {
            Elements rows = productDetailsElement.parent().select("div.a-fixed-left-grid.product-facts-detail");

            StringBuilder productDetailsText = new StringBuilder();

            // प्रत्येक रेकॉर्ड के लिए
            for (Element row : rows) {
                String key = row.select("div.a-col-left span.a-color-base").text();  // Key (जैसे Clasp type, Material type, आदि)
                String value = row.select("div.a-col-right span.a-color-base").text();  // Value (जैसे Lobster, Silver, आदि)

                if (!key.isEmpty() && !value.isEmpty()) {
                    productDetailsText.append(key).append(": ").append(value).append("\n");
                }
            }
            //return productDetailsText.toString();
        }
        return "Product details not found.";
    } //ok


    /*private String extractProductDetailsDiv(Document doc) {
        // "productDetails_feature_div" ko select karo
        Element productDetailsDiv = doc.getElementById("detailBullets_feature_div");


        // Null check karo
        if (productDetailsDiv != null) {
            String productDetailsText = productDetailsDiv.text();
            Log.d("ProductDetailsDiv", productDetailsText);
            return productDetailsText;
        } else {
            Log.d("Error", "Element 'productDetails_feature_div' not found");
            return "Product details section not found.";
        }
    }*/



    private String extractProductDetailsFromUL(Document doc) {
        // "ul" element ko select karne ke liye regex pattern
        String html = doc.html();
        Pattern pattern = Pattern.compile("<ul class=\"a-unordered-list a-nostyle a-vertical a-spacing-none detail-bullet-list\">(.*?)</ul>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        StringBuilder productDetails = new StringBuilder();

        // Agar match milta hai
        if (matcher.find()) {
            String ulContent = matcher.group(1);
            Document ulDoc = Jsoup.parse(ulContent);
            Elements listItems = ulDoc.select("li");

            // Har "li" ke text ko fetch karo
            for (Element li : listItems) {
                productDetails.append(li.text()).append("\n");
            }

            return productDetails.toString();
        } else {
            return "Product details not found.";
        }
    }


}