package com.ministry.importer.utility;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateParserTest {


    @Test
    void testValidDate_MMddyyyy() {
        String inputDate = "12/25/2023";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate result = DateParser.parseDate(inputDate);

        assertEquals(expectedDate, result, "Should parse date in format MM/dd/yyyy");
    }

    @Test
    void testValidDate_ddMMyyyy() {
        String inputDate = "25/12/2023";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate result = DateParser.parseDate(inputDate);

        assertEquals(expectedDate, result, "Should parse date in format dd/MM/yyyy");
    }

    @Test
    void testValidDate_yyyyMMdd() {
        String inputDate = "2023-12-25";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate result = DateParser.parseDate(inputDate);

        assertEquals(expectedDate, result, "Should parse date in format yyyy-MM-dd");
    }

    @Test
    void testValidDate_ddMMyyyyWithDashes() {
        String inputDate = "25-12-2023";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate result = DateParser.parseDate(inputDate);

        assertEquals(expectedDate, result, "Should parse date in format dd-MM-yyyy");
    }

    @Test
    void testValidDate_MMddyyyyWithDashes() {
        String inputDate = "12-25-2023";
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);

        LocalDate result = DateParser.parseDate(inputDate);

        assertEquals(expectedDate, result, "Should parse date in format MM-dd-yyyy");
    }

    @Test
    void testNullInput() {
        String inputDate = null;

        LocalDate result = DateParser.parseDate(inputDate);

        assertNull(result, "Should return null for null input");
    }

    @Test
    void testEmptyStringInput() {
        String inputDate = "   ";

        LocalDate result = DateParser.parseDate(inputDate);

        assertNull(result, "Should return null for empty or whitespace input");
    }

    @Test
    void testInvalidDateFormat() {
        String inputDate = "2023.12.25";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> DateParser.parseDate(inputDate));

        assertEquals("Impossible de parser la date: 2023.12.25. Formats supportés: MM/dd/yyyy, dd/MM/yyyy, yyyy-MM-dd, etc.",
                exception.getMessage(),
                "Should throw IllegalArgumentException for invalid date format");
    }
}