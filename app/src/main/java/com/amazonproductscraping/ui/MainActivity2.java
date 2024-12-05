package com.amazonproductscraping.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

    private TextView productDetails;
    private static final String TAG = "MainActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        productDetails = findViewById(R.id.productDetails);

        String amazonUrl = "https://www.amazon.in/Carlton-London-Women-Limited-Parfum/dp/B09MTR2HRP"; // आपका प्रोडक्ट URL
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
                connection.setRequestProperty("User-Agent", "Chrome/117.0.5938.92 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
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
                    String aboutThisItem = extractData(htmlContent.toString(), "<div id=\"feature-bullets\".*?>(.*?)</div>");
                    String productInformation = extractAndFormatTable(htmlContent.toString(), "<table id=\"productDetails_techSpec_section_1\".*?>(.*?)</table>");
                  //String additionalInformation = extractAndFormatTable(htmlContent.toString(), "<div id=\"productDetails_db_sections\".*?>(.*?)</div>");
                    // additionalInformation = extractAndFormatTable(htmlContent.toString(), "<div id=\"productDetails_db_sections\"[^>]*>(.*?)</div>");
                    String additionalInformation = extractAndFormatTableUsingJsoup(htmlContent.toString());


                    String productDetailsSection = extractData(htmlContent.toString(), "<div id=\"detailBulletsWrapper_feature_div\".*?>(.*?)</div>"); // Added this line


                    // "About This Item" को साफ करें और पैराग्राफ से पहले डॉट जोड़ें
                    aboutThisItem = aboutThisItem != null ? formatAboutThisItem(aboutThisItem) : "Not Found";
                    productDetailsSection = productDetailsSection != null ? formatProductDetailsSection(productDetailsSection) : "Not Found";




                    // "About This Item" को साफ करें
                    aboutThisItem = aboutThisItem != null ? cleanHTMLTags(aboutThisItem) : "Not Found";
                    additionalInformation = additionalInformation != null ? cleanHTMLTags(additionalInformation) : "Not Found";
                    productDetailsSection = productDetailsSection != null ? cleanHTMLTags(productDetailsSection) : "Not Found"; // Clean productDetailsSection

                    // फॉर्मेटेड डेटा रिटर्न करें
                    return formatProductData(productName, productPrice, aboutThisItem, productInformation, additionalInformation, productDetailsSection);
                } else {
                    return "Failed to fetch data. Response code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            productDetails.setText(result);
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

        private String formatProductData(String productName, String productPrice, String aboutThisItem, String productInformation, String additionalInformation, String productDetailsSection) {
            StringBuilder formattedData = new StringBuilder();

            // Product Name
            formattedData.append("******************************\n");
            formattedData.append("Product Name:\n");
            formattedData.append("******************************\n");
            formattedData.append(productName != null ? productName.trim() : "Not Found");
            formattedData.append("\n\n");

            // Product Price
            formattedData.append("******************************\n");
            formattedData.append("Price:\n");
            formattedData.append("******************************\n");
            formattedData.append(productPrice != null ? productPrice.trim() : "Not Found");
            formattedData.append("\n\n");

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


        private String formatProductDetailsSection(String productDetailsHtml) {
            // HTML टैग्स साफ करें
            String cleanedText = cleanHTMLTags(productDetailsHtml);

            // डेटा को पैराग्राफ के आधार पर विभाजित करें (यहां हम "\n" के बाद हर पैराग्राफ को अलग करेंगे)
            String[] details = cleanedText.split("\\r?\\n"); // नई लाइनों पर विभाजित

            // फॉर्मेटेड टेक्स्ट तैयार करें
            StringBuilder formattedText = new StringBuilder();
            formattedText.append("******************************\n");
            formattedText.append("Product Details:\n");
            formattedText.append("******************************\n");

            // हर पैराग्राफ के पहले डॉट जोड़ें
            for (String detail : details) {
                if (!detail.trim().isEmpty()) { // अगर पैराग्राफ खाली नहीं है
                    formattedText.append("• ").append(detail.trim()).append("\n");
                }
            }

            // फॉर्मेटेड टेक्स्ट को रिटर्न करें
            return formattedText.toString().trim(); // अंत में सफाई करें
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




}
