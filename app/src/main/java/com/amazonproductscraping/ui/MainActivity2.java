package com.amazonproductscraping.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity2";
    private boolean isSuccessful = false;
    private TextView name_Txt,price_Txt,discount_Txt,mrp_Txt,about_this_item_Txt,product_information_Txt,technical_details_Txt,additional_information_Txt,product_details_Txt,productSpecifications_Txt;
    private String amazonUrl_STRng,productName_STRng,discount_STRng,productPrice_STRng,mrpPrice_STRng,formattedText_STRng,aboutThisItem_STRng
            ,oprice_STRng,product_details_STRng,productInformation_STRng,additionalInformation_STRng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

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

        String amazonUrl = "https://www.amazon.in/Carlton-London-Women-Limited-Parfum/dp/B09MTR2HRP?th=1"; // आपका प्रोडक्ट URL
        new FetchAmazonDataTask().execute(amazonUrl);
    }

    private class FetchAmazonDataTask extends AsyncTask<String, Void, String> {

        private boolean isSuccessful = false; // Track if the data was successfully retrieved
        private static final int MAX_RETRY_COUNT = 2; // Maximum retry count

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
                        additionalInformation_STRng = extractAndFormatTableUsingJsoup(htmlContent.toString());//ok


                        product_details_STRng = extractData(htmlContent.toString(), "<ul class=\"a-unordered-list a-nostyle a-vertical a-spacing-none detail-bullet-list\">(.*?)</ul>");//1 to 5
                        product_details_STRng = formatProductDetailsPlainText(product_details_STRng);//2
                        product_details_STRng = product_details_STRng != null ? product_details_STRng.replace("&lrm;","").trim() : "Name not found";//3
                        product_details_STRng = product_details_STRng != null ? product_details_STRng.replace("&rlm;","").trim() : "Name not found";//4
                        product_details_STRng = product_details_STRng.replaceAll("\\s{2,}", " ");//5




                        // Retry if "Not Found" is returned for additional information
                        if (additionalInformation_STRng.equals("Not Found")) {
                            retryCount++;
                            Log.i(TAG, "Retrying... Attempt " + (retryCount + 1));
                            continue; // Retry fetching data
                        }

                        // If all data is successfully retrieved
                        if (productName_STRng != null || aboutThisItem_STRng != null || productInformation_STRng != null || product_details_STRng != null) {
                            isSuccessful = true; // Successfully retrieved data
                            result = formatProductData(productName_STRng, aboutThisItem_STRng, productInformation_STRng, product_details_STRng);
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

                if (product_details_STRng == null || product_details_STRng.trim().isEmpty()) {
                    //product_details_Txt.setVisibility(View.GONE);
                } else {
                    //product_details_Txt.setVisibility(View.VISIBLE);
                    //product_details_Txt.setText(product_details_STRng);
                }

                name_Txt.setText(productName_STRng);
                price_Txt.setText(productPrice_STRng);
                discount_Txt.setText(discount_STRng);
                mrp_Txt.setText(mrpPrice_STRng);
                //about_this_item_Txt.setText(aboutThisItem_STRng);
                //product_information_Txt.setText(productInformation_STRng);
                //additional_information_Txt.setText(additionalInformation_STRng);

                //technical_details_Txt.setText("Empty");
                //product_details_Txt.setText(product_details_STRng);

                //product_details_Txt.setText("product_details_Txt");
                //productDetails.setText(result);

                // Data retrieval successful
                Toast.makeText(MainActivity2.this, "Image Load Successful...", Toast.LENGTH_SHORT).show();
            } else {
                // All attempts failed
                Toast.makeText(MainActivity2.this, "Failed to retrieve data after multiple attempts.", Toast.LENGTH_LONG).show();
            }
        }

        private String formatProductData(String productName, String aboutThisItem, String productInformation, String productdetails) {

            StringBuilder formattedData = new StringBuilder();


            if (productdetails == null || productdetails.isEmpty()) {
                return "No Product Details Found";
            }

            // Regex for matching key-value pairs
            Pattern liPattern = Pattern.compile("<li.*?>\\s*<span.*?>\\s*<span class=\"a-text-bold\">(.*?)&rlm;.*?</span>\\s*<span>(.*?)</span>\\s*</span>\\s*</li>", Pattern.DOTALL);
            Matcher liMatcher = liPattern.matcher(productdetails);

            // Build the formatted HTML
            StringBuilder formattedDetails = new StringBuilder();
            formattedDetails.append("<h2 style='text-align:center;'>Product Details</h2>");
            formattedDetails.append("<ul style='list-style:none; padding:0;'>");

            while (liMatcher.find()) {
                String key = liMatcher.group(1).trim();
                String value = liMatcher.group(2).trim();

                // Add fat dot and key-value pair
                formattedDetails.append("<li style='margin-bottom:8px;'>")
                        .append("&#8226; <b>").append(key).append("</b>: ").append(value)
                        .append("</li>");
            }

            formattedDetails.append("</ul>");




            // Product Name
            /*formattedData.append("******************************\n");
            formattedData.append("Product Name:\n");
            formattedData.append("******************************\n");
            formattedData.append(productName != null ? productName.trim() : "Not Found");
            formattedData.append("\n\n");*/

            // About This Item
            /*formattedData.append("******************************\n");
            formattedData.append("About This Item:\n");
            formattedData.append("******************************\n");
            formattedData.append(aboutThisItem != null ? cleanHTMLTags(aboutThisItem.trim()) : "Not Found");
            formattedData.append("\n\n");*/



            // Product Information (Table)
            /*formattedData.append("******************************\n");
            formattedData.append("Product Information:\n");
            formattedData.append("******************************\n");
            formattedData.append(productInformation != null ? productInformation.trim() : "Not Found");
            formattedData.append("\n\n");*/

            // Additional Information
            /*formattedData.append("******************************\n");
            formattedData.append("Additional Information:\n");
            formattedData.append("******************************\n");
            formattedData.append(additionalInformation != null ? cleanHTMLTags(additionalInformation.trim()) : "Not Found");
            formattedData.append("\n\n");*/

            return formattedDetails.toString().trim();
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
                return "No Product Details Found";
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
            String cleanedHtml = html.replaceAll("<[^>]*>", ""); // हटाए HTML टैग्स
            cleanedHtml = cleanedHtml.replaceAll("&lrm;", ""); // हटाए &lrm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("&rlm;", ""); // हटाए &rlm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("       ", "");
            cleanedHtml = cleanedHtml.replaceAll("About this item", "");
            cleanedHtml = cleanedHtml.replaceAll("[;\\[\\]]", "");

            return cleanedHtml.trim(); // सफाई के बाद ट्रिम कर देना
        }
    }

}