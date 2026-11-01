package com.kg.macroanalyzer.shared.web;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// Shared by the getters so text and binary downloads open connections the same way
@Slf4j
final class HttpResponses {

    private HttpResponses() {
    }

    static int connect(HttpURLConnection connection) throws IOException {
        if (connection instanceof HttpsURLConnection httpsURLConnection) {
            applySSLTrustAll(httpsURLConnection);
        }

        return connection.getResponseCode();
    }

    static InputStream getInputStream(int status, HttpURLConnection connection) throws IOException {
        InputStream is = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (is == null) {
            // Fallback to inputStream if errorStream is null
            is = connection.getInputStream();
        }

        // Handle gzip compression if present
        final var contentEncoding = connection.getContentEncoding();
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(is);
        }
        return is;
    }

    static String readText(InputStream is) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append('\n');
            }
        }

        return responseBuilder.toString();
    }

    private static void applySSLTrustAll(HttpsURLConnection connection) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.warn("Failed to apply trust-all SSL context", e);
        }
    }

}
