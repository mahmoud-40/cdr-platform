package com.cdr.msloader.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdr.msloader.dto.CdrDto;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Component
public class CsvParser implements CDRParser {
    private static final Logger log = LoggerFactory.getLogger(CsvParser.class);

    @Override
    public List<CdrDto> parse(File file) throws IOException {
        List<CdrDto> cdrs = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            // Skip header row
            reader.readNext();
            
            String[] record;
            while ((record = reader.readNext()) != null) {
                try {
                    // Trim all fields
                    for (int i = 0; i < record.length; i++) {
                        record[i] = record[i].trim();
                    }
                    CdrDto cdr = new CdrDto();
                    cdr.setSource(record[0]);
                    cdr.setDestination(record[1]);
                    cdr.setStartTime(record[2]);
                    cdr.setService(record[3]);
                    cdr.setUsage(Integer.parseInt(record[4]));
                    log.info("Parsed CdrDto: {}", cdr);
                    cdrs.add(cdr);
                } catch (NumberFormatException e) {
                    log.error("Error parsing usage in CSV file: {}", file.getName(), e);
                    throw new IOException("Invalid usage format in CSV file. Expected a number.", e);
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("Invalid CSV format in file: {}", file.getName(), e);
                    throw new IOException("Invalid CSV format. Expected 5 columns: source, destination, startTime, service, usage", e);
                }
            }
        } catch (CsvValidationException e) {
            log.error("Error validating CSV file: {}", file.getName(), e);
            throw new IOException("Error validating CSV file", e);
        }
        return cdrs;
    }

    @Override
    public boolean canHandle(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".csv");
    }
}