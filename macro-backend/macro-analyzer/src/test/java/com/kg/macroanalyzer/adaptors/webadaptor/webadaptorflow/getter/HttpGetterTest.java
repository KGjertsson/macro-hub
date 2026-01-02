package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HttpGetterTest {

    private HttpGetter httpGetter;

    @Mock
    private HttpURLConnection mockConnection;

    @Mock
    private HttpsURLConnection mockHttpsConnection;

    @BeforeEach
    void setUp() {
        httpGetter = new HttpGetter();
    }

    @Test
    void shouldReturnResponseString_whenResponseIsSuccessful() throws IOException, ScrapeException {
        // given
        String expectedResponse = "success response";
        InputStream inputStream = new ByteArrayInputStream(expectedResponse.getBytes(StandardCharsets.UTF_8));

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        // when
        String result = httpGetter.get(mockConnection);

        // then
        assertEquals(expectedResponse + "\n", result);
        verify(mockConnection).disconnect();
    }

    @Test
    void shouldReturnResponseString_whenResponseIsGzipped() throws IOException, ScrapeException {
        // given
        String expectedResponse = "gzipped response";
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
            gzip.write(expectedResponse.getBytes(StandardCharsets.UTF_8));
        }
        InputStream inputStream = new ByteArrayInputStream(obj.toByteArray());

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentEncoding()).thenReturn("gzip");
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        // when
        String result = httpGetter.get(mockConnection);

        // then
        assertEquals(expectedResponse + "\n", result);
        verify(mockConnection).disconnect();
    }

    @Test
    void shouldThrowScrapeException_whenResponseIsError() throws IOException {
        // given
        String errorResponse = "error occurred";
        InputStream errorStream = new ByteArrayInputStream(errorResponse.getBytes(StandardCharsets.UTF_8));

        when(mockConnection.getResponseCode()).thenReturn(404);
        when(mockConnection.getErrorStream()).thenReturn(errorStream);

        // when && then
        ScrapeException exception = assertThrows(ScrapeException.class, () -> httpGetter.get(mockConnection));
        assertEquals("HTTP 404 from upstream: " + errorResponse + "\n", exception.getMessage());
        verify(mockConnection).disconnect();
    }

    @Test
    void shouldFallbackToInputStream_whenErrorStreamIsNull() throws IOException {
        // given
        String errorResponse = "error fallback";
        InputStream inputStream = new ByteArrayInputStream(errorResponse.getBytes(StandardCharsets.UTF_8));

        when(mockConnection.getResponseCode()).thenReturn(500);
        when(mockConnection.getErrorStream()).thenReturn(null);
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        // when && then
        ScrapeException exception = assertThrows(ScrapeException.class, () -> httpGetter.get(mockConnection));
        assertEquals("HTTP 500 from upstream: " + errorResponse + "\n", exception.getMessage());
        verify(mockConnection).disconnect();
    }

    @Test
    void shouldApplySSLTrustAll_whenConnectionIsHttps() throws IOException, ScrapeException {
        // given
        String expectedResponse = "https success";
        InputStream inputStream = new ByteArrayInputStream(expectedResponse.getBytes(StandardCharsets.UTF_8));

        when(mockHttpsConnection.getResponseCode()).thenReturn(200);
        when(mockHttpsConnection.getInputStream()).thenReturn(inputStream);

        // when
        String result = httpGetter.get(mockHttpsConnection);

        // then
        assertEquals(expectedResponse + "\n", result);
        verify(mockHttpsConnection).setSSLSocketFactory(any());
        verify(mockHttpsConnection).setHostnameVerifier(any());
        verify(mockHttpsConnection).disconnect();
    }

    @Test
    void shouldThrowScrapeException_whenIOExceptionOccurs() throws IOException {
        // given
        when(mockConnection.getResponseCode()).thenThrow(new IOException("Connection failed"));

        // when && then
        ScrapeException exception = assertThrows(ScrapeException.class, () -> httpGetter.get(mockConnection));
        assertEquals("Connection failed", exception.getMessage());
    }
}
