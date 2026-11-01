package com.kg.macroanalyzer;

import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.Stream;

public class TestJsonReader {

    public String readResource(String path) {
        try (InputStream is = TestJsonReader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing test resource " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<MacroSeries> readMacroSeriesList(String path) {
        return Stream.ofNullable(TestJsonReader.class.getClassLoader().getResource(path))
                .map(URL::getFile)
                .map(File::new)
                .flatMap(this::readMacroSeriesList)
                .toList();
    }

    private Stream<MacroSeries> readMacroSeriesList(File file) {
        final var module = new SimpleModule();
        module.addAbstractTypeMapping(Temporal.class, LocalDate.class);
        final var mapper = JsonMapper.builder()
                .addModule(module)
                .build();
        final var classType = mapper.getTypeFactory()
                .constructCollectionType(List.class, MacroSeries.class);
        final List<MacroSeries> macroSeriesList = mapper.readValue(file, classType);

        return macroSeriesList.stream();
    }

}
