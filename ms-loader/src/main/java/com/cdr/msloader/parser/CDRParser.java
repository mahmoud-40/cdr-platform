package com.cdr.msloader.parser;

import com.cdr.msloader.entity.CDR;
import java.io.File;
import java.util.List;

public interface CDRParser {
    /**
     * Parse a file and convert its contents to a list of CDR objects
     * @param file The file to parse
     * @return List of parsed CDR objects
     * @throws Exception if parsing fails
     */
    List<CDR> parse(File file) throws Exception;

    /**
     * Check if this parser can handle the given file
     * @param file The file to check
     * @return true if this parser can handle the file
     */
    boolean canHandle(File file);
} 