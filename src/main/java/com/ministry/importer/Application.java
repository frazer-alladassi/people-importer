package com.ministry.importer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.ministry.importer.service.ParallelImportService;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Démarrage de l'application");

        if (args.length == 0) {
            logger.error("Usage: java -jar importer.jar <file_path>");
            System.exit(1);
        }

        String filePath = args[0];
        try (ParallelImportService importService = new ParallelImportService()) {

            long startTime = System.currentTimeMillis();
            int importedCount = importService.importFromFile(filePath);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully imported {} people in {} ms", importedCount, duration);

        }
    }
}