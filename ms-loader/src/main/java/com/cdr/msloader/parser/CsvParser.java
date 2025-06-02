package com.cdr.msloader.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdr.msloader.entity.CDR;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Component
public class CsvParser implements CDRParser {
    private static final Logger log = LoggerFactory.getLogger(CsvParser.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public List<CDR> parse(File file) throws Exception {
        List<CDR> cdrs = new ArrayList<>();
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
                    CDR cdr = new CDR();
                    cdr.setSource(record[0]);
                    cdr.setDestination(record[1]);
                    cdr.setStartTime(LocalDateTime.parse(record[2], DATE_TIME_FORMATTER));
                    cdr.setService(record[3]);
                    cdr.setUsage(Integer.parseInt(record[4]));
                    log.info("Parsed CDR: {}", cdr);
                    cdrs.add(cdr);
                } catch (DateTimeParseException e) {
                    log.error("Error parsing date in CSV file: {}", file.getName(), e);
                    throw new IOException("Invalid date format in CSV file. Expected ISO-8601 format (yyyy-MM-ddTHH:mm:ss)", e);
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