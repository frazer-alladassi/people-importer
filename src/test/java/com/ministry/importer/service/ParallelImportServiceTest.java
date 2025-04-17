package com.ministry.importer.service;

import com.ministry.importer.model.Person;
import com.ministry.importer.parser.ExcelStreamParser;
import com.ministry.importer.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParallelImportServiceTest {
    @Mock
    private ExcelStreamParser excelParser;

    @Mock
    private PersonRepository personRepository;


    private ParallelImportService importService;

    private final List<Person> testPeople = List.of(
            new Person("EMP001", "Jean", "Dupont", null, "Actif"),
            new Person("EMP002", "Marie", "Curie", null, "Actif"));

    private final int BATCH_SIZE = 10;

    @BeforeEach
    void setUp() {
        importService = new ParallelImportService(
                excelParser,
                personRepository,
                BATCH_SIZE,
                2);
    }

    @AfterEach
    void tearDown() throws Exception {
        importService.close();
    }

    @Test
    void shouldProcessValidRecords() {
        // Arrange
        when(excelParser.parseAsStream(anyString()))
                .thenReturn(testPeople.stream());

        // Act
        int count = importService.importFromFile("test.xlsx");

        // Assert
        assertEquals(testPeople.size(), count);

        // Attendre que le traitement asynchrone soit complet
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(personRepository, atLeastOnce()).saveAll(anyList());
        });
    }

    @Test
    void shouldHandleEmptyFile() {
        // Arrange
        when(excelParser.parseAsStream(anyString())).thenReturn(Stream.empty());

        // Act
        int count = importService.importFromFile("empty.xlsx");

        // Assert
        assertEquals(0, count);
        verify(personRepository, times(0)).saveAll(anyList());
    }

    @Test
    void shouldHandleExceptionDuringParsing() {
        // Arrange
        when(excelParser.parseAsStream(anyString())).thenThrow(new RuntimeException("Parsing error"));

        // Assert
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            // Act
            importService.importFromFile("corrupted.xlsx");
        });

        assertEquals("Failed to import data", exception.getMessage());
    }

    @Test
    void shouldShutdownExecutorServiceProperly() throws InterruptedException {
        // Act
        importService.shutdown();

        // Assert
        verify(personRepository, times(0)).saveAll(anyList());
        verify(personRepository).close();
    }

    @Test
    void shouldProcessRecordsExceedingBatchSize() {
        // Arrange
        List<Person> largePeopleList = Stream.generate(() -> new Person("EMP", "Name", "LastName", null, "Actif"))
                .limit(BATCH_SIZE + 10)
                .collect(Collectors.toList());
        when(excelParser.parseAsStream(anyString())).thenReturn(largePeopleList.stream());

        // Act
        int count = importService.importFromFile("largeFile.xlsx");

        // Assert
        assertEquals(largePeopleList.size(), count);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(personRepository, atLeastOnce()).saveAll(anyList());
        });
    }
}
