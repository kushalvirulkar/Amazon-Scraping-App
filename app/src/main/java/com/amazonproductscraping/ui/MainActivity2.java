package com.amazonproductscraping.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {

    private TextView productDetails;
    private static final String TAG = "MainActivity2";
    private String productName,aboutThisItem,additionalInformation,productDescription ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        productDetails = findViewById(R.id.productDetails);

        String amazonUrl = "https://www.amazon.in/ZENEME-Rhodium-Plated-Silver-Toned-Zirconia-Jewellery/dp/B0BQJL99KR/ref=sr_1_11?dib=eyJ2IjoiMSJ9.ulzDE9OweG7vcETL5c2FqRFRH6ithf_fdEFvVjUA2uT-hRVazOWz65rE16C43Jc8SxaznT_-RW-F9wbfQia8Gi-nOK7NVd8gPKLwwqEo6icvOPesFe_o-Ua1E0904hmzS_LFsQpzKubtI31AvCsn84n5vga4cTsP2H4Ng_9fAiUWHvI9-QJjStoEXjPm2DHro5gCbNVDNo8ZO6qc-UPXkJbU_KBnhRESfwmCEKo0bpxr9qLK1knzWNzWxnZGcCpP4th7rLnhD9QSyQOW4-LrYSKfDhqlYx9hALQju0VUNxc.lzzDC9foMrGkhGdM7t2H0FCx7JRiXIORehEVairDcTI&dib_tag=se&keywords=jewellery+for+women&qid=1733335148&sr=8-11"; //URL
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
                    productName = extractData(htmlContent.toString(), "<span id=\"productTitle\".*?>(.*?)</span>");
                    String productPrice = extractData(htmlContent.toString(), "<span id=\"priceblock_ourprice\".*?>(.*?)</span>");


                    aboutThisItem = extractData(htmlContent.toString(), "<div id=\"feature-bullets\".*?>(.*?)</div>");

                    // Extracting list items under "About this item" ye code jewellery search karenge to kaam karega.
                    aboutThisItem = extractData(htmlContent.toString(), "<ul class=\"a-unordered-list a-vertical a-spacing-small\">(.*?)</ul>");




                    String productInformation = extractAndFormatTable(htmlContent.toString(), "<table id=\"productDetails_techSpec_section_1\".*?>(.*?)</table>");
                    additionalInformation = extractAndFormatTable(htmlContent.toString(), "<div id=\"productDetails_db_sections\".*?>(.*?)</div>");


                    // Check if additionalInformation is being captured
                    //additionalInformation = extractData(htmlContent.toString(), "<div id=\"productDetails_db_sections\".*?>(.*?)</div>");

                    // Clean and format if it exists
                    additionalInformation = additionalInformation != null ? cleanHTMLTags(additionalInformation) : "Not Found";
                    aboutThisItem = aboutThisItem != null ? cleanHTMLTags(aboutThisItem) : "Not Found";



                    // Product Description extraction
                    productDescription = extractData(htmlContent.toString(), "<div id=\"productDescription\".*?>(.*?)</div>");
                    productDescription = productDescription != null ? cleanHTMLTags(productDescription) : "Not Found";





                    String productDetailsSection = extractData(htmlContent.toString(), "<div id=\"detailBulletsWrapper_feature_div\".*?>(.*?)</div>"); // Added this line

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

            // Product Details Section
            formattedData.append("******************************\n");
            formattedData.append("Product Details:\n");
            formattedData.append("******************************\n");
            formattedData.append(productDetailsSection != null ? cleanHTMLTags(productDetailsSection.trim()) : "Not Found");
            formattedData.append("\n\n");

            // Product Description
            formattedData.append("******************************\n");
            formattedData.append("Product Description:\n");
            formattedData.append("******************************\n");
            formattedData.append(productDescription != null ? productDescription.trim() : "Not Found");
            formattedData.append("\n\n");

            return formattedData.toString();
        }


        private String cleanHTMLTags(String html) {
            // HTML टैग्स हटाने के लिए Regex और &lrm; को हटाने के लिए
            String cleanedHtml = html.replaceAll("<[^>]*>", ""); // हटाए HTML टैग्स
            cleanedHtml = cleanedHtml.replaceAll("&lrm;", ""); // हटाए &lrm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("&rlm;", ""); // हटाए &rlm; से जुड़ी स्ट्रिंग
            cleanedHtml = cleanedHtml.replaceAll("       ", "");
            return cleanedHtml.trim(); // सफाई के बाद ट्रिम कर देना
        }

        /*private String formatProductData(String productName, String productPrice, String aboutThisItem, String productInformation, String additionalInformation, String productDetailsSection) {
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
            formattedData.append(aboutThisItem != null ? aboutThisItem.trim() : "Not Found");
            formattedData.append("\n\n");

            // Product Information
            formattedData.append("******************************\n");
            formattedData.append("Product Information:\n");
            formattedData.append("******************************\n");
            formattedData.append(productInformation != null ? productInformation.trim() : "Not Found");
            formattedData.append("\n\n");

            // Additional Information
            formattedData.append("******************************\n");
            formattedData.append("Additional Information:\n");
            formattedData.append("******************************\n");
            formattedData.append(additionalInformation != null ? additionalInformation.trim() : "Not Found");
            formattedData.append("\n\n");

            // Product Details
            formattedData.append("******************************\n");
            formattedData.append("Product Details:\n");
            formattedData.append("******************************\n");
            formattedData.append(productDetailsSection != null ? productDetailsSection.trim() : "Not Found");
            formattedData.append("\n\n");

            return formattedData.toString();
        }*/
    }


}
