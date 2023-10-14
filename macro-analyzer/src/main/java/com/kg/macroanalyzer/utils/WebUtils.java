package com.kg.macroanalyzer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class WebUtils {

    public static String getHTTP(String endpointUrl) {
        try {
            final var url = new URI(endpointUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // Get the response as a string
            String response = responseBuilder.toString();

            // Close the connection
            connection.disconnect();

            return response;
        } catch (IOException | URISyntaxException ioException) {
            throw new RuntimeException(ioException.getMessage());
        }
    }

}
