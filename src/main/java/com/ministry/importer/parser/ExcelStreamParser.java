package com.ministry.importer.parser;

import com.ministry.importer.model.Person;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ExcelStreamParser {
    public Stream<Person> parseAsStream(String filePath) {
        try {
            FileInputStream file = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(file);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext())
                rowIterator.next();

            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(rowIterator, Spliterator.ORDERED),
                    false)
                    .onClose(() -> {
                        try {
                            workbook.close();
                            file.close();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to close resources", e);
                        }
                    })
                    .map(this::mapRowToPerson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
    }

    private Person mapRowToPerson(Row row) {
        return new Person(
                getStringValue(row.getCell(0)),
                getStringValue(row.getCell(1)),
                getStringValue(row.getCell(2)),
                getStringValue(row.getCell(3)),
                getStringValue(row.getCell(4)));
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            default: return null;
        }
    }
}
