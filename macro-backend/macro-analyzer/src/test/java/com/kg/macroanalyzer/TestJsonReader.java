package com.kg.macroanalyzer;

import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.io.File;
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
