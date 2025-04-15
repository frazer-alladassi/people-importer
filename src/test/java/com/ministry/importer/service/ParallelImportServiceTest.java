package com.ministry.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ministry.importer.model.Person;
import com.ministry.importer.parser.ExcelStreamParser;
import com.ministry.importer.repository.PersonRepository;
import static org.awaitility.Awaitility.await;

@ExtendWith(MockitoExtension.class)
public class ParallelImportServiceTest {
    @Mock
    private ExcelStreamParser excelParser;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ParallelImportService importService;

    private final List<Person> testPeople = List.of(
            new Person("EMP001", "Jean", "Dupont", null, "Actif"),
            new Person("EMP002", "Marie", "Curie", null, "Actif"));

    @BeforeEach
    void setUp() {
        importService = new ParallelImportService(
                excelParser,
                personRepository,
                10,
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

}
