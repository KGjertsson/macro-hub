package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ZipEntryGetterTest {

    @Mock
    HttpURLConnection connection;

    @Test
    void shouldReturnMatchingEntry_andSkipOthers() throws Exception {
        // given
        final var zip = zip("Val_2026_slutlig_rostfordelning_1281_KF.json", "{\"votes\":1}",
                "Val_2026_slutlig_mandatfordelning_1281_KF.json", "{\"seats\":65}");
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(zip));

        // when
        final var entry = new ZipEntryGetter("mandatfordelning").get(connection);

        // then
        assertEquals("{\"seats\":65}", entry);
    }

    @Test
    void shouldThrow_whenNoEntryMatches() throws Exception {
        // given
        final var zip = zip("Val_2026_slutlig_rostfordelning_1281_KF.json", "{}");
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(zip));

        // then
        assertThrows(ScrapeException.class, () -> new ZipEntryGetter("mandatfordelning").get(connection));
    }

    @Test
    void shouldThrow_whenUpstreamRateLimits() throws Exception {
        // given
        when(connection.getResponseCode()).thenReturn(429);
        when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream("slow down".getBytes(StandardCharsets.UTF_8)));

        // then
        final var exception = assertThrows(ScrapeException.class, () -> new ZipEntryGetter("mandatfordelning").get(connection));
        assertEquals("HTTP 429 from upstream: slow down\n", exception.getMessage());
    }

    private static byte[] zip(String... namesAndContents) throws IOException {
        final var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes)) {
            for (int i = 0; i < namesAndContents.length; i += 2) {
                zip.putNextEntry(new ZipEntry(namesAndContents[i]));
                zip.write(namesAndContents[i + 1].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }

        return bytes.toByteArray();
    }

}
