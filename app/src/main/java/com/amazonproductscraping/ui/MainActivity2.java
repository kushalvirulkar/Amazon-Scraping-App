package com.amazonproductscraping.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {

    private EditText productDetails;
    private String formattedText;


    //iss code se systmatic tarike se "About this item" ka data retrive karta hai sahi hai. ok
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        productDetails = findViewById(R.id.productDetails);

        String amazonUrl = "https://www.amazon.in/dp/B0CBTTCJL6"; // प्रोडक्ट URL
        new FetchAmazonDataTask().execute(amazonUrl);
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
                    String productName = extractData(htmlContent.toString(), "<span id=\"productTitle\".*?>(.*?)</span>");
                    String productPrice = extractData(htmlContent.toString(), "<span id=\"priceblock_ourprice\".*?>(.*?)</span>");
                    //String aboutThisItem = extractData(htmlContent.toString(), "<div id=\"feature-bullets\".*?>(.*?)</div>");

                    String aboutThisItem = extractAndFormatAboutThisItem(htmlContent.toString());

                    formattedText = String.format("Product Name: %s\nPrice: %s\nAbout This Item:\n\n%s",
                            productName, productPrice, aboutThisItem);




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
            productDetails.setText(formattedText);
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



}
