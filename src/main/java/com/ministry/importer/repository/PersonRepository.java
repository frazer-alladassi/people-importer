package com.ministry.importer.repository;

import java.sql.*;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.ministry.importer.model.Person;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PersonRepository implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(PersonRepository.class);
    private static final int BATCH_SIZE = 1000;

    private final HikariDataSource dataSource;

    public PersonRepository() {
        this(loadConfiguration());
    }

    PersonRepository(Properties config) {
        this.dataSource = createDataSource(config);
        createTableIfNotExists();
    }

    private static HikariDataSource createDataSource(Properties config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getProperty("db.url"));
        hikariConfig.setUsername(config.getProperty("db.user"));
        hikariConfig.setPassword(config.getProperty("db.password"));

        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setAutoCommit(false);
        return new HikariDataSource(hikariConfig);
    }

    public void saveAll(List<Person> people) {
        if (people == null || people.isEmpty()) {
            logger.info("No people to save");
            return;
        }
        String sql = "INSERT INTO people (matricule, first_name, last_name, birth_date, status) " +
                "VALUES (?, ?, ?, ?, ?) ";

        try (Connection connection = this.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            connection.setAutoCommit(false);
            int batchCount = 0;

            for (Person person : people) {
                stmt.setString(1, person.getMatricule());
                stmt.setString(2, person.getFirstName());
                stmt.setString(3, person.getLastName());
                stmt.setDate(4, java.sql.Date.valueOf(person.getBirthDate()));
                stmt.setString(5, person.getStatus().toString());
                stmt.addBatch();

                if (++batchCount % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    connection.commit();
                    logger.debug("Executed batch of {} records", BATCH_SIZE);
                }
            }

            int[] remainingResults = stmt.executeBatch();
            connection.commit();
            logger.info("Saved {} people successfully (last batch: {})",
                    people.size(), remainingResults.length);
        } catch (SQLException e) {
            logger.error("Failed to save people", e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed successfully");
        }
    }

    private static Properties loadConfiguration() {
        Properties props = new Properties();
        try (var input = PersonRepository.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties not found in classpath");
            }
            props.load(input);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS people (" +
                "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "matricule VARCHAR(100)," +
                "first_name VARCHAR(100)," +
                "last_name VARCHAR(100)," +
                "birth_date DATE," +
                "status VARCHAR(100)" +
                ")";

        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql, e);
        }
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
