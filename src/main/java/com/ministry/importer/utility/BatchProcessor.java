package com.ministry.importer.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BatchProcessor<T> implements AutoCloseable {
    private final int batchSize;
    private final Consumer<List<T>> batchConsumer;
    private List<T> currentBatch;
    
    public BatchProcessor(int batchSize, Consumer<List<T>> batchConsumer) {
        this.batchSize = batchSize;
        this.batchConsumer = batchConsumer;
        this.currentBatch = new ArrayList<>(batchSize);
    }
    
    public synchronized void add(T item) {
        currentBatch.add(item);
        if (currentBatch.size() >= batchSize) {
            flush();
        }
    }
    
    public synchronized void flush() {
        if (!currentBatch.isEmpty()) {
            batchConsumer.accept(currentBatch);
            currentBatch = new ArrayList<>(batchSize);
        }
    }
    
    @Override
    public void close() {
        flush();
    }
}
