package com.cdr.msloader.parser;

import com.cdr.msloader.dto.CdrDto;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CDRParser {
    /**
     * Parse a file and convert its contents to a list of CdrDto objects
     * @param file The file to parse
     * @return List of parsed CdrDto objects
     * @throws IOException if parsing fails
     */
    List<CdrDto> parse(File file) throws IOException;

    /**
     * Check if this parser can handle the given file
     * @param file The file to check
     * @return true if this parser can handle the file
     */
    boolean canHandle(File file);
} 