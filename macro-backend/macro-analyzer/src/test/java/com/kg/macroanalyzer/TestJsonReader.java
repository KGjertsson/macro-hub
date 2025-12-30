package com.kg.macroanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.Stream;

public class TestJsonReader {

    public List<MacroSeries> readMacroSeriesList(String path) {
        return Stream.ofNullable(TestJsonReader.class.getClassLoader().getResource(path))
                .map(URL::getFile)
                .map(File::new)
                .flatMap(this::readMacroSeriesList)
                .toList();
    }

    private Stream<MacroSeries> readMacroSeriesList(File file) {
        final var mapper = new ObjectMapper();
        final var module = new SimpleModule();
        mapper.registerModule(new JavaTimeModule());
        module.addAbstractTypeMapping(Temporal.class, LocalDate.class);
        mapper.registerModule(module);
        try {
            final var classType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, MacroSeries.class);
            final List<MacroSeries> macroSeriesList = mapper.readValue(file, classType);

            return macroSeriesList.stream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
