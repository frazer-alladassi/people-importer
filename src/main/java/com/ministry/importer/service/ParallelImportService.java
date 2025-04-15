package com.ministry.importer.service;

import com.ministry.importer.model.Person;
import com.ministry.importer.parser.ExcelStreamParser;
import com.ministry.importer.repository.PersonRepository;
import com.ministry.importer.utility.BatchProcessor;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ParallelImportService implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(ParallelImportService.class);
    private static final int BATCH_SIZE = 1000;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private final ExcelStreamParser excelParser;
    private final PersonRepository personRepository;
    private final ExecutorService executor;
    private final BatchProcessor<Person> batchProcessor;

    public ParallelImportService() {
        this(new ExcelStreamParser(), new PersonRepository(), BATCH_SIZE, THREAD_POOL_SIZE);
    }

    public ParallelImportService(ExcelStreamParser excelParser, PersonRepository personRepository, int batchSize, int threadPoolSize) {
        this.excelParser = excelParser;
        this.personRepository = personRepository;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.batchProcessor = new BatchProcessor<>(batchSize, this::processBatch);
    }

    public int importFromFile(String filePath) {
        AtomicInteger totalImported = new AtomicInteger(0);

        try (Stream<Person> personStream = excelParser.parseAsStream(filePath)) {
            personStream
                .parallel()
                .forEach(person -> {
                    batchProcessor.add(person);
                    totalImported.incrementAndGet();
                });

            batchProcessor.flush();

            return totalImported.get();
        } catch (Exception e) {
            logger.error("Error during import", e);
            throw new RuntimeException("Failed to import data", e);
        }
    }

    private void processBatch(List<Person> batch) {
        if (batch.isEmpty()) {
            return;
        }

        executor.submit(() -> {
            try {
                personRepository.saveAll(batch);
                logger.debug("Processed batch of {} people", batch.size());
            } catch (Exception e) {
                logger.error("Failed to save batch", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        personRepository.close();
    }


    @Override
    public void close() {
        try {
            batchProcessor.close();
            executor.shutdown();

            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
