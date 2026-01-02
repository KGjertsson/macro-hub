package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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

@Slf4j
public class HttpGetter implements Getter<HttpURLConnection, String> {

    private static InputStream getInputStream(int status, HttpURLConnection connection) throws IOException {
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

    @Override
    public String get(HttpURLConnection connection) throws ScrapeException {
        try {
            if (connection instanceof HttpsURLConnection httpsURLConnection) {
                applySSLTrustAll(httpsURLConnection);
            }

            final int status = connection.getResponseCode();

            InputStream is = getInputStream(status, connection);

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line).append('\n');
                }
            }

            final String response = responseBuilder.toString();

            // Close the connection
            connection.disconnect();

            if (status >= 400) {
                throw new ScrapeException("HTTP " + status + " from upstream: " + response);
            }

            return response;
        } catch (IOException exception) {
            throw new ScrapeException(exception.getMessage());
        }
    }

}
