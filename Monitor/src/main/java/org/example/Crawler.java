package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Crawler {
    List<String> allLines = new ArrayList<>();
    List<String> delayedLines = new ArrayList<>();

    public List<String> getAllLines() {
        return allLines;
    }

    public List<String> getDelayedLines() {
        return delayedLines;
    }

    public void crawl() {
        try {
            // Create a URL object with the MTA url
            URL url = new URL("https://new.mta.info/");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the connection properties
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000); // Set a reasonable timeout

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the content of the page
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder content = new StringBuilder();
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine());
                }
                scanner.close();

                //Parse the HTML content using jsoup
                Document document = Jsoup.parse(content.toString());

                // Extract and process the content using jsoup selectors
                Element subwayElement = document.getElementById("tab-subway");

                if (subwayElement != null) {
                    Elements innerElements = subwayElement.getAllElements();
                    for (Element innerElement : innerElements) {
                        allLines.add( innerElement.text() );

                        if (innerElement.text().equals( "Delays" )) {
                            delayedLines.add( innerElement.text() );
                        }
                    }
                }
            } else {
                System.out.println("Failed to retrieve content. Response code: " + responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}