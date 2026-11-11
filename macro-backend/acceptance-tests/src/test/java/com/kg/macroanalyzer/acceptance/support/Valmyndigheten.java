package com.kg.macroanalyzer.acceptance.support;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class Valmyndigheten {

    private static final String ELECTION_FOLDER = "/resultatfiler/val2026";

    public static final String INDEX = ELECTION_FOLDER + "/index.md5";

    public static void publish(Sources sources, ResultFile... files) {
        Stream.of(files).forEach(file -> sources.serveBytes(file.url(), file.zip()));
        sources.serveText(INDEX, Stream.of(files)
                .map(ResultFile::indexLine)
                .collect(Collectors.joining("\n", "", "\n")));
    }

    public record ResultFile(String relativePath, byte[] zip) {

        public static ResultFile of(
                String electionType, String area, String mandatfordelningFixture) {
            final var file = "Val_2026_slutlig_%s_%s".formatted(area, electionType);

            return new ResultFile(
                    "s/%s/%s.zip".formatted(electionType.toLowerCase(Locale.ROOT), file),
                    zipOf(file, Sources.readFixture(mandatfordelningFixture)));
        }

        public String url() {
            return "%s/%s".formatted(ELECTION_FOLDER, relativePath);
        }

        private String indexLine() {
            return "%s  ./%s".formatted(md5(zip), relativePath);
        }

        // The real zips hold the per-district votes too, and the service has to pick the right
        // entry. They come first here so that picking the first entry would fail, as this one isn't
        // a result file
        private static byte[] zipOf(String file, String mandatfordelning) {
            final var bytes = new ByteArrayOutputStream();
            try (var zip = new ZipOutputStream(bytes)) {
                writeEntry(zip, "%s_rostfordelning.json".formatted(file), "not a result file");
                writeEntry(zip, "%s_mandatfordelning.json".formatted(file), mandatfordelning);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return bytes.toByteArray();
        }

        private static void writeEntry(ZipOutputStream zip, String name, String content)
                throws IOException {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        private static String md5(byte[] content) {
            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(content));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

    }

}
