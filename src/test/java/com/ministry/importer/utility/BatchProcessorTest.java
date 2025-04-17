package com.ministry.importer.utility;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class BatchProcessorTest {

    @Test
    void testFlushProcessesCurrentBatchAndClearsIt() {
        // Arrange
        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        BatchProcessor<String> batchProcessor = new BatchProcessor<>(3, mockConsumer);

        batchProcessor.add("item1");
        batchProcessor.add("item2");

        // Act
        batchProcessor.flush();

        // Assert
        verify(mockConsumer, times(1)).accept(List.of("item1", "item2"));
    }

    @Test
    void testFlushDoesNothingWhenBatchIsEmpty() {
        // Arrange
        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        BatchProcessor<String> batchProcessor = new BatchProcessor<>(3, mockConsumer);

        // Act
        batchProcessor.flush();

        // Assert
        verify(mockConsumer, never()).accept(any());
    }

    @Test
    void testFlushAfterBatchReachesThreshold() {
        // Arrange
        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        BatchProcessor<String> batchProcessor = new BatchProcessor<>(3, mockConsumer);

        batchProcessor.add("item1");
        batchProcessor.add("item2");
        batchProcessor.add("item3");

        // Act
        batchProcessor.flush();

        // Assert
        verify(mockConsumer, times(1)).accept(List.of("item1", "item2", "item3"));
    }

    @Test
    void testFlushResetsBatchSizeForSubsequentItems() {
        // Arrange
        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        BatchProcessor<String> batchProcessor = new BatchProcessor<>(2, mockConsumer);

        batchProcessor.add("item1");
        batchProcessor.add("item2");
        batchProcessor.flush(); // First flush

        batchProcessor.add("item3");
        batchProcessor.add("item4");

        // Act
        batchProcessor.flush(); // Second flush

        // Assert
        verify(mockConsumer, times(1)).accept(List.of("item1", "item2"));
        verify(mockConsumer, times(1)).accept(List.of("item3", "item4"));
    }

    @Test
    void testFlushWithCloseMethod() {
        // Arrange
        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        try (BatchProcessor<String> batchProcessor = new BatchProcessor<>(3, mockConsumer)) {
            batchProcessor.add("item1");
            batchProcessor.add("item2");

            // Act
        } // Auto-close triggers flush

        // Assert
        verify(mockConsumer, times(1)).accept(List.of("item1", "item2"));
    }
}