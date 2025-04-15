# Ministry People Importer

**Ministry People Importer** is a Java application designed to import person data from a source file (e.g., Excel or CSV) into a relational database. It uses a modular architecture and supports batch imports for better performance.

## Features

- Batch data import with transaction management.
- Automatic creation of the `people` table if it does not exist.
- Uses [HikariCP](https://github.com/brettwooldridge/HikariCP) for efficient database connection pooling.
- Detailed logging with [Log4j](https://logging.apache.org/log4j/2.x/).
- Unit tests with mocks to simulate database interactions.

## Prerequisites

- **Java 11** or higher.
- A JDBC-compatible database (e.g., PostgreSQL, MySQL, or H2 for testing).
- Maven (for dependency management).

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ministry-people-importer.git
   cd ministry-people-importer
   ```

2. Configure the database in the application.properties file:
   ```properties
   db.url=jdbc:postgresql://localhost:5432/ministry
   db.user=your_username
   db.password=your_password
   ```
3. Build the project with Maven:
mvn clean package

## Usage
Running the Application
Run the application with the file path to import as an argument:
java -jar target/ministry-people-importer.jar <file_path>

## Example
java -jar target/ministry-people-importer.jar data/people.xlsx