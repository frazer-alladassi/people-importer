package com.ministry.importer.repository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;

import com.ministry.importer.enums.Status;
import com.ministry.importer.model.Person;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonRepositoryTest {
    private PersonRepository repository;
    private Connection mockConnection;
    private PreparedStatement mockStatement;

    @BeforeEach
    void setup() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        when(mockStatement.executeBatch()).thenReturn(new int[]{1, 1});
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        repository = new PersonRepository() {
            @Override
            protected Connection getConnection() {
                return mockConnection;
            }
        };
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
}
