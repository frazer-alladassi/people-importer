package com.ministry.importer.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class DateParser {
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ISO_LOCAL_DATE,
        new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .toFormatter()
    );

    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        String trimmedDate = dateString.trim();
        
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(trimmedDate, formatter);
            } catch (DateTimeParseException e) {
                // Essayer le prochain format
            }
        }

        throw new IllegalArgumentException("Impossible de parser la date: " + dateString + 
               ". Formats supportés: MM/dd/yyyy, dd/MM/yyyy, yyyy-MM-dd, etc.");
    }
}
