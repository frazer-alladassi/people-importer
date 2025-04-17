package com.ministry.importer.repository;

import com.ministry.importer.enums.Status;
import com.ministry.importer.model.Person;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonRepositoryTest {
    private PersonRepository repository;
    private Connection mockConnection;
    private PreparedStatement mockStatement;

    @BeforeEach
    void setup() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        HikariDataSource mockDataSource = mock(HikariDataSource.class);

        when(mockStatement.executeBatch()).thenReturn(new int[]{1, 1});
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        repository = new PersonRepository(mockDataSource);

    }

    @Test
    void shouldSavePeople() throws Exception {
        Person person = new Person(
                "TEST1",
                "Alice",
                "Smith",
                "01-01-1990",
                "Inactif");
        List<Person> persons = Arrays.asList(person);
        repository.saveAll(persons);
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockStatement, times(1)).setString(1, "TEST1");
        verify(mockStatement, times(1)).setString(2, "Alice");
        verify(mockStatement, times(1)).setString(3, "Smith");
        verify(mockStatement, times(1)).setDate(4, java.sql.Date.valueOf("1990-01-01"));
        verify(mockStatement, times(1)).setString(5, Status.INACTIF.toString());
        verify(mockStatement, times(1)).addBatch();
        verify(mockStatement, times(1)).executeBatch();
        verify(mockConnection, times(1)).commit();
    }

    @Test
    void shouldNotSaveEmptyList() throws Exception {
        List<Person> emptyList = Arrays.asList();
        repository.saveAll(emptyList);
        verify(mockConnection, times(0)).prepareStatement(anyString());
        verify(mockStatement, times(0)).addBatch();
        verify(mockStatement, times(0)).executeBatch();
        verify(mockConnection, times(0)).commit();
    }

    @Test
    void shouldNotSaveNullList() throws Exception {
        repository.saveAll(null);
        verify(mockConnection, times(0)).prepareStatement(anyString());
        verify(mockStatement, times(0)).addBatch();
        verify(mockStatement, times(0)).executeBatch();
        verify(mockConnection, times(0)).commit();
    }

    @Test
    void shouldHandleDatabaseErrorGracefully() throws Exception {
        when(mockStatement.executeBatch()).thenThrow(new SQLException("Database error"));

        Person person = new Person(
                "TEST2",
                "Bob",
                "Jones",
                "02-02-1985",
                "Actif");
        List<Person> persons = Arrays.asList(person);

        Assertions.assertThrows(RuntimeException.class, () -> repository.saveAll(persons));

        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockStatement, times(1)).setString(1, "TEST2");
        verify(mockStatement, times(1)).setString(2, "Bob");
        verify(mockStatement, times(1)).setString(3, "Jones");
        verify(mockStatement, times(1)).setDate(4, java.sql.Date.valueOf("1985-02-02"));
        verify(mockStatement, times(1)).setString(5, Status.ACTIF.toString());
        verify(mockStatement, times(1)).addBatch();
        verify(mockStatement, times(1)).executeBatch();
        verify(mockConnection, times(0)).commit();
    }

    @Test
    void shouldHandleLargeBatchSizes() throws Exception {
        List<Person> largeBatch = new java.util.ArrayList<>();
        for (int i = 1; i <= 1500; i++) {
            largeBatch.add(new Person(
                    "ID" + i,
                    "FirstName" + i,
                    "LastName" + i,
                    "1990-01-01",
                    "Actif"));
        }

        repository.saveAll(largeBatch);

        verify(mockStatement, times(1500)).addBatch();
        verify(mockStatement, times(2)).executeBatch(); // First batch (1000), second batch (500)
        verify(mockConnection, times(2)).commit();
    }

    @Test
    void shouldLogDatabaseOperationError() throws Exception {
        when(mockStatement.executeBatch()).thenThrow(new SQLException("Batch operation failed"));

        List<Person> persons = Arrays.asList(
                new Person("FAIL1", "Chris", "Pine", "1990-01-15", "Inactif")
        );

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> repository.saveAll(persons));

        Assertions.assertEquals("Database operation failed", exception.getMessage());
        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockStatement, times(1)).addBatch();
        verify(mockStatement, times(1)).executeBatch();
        verify(mockConnection, times(0)).commit();
    }
}
