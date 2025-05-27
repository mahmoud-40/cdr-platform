package com.cdr.msloader.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
public class CDRParserFactory {
    private final List<CDRParser> parsers;

    @Autowired
    public CDRParserFactory(List<CDRParser> parsers) {
        this.parsers = parsers;
    }

    public CDRParser getParser(File file) {
        return parsers.stream()
                .filter(parser -> parser.canHandle(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parser found for file: " + file.getName()));
    }
} 