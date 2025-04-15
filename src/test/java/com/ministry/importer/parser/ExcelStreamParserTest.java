package com.ministry.importer.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.ministry.importer.enums.Status;
import com.ministry.importer.model.Person;

public class ExcelStreamParserTest {
    @Test
    void shouldParseValidExcelStream() throws Exception {
        File tempFile = createTestExcelFile();
        ExcelStreamParser parser = new ExcelStreamParser();

        try (Stream<Person> stream = parser.parseAsStream(tempFile.getAbsolutePath())) {
            long count = stream.count();
            assertEquals(2, count);
        } finally {
            tempFile.delete();
        }
    }
    
    @Test
    void shouldMapRowDataCorrectly() throws Exception {
        File tempFile = createTestExcelFile();
        ExcelStreamParser parser = new ExcelStreamParser();

        try (Stream<Person> stream = parser.parseAsStream(tempFile.getAbsolutePath())) {
            Person firstPerson = stream.findFirst().orElseThrow();

            assertEquals("EMP001", firstPerson.getMatricule());
            assertEquals("Jean", firstPerson.getFirstName());
            assertEquals("Dupont", firstPerson.getLastName());
            assertEquals(LocalDate.of(1980, 5, 15), firstPerson.getBirthDate());
            assertEquals(Status.INACTIF, firstPerson.getStatus());
        } finally {
            tempFile.delete();
        }
    }

    @Test
    void shouldHandleEmptyFile() {
        ExcelStreamParser parser = new ExcelStreamParser();
        assertThrows(RuntimeException.class, () -> parser.parseAsStream("nonexistent.xlsx"));
    }
    
    private File createTestExcelFile() throws Exception {
        File tempFile = File.createTempFile("test", ".xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(tempFile)) {

            var sheet = workbook.createSheet("Employees");

            var headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("matricule");
            headerRow.createCell(1).setCellValue("nom");
            headerRow.createCell(2).setCellValue("prenom");
            headerRow.createCell(3).setCellValue("datedenaissance");
            headerRow.createCell(4).setCellValue("status");

            var dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("EMP001");
            dataRow1.createCell(1).setCellValue("Jean");
            dataRow1.createCell(2).setCellValue("Dupont");
            dataRow1.createCell(3).setCellValue("15/05/1980");
            dataRow1.createCell(4).setCellValue("Inactif");

            var dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("EMP002");
            dataRow2.createCell(1).setCellValue("Marie");
            dataRow2.createCell(2).setCellValue("Curie");
            dataRow2.createCell(3).setCellValue("07/11/1867");
            dataRow2.createCell(4).setCellValue("Actif");

            workbook.write(out);
        }
        
        return tempFile;
    }
}
